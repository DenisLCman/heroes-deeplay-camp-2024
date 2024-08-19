package io.deeplay.camp.botfarm.bots;

import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;

public abstract class Bot {

    public abstract PlaceUnitEvent generatePlaceUnitEvent(GameState gameState);

    public abstract MakeMoveEvent generateMakeMoveEvent(GameState gameState);
}
