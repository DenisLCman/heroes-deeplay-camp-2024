package io.deeplay.camp.botfarm.bots.denis_bots.ready_bots;

import io.deeplay.camp.botfarm.bots.Bot;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.BotTactic;
import io.deeplay.camp.botfarm.bots.denis_bots.TacticUtility;
import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.botfarm.bots.denis_bots.movement_algorithm.TimeLimitNewClastExpMaxAlg;
import io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm.MetricPlaceAlg;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.Setter;

/**
 * Бот, использующий алгоритм ЭкспектиМакс, использующий:
 * Многопоточность,
 * Улучшенную кластеризацию,
 * Расстановку основанную на метрических данных,
 * Отсечение по вероятностям,
 * Ограничение по времени для нахождения оптимального хода
 */
public class TimeLimitNewClastMetricPlaceExpMaxBot extends Bot {
    /** Тактика для данной битвы */
    UtilityFunction tacticUtility;
    /** Максимальная глубина для построения дерева */
    int maxDepth;
    /** Является ли эта расстановка первой? */
    @Setter boolean firstPlaceInGame = true;
    /** Алгоритм для игрового процесса */
    TimeLimitNewClastExpMaxAlg movementAlg;
    /** Алгоритм для фазы расстановки */
    MetricPlaceAlg placementAlg;

    /**
     * Конструктор.
     *
     * @param playerType Данная позиция игрока бота.
     * @param maxDepth Максимальная глубина дерева решений.
     */
    public TimeLimitNewClastMetricPlaceExpMaxBot(PlayerType playerType, int maxDepth){
        tacticUtility = new TacticUtility(BotTactic.BASE_TACTIC);
        tacticUtility.setCurrentPlayerType(playerType);
        this.maxDepth = maxDepth;
        movementAlg = new TimeLimitNewClastExpMaxAlg(this.maxDepth, tacticUtility);
        placementAlg = new MetricPlaceAlg(tacticUtility);
    }


    @Override
    public PlaceUnitEvent generatePlaceUnitEvent(GameState gameState) {
        if(firstPlaceInGame) {
            placementAlg.findNewTactic(gameState);
            firstPlaceInGame = false;
        }
        return placementAlg.getPlaceResult(gameState);
    }

    @Override
    public MakeMoveEvent generateMakeMoveEvent(GameState gameState) {
        firstPlaceInGame = true;
        return movementAlg.getMoveResult(gameState).getEvent();
    }

}
