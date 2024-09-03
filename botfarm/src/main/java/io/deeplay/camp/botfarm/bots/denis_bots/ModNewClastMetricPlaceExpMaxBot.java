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

public class ModNewClastMetricPlaceExpMaxBot extends Bot {
    private static final Logger logger = LoggerFactory.getLogger(RandomBot.class);
    BotTactic botTactic;
    UtilityFunction tacticUtility;
    UnitType currentGeneral;
    int maxDepth;
    @Setter boolean firstPlaceInGame = true;
    Board bestBoard;
    double eps = 0.001;

    public ModNewClastMetricPlaceExpMaxBot(PlayerType playerType, int maxDepth){
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

        return getPlaceResult(gameState);
    }

    public PlaceUnitEvent getPlaceResult(GameState gameState){
        if(firstPlaceInGame) {
            bestBoard = new Board();
            findNewTactic(gameState);
            tacticUtility.setBotTactic(botTactic);
            firstPlaceInGame = false;
        }

        switch (currentGeneral){
            case KNIGHT -> autoFillBoard(UnitType.KNIGHT);
            case MAGE -> autoFillBoard(UnitType.MAGE);
            case ARCHER -> autoFillBoard(UnitType.ARCHER);
            case HEALER -> autoFillBoard(UnitType.HEALER);
        }
        int shiftRow = 0;
        if(tacticUtility.getCurrentPlayerType() == PlayerType.FIRST_PLAYER) {
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

    private void autoFillBoard(UnitType unitType){
        int shiftRow = 0;
        PlayerType playerTypeThis = tacticUtility.getCurrentPlayerType();
        if(playerTypeThis == PlayerType.FIRST_PLAYER){
            if(unitType == UnitType.KNIGHT){
                bestBoard.setUnit(0,0, new Archer(playerTypeThis));
                bestBoard.setUnit(0,1, new Archer(playerTypeThis));
                bestBoard.setUnit(1,0, new Healer(playerTypeThis));
                bestBoard.setUnit(1,1, new Knight(playerTypeThis));
                bestBoard.setUnit(2,0, new Archer(playerTypeThis));
                bestBoard.setUnit(2,1, new Archer(playerTypeThis));
                bestBoard.getUnit(1,1).setGeneral(true);
            }
            if(unitType == UnitType.MAGE){
                bestBoard.setUnit(0,0, new Archer(playerTypeThis));
                bestBoard.setUnit(0,1, new Archer(playerTypeThis));
                bestBoard.setUnit(1,0, new Mage(playerTypeThis));
                bestBoard.setUnit(1,1, new Healer(playerTypeThis));
                bestBoard.setUnit(2,0, new Mage(playerTypeThis));
                bestBoard.setUnit(2,1, new Archer(playerTypeThis));
                bestBoard.getUnit(1,0).setGeneral(true);
            }
            if(unitType == UnitType.ARCHER){
                bestBoard.setUnit(0,0, new Archer(playerTypeThis));
                bestBoard.setUnit(0,1, new Mage(playerTypeThis));
                bestBoard.setUnit(1,0, new Mage(playerTypeThis));
                bestBoard.setUnit(1,1, new Healer(playerTypeThis));
                bestBoard.setUnit(2,0, new Archer(playerTypeThis));
                bestBoard.setUnit(2,1, new Mage(playerTypeThis));
                bestBoard.getUnit(2,0).setGeneral(true);
            }
            if(unitType == UnitType.HEALER){
                bestBoard.setUnit(0,0, new Archer(playerTypeThis));
                bestBoard.setUnit(0,1, new Archer(playerTypeThis));
                bestBoard.setUnit(1,0, new Healer(playerTypeThis));
                bestBoard.setUnit(1,1, new Knight(playerTypeThis));
                bestBoard.setUnit(2,0, new Archer(playerTypeThis));
                bestBoard.setUnit(2,1, new Archer(playerTypeThis));
                bestBoard.getUnit(1,0).setGeneral(true);
            }
        }
        else{
            if(unitType == UnitType.KNIGHT){
                bestBoard.setUnit(0,2, new Archer(playerTypeThis));
                bestBoard.setUnit(0,3, new Archer(playerTypeThis));
                bestBoard.setUnit(1,2, new Knight(playerTypeThis));
                bestBoard.setUnit(1,3, new Healer(playerTypeThis));
                bestBoard.setUnit(2,2, new Archer(playerTypeThis));
                bestBoard.setUnit(2,3, new Archer(playerTypeThis));
                bestBoard.getUnit(1,2).setGeneral(true);
            }
            if(unitType == UnitType.MAGE){
                bestBoard.setUnit(0,2, new Archer(playerTypeThis));
                bestBoard.setUnit(0,3, new Archer(playerTypeThis));
                bestBoard.setUnit(1,2, new Knight(playerTypeThis));
                bestBoard.setUnit(1,3, new Healer(playerTypeThis));
                bestBoard.setUnit(2,2, new Mage(playerTypeThis));
                bestBoard.setUnit(2,3, new Archer(playerTypeThis));
                bestBoard.getUnit(2,2).setGeneral(true);
            }
            if(unitType == UnitType.ARCHER){
                bestBoard.setUnit(0,2, new Healer(playerTypeThis));
                bestBoard.setUnit(0,3, new Archer(playerTypeThis));
                bestBoard.setUnit(1,2, new Knight(playerTypeThis));
                bestBoard.setUnit(1,3, new Archer(playerTypeThis));
                bestBoard.setUnit(2,2, new Healer(playerTypeThis));
                bestBoard.setUnit(2,3, new Archer(playerTypeThis));
                bestBoard.getUnit(2,3).setGeneral(true);
            }
            if(unitType == UnitType.HEALER){
                bestBoard.setUnit(0,2, new Knight(playerTypeThis));
                bestBoard.setUnit(0,3, new Healer(playerTypeThis));
                bestBoard.setUnit(1,2, new Knight(playerTypeThis));
                bestBoard.setUnit(1,3, new Archer(playerTypeThis));
                bestBoard.setUnit(2,2, new Healer(playerTypeThis));
                bestBoard.setUnit(2,3, new Archer(playerTypeThis));
                bestBoard.getUnit(0,3).setGeneral(true);
            }
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
        PlayerType playerType = tacticUtility.getCurrentPlayerType();
        if(enumerationMovedUnits(playerType, gameState) < enumerationTypeUnits(playerType, gameState, UnitType.HEALER)){
            movesRoot = cleanerByAttacker(UnitType.HEALER, movesRoot);
        } else if(enumerationMovedUnits(playerType, gameState) < enumerationTypeUnits(playerType, gameState, UnitType.KNIGHT)){
            movesRoot = cleanerByAttacker(UnitType.KNIGHT, movesRoot);
        } else if(enumerationMovedUnits(playerType, gameState) < enumerationTypeUnits(playerType, gameState, UnitType.ARCHER)){
            movesRoot = cleanerByAttacker(UnitType.ARCHER, movesRoot);
        }

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
                if(bestMoveInCluster.event != null) {
                    if(bestMoveInCluster.value > bestResult.value){
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
                        double result = expectMaxAlg(gameState, moveBestClast.event, originDepth, true, 1);
                        return new UtilityMoveResult(result, moveBestClast.event);
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

    public double minMaxAlg(GameState root, int depth, boolean maxPlayer, double p) {
        if(depth == 0 || root.getGameStage() == GameStage.ENDED || p < eps){
            return tacticUtility.getMoveUtility(root);
        }
        List<MakeMoveEvent> movesRoot = root.getPossibleMoves();
        PlayerType playerType = tacticUtility.getCurrentPlayerType();
        if(enumerationMovedUnits(playerType, root) < enumerationTypeUnits(playerType, root, UnitType.HEALER)){
            movesRoot = cleanerByAttacker(UnitType.HEALER, movesRoot);
        } else if(enumerationMovedUnits(playerType, root) < enumerationTypeUnits(playerType, root, UnitType.KNIGHT)){
            movesRoot = cleanerByAttacker(UnitType.KNIGHT, movesRoot);
        } else if(enumerationMovedUnits(playerType, root) < enumerationTypeUnits(playerType, root, UnitType.ARCHER)){
            movesRoot = cleanerByAttacker(UnitType.ARCHER, movesRoot);
        }

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
                return minMaxAlg(gameStateNode, depth, !maxPlayer, p);
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

    public List<Position> enumerationPlayerUnits(PlayerType playerType, Board board) {
        List<Position> unitPositions = new ArrayList<>();
        if (playerType == PlayerType.FIRST_PLAYER) {
            unitPositions.addAll(board.enumerateUnits(0, Board.ROWS / 2));
        } else {
            unitPositions.addAll(board.enumerateUnits(Board.ROWS / 2, Board.ROWS));
        }
        return unitPositions;
    }

    public Integer enumerationMovedUnits(PlayerType playerType, GameState gameState) {
        int countMovedUnits = 0;
        if (playerType == PlayerType.FIRST_PLAYER) {
            for(int column = 0; column<Board.COLUMNS;column++){
                for(int row = 0; row< Board.ROWS/2;row++){
                    if(gameState.getCurrentBoard().getUnit(column,row).isMoved() &&
                            gameState.getCurrentBoard().getUnit(column,row).isAlive()){
                        countMovedUnits++;
                    }
                }
            }
        } else {
            for(int column = 0; column<Board.COLUMNS;column++){
                for(int row = Board.ROWS/2; row< Board.ROWS;row++){
                    if(gameState.getCurrentBoard().getUnit(column,row).isMoved() &&
                            gameState.getCurrentBoard().getUnit(column,row).isAlive()){
                        countMovedUnits++;
                    }
                }
            }
        }
        return countMovedUnits;
    }

    public Integer enumerationTypeUnits(PlayerType playerType, GameState gameState, UnitType unitType) {
        int countTypeUnits = 0;
        if (playerType == PlayerType.FIRST_PLAYER) {
            for(int column = 0; column<Board.COLUMNS;column++){
                for(int row = 0; row< Board.ROWS/2;row++){
                    if(gameState.getCurrentBoard().getUnit(column,row).getUnitType() == unitType &&
                            gameState.getCurrentBoard().getUnit(column,row).isAlive()){
                        countTypeUnits++;
                    }
                }
            }
        } else {
            for(int column = 0; column<Board.COLUMNS;column++){
                for(int row = Board.ROWS/2; row< Board.ROWS;row++){
                    if(gameState.getCurrentBoard().getUnit(column,row).getUnitType() == unitType &&
                            gameState.getCurrentBoard().getUnit(column,row).isAlive()){
                        countTypeUnits++;
                    }
                }
            }
        }
        return countTypeUnits;
    }

    public List<MakeMoveEvent> cleanerByAttacker(UnitType unitType, List<MakeMoveEvent> movesRoot) {
        List<MakeMoveEvent> tmpList = new ArrayList<>();
        for(MakeMoveEvent moveEvent : movesRoot){
            if(moveEvent.getAttacker().getUnitType() == unitType){
                tmpList.add(moveEvent);
            }
        }
        return tmpList;
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
