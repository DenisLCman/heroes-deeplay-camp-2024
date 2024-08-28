package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.botfarm.bots.Bot;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;

import java.util.Random;

public class RandomUtility implements UtilityFunction{
    BotTactic botTactic;
    PlayerType currentPlayerType;
    @Override
    public double getMoveUtility(GameState gameState) {
        Random r = new Random();
        double rangeMin = 0;
        double rangeMax = 1;
        double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        return randomValue;
    }

    @Override
    public double getPlaceUtility(GameState gameState) {
        Random r = new Random();
        double rangeMin = 0;
        double rangeMax = 1;
        double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        return randomValue;
    }

    @Override
    public void setBotTactic(BotTactic botTactic) {
        this.botTactic = botTactic;
    }
    @Override
    public void setCurrentPlayerType(PlayerType playerType) {
        currentPlayerType = playerType;
    }

    @Override
    public PlayerType getCurrentPlayerType() {
        return currentPlayerType;
    }

    @Override
    public double monteCarloAlg(GameState root, int countGame, PlaceUnitEvent event) {
        return 0;
    }

}
