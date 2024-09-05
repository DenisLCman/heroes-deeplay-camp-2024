package io.deeplay.camp.botfarm.bots.denis_bots.entities;

import io.deeplay.camp.game.events.MakeMoveEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * Сущность, используемая для сохранения действия юнита и его
 * ценности для игрового состояния.
 */
@Getter
@AllArgsConstructor
public class UtilityMoveResult implements Comparable<UtilityMoveResult>{
    double value;
    MakeMoveEvent event;

    /**
     * Переопределённый метод для сравнения с другим UtilityMoveResult
     *
     * @param otherEvent объект, с которым сравнивается текущий объект.
     * @return различие между ценностями объектов.
     */
    @Override
    public int compareTo(UtilityMoveResult otherEvent) {
        return (int) (this.value*10000 - otherEvent.value*10000);
    }
}