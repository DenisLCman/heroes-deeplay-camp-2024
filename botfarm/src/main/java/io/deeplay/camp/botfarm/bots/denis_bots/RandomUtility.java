package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.game.mechanics.GameState;

import java.util.Random;

public class RandomUtility implements UtilityFunction{

    @Override
    public double getUtility(GameState gameState) {
        Random r = new Random();
        double rangeMin = 0;
        double rangeMax = 1;
        double randomValue = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
        return randomValue;
    }

}
