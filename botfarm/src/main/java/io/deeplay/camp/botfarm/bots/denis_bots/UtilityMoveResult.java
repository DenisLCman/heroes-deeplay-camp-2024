package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
 class UtilityMoveResult {
    double value;
    MakeMoveEvent event;
}