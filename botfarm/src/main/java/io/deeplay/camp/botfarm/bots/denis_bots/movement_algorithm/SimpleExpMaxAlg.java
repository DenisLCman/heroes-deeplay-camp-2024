package io.deeplay.camp.botfarm.bots.denis_bots.movement_algorithm;

import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.UtilityMoveResult;
import io.deeplay.camp.game.entities.Board;
import io.deeplay.camp.game.entities.Position;
import io.deeplay.camp.game.entities.StateChance;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.exceptions.GameException;
import io.deeplay.camp.game.mechanics.GameStage;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Алгоритм игрового процесса, использующий:
 * ЭкспектиМакс,
 * Начальную многопоточность.
 */
public class SimpleExpMaxAlg {
    final int originDepth;
    UtilityFunction tacticUtility;
    public SimpleExpMaxAlg(int maxDepth, UtilityFunction tacticUtility){
        this.originDepth = maxDepth;
        this.tacticUtility = tacticUtility;
    }

    public UtilityMoveResult getMoveResult(GameState gameState) {

        int originDepth = this.originDepth;
        List<MakeMoveEvent> movesRoot = gameState.getPossibleMoves();

        if (movesRoot.isEmpty()) {
            return new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
        } else {
            List<RecursiveTask<UtilityMoveResult>> tasks = new ArrayList<>();
            for (MakeMoveEvent moveEvent : movesRoot) {
                RecursiveTask<UtilityMoveResult> task = new RecursiveTask<>() {
                    @SneakyThrows
                    @Override
                    protected UtilityMoveResult compute() {
                        double result = expectMaxAlg(gameState, moveEvent,originDepth,
                                true);
                        return new UtilityMoveResult(result, moveEvent);
                    }
                };
                tasks.add(task);

            }
            List<UtilityMoveResult> results = ForkJoinTask.invokeAll(tasks).stream()
                    .map(ForkJoinTask::join)
                    .toList();

            return getMaxFromTasks(results);
        }
    }

    private double minMaxAlg(GameState root, int depth, boolean maxPlayer) throws GameException {
        if(depth == 0 || root.getGameStage() == GameStage.ENDED){
            return tacticUtility.getMoveUtility(root);
        }
        List<MakeMoveEvent> movesRoot = root.getPossibleMoves();
        double bestValue = maxPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        if(movesRoot.isEmpty()){
            if(root.getGameStage() == GameStage.ENDED){
                return tacticUtility.getMoveUtility(root);
            }
            else {
                GameState gameStateNode = root.getCopy();
                gameStateNode.changeCurrentPlayer();
                return minMaxAlg(gameStateNode, depth, !maxPlayer);
            }
        } else {
            for (MakeMoveEvent moveEvent : movesRoot) {
                double v = expectMaxAlg(root, moveEvent, depth, maxPlayer);
                bestValue = maxPlayer ? Math.max(bestValue,v) : Math.min(bestValue,v);
            }
            return bestValue;
        }
    }

    private double expectMaxAlg(GameState root, MakeMoveEvent event, int depth, boolean maxPlayer) throws GameException {
        List<StateChance> chancesRoot = root.getPossibleState(event);
        double excepted = 0;
        for (StateChance chance : chancesRoot) {
            GameState nodeGameState = chance.gameState().getCopy();
            double v = minMaxAlg(nodeGameState, depth-1, maxPlayer);
            excepted += chance.chance() * v;
        }
        return excepted;
    }


    public List<Position> enumerationPlayerUnits(PlayerType playerType, Board board) {
        List<Position> unitPositions = new ArrayList<>();
        if (playerType == PlayerType.FIRST_PLAYER) {
            unitPositions.addAll(board.enumerateUnits(0, Board.ROWS / 2));
        } else {
            unitPositions.addAll(board.enumerateUnits(Board.ROWS / 2, Board.ROWS));
        }
        return unitPositions;
    }

    private UtilityMoveResult getMaxFromTasks(List<UtilityMoveResult> results){
        UtilityMoveResult bestValue = new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
        for (UtilityMoveResult task : results) {
            if (bestValue.getValue() < task.getValue()) {
                bestValue = task;
            }
        }
        return bestValue;
    }
}
