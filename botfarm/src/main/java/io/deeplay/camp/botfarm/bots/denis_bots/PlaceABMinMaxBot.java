package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.botfarm.bots.Bot;
import io.deeplay.camp.botfarm.bots.RandomBot;
import io.deeplay.camp.game.entities.Board;
import io.deeplay.camp.game.entities.Position;
import io.deeplay.camp.game.entities.UnitType;
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

public class PlaceABMinMaxBot extends Bot {
    private static final Logger logger = LoggerFactory.getLogger(RandomBot.class);
    BotTactic botTactic;
    UnitType currentGeneral;
    UtilityFunction tacticUtility;
    int maxDepth;
    @Setter
    boolean firstPlaceInGame = true;

    public PlaceABMinMaxBot(PlayerType playerType, int maxDepth){
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

    public void findNewTactic(GameState gameState){
        if(gameState == null){
            botTactic = BotTactic.KNIGHT_TACTIC;
            currentGeneral = UnitType.KNIGHT;
            return;
        }
        if(gameState.getCurrentPlayer() == PlayerType.FIRST_PLAYER){
            int rand = (int)(Math.random()*5);
            switch (rand){
                case 0 -> {
                    botTactic = BotTactic.KNIGHT_TACTIC;
                }
                case 1 -> {
                    botTactic = BotTactic.HEALER_TACTIC;
                }
                case 2,3 -> {
                    botTactic = BotTactic.MAGE_TACTIC;
                }
                case 4 -> {
                    botTactic = BotTactic.ARCHER_TACTIC;
                }
                default -> botTactic = BotTactic.MAGE_TACTIC;
            }
        }
        else if (gameState.getCurrentPlayer() == PlayerType.SECOND_PLAYER){
            UnitType generalOpponent = null;
            for(int column = 0; column < Board.COLUMNS; column++){
                for(int row = 0; row < Board.ROWS/2;row++){
                    if(gameState.getCurrentBoard().getUnit(column, row).isGeneral()){
                        generalOpponent = gameState.getCurrentBoard().getUnit(column, row).getUnitType();
                        break;
                    }
                }
            }
            if(generalOpponent == UnitType.KNIGHT){
                botTactic = BotTactic.ARCHER_TACTIC;
            } else if(generalOpponent == UnitType.HEALER){
                botTactic = BotTactic.KNIGHT_TACTIC;
            } else if (generalOpponent == UnitType.ARCHER) {
                botTactic = BotTactic.HEALER_TACTIC;
            } else if (generalOpponent == UnitType.MAGE){
                int rand = (int) (Math.random() * 2);
                switch (rand){
                    case 0 -> botTactic = BotTactic.MAGE_TACTIC;
                    case 1 -> botTactic = BotTactic.KNIGHT_TACTIC;
                }
            }
        }
        switch (botTactic){
            case MAGE_TACTIC -> currentGeneral = UnitType.MAGE;
            case HEALER_TACTIC -> currentGeneral = UnitType.HEALER;
            case KNIGHT_TACTIC -> currentGeneral = UnitType.KNIGHT;
            case ARCHER_TACTIC -> currentGeneral = UnitType.ARCHER;
            case null, default -> currentGeneral = UnitType.HEALER;
        }
        tacticUtility.setBotTactic(botTactic);
    }

    @Override
    public PlaceUnitEvent generatePlaceUnitEvent(GameState gameState) {
        if(firstPlaceInGame) {
            findNewTactic(gameState);
            tacticUtility.setBotTactic(botTactic);
            firstPlaceInGame = false;
        }
        int originDepth;
        if(gameState.getCurrentPlayer() == PlayerType.FIRST_PLAYER){
            originDepth = enumerationPlayerUnits(PlayerType.FIRST_PLAYER,gameState.getCurrentBoard()).size();
        }
        else{
            originDepth = enumerationPlayerUnits(PlayerType.SECOND_PLAYER,gameState.getCurrentBoard()).size();
        }

        List<PlaceUnitEvent> possiblePlaces =  gameState.getPossiblePlaces();
        if (possiblePlaces.isEmpty()) {
            return new AiBot.UtilityPlaceResult(Double.NEGATIVE_INFINITY, null).place;
        } else{
            AiBot.UtilityPlaceResult bestValue = new AiBot.UtilityPlaceResult(Double.NEGATIVE_INFINITY,null);
            List<RecursiveTask<AiBot.UtilityPlaceResult>> tasks = new ArrayList<>();
            for (PlaceUnitEvent placeRoot : possiblePlaces) {
                RecursiveTask<AiBot.UtilityPlaceResult> task = new RecursiveTask<AiBot.UtilityPlaceResult>() {
                    @SneakyThrows
                    @Override
                    protected AiBot.UtilityPlaceResult compute() {
                        GameState gameStateNode = gameState.getCopy();
                        gameStateNode.makePlacement(placeRoot);
                        double result = maximumPlaceAlg(gameStateNode, originDepth);
                        return new AiBot.UtilityPlaceResult(result, placeRoot);
                    }
                };
                tasks.add(task);
            }
            List<AiBot.UtilityPlaceResult> results = ForkJoinTask.invokeAll(tasks).stream()
                    .map(ForkJoinTask::join)
                    .toList();
            for (AiBot.UtilityPlaceResult task : results) {
                try {

                    if (bestValue.value < task.value) {
                        bestValue = task;
                    }
                } catch (CancellationException e) {
                }
            }
            return bestValue.place;
        }

    }


    public double maximumPlaceAlg(GameState root, int depth) throws GameException {
        if(depth == 0 || root.getGameStage() == GameStage.ENDED){
            if(tacticUtility.getCurrentPlayerType() == PlayerType.FIRST_PLAYER){
                if(root.getArmyFirst().getGeneralType() == currentGeneral){
                    return 5*tacticUtility.getPlaceUtility(root);
                }
                else{
                    return tacticUtility.getPlaceUtility(root);
                }
            }
            else{
                if(root.getArmySecond().getGeneralType() == currentGeneral){
                    return 5*tacticUtility.getPlaceUtility(root);
                }
                else{
                    return tacticUtility.getPlaceUtility(root);
                }
            }
        }
        List<PlaceUnitEvent> placeRoot = root.getPossiblePlaces();

        double bestValue = Double.NEGATIVE_INFINITY;

        if(placeRoot.isEmpty()){
            if(tacticUtility.getCurrentPlayerType() == PlayerType.FIRST_PLAYER){
                if(root.getArmyFirst().getGeneralType() == currentGeneral){
                    return 10000*tacticUtility.getPlaceUtility(root);
                }
                else{
                    return tacticUtility.getPlaceUtility(root);
                }
            }
            else{
                if(root.getArmySecond().getGeneralType() == currentGeneral){
                    return 10000*tacticUtility.getPlaceUtility(root);
                }
                else{
                    return tacticUtility.getPlaceUtility(root);
                }
            }
        } else {
            for (PlaceUnitEvent placeEvent : placeRoot) {
                GameState gameStateNode = root.getCopy();
                gameStateNode.makePlacement(placeEvent);
                double v = maximumPlaceAlg(gameStateNode, depth - 1);
                bestValue = Math.max(bestValue,v);
            }
            return bestValue;
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
                        double result = MinMaxAlg(gameState, originDepth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
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

    public double MinMaxAlg(GameState root, int depth, double alpha, double beta, boolean maxPlayer) throws GameException {
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
                return MinMaxAlg(gameStateNode, depth,alpha, beta ,!maxPlayer);
            }
        } else {
            for (MakeMoveEvent moveEvent : movesRoot) {
                GameState gameStateNode = root.getCopy();
                gameStateNode.makeMove(moveEvent);
                double v = MinMaxAlg(gameStateNode, depth-1,alpha,beta,maxPlayer);
                bestValue = maxPlayer ? Math.max(bestValue,v) : Math.min(bestValue,v);
                if(maxPlayer){
                    alpha = Math.max(alpha,bestValue);
                }
                else{
                    beta = Math.min(beta, bestValue);
                }
                if(beta <= alpha){
                    break;
                }

            }
            return bestValue;
        }
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

    public List<Position> enumerationPlayerUnits(PlayerType playerType, Board board) {
        List<Position> unitPositions = new ArrayList<>();
        if (playerType == PlayerType.FIRST_PLAYER) {
            unitPositions.addAll(board.enumerateUnits(0, Board.ROWS / 2));
        } else {
            unitPositions.addAll(board.enumerateUnits(Board.ROWS / 2, Board.ROWS));
        }
        return unitPositions;
    }

}
