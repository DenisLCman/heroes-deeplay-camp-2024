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
public class PossibleStartState {
    Board enemyUnits;
    Board allyUnits;
    int countWinRound;
    PlayerType forPlayerType;
}