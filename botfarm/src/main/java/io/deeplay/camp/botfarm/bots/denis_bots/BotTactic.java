package io.deeplay.camp.botfarm.bots.denis_bots;

public enum BotTactic {
    MAGE_TACTIC,
    KNIGHT_TACTIC,
    ARCHER_TACTIC,
    HEALER_TACTIC;

    public static BotTactic getRandom() {
        return values()[(int) (Math.random() * values().length)];
    }
}
