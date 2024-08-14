package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.game.entities.Board;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;

public class TacticUtility implements UtilityFunction{

    BotTactic currentBotTactic;

    public TacticUtility(BotTactic botTactic){
        currentBotTactic = botTactic;
    }
    @Override
    public double getUtility(GameState gameState) {
        switch (currentBotTactic){
            case MAGE_TACTIC -> {
                return mageTacticUtility(gameState);
            }
            case KNIGHT_TACTIC -> {
                return knightTacticUtility(gameState);
            }
            case ARCHER_TACTIC -> {
                return archerTacticUtility(gameState);
            }
            case HEALER_TACTIC -> {
                return healerTacticUtility(gameState);
            }
        }
        return 0;
    }

    private double mageTacticUtility(GameState gameState){
        int FirstHp = 0;
        int SecondHp = 0;
        int AllHp = 0;
        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = 0; row < Board.ROWS;row++){
                if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.FIRST_PLAYER){
                    FirstHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                }
                else{
                    SecondHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                }
            }
        }
        AllHp = FirstHp + SecondHp;
        double perFirst =  ((double)FirstHp / AllHp) * 100;
        double perSecond = ((double)SecondHp / AllHp) * 100;
        double result = (double) perFirst /perSecond  - 1;
        return result;
    }
    private double knightTacticUtility(GameState gameState){
        int FirstHp = 0;
        int SecondHp = 0;
        int AllHp = 0;
        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = 0; row < Board.ROWS;row++){
                if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.FIRST_PLAYER){
                    FirstHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                }
                else{
                    SecondHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                }
            }
        }
        AllHp = FirstHp + SecondHp;
        double perFirst =  ((double)FirstHp / AllHp) * 100;
        double perSecond = ((double)SecondHp / AllHp) * 100;
        double result = (double) perFirst /perSecond  - 1;
        return result;
    }
    private double archerTacticUtility(GameState gameState){
        int FirstHp = 0;
        int SecondHp = 0;
        int AllHp = 0;
        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = 0; row < Board.ROWS;row++){
                if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.FIRST_PLAYER){
                    FirstHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                }
                else{
                    SecondHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                }
            }
        }
        AllHp = FirstHp + SecondHp;
        double perFirst =  ((double)FirstHp / AllHp) * 100;
        double perSecond = ((double)SecondHp / AllHp) * 100;
        double result = (double) perFirst /perSecond  - 1;
        return result;
    }
    private double healerTacticUtility(GameState gameState){
        int FirstHp = 0;
        int SecondHp = 0;
        int AllHp = 0;
        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = 0; row < Board.ROWS;row++){
                if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.FIRST_PLAYER){
                    FirstHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                }
                else{
                    SecondHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                }
            }
        }
        AllHp = FirstHp + SecondHp;
        double perFirst =  ((double)FirstHp / AllHp) * 100;
        double perSecond = ((double)SecondHp / AllHp) * 100;
        double result = (double) perFirst /perSecond  - 1;
        return result;
    }
}
