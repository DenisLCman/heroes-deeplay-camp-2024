package io.deeplay.camp.entities;

import io.deeplay.camp.mechanics.PlayerType;

public class Mage extends Unit {
  public Mage(PlayerType playerType) {
    super(UnitType.MAGE, 10, 10, 2, 5, 12, false);
    this.playerType = playerType;
    setAttack(AttackType.MASS_ATTACK);
  }

  @Override
  public void playMove(Unit targetUnit) {
    int diceRoll = (int) (Math.random() * 20);
    if (diceRoll + accuracy > targetUnit.getArmor()) {
      targetUnit.setNowHp(targetUnit.getNowHp() - damage);
    }
    isMoved = true;
  }
}
