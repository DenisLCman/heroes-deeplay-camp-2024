package io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm;

import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.mechanics.GameState;

import java.util.List;

public class RandomPlaceAlg {
    public RandomPlaceAlg(UtilityFunction tacticUtility){

    }
    public void findNewTactic(GameState gameState){

    }
    public PlaceUnitEvent getPlaceResult(GameState gameState){
        List<PlaceUnitEvent> placeUnitEvents = gameState.getPossiblePlaces();
        if(!placeUnitEvents.isEmpty()){
            return placeUnitEvents.get((int)(Math.random()*placeUnitEvents.size()));
        }
        else{
            return null;
        }
    }
}
