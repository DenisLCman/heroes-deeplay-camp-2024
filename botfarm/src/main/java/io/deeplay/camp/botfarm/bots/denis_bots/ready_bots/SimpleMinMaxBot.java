package io.deeplay.camp.botfarm.bots.denis_bots.ready_bots;

import io.deeplay.camp.botfarm.bots.Bot;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.BotTactic;
import io.deeplay.camp.botfarm.bots.denis_bots.TacticUtility;
import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.botfarm.bots.denis_bots.movement_algorithm.SimpleMinMaxAlg;
import io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm.RandomPlaceAlg;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.Setter;

/**
 * Бот, использующий простой алгоритм МинМакса, использующий:
 * Начальную многопоточность,
 * Рандомную расстановку
 */
public class SimpleMinMaxBot extends Bot {
    /** Тактика для данной битвы */
    UtilityFunction tacticUtility;
    /** Максимальная глубина для построения дерева */
    int maxDepth;
    /** Является ли эта расстановка первой? */
    @Setter boolean firstPlaceInGame = true;
    /** Алгоритм для игрового процесса */
    SimpleMinMaxAlg movementAlg;
    /** Алгоритм для фазы расстановки */
    RandomPlaceAlg placementAlg;

    /**
     * Конструктор.
     *
     * @param playerType Данная позиция игрока бота.
     * @param maxDepth Максимальная глубина дерева решений.
     */
    public SimpleMinMaxBot(PlayerType playerType, int maxDepth){
        tacticUtility = new TacticUtility(BotTactic.BASE_TACTIC);
        tacticUtility.setCurrentPlayerType(playerType);
        this.maxDepth = maxDepth;
        movementAlg = new SimpleMinMaxAlg(this.maxDepth, tacticUtility);
        placementAlg = new RandomPlaceAlg(tacticUtility);
    }

    @Override
    public PlaceUnitEvent generatePlaceUnitEvent(GameState gameState) {
        return placementAlg.getPlaceResult(gameState);
    }

    @Override
    public MakeMoveEvent generateMakeMoveEvent(GameState gameState) {
        firstPlaceInGame = true;
        return movementAlg.getMoveResult(gameState).getEvent();
    }




}


