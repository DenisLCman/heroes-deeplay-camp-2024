package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.game.entities.UnitType;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;

import java.util.List;

public interface UtilityFunction {
    /**
     * Оценка состояния
     * Свойства:
     * 1 - победа первого игрока
     * 0 - ничья
     * -1 - победа второго игрока
     *
     */
    PlayerType currentPlayerType = null;
    BotTactic botTactic = null;
    double getMoveUtility(final GameState gameState);
    double getPlaceUtility(final GameState gameState);
    void setBotTactic(BotTactic botTactic);
    void setCurrentPlayerType(PlayerType playerType);
    PlayerType getCurrentPlayerType();
    double monteCarloAlg(GameState root, int countGame, PlaceUnitEvent placeUnitEvent);
    List<MakeMoveEvent> changeMoveByTactic(GameState gameState, List<MakeMoveEvent> eventList);
}
