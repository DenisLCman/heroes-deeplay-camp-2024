package io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm;

import io.deeplay.camp.botfarm.bots.denis_bots.entities.BotTactic;
import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.game.entities.*;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;

import java.util.ArrayList;
import java.util.List;

public class MetricPlaceAlg {
    UtilityFunction tacticUtility;
    BotTactic botTactic;
    UnitType currentGeneral;
    Board bestBoard;

    public MetricPlaceAlg(UtilityFunction tacticUtility){
        this.tacticUtility = tacticUtility;
    }
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
                case 0,1 -> {
                    botTactic = BotTactic.KNIGHT_TACTIC;
                }
                case 2,3 -> {
                    botTactic = BotTactic.HEALER_TACTIC;
                }
                default -> botTactic = BotTactic.HEALER_TACTIC;
            }
        }
        else if (gameState.getCurrentPlayer() == PlayerType.SECOND_PLAYER){
            UnitType generalOpponent = null;
            for(int column = 0; column < Board.COLUMNS;column++){
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


    public PlaceUnitEvent getPlaceResult(GameState gameState){

        switch (currentGeneral){
            case KNIGHT -> autoFillBoard(UnitType.KNIGHT);
            case MAGE -> autoFillBoard(UnitType.MAGE);
            case ARCHER -> autoFillBoard(UnitType.ARCHER);
            case HEALER -> autoFillBoard(UnitType.HEALER);
        }
        int shiftRow = 0;
        if(tacticUtility.getCurrentPlayerType() == PlayerType.FIRST_PLAYER) {
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

    private void autoFillBoard(UnitType unitType){
        PlayerType playerTypeThis = tacticUtility.getCurrentPlayerType();
        if(playerTypeThis == PlayerType.FIRST_PLAYER){
            if(unitType == UnitType.KNIGHT){
                bestBoard.setUnit(0,0, new Archer(playerTypeThis));
                bestBoard.setUnit(0,1, new Archer(playerTypeThis));
                bestBoard.setUnit(1,0, new Healer(playerTypeThis));
                bestBoard.setUnit(1,1, new Knight(playerTypeThis));
                bestBoard.setUnit(2,0, new Archer(playerTypeThis));
                bestBoard.setUnit(2,1, new Archer(playerTypeThis));
                bestBoard.getUnit(1,1).setGeneral(true);
            }
            if(unitType == UnitType.MAGE){
                bestBoard.setUnit(0,0, new Archer(playerTypeThis));
                bestBoard.setUnit(0,1, new Archer(playerTypeThis));
                bestBoard.setUnit(1,0, new Mage(playerTypeThis));
                bestBoard.setUnit(1,1, new Healer(playerTypeThis));
                bestBoard.setUnit(2,0, new Mage(playerTypeThis));
                bestBoard.setUnit(2,1, new Archer(playerTypeThis));
                bestBoard.getUnit(1,0).setGeneral(true);
            }
            if(unitType == UnitType.ARCHER){
                bestBoard.setUnit(0,0, new Archer(playerTypeThis));
                bestBoard.setUnit(0,1, new Mage(playerTypeThis));
                bestBoard.setUnit(1,0, new Mage(playerTypeThis));
                bestBoard.setUnit(1,1, new Healer(playerTypeThis));
                bestBoard.setUnit(2,0, new Archer(playerTypeThis));
                bestBoard.setUnit(2,1, new Mage(playerTypeThis));
                bestBoard.getUnit(2,0).setGeneral(true);
            }
            if(unitType == UnitType.HEALER){
                bestBoard.setUnit(0,0, new Archer(playerTypeThis));
                bestBoard.setUnit(0,1, new Archer(playerTypeThis));
                bestBoard.setUnit(1,0, new Healer(playerTypeThis));
                bestBoard.setUnit(1,1, new Knight(playerTypeThis));
                bestBoard.setUnit(2,0, new Archer(playerTypeThis));
                bestBoard.setUnit(2,1, new Archer(playerTypeThis));
                bestBoard.getUnit(1,0).setGeneral(true);
            }
        }
        else{
            if(unitType == UnitType.KNIGHT){
                bestBoard.setUnit(0,2, new Archer(playerTypeThis));
                bestBoard.setUnit(0,3, new Archer(playerTypeThis));
                bestBoard.setUnit(1,2, new Knight(playerTypeThis));
                bestBoard.setUnit(1,3, new Healer(playerTypeThis));
                bestBoard.setUnit(2,2, new Archer(playerTypeThis));
                bestBoard.setUnit(2,3, new Archer(playerTypeThis));
                bestBoard.getUnit(1,2).setGeneral(true);
            }
            if(unitType == UnitType.MAGE){
                bestBoard.setUnit(0,2, new Archer(playerTypeThis));
                bestBoard.setUnit(0,3, new Archer(playerTypeThis));
                bestBoard.setUnit(1,2, new Knight(playerTypeThis));
                bestBoard.setUnit(1,3, new Healer(playerTypeThis));
                bestBoard.setUnit(2,2, new Mage(playerTypeThis));
                bestBoard.setUnit(2,3, new Archer(playerTypeThis));
                bestBoard.getUnit(2,2).setGeneral(true);
            }
            if(unitType == UnitType.ARCHER){
                bestBoard.setUnit(0,2, new Healer(playerTypeThis));
                bestBoard.setUnit(0,3, new Archer(playerTypeThis));
                bestBoard.setUnit(1,2, new Knight(playerTypeThis));
                bestBoard.setUnit(1,3, new Archer(playerTypeThis));
                bestBoard.setUnit(2,2, new Healer(playerTypeThis));
                bestBoard.setUnit(2,3, new Archer(playerTypeThis));
                bestBoard.getUnit(2,3).setGeneral(true);
            }
            if(unitType == UnitType.HEALER){
                bestBoard.setUnit(0,2, new Knight(playerTypeThis));
                bestBoard.setUnit(0,3, new Healer(playerTypeThis));
                bestBoard.setUnit(1,2, new Knight(playerTypeThis));
                bestBoard.setUnit(1,3, new Archer(playerTypeThis));
                bestBoard.setUnit(2,2, new Healer(playerTypeThis));
                bestBoard.setUnit(2,3, new Archer(playerTypeThis));
                bestBoard.getUnit(0,3).setGeneral(true);
            }
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
