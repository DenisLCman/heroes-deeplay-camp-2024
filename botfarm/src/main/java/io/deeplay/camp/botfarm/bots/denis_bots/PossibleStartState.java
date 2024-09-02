package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.game.entities.Board;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PossibleStartState implements Comparable<PossibleStartState>{
    Board enemyUnits;
    Board allyUnits;
    int countWinRound;
    PlayerType forPlayerType;

    @Override
    public int compareTo(PossibleStartState o) {
        return this.countWinRound - o.getCountWinRound();
    }
}