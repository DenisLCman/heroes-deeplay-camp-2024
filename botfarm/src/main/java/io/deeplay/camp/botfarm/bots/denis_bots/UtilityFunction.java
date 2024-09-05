package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.botfarm.bots.denis_bots.entities.BotTactic;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;

import java.util.List;

public interface UtilityFunction {


    /**
     * Показывает, каким игроком в данный момент является бот
     */
    PlayerType currentPlayerType = null;
    /**
     * Хранит тактику бота, которую применяет в данной игре бот
     */
    BotTactic botTactic = null;

    /**
     * Метод, находящий текущую ценность данного игрового состояния
     * в после хода игрока.
     *
     * @param gameState Игровое состояние.
     * @return Ценность игрового состояния gameState.
     */
    double getMoveUtility(final GameState gameState);

    /**
     * Метод, находящий текущую ценность данного игрового состояния
     * в зависимости от расстановки игрока.
     *
     * @param gameState Игровое состояние.
     * @return Ценность игрового состояния gameState.
     */
    double getPlaceUtility(final GameState gameState);

    /**
     * Сеттер тактики для текущей битвы для бота
     */
    void setBotTactic(BotTactic botTactic);

    /**
     * Сеттер текущей позиции игрока для текущей битвы для бота
     */
    void setCurrentPlayerType(PlayerType playerType);

    /**
     * Геттер текущей позиции игрока в текущей битвы у бота
     */
    PlayerType getCurrentPlayerType();

    /**
     * Метод, находящий текущую ценность данного игрового состояния
     * в зависимости от расстановки игрока.
     *
     * @param root Игровое состояние.
     * @param countGame Количество игр для отыгровки ботами.
     * @param placeUnitEvent Последняя применённая расстановка.
     * @return Ценность игрового состояния root.
     */
    double monteCarloAlg(GameState root, int countGame, PlaceUnitEvent placeUnitEvent);

    /**
     * Метод, меняющий очередность действий бота в зависимости
     * от типа Юнита и их количества на поле боя
     * @param gameState Игровое состояние.
     * @param eventList Всевозможные на данный момент ходы бота.
     * @return Обновлённый список с убранными ненужными в данный момент действиями бота.
     */
    List<MakeMoveEvent> changeMoveByTactic(GameState gameState, List<MakeMoveEvent> eventList);
}
