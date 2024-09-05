package io.deeplay.camp.botfarm;


import io.deeplay.camp.botfarm.bots.denis_bots.*;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.BotTactic;
import io.deeplay.camp.botfarm.bots.denis_bots.movement_algorithm.ModClastExpMaxAlg;
import io.deeplay.camp.botfarm.bots.denis_bots.movement_algorithm.OptNewClastExpMaxAlg;
import io.deeplay.camp.botfarm.bots.denis_bots.movement_algorithm.TimeLimitNewClastExpMaxAlg;
import io.deeplay.camp.botfarm.bots.denis_bots.placement_algorithm.MetricPlaceAlg;
import io.deeplay.camp.botfarm.bots.denis_bots.ready_bots.*;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

  static String path = "C:\\Deeplay\\heroes-deeplay-camp-2024\\botfarm\\src\\main\\java\\io\\deeplay\\camp\\botfarm";

  public static void main(String[] args) throws IOException {

    deleteFilesForPathByPrefix(path, "resultgame");
    //TreeBuilderFun();
    BotFightFun();
  }
  public static void TreeBuilderFun(){
    GameState gameState = new GameState();
    gameState.setDefaultPlacement();
    TreeBuilder treeBuilder = new TreeBuilder();
    TreeBuilder.Stats stats = treeBuilder.buildGameTree(gameState, 0, 5);
    long endTreeBuilder = System.currentTimeMillis();
    System.out.println("Количество узлов = " + stats.getNumNodes());
    System.out.println("Количество терминальных узлов = " + stats.getNumTerminalNodes());
    System.out.println("Время сбора стастистики = " + (endTreeBuilder - stats.getWorkTimeMS()));
    System.out.println("Максимальная глубина дерева = " + stats.getMaxDepth());
    System.out.println("Победы 1 игрока = " + stats.getWinRateFirst());
    System.out.println("Победы 2 игрока = " + stats.getWinRateSecond());
    System.out.println("Количесество ничьей = " + stats.getWinRateDraw());
  }

  public static void BotFightFun() throws IOException {
    TimeLimitNewClastMetricPlaceExpMaxBot bot1 = new TimeLimitNewClastMetricPlaceExpMaxBot(PlayerType.FIRST_PLAYER, 4);
    TimeLimitNewClastMCCacheExpMaxBot bot2 = new TimeLimitNewClastMCCacheExpMaxBot(PlayerType.SECOND_PLAYER,4);

    for(int i = 0; i<1;i++){
      BotFight fight = new BotFight(bot1, bot2, 10, true);
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
