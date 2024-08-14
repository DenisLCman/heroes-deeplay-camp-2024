package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.game.mechanics.GameState;

public interface UtilityFunction {
    /**
     * Оценка состояния
     * Свойства:
     * 1 - победа первого игрока
     * 0 - ничья
     * -1 - победа второго игрока
     *
     */

    double getUtility(final GameState gameState);
}
