package io.deeplay.camp.botfarm.bots.denis_bots.entities;

import io.deeplay.camp.game.events.PlaceUnitEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Сущность, используемая для сохранение одной расстановки юнита и его
 * ценности для игрового состояния.
 */
@Getter
@AllArgsConstructor
public class UtilityPlaceResult {
    double value;
    PlaceUnitEvent place;
}