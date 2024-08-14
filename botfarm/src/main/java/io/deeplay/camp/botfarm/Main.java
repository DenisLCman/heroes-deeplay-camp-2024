package io.deeplay.camp.botfarm;


import io.deeplay.camp.botfarm.bots.RandomBot;
import io.deeplay.camp.botfarm.bots.denis_bots.TreeBuilder;
import io.deeplay.camp.game.exceptions.GameException;
import io.deeplay.camp.game.mechanics.GameState;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

  static String path = "C:\\Deeplay\\deeplay-heroes\\botfarm\\src\\main\\java\\io\\deeplay\\camp\\botfarm";

  public static void main(String[] args) throws IOException {

    deleteFilesForPathByPrefix(path, "resultgame");
    TreeBuilderFun();

  }
  public static void TreeBuilderFun(){
    GameState gameState = new GameState();
    gameState.setDefaultPlacement();
    TreeBuilder treeBuilder = new TreeBuilder();
    TreeBuilder.Stats stats = treeBuilder.buildGameTree(gameState, 0, 6);
    long endTreeBuilder = System.currentTimeMillis();
    System.out.println("Количество узлов = " + stats.getNumNodes());
    System.out.println("Количество терминальных узлов = " + stats.getNumTerminalNodes());
    System.out.println("Время сбора стастистики = " + (endTreeBuilder - stats.getWorkTimeMS()));
    System.out.println("Максимальная глубина дерева = " + stats.getMaxDepth());
    System.out.println("Средний коэффициент ветвляемости = " + (stats.getNumNodes()/stats.getCoefBranch())/stats.getMaxDepth());
    System.out.println("Победы 1 игрока = " + stats.getWinRateFirst());
    System.out.println("Победы 2 игрока = " + stats.getWinRateSecond());
    System.out.println("Количесество ничьей = " + stats.getWinRateDraw());
  }
  public static void BotFightFun() throws IOException {
    RandomBot bot1 = new RandomBot();
    RandomBot bot2 = new RandomBot();
    for(int i = 0; i<1;i++){
      BotFight fight = new BotFight(bot1, bot2, 5, true);
    }
  }

  public static boolean deleteFilesForPathByPrefix(final String path, final String prefix) {
    boolean success = true;
    try (DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(Paths.get(path), prefix + "*")) {
      for (final Path newDirectoryStreamItem : newDirectoryStream) {
        Files.delete(newDirectoryStreamItem);
      }
    } catch (final Exception e) {
      success = false;
      e.printStackTrace();
    }
    return success;
  }
}
