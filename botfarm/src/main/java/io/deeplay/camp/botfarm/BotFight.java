package io.deeplay.camp.botfarm;

import io.deeplay.camp.botfarm.bots.Bot;
import io.deeplay.camp.botfarm.bots.denis_bots.GameStateCache;
import io.deeplay.camp.game.Game;
import io.deeplay.camp.game.entities.Board;
import io.deeplay.camp.game.entities.Unit;
import io.deeplay.camp.game.entities.UnitType;
import io.deeplay.camp.game.events.ChangePlayerEvent;
import io.deeplay.camp.game.events.GiveUpEvent;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.exceptions.GameException;
import io.deeplay.camp.game.mechanics.GameStage;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;

import java.awt.Font;
import java.io.IOException;
import java.util.Timer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class BotFight extends Thread{

    private static int winsFirstPlayer = 0;
    private static int winsSecondPlayer = 0;
    private static int countDraw = 0;
    private final int countGame;

    private final int timeSkeep = 0;
    Game game;
    GameAnalisys gameAnalisys;
    GameStateCache gameStateCache;
    Bot botFirst;
    Bot botSecond;
    boolean consoleOut = true;
    boolean outInfoGame;
    final Timer timer = new Timer();

    String separator = System.lineSeparator();

    int fightId;

    JFrame frame;
    JTextArea field;
    JPanel contents;

    Thread threadFight;

    public BotFight(Bot botFirst, Bot botSecond, int countGame, boolean infoGame) throws IOException {
        this.botFirst = botFirst;
        this.botSecond = botSecond;
        this.countGame = countGame;
        gameStateCache = new GameStateCache();
        fightId = (int)(100000+Math.random()*999999);
        gameAnalisys = new GameAnalisys(countGame, fightId);
        this.outInfoGame = infoGame;
        threadFight = new Thread(this);

        frame = new JFrame();
        frame.setSize(800, 500);
        field = new JTextArea(20, 50);
        field.setFont(new Font("Dialog", Font.PLAIN, 14));
        field.setTabSize(10);
        contents = new JPanel();
        contents.add(field);
        frame.add(contents);
        frame.setVisible(true);

        threadFight.start();

    }

    @Override
    public void run() {
        try {
            playGames();
            frame.setVisible(false);
            threadFight.interrupt();
        } catch (GameException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void playGames() throws GameException, InterruptedException, IOException {
        try {
            gameStateCache = gameStateCache.loadCacheFromFile("hashStartGame.json");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        for (int gameCount = 0; gameCount < countGame; gameCount++) {

            game = new Game();


            executePlace(game.getGameState(), gameCount);
            game.getGameState().changeCurrentPlayer();
            executePlace(game.getGameState(), gameCount);
            GameState gameStartState = game.getGameState().getCopy();


            game.getGameState().changeCurrentPlayer();

/*
            game.getGameState().setDefaultPlacement();

 */
            gameAnalisys.setCurrentBoard(game.getGameState().getCurrentBoard().getUnits());

            while (game.getGameState().getGameStage() != GameStage.ENDED) {
                executeMove(game.getGameState(), gameCount);
                game.changePlayer(new ChangePlayerEvent(game.getGameState().getCurrentPlayer()));
                executeMove(game.getGameState(), gameCount);
                game.changePlayer(new ChangePlayerEvent(game.getGameState().getCurrentPlayer()));
            }

            gameAnalisys.reviewGame(game.getGameState(), gameCount);

            if(game.getGameState().getWinner() != PlayerType.DRAW) {
                gameStateCache.addCache(gameStartState, 10 - game.getGameState().getCountRound(), game.getGameState().getWinner());
            }

            game = null;
            System.out.println("Завершение игры номер - " + gameCount);
        }

        //gameStateCache.saveCacheToFile("hashStartGame.json");
        if (outInfoGame) {
            gameAnalisys.outputInfo();
        }
    }

    public void executeMove(GameState gameState, int countGame)
            throws GameException, InterruptedException {
        for (int i = 0; i < 6; i++) {
            if (gameState.getCurrentPlayer() == PlayerType.FIRST_PLAYER) {
                long startTimer = System.currentTimeMillis();
                MakeMoveEvent event = botFirst.generateMakeMoveEvent(gameState);
                if (event == null) {
                    continue;
                }
                long endTimer = System.currentTimeMillis();
                if(endTimer - startTimer > 500000){
                    GiveUpEvent giveUpEvent = new GiveUpEvent(PlayerType.FIRST_PLAYER);
                    gameState.giveUp(giveUpEvent);
                }

                game.makeMove(event);
                Thread.sleep(timeSkeep);
                outInFrame(event, countGame);
            } else {
                long startTimer = System.currentTimeMillis();
                MakeMoveEvent event = botSecond.generateMakeMoveEvent(gameState);
                if (event == null) {
                    continue;
                }
                long endTimer = System.currentTimeMillis();
                gameAnalisys.reviewTimeMove(endTimer - startTimer, countGame);
                if(endTimer - startTimer > 500000){
                    GiveUpEvent giveUpEvent = new GiveUpEvent(PlayerType.SECOND_PLAYER);
                    gameState.giveUp(giveUpEvent);
                }

                game.makeMove(event);
                Thread.sleep(timeSkeep);
                outInFrame(event, countGame);
            }
        }
    }

    public void executePlace(GameState gameState, int countGame)
            throws GameException, InterruptedException {
        for (int i = 0; i < 6; i++) {
            if (gameState.getCurrentPlayer() == PlayerType.FIRST_PLAYER) {
                PlaceUnitEvent event = botFirst.generatePlaceUnitEvent(gameState);
                game.placeUnit(event);
                if(event == null){
                    continue;
                }
                Thread.sleep(timeSkeep);
                outInFrame(null, countGame);
            } else {
                PlaceUnitEvent event =botSecond.generatePlaceUnitEvent(gameState);
                if(event == null){
                    continue;
                }
                game.placeUnit(event);

                Thread.sleep(timeSkeep);
                outInFrame(null, countGame);
            }
        }
    }

    // Вывод в окно JFrame
    public void outInFrame(MakeMoveEvent move, int countGame) {
        Board board = game.getGameState().getCurrentBoard();
        field.setText(null);
        field.append("Number game # "+ countGame);
        field.append(separator);
        field.append(separator);
        field.append("SECOND_PLAYER");
        field.append(separator);
        field.append(separator);
        String s = "20";
        for (int row = 3; row >= 0; row--) {
            field.append(String.format("%-" + s + "d", row));
            for (int column = 0; column < 3; column++) {
                field.append(
                        String.format(
                                "%-" + s + "s",
                                outUnitIsMoved(board.getUnit(column, row))
                                        + outUnitIsGeneral(board.getUnit(column, row))
                                        + outUnitInfo(board.getUnit(column, row))));
            }
            field.append(separator);
            field.append(separator);
        }
        field.append(String.format("%-25s", "#"));
        field.append(String.format("%-25s", "0"));
        field.append(String.format("%-27s", "1"));
        field.append(String.format("%-26s", "2"));
        field.append(separator);
        field.append(separator);
        field.append("FIRST_PLAYER");
        field.append(separator);
        field.append(separator);
        field.append(separator);

        if (move != null) {
            field.append(
                    outUnitMove(
                            move.getAttacker().getUnitType(),
                            move.getFrom().x(),
                            move.getFrom().y(),
                            move.getTo().x(),
                            move.getTo().y()));
        }
    }

    // Методы для отображения стринговой информации о юните
    private String outUnitIsMoved(Unit unit) {
        String result = "?";
        if (unit == null) {
            return "";
        }
        if (unit.isMoved()) {
            result = "!";
        }
        return result;
    }

    private String outUnitInfo(Unit unit) {
        String result = "?";
        if (unit == null) {
            return result = "------";
        }
        switch (unit.getUnitType()) {
            case KNIGHT -> result = "Knight" + unit.getCurrentHp();
            case ARCHER -> result = "Archer" + unit.getCurrentHp();
            case MAGE -> result = "Wizard" + unit.getCurrentHp();
            case HEALER -> result = "Healer" + unit.getCurrentHp();
            default -> result = "------";
        }
        return result;
    }

    private String outUnitIsGeneral(Unit unit) {
        String result = "";
        if (unit == null) {
            return "";
        }
        if (unit.isGeneral()) {
            result = "G";
        }
        return result;
    }

    private String outUnitMove(UnitType unitType, int fromX, int fromY, int toX, int toY) {
        if (unitType == UnitType.MAGE) {
            return "Unit Mage" + "(" + fromX + "," + fromY + ") attack all enemys units";
        } else {
            String action;
            if (unitType != UnitType.HEALER) {
                action = " attack ";
            } else {
                action = " heal ";
            }
            return "Unit "
                    + unitType.name()
                    + "("
                    + fromX
                    + ","
                    + fromY
                    + ")"
                    + action
                    + game.getGameState().getCurrentBoard().getUnit(toX, toY).getUnitType().name()
                    + "("
                    + toX
                    + ","
                    + toY
                    + ")";
        }
    }
}
