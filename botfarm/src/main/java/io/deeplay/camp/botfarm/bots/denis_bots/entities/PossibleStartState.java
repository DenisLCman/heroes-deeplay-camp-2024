package io.deeplay.camp.botfarm.bots.denis_bots.entities;

import io.deeplay.camp.game.entities.Board;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Класс хранящий в себе информацию о расстановке и о её данных, для
 * сохранения её в кэщ и дальнейшего использования.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PossibleStartState implements Comparable<PossibleStartState>{
    /** Вражеская расстановка. */
    Board enemyUnits;
    /** Общая расстановка */
    Board allyUnits;
    /** Количество раундов, требуемое для победы при данной союзной расстановки
     * против данной вражеской расстановки */
    int countWinRound;
    /** Для какого игрока данная расстановка создавалась*/
    PlayerType forPlayerType;

    /** Переопределённый метод для сравнения Первоначальных расстановок*/
    @Override
    public int compareTo(PossibleStartState otherPossibleStartState) {
        return this.countWinRound - otherPossibleStartState.getCountWinRound();
    }
}