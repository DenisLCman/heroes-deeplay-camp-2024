package io.deeplay.camp.botfarm.bots.denis_bots.ready_bots;

import io.deeplay.camp.botfarm.bots.Bot;
import io.deeplay.camp.botfarm.bots.RandomBot;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.BotTactic;
import io.deeplay.camp.botfarm.bots.denis_bots.TacticUtility;
import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.botfarm.bots.denis_bots.movement_algorithm.NewClastExpMaxAlg;
import io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm.MCPlaceAlg;
import io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm.MetricPlaceAlg;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Бот, использующий алгоритм ЭкспектиМакс, использующий:
 * Многопоточность,
 * Улучшенную кластеризацию,
 * Отсечение по вероятностям,
 * Расстановку основанную на метрических данных.
 */
public class ModNewClastMCPlaceExpMaxBot extends Bot {
    /** Тактика для данной битвы */
    UtilityFunction tacticUtility;
    /** Максимальная глубина для построения дерева */
    int maxDepth;
    /** Является ли эта расстановка первой? */
    @Setter boolean firstPlaceInGame = true;
    /** Алгоритм для игрового процесса */
    NewClastExpMaxAlg movementAlg;
    /** Алгоритм для фазы расстановки процесса */
    MCPlaceAlg placementAlg;

    /**
     * Конструктор.
     *
     * @param playerType Данная позиция игрока бота.
     * @param maxDepth Максимальная глубина дерева решений.
     */
    public ModNewClastMCPlaceExpMaxBot(PlayerType playerType, int maxDepth){
        tacticUtility = new TacticUtility(BotTactic.KNIGHT_TACTIC);
        tacticUtility.setCurrentPlayerType(playerType);
        this.maxDepth = maxDepth;
        movementAlg = new NewClastExpMaxAlg(maxDepth, tacticUtility);
        placementAlg = new MCPlaceAlg(tacticUtility);
    }


    @Override
    public PlaceUnitEvent generatePlaceUnitEvent(GameState gameState) {
        if(firstPlaceInGame) {
            placementAlg.findNewTactic(gameState);
            firstPlaceInGame = false;
        }
        return placementAlg.getPlaceResult(gameState).getPlace();
    }

    @Override
    public MakeMoveEvent generateMakeMoveEvent(GameState gameState) {
        firstPlaceInGame = true;
        return movementAlg.getMoveResult(gameState).getEvent();
    }


}
