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
import io.deeplay.camp.game.mechanics.PossibleActions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;

public class ModClastPlaceExpMaxBot extends Bot {
    private static final Logger logger = LoggerFactory.getLogger(RandomBot.class);
    BotTactic botTactic;
    UtilityFunction tacticUtility;
    UnitType currentGeneral;
    int maxDepth;
    @Setter boolean firstPlaceInGame = true;
    double eps = 0.05;
    static final Double PRECISION = 0.0;

    public ModClastPlaceExpMaxBot(PlayerType playerType, int maxDepth){
        tacticUtility = new TacticUtility(BotTactic.KNIGHT_TACTIC);
        tacticUtility.setCurrentPlayerType(playerType);
        this.maxDepth = maxDepth;
    }

    public void findNewTactic(GameState gameState){
        if(gameState == null){
            botTactic = BotTactic.KNIGHT_TACTIC;
            currentGeneral = UnitType.KNIGHT;
            return;
        }
        if(gameState.getCurrentPlayer() == PlayerType.FIRST_PLAYER){
            int rand = (int)(Math.random()*5);
            switch (rand){
                case 0 -> {
                    botTactic = BotTactic.KNIGHT_TACTIC;
                }
                case 1 -> {
                    botTactic = BotTactic.HEALER_TACTIC;
                }
                case 2,3 -> {
                    botTactic = BotTactic.MAGE_TACTIC;
                }
                case 4 -> {
                    botTactic = BotTactic.ARCHER_TACTIC;
                }
                default -> botTactic = BotTactic.MAGE_TACTIC;
            }
        }
        else if (gameState.getCurrentPlayer() == PlayerType.SECOND_PLAYER){
            UnitType generalOpponent = null;
            for(int column = 0; column < Board.COLUMNS;column++){
                for(int row = 0; row < Board.ROWS/2;row++){
                    if(gameState.getCurrentBoard().getUnit(column, row).isGeneral()){
                        generalOpponent = gameState.getCurrentBoard().getUnit(column, row).getUnitType();
                        break;
                    }
                }
            }
            if(generalOpponent == UnitType.KNIGHT){
                botTactic = BotTactic.ARCHER_TACTIC;
            } else if(generalOpponent == UnitType.HEALER){
                botTactic = BotTactic.KNIGHT_TACTIC;
            } else if (generalOpponent == UnitType.ARCHER) {
                botTactic = BotTactic.HEALER_TACTIC;
            } else if (generalOpponent == UnitType.MAGE){
                int rand = (int) (Math.random() * 2);
                switch (rand){
                    case 0 -> botTactic = BotTactic.MAGE_TACTIC;
                    case 1 -> botTactic = BotTactic.KNIGHT_TACTIC;
                }
            }
        }
        switch (botTactic){
            case MAGE_TACTIC -> currentGeneral = UnitType.MAGE;
            case HEALER_TACTIC -> currentGeneral = UnitType.HEALER;
            case KNIGHT_TACTIC -> currentGeneral = UnitType.KNIGHT;
            case ARCHER_TACTIC -> currentGeneral = UnitType.ARCHER;
            case BASE_TACTIC -> currentGeneral = UnitType.KNIGHT;
        }
        tacticUtility.setBotTactic(botTactic);
    }

    @Getter
    @AllArgsConstructor
    static class UtilityPlaceResult {
        double value;
        PlaceUnitEvent place;

    }

    @Override
    public PlaceUnitEvent generatePlaceUnitEvent(GameState gameState) {
        if(firstPlaceInGame) {
            findNewTactic(gameState);
            tacticUtility.setBotTactic(botTactic);
            firstPlaceInGame = false;
        }
        int originDepth;
        if(gameState.getCurrentPlayer() == PlayerType.FIRST_PLAYER){
            originDepth = enumerationPlayerUnits(PlayerType.FIRST_PLAYER,gameState.getCurrentBoard()).size();
        }
        else{
            originDepth = enumerationPlayerUnits(PlayerType.SECOND_PLAYER,gameState.getCurrentBoard()).size();
        }

        List<PlaceUnitEvent> possiblePlaces =  gameState.getPossiblePlaces();
        if (possiblePlaces.isEmpty()) {
            return new UtilityPlaceResult(Double.NEGATIVE_INFINITY, null).place;
        } else{
            UtilityPlaceResult bestValue = new UtilityPlaceResult(Double.NEGATIVE_INFINITY,null);
            List<RecursiveTask<UtilityPlaceResult>> tasks = new ArrayList<>();
            for (PlaceUnitEvent placeRoot : possiblePlaces) {
                RecursiveTask<UtilityPlaceResult> task = new RecursiveTask<UtilityPlaceResult>() {
                    @SneakyThrows
                    @Override
                    protected UtilityPlaceResult compute() {
                        GameState gameStateNode = gameState.getCopy();
                        gameStateNode.makePlacement(placeRoot);
                        double result = maximumPlaceAlg(gameStateNode, originDepth);
                        return new UtilityPlaceResult(result, placeRoot);
                    }
                };
                tasks.add(task);
            }
            List<UtilityPlaceResult> results = ForkJoinTask.invokeAll(tasks).stream()
                    .map(ForkJoinTask::join)
                    .toList();

            for (UtilityPlaceResult task : results) {
                try {
                    //System.out.println("Значение цены у данного расположения: " + task.value);
                    if (bestValue.value < task.value) {
                        bestValue = task;
                    }
                } catch (CancellationException e) {
                }
            }

            //System.out.println("Наивысшая цена расположения: " + bestValue.value);
            return bestValue.place;
        }

    }

    public double maximumPlaceAlg(GameState root, int depth) throws GameException {
        if(depth == 0 || root.getGameStage() == GameStage.ENDED){
            if(tacticUtility.getCurrentPlayerType() == PlayerType.FIRST_PLAYER){
                if(root.getArmyFirst().getGeneralType() == currentGeneral){
                    return 5*tacticUtility.getPlaceUtility(root);
                }
                else{
                    return tacticUtility.getPlaceUtility(root);
                }
            }
            else{
                if(root.getArmySecond().getGeneralType() == currentGeneral){
                    return 5*tacticUtility.getPlaceUtility(root);
                }
                else{
                    return tacticUtility.getPlaceUtility(root);
                }
            }
        }
        List<PlaceUnitEvent> placeRoot = root.getPossiblePlaces();

        double bestValue = Double.NEGATIVE_INFINITY;

        if(placeRoot.isEmpty()){
            if(tacticUtility.getCurrentPlayerType() == PlayerType.FIRST_PLAYER){
                if(root.getArmyFirst().getGeneralType() == currentGeneral){
                    return 5*tacticUtility.getPlaceUtility(root);
                }
                else{
                    return tacticUtility.getPlaceUtility(root);
                }
            }
            else{
                if(root.getArmySecond().getGeneralType() == currentGeneral){
                    return 5*tacticUtility.getPlaceUtility(root);
                }
                else{
                    return tacticUtility.getPlaceUtility(root);
                }
            }
        } else {
            for (PlaceUnitEvent placeEvent : placeRoot) {
                GameState gameStateNode = root.getCopy();
                gameStateNode.makePlacement(placeEvent);
                double v = maximumPlaceAlg(gameStateNode, depth - 1);
                bestValue = Math.max(bestValue,v);
            }
            return bestValue;
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
                UtilityMoveResult features = extractFeatures(gameState, move);
                points.add(features);
            }
            KMeans kMeans = new KMeans();
            int numClusters = Math.min(4, movesRoot.size());  // количество кластеров можно варьировать
            List<KMeans.Cluster> clusters = kMeans.kMeansCluster(points, numClusters, 10);


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
                        double result = expectMaxAlg(gameState, moveBestClast.event, originDepth,
                                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true, 1);
                        return new UtilityMoveResult(result, moveBestClast.event);
                    }
                };
                recursiveTasks.add(task);

            }
            List<UtilityMoveResult> results = ForkJoinTask.invokeAll(recursiveTasks).stream()
                    .map(ForkJoinTask::join)
                    .toList();

            System.out.println("Ход сделан!");
            return getMaxFromTasks(results);
        }
    }





    public double alphaBetaMinMaxAlg(GameState root, int depth, double alpha, double beta, boolean maxPlayer, double p) throws GameException {
        if(depth == 0 || root.getGameStage() == GameStage.ENDED || p < eps){
            return tacticUtility.getMoveUtility(root);
        }
        List<MakeMoveEvent> movesRoot = root.getPossibleMoves();
        double bestValue = maxPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;

        List<UtilityMoveResult> points = new ArrayList<>();
        for (MakeMoveEvent move : movesRoot) {
            UtilityMoveResult features = extractFeatures(root, move);
            points.add(features);
        }
        KMeans kMeans = new KMeans();
        int numClusters = Math.min(3, movesRoot.size());  // количество кластеров можно варьировать
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
                return alphaBetaMinMaxAlg(gameStateNode, depth, alpha,beta,!maxPlayer, p);
            }
        } else {
            for (UtilityMoveResult moveEvent : bestMoves) {
                double v = expectMaxAlg(root, moveEvent.event, depth, alpha,beta,maxPlayer, p);
                bestValue = maxPlayer ? Math.max(bestValue,v) : Math.min(bestValue,v);
            }
            return bestValue;
        }
    }

    private double expectMaxAlg(GameState root, MakeMoveEvent event, int depth, double alpha, double beta, boolean maxPlayer, double p) throws GameException {
        List<StateChanceResult> chancesRoot = root.getPossibleIssue(event);
        double excepted = 0;
        for (StateChanceResult chance : chancesRoot) {
            GameState nodeGameState = chance.gameState().getCopy();
            double v = alphaBetaMinMaxAlg(nodeGameState, depth-1, alpha,beta,maxPlayer, p*chance.chance());
            excepted += chance.chance() * v;
        }
        return excepted;
    }

    private UtilityMoveResult extractFeatures(GameState gameState, MakeMoveEvent move) {
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
            System.out.println("Значение цены у данного хода: " + task.value);
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
                    double result = expectMaxAlg(gameState, move, 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, maxPlayer,1);
                    return new UtilityMoveResult(result, move);
                }
            };
            tasks.add(task);
        }

        List<UtilityMoveResult> results = ForkJoinTask.invokeAll(tasks).stream()
                .map(ForkJoinTask::join)
                .toList();

        return getMaxFromTasks(results);
    }
}
