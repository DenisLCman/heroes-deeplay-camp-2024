package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.game.entities.*;
import io.deeplay.camp.game.mechanics.GameStage;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;

import lombok.Setter;

public class TacticUtility implements UtilityFunction{

    BotTactic currentBotTactic;
    PlayerType currentPlayerType;

    public TacticUtility(BotTactic botTactic){
        currentBotTactic = botTactic;
    }

    public void setCurrentBotTactic(BotTactic currentBotTactic) {
        this.currentBotTactic = currentBotTactic;
    }

    public void setCurrentPlayerType(PlayerType playerType){
        this.currentPlayerType = playerType;
    }

    @Override
    public PlayerType getCurrentPlayerType() {
        return currentPlayerType;
    }

    @Override
    public double getMoveUtility(GameState gameState) {
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
            case BASE_TACTIC -> {
                return baseTacticUtility(gameState);
            }
        }
        return 0;
    }

    private double baseTacticUtility(GameState gameState) {
        int FirstHp = 0;
        int SecondHp = 0;
        int liveSecond = 0;
        int liveFirst = 0;
        int AllHp;
        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = 0; row < Board.ROWS;row++){
                if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.FIRST_PLAYER &&
                        gameState.getBoard().getUnit(column,row).isAlive()){
                    FirstHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                    liveFirst++;
                }
                else if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.SECOND_PLAYER &&
                        gameState.getBoard().getUnit(column,row).isAlive()){
                    SecondHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                    liveSecond++;
                }
            }
        }
        AllHp = FirstHp + SecondHp;
        double AllLives = liveFirst + liveSecond;
        double perFirst =  ((double)FirstHp/AllHp)*100;
        double perSecond = ((double)SecondHp/AllHp)*100;
        double result = perSecond/perFirst;

        double perLiveFirst = ((double)liveFirst/AllLives)*100;
        double perLiveSecond = ((double)liveSecond/AllLives)*100;

        if(currentPlayerType == PlayerType.SECOND_PLAYER){
            return (((liveSecond*perSecond)/((liveFirst*6)+1)));
        }
        else{
            return (((liveFirst*perFirst)/((liveSecond*6)+1)));
        }
    }

    @Override
    public double getPlaceUtility(GameState gameState) {

        double profitFirst = 0;
        double profitSecond = 0;
        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = 0; row < Board.ROWS;row++){
                if(!gameState.getBoard().isEmptyCell(column, row)) {
                    if (gameState.getBoard().getUnit(column, row).getPlayerType() == PlayerType.FIRST_PLAYER &&
                            gameState.getBoard().getUnit(column, row).isAlive()) {
                        if (row == 1){
                            profitFirst += calculateProfit(gameState.getBoard().getUnit(column, row).getUnitType(), AttackType.CLOSE_ATTACK);
                        }
                        else{
                            profitFirst += calculateProfit(gameState.getBoard().getUnit(column, row).getUnitType(), AttackType.LONG_ATTACK);
                        }
                    } else if (gameState.getBoard().getUnit(column, row).getPlayerType() == PlayerType.SECOND_PLAYER &&
                            gameState.getBoard().getUnit(column, row).isAlive()) {
                        if (row == 2){
                            profitSecond += calculateProfit(gameState.getBoard().getUnit(column, row).getUnitType(), AttackType.CLOSE_ATTACK);
                        }
                        else{
                            profitSecond += calculateProfit(gameState.getBoard().getUnit(column, row).getUnitType(), AttackType.LONG_ATTACK);
                        }
                    }
                }
            }
        }

        if(currentPlayerType == PlayerType.FIRST_PLAYER){
            return profitFirst;
        }
        else {
            return profitSecond;
        }

    }

    @Override
    public void setBotTactic(BotTactic botTactic) {
        this.currentBotTactic = botTactic;
    }

    private double mageTacticUtility(GameState gameState){
        int FirstHp = 0;
        int SecondHp = 0;
        int liveSecond = 0;
        int liveFirst = 0;
        int AllHp;
        int liveGeneralFirst = 0;
        int liveGeneralSecond = 0;
        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = 0; row < Board.ROWS;row++){
                if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.FIRST_PLAYER &&
                        gameState.getBoard().getUnit(column,row).isAlive()){
                    FirstHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                    liveFirst++;
                    if (gameState.getBoard().getUnit(column,row).isGeneral()){
                        liveGeneralFirst++;
                    }
                }
                else if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.SECOND_PLAYER &&
                        gameState.getBoard().getUnit(column,row).isAlive()){
                    SecondHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                    liveSecond++;
                    if (gameState.getBoard().getUnit(column,row).isGeneral()){
                        liveGeneralSecond++;
                    }
                }
            }
        }
        AllHp = FirstHp + SecondHp;
        double AllLives = liveFirst + liveSecond;
        double perFirst =  ((double)FirstHp/AllHp)*100;
        double perSecond = ((double)SecondHp/AllHp)*100;
        double result = perSecond/perFirst;

        double perLiveFirst = ((double)liveFirst/AllLives)*100;
        double perLiveSecond = ((double)liveSecond/AllLives)*100;

        double modWinner = 1;
        if(currentPlayerType == PlayerType.SECOND_PLAYER ){
            if (liveSecond > liveFirst && gameState.getGameStage() == GameStage.ENDED){
                modWinner = 100;
            }
            return (((liveSecond*perSecond+liveGeneralSecond*10)/((liveFirst*6+liveGeneralFirst*12)+1)))*modWinner;
        }
        else{
            if (liveFirst > liveSecond && gameState.getGameStage() == GameStage.ENDED){
                modWinner = 100;
            }
            return (((liveFirst*perFirst+liveGeneralFirst*10)/((liveSecond*6+liveGeneralSecond*12)+1)))*modWinner;
        }
    }

    private double knightTacticUtility(GameState gameState){
        int FirstHp = 0;
        int SecondHp = 0;
        int liveSecond = 0;
        int liveFirst = 0;

        int AllHp;
        int liveGeneralFirst = 0;
        int liveGeneralSecond = 0;
        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = 0; row < Board.ROWS;row++){
                if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.FIRST_PLAYER &&
                        gameState.getBoard().getUnit(column,row).isAlive()){
                    FirstHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                    liveFirst++;
                    if (gameState.getBoard().getUnit(column,row).isGeneral()){
                        liveGeneralFirst++;
                    }
                }
                else if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.SECOND_PLAYER &&
                        gameState.getBoard().getUnit(column,row).isAlive()){
                    SecondHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                    liveSecond++;
                    if (gameState.getBoard().getUnit(column,row).isGeneral()){
                        liveGeneralSecond++;
                    }
                }
            }
        }
        AllHp = FirstHp + SecondHp;
        double AllLives = liveFirst + liveSecond;
        double perFirst =  ((double)FirstHp/AllHp)*100;
        double perSecond = ((double)SecondHp/AllHp)*100;
        double result = perSecond/perFirst;

        double perLiveFirst = ((double)liveFirst/AllLives)*100;
        double perLiveSecond = ((double)liveSecond/AllLives)*100;

        double modWinner = 1;
        if(currentPlayerType == PlayerType.SECOND_PLAYER){
            if (liveSecond > liveFirst && gameState.getGameStage() == GameStage.ENDED){
                modWinner = 100;
            }
            return (((liveSecond*perSecond+liveGeneralSecond*10)/((liveFirst*6+liveGeneralFirst*12)+1)))*modWinner;
        }
        else{
            if (liveFirst > liveSecond && gameState.getGameStage() == GameStage.ENDED){
                modWinner = 100;
            }
            return (((liveFirst*perFirst+liveGeneralFirst*10)/((liveSecond*6+liveGeneralSecond*12)+1)))*modWinner;
        }

    }
    private double archerTacticUtility(GameState gameState){
        int FirstHp = 0;
        int SecondHp = 0;
        int liveSecond = 0;
        int liveFirst = 0;
        int AllHp;
        int liveGeneralFirst = 0;
        int liveGeneralSecond = 0;
        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = 0; row < Board.ROWS;row++){
                if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.FIRST_PLAYER &&
                        gameState.getBoard().getUnit(column,row).isAlive()){
                    FirstHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                    liveFirst++;
                    if (gameState.getBoard().getUnit(column,row).isGeneral()){
                        liveGeneralFirst++;
                    }
                }
                else if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.SECOND_PLAYER &&
                        gameState.getBoard().getUnit(column,row).isAlive()){
                    SecondHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                    liveSecond++;
                    if (gameState.getBoard().getUnit(column,row).isGeneral()){
                        liveGeneralSecond++;
                    }
                }
            }
        }
        AllHp = FirstHp + SecondHp;
        double AllLives = liveFirst + liveSecond;
        double perFirst =  ((double)FirstHp/AllHp)*100;
        double perSecond = ((double)SecondHp/AllHp)*100;
        double result = perSecond/perFirst;

        double perLiveFirst = ((double)liveFirst/AllLives)*100;
        double perLiveSecond = ((double)liveSecond/AllLives)*100;

        double modWinner = 1;
        if(currentPlayerType == PlayerType.SECOND_PLAYER ){
            if (liveSecond > liveFirst && gameState.getGameStage() == GameStage.ENDED){
                modWinner = 100;
            }
            return (((liveSecond*perSecond+liveGeneralSecond*10)/((liveFirst*6+liveGeneralFirst*12)+1)))*modWinner;
        }
        else{
            if (liveFirst > liveSecond && gameState.getGameStage() == GameStage.ENDED){
                modWinner = 100;
            }
            return (((liveFirst*perFirst+liveGeneralFirst*10)/((liveSecond*6+liveGeneralSecond*12)+1)))*modWinner;
        }
    }
    private double healerTacticUtility(GameState gameState){
        int FirstHp = 0;
        int SecondHp = 0;
        int liveSecond = 0;
        int liveFirst = 0;
        int AllHp;
        int liveGeneralFirst = 0;
        int liveGeneralSecond = 0;
        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = 0; row < Board.ROWS;row++){
                if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.FIRST_PLAYER &&
                        gameState.getBoard().getUnit(column,row).isAlive()){
                    FirstHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                    liveFirst++;
                    if (gameState.getBoard().getUnit(column,row).isGeneral()){
                        liveGeneralFirst++;
                    }
                }
                else if(gameState.getBoard().getUnit(column,row).getPlayerType() == PlayerType.SECOND_PLAYER &&
                        gameState.getBoard().getUnit(column,row).isAlive()){
                    SecondHp+=gameState.getBoard().getUnit(column,row).getCurrentHp();
                    liveSecond++;
                    if (gameState.getBoard().getUnit(column,row).isGeneral()){
                        liveGeneralSecond++;
                    }
                }
            }
        }
        AllHp = FirstHp + SecondHp;
        double AllLives = liveFirst + liveSecond;
        double perFirst =  ((double)FirstHp/AllHp)*100;
        double perSecond = ((double)SecondHp/AllHp)*100;
        double result = perSecond/perFirst;

        double perLiveFirst = ((double)liveFirst/AllLives)*100;
        double perLiveSecond = ((double)liveSecond/AllLives)*100;

        double modWinner = 1;
        if(currentPlayerType == PlayerType.SECOND_PLAYER){
            if (liveSecond > liveFirst && gameState.getGameStage() == GameStage.ENDED){
                modWinner = 10;
            }
            return (((liveSecond*perSecond+liveGeneralSecond*10)/((liveFirst*6+liveGeneralFirst*12)+1)))*modWinner;
        }
        else{
            if (liveFirst > liveSecond && gameState.getGameStage() == GameStage.ENDED){
                modWinner = 10;
            }
            return (((liveFirst*perFirst+liveGeneralFirst*10)/((liveSecond*6+liveGeneralSecond*12)+1)))*modWinner;
        }
    }


    public double calculateProfit(UnitType takeUnit, AttackType attackType){
        Unit unit = null;
        switch (takeUnit){
            case KNIGHT -> unit = new Knight(getCurrentPlayerType());
            case ARCHER -> unit = new Archer(getCurrentPlayerType());
            case HEALER -> unit = new Healer(getCurrentPlayerType());
            case MAGE -> unit = new Mage(getCurrentPlayerType());
        }
        int unitHp = unit.getMaxHp();
        int unitDamage = unit.getDamage();
        int unitArm = unit.getArmor();
        int unitAcc = unit.getAccuracy();
        int buffHp = 0;
        int buffDamage = 0;
        int buffArm = 0;
        int buffAcc = 0;
        double resultProfit = 0;
        int placeMod = 0;
        if(attackType == AttackType.CLOSE_ATTACK){
            placeMod = 1;
        }
        else{
            placeMod = -1;
        }

        double attackTypeMod = 0;
        if(takeUnit != UnitType.MAGE) {
            attackTypeMod = -1 * ((float) unitArm / 20 - 1) * placeMod;
        }
        else{
            attackTypeMod = -1 * ((float) unitArm / 20 - 1)  * placeMod;
        }
        switch (currentBotTactic){
            case KNIGHT_TACTIC -> buffArm+=GeneralBuff.Buffs.ARMOR.getValue();
            case MAGE_TACTIC -> buffDamage+=GeneralBuff.Buffs.DAMAGE.getValue();
            case HEALER_TACTIC -> buffHp+=GeneralBuff.Buffs.MAXHP.getValue();
            case ARCHER_TACTIC -> buffAcc+=GeneralBuff.Buffs.ACCURACY.getValue();
        }
        switch (takeUnit){
            case KNIGHT -> resultProfit = (unitHp+buffHp+unitDamage+buffDamage+unitAcc+buffAcc+unitArm+buffArm)*(1 + attackTypeMod);
            case ARCHER -> resultProfit = Math.round(unitHp+buffHp+unitDamage+buffDamage+unitAcc+buffAcc+(unitArm+buffArm)*1.35)*(1 + -1*attackTypeMod);
            case HEALER -> resultProfit = Math.round((unitHp+buffHp)*2+unitDamage+buffDamage+ (float) (unitAcc + buffAcc) /2+unitArm+buffArm)*(1 + -1*attackTypeMod);
            case MAGE -> resultProfit = (unitHp+buffHp+unitDamage+buffDamage+(unitAcc+buffAcc)*3+unitArm+buffArm)*(1 + -1*attackTypeMod);

        }
        return resultProfit;
    }
}
