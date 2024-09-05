package io.deeplay.camp.botfarm.bots.denis_bots.movement_algorithm;

import io.deeplay.camp.botfarm.bots.denis_bots.tools.KMeans;
import io.deeplay.camp.botfarm.bots.denis_bots.UtilityFunction;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.UtilityMoveResult;
import io.deeplay.camp.game.entities.StateChance;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.exceptions.GameException;
import io.deeplay.camp.game.mechanics.GameStage;
import io.deeplay.camp.game.mechanics.GameState;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Алгоритм игрового процесса, использующий:
 * ЭкспектиМакс,
 * Улучшенную кластеризацию,
 * Оптимизацию количества возможных действий игрока,
 * Многопоточность,
 * Отсечение по вероятностям.
 */
public class OptNewClastExpMaxAlg {
    final int originDepth;
    UtilityFunction tacticUtility;
    final double eps = 0.001;
    public OptNewClastExpMaxAlg(int maxDepth, UtilityFunction tacticUtility){
        this.originDepth = maxDepth;
        this.tacticUtility = tacticUtility;
    }


    public UtilityMoveResult getMoveResult(GameState gameState) {
        int originDepth = this.originDepth;
        List<MakeMoveEvent> movesRoot = gameState.getPossibleMoves();
        movesRoot = tacticUtility.changeMoveByTactic(gameState, movesRoot);

        if (movesRoot.isEmpty()) {
            return new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
        } else {
            List<UtilityMoveResult> points = new ArrayList<>();
            for (MakeMoveEvent move : movesRoot) {
                UtilityMoveResult features = extractValue(gameState, move);
                points.add(features);
            }

            KMeans kMeans = new KMeans();
            int numClusters = Math.min(5, movesRoot.size());  // количество кластеров можно варьировать
            List<KMeans.Cluster> clusters = kMeans.kMeansCluster(points, numClusters, 100);

            List<UtilityMoveResult> bestMoves;

            UtilityMoveResult bestResult = new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
            KMeans.Cluster bestCluster = null;
            for (KMeans.Cluster cluster : clusters) {
                UtilityMoveResult bestMoveInCluster = getBestMoveInCluster(points ,cluster, movesRoot, gameState, true);
                if(bestMoveInCluster.getEvent() != null) {
                    if(bestMoveInCluster.getValue() > bestResult.getValue()){
                        bestCluster = cluster;
                        bestResult = bestMoveInCluster;
                    }
                    //bestMoves.add(bestMoveInCluster);
                }
            }
            bestMoves = bestCluster.points;
            bestMoves.sort(Collections.reverseOrder());



            List<RecursiveTask<UtilityMoveResult>> recursiveTasks = new ArrayList<>();
            for (UtilityMoveResult moveBestClast : bestMoves) {
                RecursiveTask<UtilityMoveResult> task = new RecursiveTask<>() {
                    @SneakyThrows
                    @Override
                    protected UtilityMoveResult compute() {
                        double result = expectMaxAlg(gameState, moveBestClast.getEvent(), originDepth, true, 1);
                        return new UtilityMoveResult(result, moveBestClast.getEvent());
                    }
                };
                recursiveTasks.add(task);

            }

            List<UtilityMoveResult> results = ForkJoinTask.invokeAll(recursiveTasks).stream()
                    .map(ForkJoinTask::join)
                    .toList();



            System.out.println("Ход сделан!");
            return getMaxMoveFromTasks(results);
        }
    }

    private double minMaxAlg(GameState root, int depth, boolean maxPlayer, double p) {
        if(depth == 0 || root.getGameStage() == GameStage.ENDED || p < eps){
            return tacticUtility.getMoveUtility(root);
        }
        List<MakeMoveEvent> movesRoot = root.getPossibleMoves();
        movesRoot = maxPlayer ? tacticUtility.changeMoveByTactic(root, movesRoot): movesRoot;

        List<UtilityMoveResult> points = new ArrayList<>();
        for (MakeMoveEvent move : movesRoot) {
            UtilityMoveResult features = extractValue(root, move);
            points.add(features);
        }


        KMeans kMeans = new KMeans();
        int numClusters = Math.min(5, movesRoot.size());  // количество кластеров можно варьировать
        List<KMeans.Cluster> clusters = kMeans.kMeansCluster(points, numClusters, 100);

        List<UtilityMoveResult> bestMoves = new ArrayList<>();

        UtilityMoveResult bestResult = new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
        KMeans.Cluster bestCluster = null;
        for (KMeans.Cluster cluster : clusters) {
            UtilityMoveResult bestMoveInCluster = getBestMoveInCluster(points ,cluster, movesRoot, root, maxPlayer);
            if(bestMoveInCluster.getEvent() != null) {
                if(bestMoveInCluster.getValue() > bestResult.getValue()){
                    bestCluster = cluster;
                    bestResult = bestMoveInCluster;
                    bestMoves = bestCluster.points;
                }
            }
        }
        bestMoves.sort(Collections.reverseOrder());


        if(movesRoot.isEmpty()){
            if(root.getGameStage() == GameStage.ENDED){
                return tacticUtility.getMoveUtility(root);
            }
            else {
                GameState gameStateNode = root.getCopy();
                gameStateNode.changeCurrentPlayer();
                return minMaxAlg(gameStateNode, depth, !maxPlayer, p);
            }
        } else {
            List<RecursiveTask<UtilityMoveResult>> recursiveTasks = new ArrayList<>();
            for (UtilityMoveResult moveEvent : bestMoves) {
                RecursiveTask<UtilityMoveResult> task = new RecursiveTask<>() {
                    @Override
                    protected UtilityMoveResult compute() {
                        try {
                            return new UtilityMoveResult(expectMaxAlg(root, moveEvent.getEvent(), depth, maxPlayer, p), moveEvent.getEvent());
                        } catch (GameException e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                recursiveTasks.add(task);
            }
            List<UtilityMoveResult> results = ForkJoinTask.invokeAll(recursiveTasks).stream()
                    .map(ForkJoinTask::join)
                    .toList();

            return maxPlayer ? getMaxMoveFromTasks(results).getValue() : getMinMoveFromTasks(results).getValue();
        }
    }

    private double expectMaxAlg(GameState root, MakeMoveEvent event, int depth, boolean maxPlayer, double p) throws GameException {
        List<StateChance> chancesRoot = root.getPossibleState(event);
        double excepted = 0;
        for (StateChance chance : chancesRoot) {
            GameState nodeGameState = chance.gameState().getCopy();
            double v = minMaxAlg(nodeGameState, depth-1, maxPlayer, p*chance.chance());
            excepted += chance.chance() * v;
        }
        return excepted;
    }

    private UtilityMoveResult extractValue(GameState gameState, MakeMoveEvent move) {
        GameState gameStateNode = gameState.getCopy();
        try {
            gameStateNode.makeMove(move);
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
        return new UtilityMoveResult(tacticUtility.getMoveUtility(gameStateNode), move);
    }

    private UtilityMoveResult getMaxMoveFromTasks(List<UtilityMoveResult> results){
        UtilityMoveResult bestValue = new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
        for (UtilityMoveResult task : results) {
            if (bestValue.getValue() < task.getValue()) {
                bestValue = task;
            }
        }

        return bestValue;
    }

    private UtilityMoveResult getMinMoveFromTasks(List<UtilityMoveResult> results){
        UtilityMoveResult bestValue = new UtilityMoveResult(Double.POSITIVE_INFINITY, null);
        for (UtilityMoveResult task : results) {
            if (task.getValue() < bestValue.getValue()) {
                bestValue = task;
            }
        }
        return bestValue;
    }

    @SneakyThrows
    private UtilityMoveResult getBestMoveInCluster(List<UtilityMoveResult> points, KMeans.Cluster cluster, List<MakeMoveEvent> movesRoot, GameState gameState, boolean maxPlayer) {
        List<RecursiveTask<UtilityMoveResult>> tasks = new ArrayList<>();
        for (UtilityMoveResult point : cluster.points) {
            RecursiveTask<UtilityMoveResult> task = new RecursiveTask<>() {
                @SneakyThrows
                @Override
                protected UtilityMoveResult compute() {
                    MakeMoveEvent move = movesRoot.get(points.indexOf(point));
                    double result = expectMaxAlg(gameState, move, 1, maxPlayer,1);
                    return new UtilityMoveResult(result, move);
                }
            };
            tasks.add(task);
        }

        List<UtilityMoveResult> results = ForkJoinTask.invokeAll(tasks).stream()
                .map(ForkJoinTask::join)
                .toList();

        return maxPlayer ? getMaxMoveFromTasks(results) : getMinMoveFromTasks(results);
    }

}
