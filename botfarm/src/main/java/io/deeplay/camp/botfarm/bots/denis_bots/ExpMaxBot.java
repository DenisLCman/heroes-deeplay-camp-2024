package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.botfarm.bots.Bot;
import io.deeplay.camp.botfarm.bots.RandomBot;
import io.deeplay.camp.game.entities.*;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.exceptions.GameException;
import io.deeplay.camp.game.mechanics.GameStage;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ExpMaxBot extends Bot {
    private static final Logger logger = LoggerFactory.getLogger(RandomBot.class);
    BotTactic botTactic;
    UtilityFunction tacticUtility;
    UnitType currentGeneral;
    int maxDepth;
    @Setter boolean firstPlaceInGame = true;

    public ExpMaxBot(PlayerType playerType, int maxDepth){
        tacticUtility = new TacticUtility(BotTactic.BASE_TACTIC);
        tacticUtility.setCurrentPlayerType(playerType);
        this.maxDepth = maxDepth;
    }


    @Getter
    @AllArgsConstructor
    static class UtilityMoveResult {
        double value;
        MakeMoveEvent event;

    }


    @Override
    public PlaceUnitEvent generatePlaceUnitEvent(GameState gameState) {
        List<PlaceUnitEvent> placeUnitEvents = gameState.getPossiblePlaces();
        if(!placeUnitEvents.isEmpty()){
            return placeUnitEvents.get((int)(Math.random()*placeUnitEvents.size()));
        }
        else{
            return null;
        }
    }


    @Override
    public MakeMoveEvent generateMakeMoveEvent(GameState gameState) {
        firstPlaceInGame = true;
        return getMoveResult(gameState).event;
    }

    private UtilityMoveResult getMoveResult(GameState gameState) {

        int originDepth = maxDepth;
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
                                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
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

    public double alphaBetaMinMaxAlg(GameState root, int depth, double alpha, double beta, boolean maxPlayer) throws GameException {
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
                return alphaBetaMinMaxAlg(gameStateNode, depth, alpha,beta,!maxPlayer);
            }
        } else {
            for (MakeMoveEvent moveEvent : movesRoot) {
                double v = expectMaxAlg(root, moveEvent, depth, alpha,beta,maxPlayer);
                bestValue = maxPlayer ? Math.max(bestValue,v) : Math.min(bestValue,v);
            }
            return bestValue;
        }
    }

    private double expectMaxAlg(GameState root, MakeMoveEvent event, int depth, double alpha, double beta, boolean maxPlayer) throws GameException {
        List<StateChance> chancesRoot = root.getPossibleState(event);
        double excepted = 0;
        for (StateChance chance : chancesRoot) {
            GameState nodeGameState = chance.gameState().getCopy();
            double v = alphaBetaMinMaxAlg(nodeGameState, depth-1, alpha,beta,maxPlayer);
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
            if (bestValue.value < task.value) {
                bestValue = task;
            }
        }
        return bestValue;
    }

}
