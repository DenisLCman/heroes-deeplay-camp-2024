package io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm;

import io.deeplay.camp.botfarm.bots.denis_bots.entities.BotTactic;
import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.UtilityPlaceResult;
import io.deeplay.camp.game.entities.Board;
import io.deeplay.camp.game.entities.Position;
import io.deeplay.camp.game.entities.UnitType;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.exceptions.GameException;
import io.deeplay.camp.game.mechanics.GameStage;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class EvristicPlaceAlg {
    UtilityFunction tacticUtility;
    BotTactic botTactic;
    UnitType currentGeneral;
    Board bestBoard;

    public EvristicPlaceAlg(UtilityFunction tacticUtility){
        this.tacticUtility = tacticUtility;
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
            case BASE_TACTIC -> currentGeneral = UnitType.KNIGHT;
        }
        tacticUtility.setBotTactic(botTactic);
    }

    public PlaceUnitEvent getPlaceResult(GameState gameState) {

        int originDepth;
        if(gameState.getCurrentPlayer() == PlayerType.FIRST_PLAYER){
            originDepth = enumerationPlayerUnits(PlayerType.FIRST_PLAYER,gameState.getCurrentBoard()).size();
        }
        else{
            originDepth = enumerationPlayerUnits(PlayerType.SECOND_PLAYER,gameState.getCurrentBoard()).size();
        }

        List<PlaceUnitEvent> possiblePlaces =  gameState.getPossiblePlaces();
        if (possiblePlaces.isEmpty()) {
            return new UtilityPlaceResult(Double.NEGATIVE_INFINITY, null).getPlace();
        } else{
            UtilityPlaceResult bestValue = new UtilityPlaceResult(Double.NEGATIVE_INFINITY,null);
            List<RecursiveTask<UtilityPlaceResult>> tasks = new ArrayList<>();
            for (PlaceUnitEvent placeRoot : possiblePlaces) {
                RecursiveTask<UtilityPlaceResult> task = new RecursiveTask<UtilityPlaceResult>() {
                    @SneakyThrows
                    @Override
                    protected UtilityPlaceResult compute() {
                        GameState gameStateNode = gameState.getCopy();
                        gameStateNode.makePlacement(placeRoot);
                        double result = maximumPlaceAlg(gameStateNode, originDepth);
                        return new UtilityPlaceResult(result, placeRoot);
                    }
                };
                tasks.add(task);
            }
            List<UtilityPlaceResult> results = ForkJoinTask.invokeAll(tasks).stream()
                    .map(ForkJoinTask::join)
                    .toList();

            for (UtilityPlaceResult task : results) {
                try {
                    //System.out.println("Значение цены у данного расположения: " + task.value);
                    if (bestValue.getValue() < task.getValue()) {
                        bestValue = task;
                    }
                } catch (CancellationException e) {
                }
            }

            //System.out.println("Наивысшая цена расположения: " + bestValue.value);
            return bestValue.getPlace();
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
    private List<Position> enumerationPlayerUnits(PlayerType playerType, Board board) {
        List<Position> unitPositions = new ArrayList<>();
        if (playerType == PlayerType.FIRST_PLAYER) {
            unitPositions.addAll(board.enumerateUnits(0, Board.ROWS / 2));
        } else {
            unitPositions.addAll(board.enumerateUnits(Board.ROWS / 2, Board.ROWS));
        }
        return unitPositions;
    }
}

