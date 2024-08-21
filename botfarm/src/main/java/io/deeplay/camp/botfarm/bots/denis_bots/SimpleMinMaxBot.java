package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.botfarm.bots.Bot;
import io.deeplay.camp.botfarm.bots.RandomBot;
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
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class SimpleMinMaxBot extends Bot {
    private static final Logger logger = LoggerFactory.getLogger(RandomBot.class);
    UtilityFunction tacticUtility;
    int maxDepth;
    @Setter
    boolean firstPlaceInGame = true;

    public SimpleMinMaxBot(PlayerType playerType, int maxDepth){
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

    @Getter
    @AllArgsConstructor
    static class UtilityPlaceResult {
        double value;
        PlaceUnitEvent place;

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

    public double MinMaxAlg(GameState root, int depth, boolean maxPlayer) throws GameException {
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
            System.out.println("Значение цены у данного хода: " + task.value);
            if (bestValue.value < task.value) {
                bestValue = task;
            }
        }
        return bestValue;
    }

    private UtilityMoveResult getMinFromTasks(List<UtilityMoveResult> results){
        UtilityMoveResult bestValue = new UtilityMoveResult(Double.POSITIVE_INFINITY, null);
        for (UtilityMoveResult task : results) {
            System.out.println("Значение цены у данного хода: " + task.value);
            if (task.value < bestValue.value) {
                bestValue = task;
            }
        }
        return bestValue;
    }

}


