package io.deeplay.camp.botfarm.bots.denis_bots.ready_bots;

import io.deeplay.camp.botfarm.bots.Bot;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.BotTactic;
import io.deeplay.camp.botfarm.bots.denis_bots.TacticUtility;
import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.botfarm.bots.denis_bots.movement_algorithm.ABMinMaxAlg;
import io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm.EvristicPlaceAlg;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.Setter;

/**
 * Бот, использующий алгоритм МинМакс, использующий:
 * Расстановку, создаваемую через эмпирическую функцию полезности юнита при расстановке,
 * Альфа-Бета отсечение.
 */
public class PlaceABMinMaxBot extends Bot {
    UtilityFunction tacticUtility;
    int maxDepth;
    @Setter
    boolean firstPlaceInGame = true;
    ABMinMaxAlg movementAlg;
    EvristicPlaceAlg placementAlg;

    public PlaceABMinMaxBot(PlayerType playerType, int maxDepth){
        tacticUtility = new TacticUtility(BotTactic.BASE_TACTIC);
        tacticUtility.setCurrentPlayerType(playerType);
        this.maxDepth = maxDepth;
        movementAlg = new ABMinMaxAlg(this.maxDepth, tacticUtility);
        placementAlg = new EvristicPlaceAlg(tacticUtility);
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
