package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.game.events.MakeMoveEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
 class UtilityMoveResult implements Comparable<UtilityMoveResult>{
    double value;
    MakeMoveEvent event;

    @Override
    public int compareTo(UtilityMoveResult o) {
        return (int) (this.value*1000 - o.value*1000);
    }
}