package io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm;

import io.deeplay.camp.botfarm.bots.denis_bots.entities.BotTactic;
import io.deeplay.camp.botfarm.bots.denis_bots.tools.GameStateCache;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.PossibleStartState;
import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.game.entities.Board;
import io.deeplay.camp.game.entities.Position;
import io.deeplay.camp.game.entities.UnitType;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.SneakyThrows;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Алгоритм расстановки основанный взятых расстановок из кэша.
 */
public class MCCachePlaceAlg {
    UtilityFunction tacticUtility;
    BotTactic botTactic;
    UnitType currentGeneral;
    Board bestBoard;
    List<PossibleStartState> possibleStartStates;
    GameStateCache gameStateCache;

    public MCCachePlaceAlg(UtilityFunction tacticUtility){
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
        try {
            findBestBoardFromCache(gameState);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Функция для нахождения лучшей расстановки из возможных,
     * которые будут взяты из кэша хороших расстановок.
     * Если вражеская расстановка есть в кэше, будет взята
     * противоборствующая ей расстановка. Если расстановка
     * не найдена, будет взята лучшая расстановка в зависимости
     * от выбранного генерала
     */
    private void findBestBoardFromCache(GameState gameState) throws IOException, ClassNotFoundException {
        bestBoard = null;
        gameStateCache = gameStateCache.loadCacheFromFile(".\\botfarm\\src\\main\\java\\io\\deeplay\\camp\\botfarm\\bots\\denis_bots\\hashStartGame.json");
        possibleStartStates = gameStateCache.getCache();
        Collections.sort(possibleStartStates);

        if(tacticUtility.getCurrentPlayerType() == PlayerType.SECOND_PLAYER) {
            for (PossibleStartState pos : possibleStartStates) {
                if (pos.getForPlayerType() == tacticUtility.getCurrentPlayerType()) {
                    if (equalsBoard(pos.getEnemyUnits(), gameState.getCurrentBoard())) {
                        bestBoard = pos.getAllyUnits();
                        break;
                    }
                }
            }

            if(bestBoard == null){
                List<PossibleStartState> possibleSecondPlayerStart = new ArrayList<>();
                for (PossibleStartState pos : possibleStartStates) {
                    if (pos.getForPlayerType() == tacticUtility.getCurrentPlayerType() && pos.getCountWinRound() < 5) {
                        UnitType generalThisPos = null;
                        for(int column = 0;column<Board.COLUMNS;column++){
                            for(int row = Board.ROWS/2;row< Board.ROWS;row++){
                                if(pos.getAllyUnits().getUnit(column,row).isGeneral()){
                                    generalThisPos = pos.getAllyUnits().getUnit(column,row).getUnitType();
                                    break;
                                }
                            }
                        }
                        if(generalThisPos == currentGeneral) {
                            possibleSecondPlayerStart.add(pos);
                        }
                    }
                    if(pos.getCountWinRound() >= 5){
                        break;
                    }
                }
                bestBoard = possibleSecondPlayerStart.get((int) (Math.random() * possibleSecondPlayerStart.size())).getAllyUnits();
            }

        }
        else{
            List<PossibleStartState> possibleFirstPlayerStart = new ArrayList<>();
            for (PossibleStartState pos : possibleStartStates) {
                if (pos.getForPlayerType() == tacticUtility.getCurrentPlayerType() && pos.getCountWinRound() < 4) {
                    possibleFirstPlayerStart.add(pos);
                }
                if(pos.getCountWinRound() >= 5){
                    break;
                }
            }
            bestBoard = possibleFirstPlayerStart.get((int) (Math.random() * possibleFirstPlayerStart.size())).getAllyUnits();
        }
    }

    /**
     * Функция для приемлемой расстановки.
     */
    @SneakyThrows
    public PlaceUnitEvent getPlaceResult(GameState gameState){
        PlayerType forPlayerType = tacticUtility.getCurrentPlayerType();

        int shiftRow = 0;
        if(forPlayerType == PlayerType.FIRST_PLAYER) {
            shiftRow = 0;
        }
        else{
            shiftRow = 2;
        }

        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = shiftRow;row < Board.ROWS/2 + shiftRow;row++){
                if(gameState.getCurrentBoard().isEmptyCell(column,row)){
                    if(enumerationPlayerUnits(tacticUtility.getCurrentPlayerType(), gameState.getCurrentBoard()).size() != 5){
                        return new PlaceUnitEvent(column, row, bestBoard.getUnit(column,row), tacticUtility.getCurrentPlayerType(), true, bestBoard.getUnit(column,row).isGeneral());
                    }
                    else{
                        return new PlaceUnitEvent(column, row, bestBoard.getUnit(column,row), tacticUtility.getCurrentPlayerType(), false, bestBoard.getUnit(column,row).isGeneral());
                    }
                }
            }
        }

        return null;

    }

    private boolean equalsBoard(Board enemyBoard, Board allyBoard){
        boolean result = true;
        int shiftRow;
        if(tacticUtility.getCurrentPlayerType() == PlayerType.FIRST_PLAYER) {
            shiftRow = 2;
        }
        else{
            shiftRow = 0;
        }
        for(int column = 0;column < Board.COLUMNS;column++) {
            for (int row = shiftRow; row < Board.ROWS / 2 + shiftRow; row++) {
                if((enemyBoard.getUnit(column,row).isGeneral() != allyBoard.getUnit(column,row).isGeneral()) ||
                        (enemyBoard.getUnit(column,row).getUnitType() != allyBoard.getUnit(column,row).getUnitType())){
                    result = false;
                }
            }
        }
        return result;
    }

    /**
     * Метод для подсчитывания количества юнитов у определённого игрока
     */
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
