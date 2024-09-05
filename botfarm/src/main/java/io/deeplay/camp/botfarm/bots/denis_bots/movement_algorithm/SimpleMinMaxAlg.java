package io.deeplay.camp.botfarm.bots.denis_bots.movement_algorithm;

import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.UtilityMoveResult;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.exceptions.GameException;
import io.deeplay.camp.game.mechanics.GameStage;
import io.deeplay.camp.game.mechanics.GameState;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class SimpleMinMaxAlg {
    final int originDepth;
    UtilityFunction tacticUtility;
    public SimpleMinMaxAlg(int maxDepth, UtilityFunction tacticUtility){
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
                        double result = MinMaxAlg(gameState, originDepth, true);
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

    private double MinMaxAlg(GameState root, int depth, boolean maxPlayer) throws GameException {
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
                return MinMaxAlg(gameStateNode, depth-1,!maxPlayer);
            }
        } else {
            for (MakeMoveEvent moveEvent : movesRoot) {
                GameState gameStateNode = root.getCopy();
                gameStateNode.makeMove(moveEvent);
                double v = MinMaxAlg(gameStateNode, depth-1,maxPlayer);
                bestValue = maxPlayer ? Math.max(bestValue,v) : Math.min(bestValue,v);
            }
            return bestValue;
        }
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
