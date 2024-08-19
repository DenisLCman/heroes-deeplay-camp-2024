package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.game.entities.Archer;
import io.deeplay.camp.game.entities.Healer;
import io.deeplay.camp.game.entities.Knight;
import io.deeplay.camp.game.entities.Mage;
import io.deeplay.camp.game.events.ChangePlayerEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.exceptions.GameException;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TreeBuilderTest {


    @Test
    void buildGameTreeNodeOneEnemy() {
        try{
            GameState gameStateTest = new GameState();
            TreeBuilder treeBuilder = new TreeBuilder();
            Knight generalKnight = new Knight(PlayerType.FIRST_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 0, generalKnight, PlayerType.FIRST_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 0, new Mage(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 0, new Healer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 1, new Archer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.FIRST_PLAYER));

            generalKnight = new Knight(PlayerType.SECOND_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 3, generalKnight, PlayerType.SECOND_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 3, new Mage(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 3, new Healer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 2, new Archer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.SECOND_PLAYER));

            gameStateTest.getCurrentBoard().getUnit(2,3).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,3).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,3).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(2,2).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,2).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,2).setCurrentHp(0);

            gameStateTest.getCurrentBoard().getUnit(0,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,1).setCurrentHp(15);
            gameStateTest.getCurrentBoard().getUnit(0,1).setAccuracy(100);
            gameStateTest.getCurrentBoard().getUnit(1,1).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,1).setCurrentHp(0);

            TreeBuilder.Stats stats = treeBuilder.buildGameTree(gameStateTest,0, 0);
            assertEquals(stats.getNumNodes(),2);

        }catch (GameException e){
        }
    }

    @Test
    void buildGameTreeDepthTwoEnemy() {
        try{
            GameState gameStateTest = new GameState();
            Knight generalKnight = new Knight(PlayerType.FIRST_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 0, generalKnight, PlayerType.FIRST_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 0, new Mage(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 0, new Healer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 1, new Archer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.FIRST_PLAYER));

            generalKnight = new Knight(PlayerType.SECOND_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 3, generalKnight, PlayerType.SECOND_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 3, new Mage(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 3, new Healer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 2, new Archer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.SECOND_PLAYER));


            gameStateTest.getCurrentBoard().getUnit(0,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,1).setCurrentHp(15);
            gameStateTest.getCurrentBoard().getUnit(0,1).setAccuracy(15);
            gameStateTest.getCurrentBoard().getUnit(1,1).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,1).setCurrentHp(0);

            gameStateTest.getCurrentBoard().getUnit(2,3).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,3).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(0,3).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(2,2).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,2).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,2).setCurrentHp(0);


            TreeBuilder treeBuilder = new TreeBuilder();
            TreeBuilder.Stats stats = treeBuilder.buildGameTree(gameStateTest,0,3);
            assertEquals(stats.getDepth(),3);

        }catch (GameException e){
        }
    }

    @Test
    void buildGameTreeNodeTwoEnemy() {
        try{
            GameState gameStateTest = new GameState();
            Knight generalKnight = new Knight(PlayerType.FIRST_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 0, generalKnight, PlayerType.FIRST_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 0, new Mage(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 0, new Healer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 1, new Archer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.FIRST_PLAYER));

            generalKnight = new Knight(PlayerType.SECOND_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 3, generalKnight, PlayerType.SECOND_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 3, new Mage(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 3, new Healer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 2, new Archer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.SECOND_PLAYER));


            gameStateTest.getCurrentBoard().getUnit(0,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,1).setCurrentHp(15);
            gameStateTest.getCurrentBoard().getUnit(0,1).setAccuracy(15);
            gameStateTest.getCurrentBoard().getUnit(1,1).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,1).setCurrentHp(0);

            gameStateTest.getCurrentBoard().getUnit(2,3).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,3).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(0,3).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(2,2).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,2).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,2).setCurrentHp(0);


            TreeBuilder treeBuilder = new TreeBuilder();
            TreeBuilder.Stats stats = treeBuilder.buildGameTree(gameStateTest,0,3);
            assertEquals(stats.getNumNodes(),7);

        }catch (GameException e){
        }
    }

    @Test
    void buildGameTreeWinFirstTwoEnemy() {
        try{
            GameState gameStateTest = new GameState();
            Knight generalKnight = new Knight(PlayerType.FIRST_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 0, generalKnight, PlayerType.FIRST_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 0, new Mage(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 0, new Healer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 1, new Archer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.FIRST_PLAYER));

            generalKnight = new Knight(PlayerType.SECOND_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 3, generalKnight, PlayerType.SECOND_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 3, new Mage(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 3, new Healer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 2, new Archer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.SECOND_PLAYER));


            gameStateTest.getCurrentBoard().getUnit(0,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,1).setCurrentHp(15);
            gameStateTest.getCurrentBoard().getUnit(0,1).setAccuracy(15);
            gameStateTest.getCurrentBoard().getUnit(1,1).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,1).setCurrentHp(0);

            gameStateTest.getCurrentBoard().getUnit(2,3).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,3).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(0,3).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(2,2).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,2).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,2).setCurrentHp(0);


            TreeBuilder treeBuilder = new TreeBuilder();
            TreeBuilder.Stats stats = treeBuilder.buildGameTree(gameStateTest,0,10);
            assertEquals(stats.getWinRateFirst(),2);

        }catch (GameException e){
        }
    }

    @Test
    void buildGameTreeDepthThreeEnemy() {
        try{
            GameState gameStateTest = new GameState();
            Knight generalKnight = new Knight(PlayerType.FIRST_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 0, generalKnight, PlayerType.FIRST_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 0, new Mage(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 0, new Healer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 1, new Archer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.FIRST_PLAYER));

            generalKnight = new Knight(PlayerType.SECOND_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 3, generalKnight, PlayerType.SECOND_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 3, new Mage(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 3, new Healer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 2, new Archer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.SECOND_PLAYER));


            gameStateTest.getCurrentBoard().getUnit(0,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,1).setCurrentHp(15);
            gameStateTest.getCurrentBoard().getUnit(0,1).setAccuracy(30);
            gameStateTest.getCurrentBoard().getUnit(0,1).setArmor(30);
            gameStateTest.getCurrentBoard().getUnit(1,1).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,1).setCurrentHp(0);

            gameStateTest.getCurrentBoard().getUnit(2,3).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,3).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(0,3).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,2).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(1,2).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(0,2).setCurrentHp(0);


            TreeBuilder treeBuilder = new TreeBuilder();
            TreeBuilder.Stats stats = treeBuilder.buildGameTree(gameStateTest,0,0);
            assertEquals(stats.getDepth(),6);
        }catch (GameException e){
        }
    }

    @Test
    void buildGameTreeNodeThreeEnemy() {
        try{
            GameState gameStateTest = new GameState();
            Knight generalKnight = new Knight(PlayerType.FIRST_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 0, generalKnight, PlayerType.FIRST_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 0, new Mage(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 0, new Healer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 1, new Archer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.FIRST_PLAYER));

            generalKnight = new Knight(PlayerType.SECOND_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 3, generalKnight, PlayerType.SECOND_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 3, new Mage(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 3, new Healer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 2, new Archer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.SECOND_PLAYER));


            gameStateTest.getCurrentBoard().getUnit(0,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,1).setCurrentHp(15);
            gameStateTest.getCurrentBoard().getUnit(0,1).setAccuracy(30);
            gameStateTest.getCurrentBoard().getUnit(0,1).setArmor(30);
            gameStateTest.getCurrentBoard().getUnit(1,1).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,1).setCurrentHp(0);

            gameStateTest.getCurrentBoard().getUnit(2,3).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,3).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(0,3).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,2).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(1,2).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(0,2).setCurrentHp(0);


            TreeBuilder treeBuilder = new TreeBuilder();
            TreeBuilder.Stats stats = treeBuilder.buildGameTree(gameStateTest,0,0);
            assertEquals(stats.getNumNodes(),52);
        }catch (GameException e){
        }
    }

    @Test
    void buildGameTreeTerminalNodeThreeEnemy() {
        try{
            GameState gameStateTest = new GameState();
            Knight generalKnight = new Knight(PlayerType.FIRST_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 0, generalKnight, PlayerType.FIRST_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 0, new Mage(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 0, new Healer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 1, new Archer(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 1, new Knight(PlayerType.FIRST_PLAYER), PlayerType.FIRST_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.FIRST_PLAYER));

            generalKnight = new Knight(PlayerType.SECOND_PLAYER);
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 3, generalKnight, PlayerType.SECOND_PLAYER, true, true));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 3, new Mage(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 3, new Healer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(2, 2, new Archer(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(1, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, true, false));
            gameStateTest.makePlacement(new PlaceUnitEvent(0, 2, new Knight(PlayerType.SECOND_PLAYER), PlayerType.SECOND_PLAYER, false, false));
            gameStateTest.makeChangePlayer(new ChangePlayerEvent(PlayerType.SECOND_PLAYER));


            gameStateTest.getCurrentBoard().getUnit(0,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,0).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(0,1).setCurrentHp(15);
            gameStateTest.getCurrentBoard().getUnit(0,1).setAccuracy(30);
            gameStateTest.getCurrentBoard().getUnit(0,1).setArmor(30);
            gameStateTest.getCurrentBoard().getUnit(1,1).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,1).setCurrentHp(0);

            gameStateTest.getCurrentBoard().getUnit(2,3).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(1,3).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(0,3).setCurrentHp(0);
            gameStateTest.getCurrentBoard().getUnit(2,2).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(1,2).setCurrentHp(1);
            gameStateTest.getCurrentBoard().getUnit(0,2).setCurrentHp(0);


            TreeBuilder treeBuilder = new TreeBuilder();
            TreeBuilder.Stats stats = treeBuilder.buildGameTree(gameStateTest,0,0);
            assertEquals(stats.getNumTerminalNodes(),12);
        }catch (GameException e){
        }
    }




}