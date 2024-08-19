package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.game.entities.Unit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnitQuality {
    private Unit unit;
    private double weigth;
    private double price;

    public UnitQuality(Unit unit, double weigth, double price){
        this.unit = unit;
        this.weigth = weigth;
        this.price = price;
    }




}
