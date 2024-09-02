package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.botfarm.bots.Bot;
import io.deeplay.camp.botfarm.bots.RandomBot;
import io.deeplay.camp.game.entities.*;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.events.PlaceUnitEvent;
import io.deeplay.camp.game.exceptions.GameException;
import io.deeplay.camp.game.mechanics.GameStage;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class ModClastExpMaxBot extends Bot {
    private static final Logger logger = LoggerFactory.getLogger(RandomBot.class);
    BotTactic botTactic;
    UtilityFunction tacticUtility;
    UnitType currentGeneral;
    int maxDepth;
    @Setter boolean firstPlaceInGame = true;
    double eps = 0.001;

    public ModClastExpMaxBot(PlayerType playerType, int maxDepth){
        tacticUtility = new TacticUtility(BotTactic.BASE_TACTIC);
        tacticUtility.setCurrentPlayerType(playerType);
        this.maxDepth = maxDepth;
    }


    @Override
    public PlaceUnitEvent generatePlaceUnitEvent(GameState gameState) {
        List<PlaceUnitEvent> placeUnitEvents = gameState.getPossiblePlaces();
        if(!placeUnitEvents.isEmpty()){
            return placeUnitEvents.get((int)(Math.random()*placeUnitEvents.size()));
        }
        else{
            return null;
        }
    }

    @Override
    public MakeMoveEvent generateMakeMoveEvent(GameState gameState) {
        firstPlaceInGame = true;

        return getMoveResult(gameState).event;
    }

    private UtilityMoveResult getMoveResult(GameState gameState) {
        int originDepth = maxDepth;
        List<MakeMoveEvent> movesRoot = gameState.getPossibleMoves();

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


            List<UtilityMoveResult> bestMoves = new ArrayList<>();

            for (KMeans.Cluster cluster : clusters) {
                UtilityMoveResult bestMoveInCluster = getBestMoveInCluster(points ,cluster, movesRoot, gameState, true);
                if(bestMoveInCluster.event != null) {
                    bestMoves.add(bestMoveInCluster);
                }
            }

            List<RecursiveTask<UtilityMoveResult>> recursiveTasks = new ArrayList<>();
            for (UtilityMoveResult moveBestClast : bestMoves) {
                RecursiveTask<UtilityMoveResult> task = new RecursiveTask<>() {
                    @SneakyThrows
                    @Override
                    protected UtilityMoveResult compute() {
                        double result = expectMaxAlg(gameState, moveBestClast.event, originDepth, true, 1);
                        return new UtilityMoveResult(result, moveBestClast.event);
                    }
                };
                recursiveTasks.add(task);

            }
            List<UtilityMoveResult> results = ForkJoinTask.invokeAll(recursiveTasks).stream()
                    .map(ForkJoinTask::join)
                    .toList();

            return getMaxFromTasks(results);
        }
    }

    public double alphaBetaMinMaxAlg(GameState root, int depth, boolean maxPlayer, double p) {
        if(depth == 0 || root.getGameStage() == GameStage.ENDED || p < eps){
            return tacticUtility.getMoveUtility(root);
        }
        List<MakeMoveEvent> movesRoot = root.getPossibleMoves();

        List<UtilityMoveResult> points = new ArrayList<>();
        for (MakeMoveEvent move : movesRoot) {
            UtilityMoveResult features = extractValue(root, move);
            points.add(features);
        }
        KMeans kMeans = new KMeans();
        int numClusters = Math.min(5, movesRoot.size());  // количество кластеров можно варьировать
        List<KMeans.Cluster> clusters = kMeans.kMeansCluster(points, numClusters, 100);

        List<UtilityMoveResult> bestMoves = new ArrayList<>();

        for (KMeans.Cluster cluster : clusters) {
            UtilityMoveResult bestMoveInCluster = getBestMoveInCluster(points ,cluster, movesRoot, root, maxPlayer);
            if(bestMoveInCluster.event != null) {
                bestMoves.add(bestMoveInCluster);
            }
        }

        if(movesRoot.isEmpty()){
            if(root.getGameStage() == GameStage.ENDED){
                return tacticUtility.getMoveUtility(root);
            }
            else {
                GameState gameStateNode = root.getCopy();
                gameStateNode.changeCurrentPlayer();
                return alphaBetaMinMaxAlg(gameStateNode, depth, !maxPlayer, p);
            }
        } else {
            List<RecursiveTask<UtilityMoveResult>> recursiveTasks = new ArrayList<>();
            for (UtilityMoveResult moveEvent : bestMoves) {
                RecursiveTask<UtilityMoveResult> task = new RecursiveTask<>() {
                    @Override
                    protected UtilityMoveResult compute() {
                        try {
                            return new UtilityMoveResult(expectMaxAlg(root, moveEvent.event, depth, maxPlayer, p), moveEvent.event);
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

            return maxPlayer ? getMaxFromTasks(results).getValue() : getMinFromTasks(results).getValue();
        }
    }

    private double expectMaxAlg(GameState root, MakeMoveEvent event, int depth, boolean maxPlayer, double p) throws GameException {
        List<StateChance> chancesRoot = root.getPossibleState(event);
        double excepted = 0;
        for (StateChance chance : chancesRoot) {
            GameState nodeGameState = chance.gameState().getCopy();
            double v = alphaBetaMinMaxAlg(nodeGameState, depth-1, maxPlayer, p*chance.chance());
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

    private UtilityMoveResult getMaxFromTasks(List<UtilityMoveResult> results){
        UtilityMoveResult bestValue = new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
        for (UtilityMoveResult task : results) {
            if (bestValue.value < task.value) {
                bestValue = task;
            }
        }

        return bestValue;
    }

    private UtilityMoveResult getMinFromTasks(List<UtilityMoveResult> results){
        UtilityMoveResult bestValue = new UtilityMoveResult(Double.POSITIVE_INFINITY, null);
        for (UtilityMoveResult task : results) {
            if (task.value < bestValue.value) {
                bestValue = task;
            }
        }
        return bestValue;
    }

    public List<Position> enumerationPlayerUnits(PlayerType playerType, Board board) {
        List<Position> unitPositions = new ArrayList<>();
        if (playerType == PlayerType.FIRST_PLAYER) {
            unitPositions.addAll(board.enumerateUnits(0, Board.ROWS / 2));
        } else {
            unitPositions.addAll(board.enumerateUnits(Board.ROWS / 2, Board.ROWS));
        }
        return unitPositions;
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

        return maxPlayer ? getMaxFromTasks(results) : getMinFromTasks(results);
    }
}
