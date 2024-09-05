package io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm;

import io.deeplay.camp.botfarm.bots.denis_bots.*;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.BotTactic;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.PossibleStartState;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.UtilityPlaceResult;
import io.deeplay.camp.botfarm.bots.denis_bots.tools.GameStateCache;
import io.deeplay.camp.game.entities.Board;
import io.deeplay.camp.game.entities.Position;
import io.deeplay.camp.game.entities.UnitType;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Алгоритм расстановки основанный на Монте-Карло.
 */
public class MCPlaceAlg {
    UtilityFunction tacticUtility;
    BotTactic botTactic;
    UnitType currentGeneral;
    Board bestBoard;
    List<PossibleStartState> possibleStartStates;
    GameStateCache gameStateCache;

    public MCPlaceAlg(UtilityFunction tacticUtility){
        this.tacticUtility = tacticUtility;
        gameStateCache = new GameStateCache();
    }

    /**
     * Функция для нахождения более приемлемой тактики, основанной на
     * выборе вражеского генерала, или случайного генерала для первого
     * игрока.
     */
    public void findNewTactic(GameState gameState){
        bestBoard = new Board();
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
                botTactic = BotTactic.HEALER_TACTIC;
            } else if (generalOpponent == UnitType.ARCHER) {
                botTactic = BotTactic.ARCHER_TACTIC;
            } else if (generalOpponent == UnitType.MAGE){
                botTactic = BotTactic.KNIGHT_TACTIC;
            }
        }
        switch (botTactic){
            case MAGE_TACTIC -> currentGeneral = UnitType.MAGE;
            case HEALER_TACTIC -> currentGeneral = UnitType.HEALER;
            case KNIGHT_TACTIC, BASE_TACTIC -> currentGeneral = UnitType.KNIGHT;
            case ARCHER_TACTIC -> currentGeneral = UnitType.ARCHER;
        }
        tacticUtility.setBotTactic(botTactic);
    }

    /**
     * Функция для выбора приемлемой расстановки.
     */
    public UtilityPlaceResult getPlaceResult(GameState gameState){
        int originDepth = 1;

        List<PlaceUnitEvent> possiblePlacesOrigin =  gameState.getPossiblePlaces();
        List<PlaceUnitEvent> possiblePlaces = new ArrayList<>();
        for(PlaceUnitEvent event :possiblePlacesOrigin){
            if(event.getUnit().isGeneral()){
                if(event.getUnit().getUnitType() != currentGeneral){
                    if(enumerationPlayerUnits(tacticUtility.getCurrentPlayerType(),gameState.getCurrentBoard()).size() != 5){
                        continue;
                    }
                }
            }
            if((event.getRows() == 0 || event.getRows() == Board.ROWS-1)){
                if(event.getUnit().getUnitType() == UnitType.KNIGHT){
                    continue;
                }
            }
            if((event.getRows() == 1 || event.getRows() == 2)){
                if(event.getUnit().getUnitType() != UnitType.KNIGHT){
                    continue;
                }
            }

            possiblePlaces.add(event);
        }

        if (possiblePlaces.isEmpty()) {
            return new UtilityPlaceResult(Double.NEGATIVE_INFINITY, null);
        } else{
            List<RecursiveTask<UtilityPlaceResult>> tasks = new ArrayList<>();
            for (PlaceUnitEvent placeRoot : possiblePlaces) {
                int finalOriginDepth = originDepth;
                RecursiveTask<UtilityPlaceResult> task = new RecursiveTask<>() {
                    @SneakyThrows
                    @Override
                    protected UtilityPlaceResult compute() {
                        GameState gameStateNode = gameState.getCopy();
                        gameStateNode.makePlacement(placeRoot);
                        double result = maximumPlaceAlg(gameStateNode, finalOriginDepth, placeRoot);
                        return new UtilityPlaceResult(result, placeRoot);
                    }
                };
                tasks.add(task);
            }
            List<UtilityPlaceResult> results = ForkJoinTask.invokeAll(tasks).stream()
                    .map(ForkJoinTask::join)
                    .toList();

            return getMaxPlaceFromTasks(results);
        }
    }
    /**
     * Функция для находения максимизирующего набора расстановок через алгоритм Монте-Карло.
     */
    public double maximumPlaceAlg(GameState root, int depth, PlaceUnitEvent placeUnitEvent) {
        if(depth == 0){
            return tacticUtility.monteCarloAlg(root, 10, placeUnitEvent);
        }
        List<PlaceUnitEvent> possiblePlacesOrigin =  root.getPossiblePlaces();
        List<PlaceUnitEvent> placeRoot =  new ArrayList<>();
        for(PlaceUnitEvent event :possiblePlacesOrigin){
            if(event.getUnit().isGeneral()){
                if(event.getUnit().getUnitType() != currentGeneral){
                    if(enumerationPlayerUnits(tacticUtility.getCurrentPlayerType(),root.getCurrentBoard()).size() != 5){
                        continue;
                    }
                }
            }

            if((event.getRows() == 0 || event.getRows() == Board.ROWS-1)){
                if(event.getUnit().getUnitType() == UnitType.KNIGHT){
                    continue;
                }
            }
            if((event.getRows() == 1 || event.getRows() == 2)){
                if(event.getUnit().getUnitType() != UnitType.KNIGHT){
                    continue;
                }
            }
            placeRoot.add(event);
        }

        if(placeRoot.isEmpty()){
            return tacticUtility.monteCarloAlg(root, 10, placeUnitEvent);
        } else {
            List<RecursiveTask<UtilityPlaceResult>> recursiveTasks = new ArrayList<>();
            for (PlaceUnitEvent placeEvent : placeRoot) {
                RecursiveTask<UtilityPlaceResult> task = new RecursiveTask<>() {
                    @SneakyThrows
                    @Override
                    protected UtilityPlaceResult compute() {
                        GameState gameStateNode = root.getCopy();
                        gameStateNode.makePlacement(placeEvent);
                        return new UtilityPlaceResult(maximumPlaceAlg(gameStateNode, depth-1, placeUnitEvent), placeEvent);
                    }
                };
                recursiveTasks.add(task);
            }

            List<UtilityPlaceResult> results = ForkJoinTask.invokeAll(recursiveTasks).stream()
                    .map(ForkJoinTask::join)
                    .toList();

            return getMaxPlaceFromTasks(results).getValue();
        }
    }
    /**
     * Функция находящая максимальную расстановку из массива results.
     */
    private UtilityPlaceResult getMaxPlaceFromTasks(List<UtilityPlaceResult> results){
        UtilityPlaceResult bestValue = new UtilityPlaceResult(Double.NEGATIVE_INFINITY, null);
        for (UtilityPlaceResult task : results) {
            if (bestValue.getValue() <= task.getValue()) {
                bestValue = task;
            }
        }

        return bestValue;
    }

    /**
     * Метод для подсчитывания количества юнитов у определённого игрока
     */
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
