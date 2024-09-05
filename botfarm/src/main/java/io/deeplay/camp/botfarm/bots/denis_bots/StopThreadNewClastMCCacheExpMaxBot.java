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
import lombok.Setter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class StopThreadNewClastMCCacheExpMaxBot extends Bot {
    private static final Logger logger = LoggerFactory.getLogger(RandomBot.class);
    BotTactic botTactic;
    UtilityFunction tacticUtility;
    UnitType currentGeneral;
    int maxDepth;
    GameStateCache gameStateCache;
    @Setter boolean firstPlaceInGame = true;
    List<PossibleStartState> possibleStartStates;
    Board bestBoard;
    double eps = 0.001;

    public StopThreadNewClastMCCacheExpMaxBot(PlayerType playerType, int maxDepth){
        tacticUtility = new TacticUtility(BotTactic.BASE_TACTIC);
        tacticUtility.setCurrentPlayerType(playerType);
        gameStateCache = new GameStateCache();
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
                botTactic = BotTactic.HEALER_TACTIC;
            } else if (generalOpponent == UnitType.ARCHER) {
                botTactic = BotTactic.ARCHER_TACTIC;
            } else if (generalOpponent == UnitType.MAGE){
                botTactic = BotTactic.KNIGHT_TACTIC;
            }
        }
        switch (botTactic){
            case MAGE_TACTIC -> currentGeneral = UnitType.MAGE;
            case HEALER_TACTIC -> currentGeneral = UnitType.HEALER;
            case KNIGHT_TACTIC, BASE_TACTIC -> currentGeneral = UnitType.KNIGHT;
            case ARCHER_TACTIC -> currentGeneral = UnitType.ARCHER;
        }
        tacticUtility.setBotTactic(botTactic);
    }


    @Override
    public PlaceUnitEvent generatePlaceUnitEvent(GameState gameState) {
        return getPlaceCacheResult(gameState);
    }

    @SneakyThrows
    public PlaceUnitEvent getPlaceCacheResult(GameState gameState){
        PlayerType forPlayerType = tacticUtility.getCurrentPlayerType();
        if(firstPlaceInGame) {

            bestBoard = null;
            gameStateCache = gameStateCache.loadCacheFromFile("hashStartGame.json");
            possibleStartStates = gameStateCache.getCache();
            Collections.sort(possibleStartStates);

            if(tacticUtility.getCurrentPlayerType() == PlayerType.SECOND_PLAYER) {
                for (PossibleStartState pos : possibleStartStates) {
                    if (pos.getForPlayerType() == tacticUtility.getCurrentPlayerType()) {
                        if (equalsBoard(pos.getEnemyUnits(), gameState.getCurrentBoard())) {
                            bestBoard = pos.allyUnits;
                            firstPlaceInGame = false;
                            break;
                        }
                    }
                }

                if(bestBoard == null){
                    findNewTactic(gameState);
                    List<PossibleStartState> possibleSecondPlayerStart = new ArrayList<>();
                    for (PossibleStartState pos : possibleStartStates) {
                        if (pos.getForPlayerType() == tacticUtility.getCurrentPlayerType() && pos.countWinRound < 5) {
                            UnitType generalThisPos = null;
                            for(int column = 0;column<Board.COLUMNS;column++){
                                for(int row = Board.ROWS/2;row< Board.ROWS;row++){
                                    if(pos.getAllyUnits().getUnit(column,row).isGeneral()){
                                        generalThisPos = pos.getAllyUnits().getUnit(column,row).getUnitType();
                                        break;
                                    }
                                }
                            }
                            if(generalThisPos == currentGeneral) {
                                possibleSecondPlayerStart.add(pos);
                            }
                        }
                        if(pos.countWinRound >= 5){
                            break;
                        }
                    }
                    bestBoard =  possibleSecondPlayerStart.get((int) (Math.random() * possibleSecondPlayerStart.size())).allyUnits;
                    firstPlaceInGame = false;
                }

            }
            else{
                List<PossibleStartState> possibleFirstPlayerStart = new ArrayList<>();
                for (PossibleStartState pos : possibleStartStates) {
                    if (pos.getForPlayerType() == tacticUtility.getCurrentPlayerType() && pos.countWinRound < 4) {
                        possibleFirstPlayerStart.add(pos);
                    }
                    if(pos.countWinRound >= 5){
                        break;
                    }
                }
                bestBoard =  possibleFirstPlayerStart.get((int) (Math.random() * possibleFirstPlayerStart.size())).allyUnits;
                firstPlaceInGame = false;
            }
        }

        int shiftRow = 0;
        if(forPlayerType == PlayerType.FIRST_PLAYER) {
            shiftRow = 0;
        }
        else{
            shiftRow = 2;
        }

        for(int column = 0;column < Board.COLUMNS;column++){
            for(int row = shiftRow;row < Board.ROWS/2 + shiftRow;row++){
                if(gameState.getCurrentBoard().isEmptyCell(column,row)){
                    if(enumerationPlayerUnits(tacticUtility.getCurrentPlayerType(), gameState.getCurrentBoard()).size() != 5){
                        return new PlaceUnitEvent(column, row, bestBoard.getUnit(column,row), tacticUtility.getCurrentPlayerType(), true, bestBoard.getUnit(column,row).isGeneral());
                    }
                    else{
                        return new PlaceUnitEvent(column, row, bestBoard.getUnit(column,row), tacticUtility.getCurrentPlayerType(), false, bestBoard.getUnit(column,row).isGeneral());
                    }
                }
            }
        }

        return null;

    }

    private boolean equalsBoard(Board enemyBoard, Board allyBoard){
        boolean result = true;
        int shiftRow;
        if(tacticUtility.getCurrentPlayerType() == PlayerType.FIRST_PLAYER) {
            shiftRow = 2;
        }
        else{
            shiftRow = 0;
        }
        for(int column = 0;column < Board.COLUMNS;column++) {
            for (int row = shiftRow; row < Board.ROWS / 2 + shiftRow; row++) {
                if((enemyBoard.getUnit(column,row).isGeneral() != allyBoard.getUnit(column,row).isGeneral()) ||
                        (enemyBoard.getUnit(column,row).getUnitType() != allyBoard.getUnit(column,row).getUnitType())){
                    result = false;
                }
            }
        }
        return result;
    }

    @Override
    public MakeMoveEvent generateMakeMoveEvent(GameState gameState) {
        firstPlaceInGame = true;

        return getMoveResult(gameState).event;
    }

    private UtilityMoveResult getMoveResult(GameState gameState) {
        int originDepth = maxDepth;
        List<MakeMoveEvent> movesRoot = gameState.getPossibleMoves();
        movesRoot = tacticUtility.changeMoveByTactic(gameState, movesRoot);
        long timeToThink = 3000;

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

            UtilityMoveResult bestResult = new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
            KMeans.Cluster bestCluster = null;
            for (KMeans.Cluster cluster : clusters) {
                UtilityMoveResult bestMoveInCluster = getBestMoveInCluster(points ,cluster, movesRoot, gameState, true);
                if(bestMoveInCluster.event != null) {
                    if(bestMoveInCluster.value > bestResult.value){
                        bestCluster = cluster;
                        bestResult = bestMoveInCluster;
                    }
                }
            }
            bestMoves = bestCluster.points;
            bestMoves.sort(Collections.reverseOrder());



            List<RecursiveTask<UtilityMoveResult>> recursiveTasks = new ArrayList<>();
            for (UtilityMoveResult moveBestClast : bestMoves) {
                List<UtilityMoveResult> finalBestMoves = bestMoves;
                RecursiveTask<UtilityMoveResult> task = new RecursiveTask<>() {
                    @SneakyThrows
                    @Override
                    protected UtilityMoveResult compute() {
                        double result = expectMaxAlg(gameState, moveBestClast.event, originDepth, true, 1, timeToThink/finalBestMoves.size());
                        return new UtilityMoveResult(result, moveBestClast.event);
                    }
                };
                recursiveTasks.add(task);

            }

            List<UtilityMoveResult> results = ForkJoinTask.invokeAll(recursiveTasks).stream()
                    .map(ForkJoinTask::join)
                    .toList();



            return getMaxMoveFromTasks(results);
        }
    }

    public double minMaxAlg(GameState root, int depth, boolean maxPlayer, double p, long timeToThink) {
        if(depth == 0 || root.getGameStage() == GameStage.ENDED || p < eps){
            return tacticUtility.getMoveUtility(root);
        }
        List<MakeMoveEvent> movesRoot = root.getPossibleMoves();
        movesRoot = maxPlayer ? tacticUtility.changeMoveByTactic(root, movesRoot): movesRoot;
        long startTimeToThink = System.currentTimeMillis();

        List<UtilityMoveResult> points = new ArrayList<>();
        for (MakeMoveEvent move : movesRoot) {
            UtilityMoveResult features = extractValue(root, move);
            points.add(features);
        }
        KMeans kMeans = new KMeans();
        int numClusters = Math.min(5, movesRoot.size());
        List<KMeans.Cluster> clusters = kMeans.kMeansCluster(points, numClusters, 100);

        List<UtilityMoveResult> bestMoves = new ArrayList<>();

        UtilityMoveResult bestResult = new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
        KMeans.Cluster bestCluster = null;
        for (KMeans.Cluster cluster : clusters) {
            UtilityMoveResult bestMoveInCluster = getBestMoveInCluster(points ,cluster, movesRoot, root, maxPlayer);
            if(bestMoveInCluster.event != null) {
                if(bestMoveInCluster.value > bestResult.value){
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
                return minMaxAlg(gameStateNode, depth, !maxPlayer, p, timeToThink);
            }
        } else {
            List<RecursiveTask<UtilityMoveResult>> recursiveTasks = new ArrayList<>();
            for (UtilityMoveResult moveEvent : bestMoves) {
                List<UtilityMoveResult> finalBestMoves = bestMoves;
                RecursiveTask<UtilityMoveResult> task = new RecursiveTask<>() {
                    @Override
                    protected UtilityMoveResult compute() {
                        try {
                            long endTimeToThink = System.currentTimeMillis();
                            if(endTimeToThink - startTimeToThink > timeToThink){
                                return new UtilityMoveResult(maxPlayer ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY, null);
                            }
                            else{
                                return new UtilityMoveResult(expectMaxAlg(root, moveEvent.event, depth, maxPlayer, p, timeToThink/ finalBestMoves.size()), moveEvent.event);
                            }
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

    private double expectMaxAlg(GameState root, MakeMoveEvent event, int depth, boolean maxPlayer, double p, long timeToThink) throws GameException {
        List<StateChance> chancesRoot = root.getPossibleState(event);
        double excepted = 0;
        for (StateChance chance : chancesRoot) {
            GameState nodeGameState = chance.gameState().getCopy();
            double v = minMaxAlg(nodeGameState, depth-1, maxPlayer, p*chance.chance(), timeToThink);
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
            if (bestValue.value < task.value) {
                bestValue = task;
            }
        }

        return bestValue;
    }

    private UtilityMoveResult getMinMoveFromTasks(List<UtilityMoveResult> results){
        UtilityMoveResult bestValue = new UtilityMoveResult(Double.POSITIVE_INFINITY, null);
        for (UtilityMoveResult task : results) {
            if (task.value < bestValue.value) {
                bestValue = task;
            }
        }
        return bestValue;
    }

    private UtilityMoveResult getMaxMoveWithTime(List<RecursiveTask<UtilityMoveResult>> results, long timeToThink, long startTimeToThink){
        long endTimeToThink = 0;
        UtilityMoveResult bestValue = new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
        for (RecursiveTask<UtilityMoveResult> task : results) {
            UtilityMoveResult result = task.join();
            if (bestValue.value < result.value) {
                bestValue = result;
            }
            endTimeToThink = System.currentTimeMillis();
            if ((endTimeToThink - startTimeToThink) > timeToThink) {
                for (RecursiveTask<UtilityMoveResult> taskForClose : results) {
                    taskForClose.cancel(true);
                }
                break;
            }
        }

        return bestValue;
    }

    private UtilityMoveResult getMinMoveWithTime(List<RecursiveTask<UtilityMoveResult>> results, long timeToThink, long startTimeToThink){
        long endTimeToThink = 0;
        UtilityMoveResult bestValue = new UtilityMoveResult(Double.POSITIVE_INFINITY, null);
        for (RecursiveTask<UtilityMoveResult> task : results) {
            UtilityMoveResult result = task.join();
            if (result.value < bestValue.value) {
                bestValue = result;
            }
            endTimeToThink = System.currentTimeMillis();
            if ((endTimeToThink - startTimeToThink) > timeToThink) {
                for (RecursiveTask<UtilityMoveResult> taskForClose : results) {
                    taskForClose.cancel(true);
                }
                break;
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
                    double result = expectMaxAlg(gameState, move, 1, maxPlayer,1, 5000);
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
