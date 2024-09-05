package io.deeplay.camp.botfarm.bots.denis_bots.ready_bots;

import io.deeplay.camp.botfarm.bots.Bot;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.BotTactic;
import io.deeplay.camp.botfarm.bots.denis_bots.TacticUtility;
import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.botfarm.bots.denis_bots.movement_algorithm.SimpleExpMaxAlg;
import io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm.EvristicPlaceAlg;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.Setter;

/**
 * Бот, использующий алгоритм ЭкспектиМакс, использующий:
 * Расстановку, создаваемую через эмпирическую функцию полезности юнита при расстановке.
 */
public class PlaceExpMaxBot extends Bot {
    /** Тактика для данной битвы */
    UtilityFunction tacticUtility;
    /** Максимальная глубина для построения дерева */
    int maxDepth;
    /** Является ли эта расстановка первой? */
    @Setter boolean firstPlaceInGame = true;
    /** Алгоритм для игрового процесса */
    SimpleExpMaxAlg movementAlg;
    /** Алгоритм для фазы расстановки */
    EvristicPlaceAlg placementAlg;

    /**
     * Конструктор.
     *
     * @param playerType Данная позиция игрока бота.
     * @param maxDepth Максимальная глубина дерева решений.
     */
    public PlaceExpMaxBot(PlayerType playerType, int maxDepth){
        tacticUtility = new TacticUtility(BotTactic.KNIGHT_TACTIC);
        tacticUtility.setCurrentPlayerType(playerType);
        this.maxDepth = maxDepth;
        movementAlg = new SimpleExpMaxAlg(this.maxDepth, tacticUtility);
        placementAlg = new EvristicPlaceAlg(tacticUtility);
    }


    @Override
    public PlaceUnitEvent generatePlaceUnitEvent(GameState gameState){
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
