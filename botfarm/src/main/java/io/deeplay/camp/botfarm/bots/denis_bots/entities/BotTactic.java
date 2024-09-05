package io.deeplay.camp.botfarm.bots.denis_bots.entities;

/**
 * Энам, реализующий тактики для бота, определяющая его
 * Генерала.
 */
public enum BotTactic {
    MAGE_TACTIC,
    KNIGHT_TACTIC,
    ARCHER_TACTIC,
    HEALER_TACTIC,
    BASE_TACTIC;

    public static BotTactic getRandom() {
        return values()[(int) (Math.random() * values().length)];
    }
}
