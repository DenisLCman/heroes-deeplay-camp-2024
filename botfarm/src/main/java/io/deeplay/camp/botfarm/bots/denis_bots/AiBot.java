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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class AiBot extends Bot {
    private static final Logger logger = LoggerFactory.getLogger(RandomBot.class);
    BotTactic botTactic;
    UtilityFunction tacticUtility;
    UnitType currentGeneral;
    GameState gameState;
    int maxDepth;
    List<UnitQuality> placePack;
    @Setter boolean firstPlaceInGame = true;

    public AiBot(PlayerType playerType, int maxDepth){
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
            case null, default -> currentGeneral = UnitType.HEALER;
        }
        tacticUtility.setBotTactic(botTactic);
    }



    @Getter
    @AllArgsConstructor
    static class UtilityMoveResult {
        double value;
        MakeMoveEvent event;

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
                        double result = maximum_place(gameStateNode, originDepth);
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
                    System.out.println("Значение цены у данного расположения: " + task.value);
                    if (bestValue.value < task.value) {
                        bestValue = task;
                    }
                } catch (CancellationException e) {
                }
            }

            System.out.println("Наивысшая цена расположения: " + bestValue.value);
            return bestValue.place;
        }

    }

    public double maximum_place(GameState root, int depth) throws GameException {
        if(depth == 0 || root.getGameStage() == GameStage.ENDED){
            if(tacticUtility.getCurrentPlayerType() == PlayerType.FIRST_PLAYER){
                if(root.getArmyFirst().getGeneralType() == currentGeneral){
                    return 10000*tacticUtility.getPlaceUtility(root);
                }
                else{
                    return tacticUtility.getPlaceUtility(root);
                }
            }
            else{
                if(root.getArmySecond().getGeneralType() == currentGeneral){
                    return 10000*tacticUtility.getPlaceUtility(root);
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
                    return 10000*tacticUtility.getPlaceUtility(root);
                }
                else{
                    return tacticUtility.getPlaceUtility(root);
                }
            }
            else{
                if(root.getArmySecond().getGeneralType() == currentGeneral){
                    return 10000*tacticUtility.getPlaceUtility(root);
                }
                else{
                    return tacticUtility.getPlaceUtility(root);
                }
            }
        } else {
            for (PlaceUnitEvent placeEvent : placeRoot) {
                GameState gameStateNode = root.getCopy();
                gameStateNode.makePlacement(placeEvent);
                double v = maximum_place(gameStateNode, depth - 1);
                bestValue = Math.max(bestValue,v);
            }
            return bestValue;
        }

    }



/*
    @Override
    public PlaceUnitEvent generatePlaceUnitEvent(GameState gameState) {
        int shiftRow = 0;
        if (gameState.getCurrentPlayer() == PlayerType.FIRST_PLAYER) {
            shiftRow = 0;
        } else if (gameState.getCurrentPlayer() == PlayerType.SECOND_PLAYER) {
            shiftRow = 2;
        }
        for (int i = 0; i < gameState.getCurrentBoard().getUnits().length; i++) {
            for (int j = shiftRow;
                 j < gameState.getCurrentBoard().getUnits()[i].length / 2 + shiftRow;
                 j++) {
                if (!gameState.getCurrentBoard().isEmptyCell(i, j)) {
                    continue;
                }
                PossibleActions<Position, Unit> positionPossiblePlacement =
                        unitsPossiblePlacement(gameState);
                Position pos1 = new Position(i, j);
                if (positionPossiblePlacement.get(pos1).isEmpty()) {
                    continue;
                }
                boolean inProcess = true;
                boolean general = false;
                int randUnit = (int) (Math.random() * positionPossiblePlacement.get(pos1).size());
                if (enumerationPlayerUnits(gameState.getCurrentPlayer(), gameState.getCurrentBoard()).size()
                        + 1
                        == 6) {
                    int randGeneral = (int) (Math.random() * 6);
                    if (randGeneral != 5) {
                        gameState
                                .getCurrentBoard()
                                .getUnit(intToPos(randGeneral).x(), intToPos(randGeneral).y() + shiftRow)
                                .setGeneral(true);
                    } else {
                        general = true;
                    }
                    inProcess = false;
                }
                return new PlaceUnitEvent(
                        pos1.x(),
                        pos1.y(),
                        positionPossiblePlacement.get(pos1).get(randUnit),
                        gameState.getCurrentPlayer(),
                        inProcess,
                        general);
            }
        }
        return null;
    }
*/
    @Override
    public MakeMoveEvent generateMakeMoveEvent(GameState gameState) {
        firstPlaceInGame = true;
        return getMaxMoveResult(gameState).event;
    }

    private UtilityMoveResult getMaxMoveResult(GameState gameState) {
        try {
            int originDepth = maxDepth;
            List<MakeMoveEvent> movesRoot = gameState.getPossibleMoves();

            if (movesRoot.isEmpty()) {
                return new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
            } else {
                UtilityMoveResult bestValue = new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
                List<RecursiveTask<UtilityMoveResult>> tasks = new ArrayList<>();
                for (MakeMoveEvent moveEvent : movesRoot) {
                    RecursiveTask<UtilityMoveResult> task = new RecursiveTask<UtilityMoveResult>() {
                        @SneakyThrows
                        @Override
                        protected UtilityMoveResult compute() {
                            double result = exp_max(gameState, moveEvent,originDepth,
                                    Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, true);
                            return new UtilityMoveResult(result, moveEvent);
                        }
                    };
                    tasks.add(task);

                }
                List<UtilityMoveResult> results = ForkJoinTask.invokeAll(tasks).stream()
                        .map(ForkJoinTask::join)
                        .toList();

                for (UtilityMoveResult task : results) {
                    try {
                        System.out.println("Значение цены у данного хода: " + task.value);
                        if (bestValue.value < task.value) {
                            bestValue = task;
                        }
                    } catch (CancellationException e) {
                    }
                }

                System.out.println("Наивысшая цена действия: " + bestValue.value);
                return bestValue;
            }
        } catch (Exception e) {
            e.printStackTrace(); // Вывод исключения для отладки
        }
        return new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
    }

    public double alpha_beta_min_max(GameState root, int depth,double alpha, double beta, boolean maxPlayer) throws GameException {
        if(depth == 0 || root.getGameStage() == GameStage.ENDED){
            return tacticUtility.getMoveUtility(root);
        }
        List<MakeMoveEvent> movesRoot = root.getPossibleMoves();
        if(maxPlayer){
            double bestValue = Double.NEGATIVE_INFINITY;
            if(movesRoot.isEmpty()){
                if(root.getGameStage() == GameStage.ENDED){
                    return tacticUtility.getMoveUtility(root);
                }
                else {
                    GameState gameStateNode = root.getCopy();
                    gameStateNode.changeCurrentPlayer();
                    return alpha_beta_min_max(gameStateNode, depth-1, alpha,beta,false);
                }
            } else {
                for (MakeMoveEvent moveEvent : movesRoot) {
                    double v = exp_max(root, moveEvent, depth, alpha,beta,true);
                    bestValue = Math.max(bestValue,v);
                    alpha = Math.max(alpha, bestValue);
                    if (beta <= alpha) {
                        break;
                    }
                }
                return bestValue;
            }
        }
        else{
            double bestValue = Double.POSITIVE_INFINITY;
            if(movesRoot.isEmpty()){
                if(root.getGameStage() == GameStage.ENDED){
                    return tacticUtility.getMoveUtility(root);
                }
                else{
                    GameState gameStateNode = root.getCopy();
                    gameStateNode.changeCurrentPlayer();
                    return alpha_beta_min_max(gameStateNode, depth - 1, alpha,beta,true);
                }
            } else {
                for (MakeMoveEvent moveEvent : movesRoot) {
                    double v = exp_max(root, moveEvent, depth, alpha,beta,false);
                    bestValue = Math.min(bestValue,v);
                    beta = Math.min(beta, bestValue);
                    if (beta <= alpha){
                        break;
                    }
                }
                return bestValue;
            }
        }
    }



    private double exp_max(GameState root, MakeMoveEvent event, int depth, double alpha, double beta, boolean maxPlayer) throws GameException {
        List<StateChanceResult> chancesRoot = root.getPossibleIssue(event);
        if(maxPlayer){
            double excepted = 0;
            for (StateChanceResult chance : chancesRoot) {
                GameState nodeGameState = chance.gameState().getCopy();
                double v = alpha_beta_min_max(nodeGameState, depth-1, alpha,beta,true);
                excepted += chance.chance() * v;
            }
            return excepted;
        }
        else{
            double excepted = 0;
            for (StateChanceResult chance : chancesRoot) {
                GameState nodeGameState = chance.gameState().getCopy();
                double v = alpha_beta_min_max(nodeGameState, depth-1, alpha,beta,false);
                excepted += chance.chance() * v;
            }
            return excepted;
        }


    }





    public UtilityMoveResult MinMax(GameState root, int depth, boolean maxPlayer) throws GameException {
        if(depth == 0 || root.getGameStage() == GameStage.ENDED){
            return new UtilityMoveResult(tacticUtility.getMoveUtility(root), null);
        }
        List<MakeMoveEvent> movesRoot = root.getPossibleMoves();
        if(maxPlayer){
            UtilityMoveResult bestValue = new UtilityMoveResult(Double.NEGATIVE_INFINITY, null);
            if(movesRoot.isEmpty()){
                if(root.getGameStage() == GameStage.ENDED){
                    return new UtilityMoveResult(tacticUtility.getMoveUtility(root), null);
                }
                else {
                    GameState gameStateNode = root.getCopy();
                    gameStateNode.changeCurrentPlayer();
                    return MinMax(gameStateNode, depth-1, false);
                }
            } else {
                for (MakeMoveEvent moveEvent : movesRoot) {
                    GameState nodeGameState = root.getCopy();
                    nodeGameState.makeMove(moveEvent);
                    UtilityMoveResult v = MinMax(nodeGameState, depth-1, true);
                    if (bestValue.value < v.value) {
                        bestValue.value = v.value;
                    }
                    bestValue.event = moveEvent;
                }
                return bestValue;
            }

        }
        else{
            UtilityMoveResult bestValue = new UtilityMoveResult(Double.POSITIVE_INFINITY, null);
            if(movesRoot.isEmpty()){
                if(root.getGameStage() == GameStage.ENDED){
                    return new UtilityMoveResult(tacticUtility.getMoveUtility(root),null);
                }
                else{
                    GameState gameStateNode = root.getCopy();
                    gameStateNode.changeCurrentPlayer();
                    return MinMax(gameStateNode, depth-1, true);
                }
            } else {
                for (MakeMoveEvent moveEvent : movesRoot) {
                    GameState nodeGameState = root.getCopy();
                    nodeGameState.makeMove(moveEvent);
                    UtilityMoveResult v = MinMax(nodeGameState, depth - 1, false);
                    if (bestValue.value > v.value) {
                        bestValue.value = v.value;
                    }
                    bestValue.event = moveEvent;
                }
                return bestValue;
            }
        }
    }

    public List<Unit> enumerationUnit(PlayerType playerType) {
        List<Unit> tmp = new ArrayList<>();
        tmp.add(new Knight(playerType));
        tmp.add(new Archer(playerType));
        tmp.add(new Healer(playerType));
        tmp.add(new Mage(playerType));
        return tmp;
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

    public List<Position> enumerationEmptyCells(PlayerType playerType, Board board) {
        List<Position> unitPositions = new ArrayList<>();
        if (playerType == PlayerType.FIRST_PLAYER) {
            unitPositions.addAll(board.enumerateEmptyCells(0, Board.ROWS / 2));
        } else {
            unitPositions.addAll(board.enumerateEmptyCells(Board.ROWS / 2, Board.ROWS));
        }
        return unitPositions;
    }

    public PossibleActions<Position, Position> unitsPossibleActions(GameState gameState) {
        Board board = gameState.getCurrentBoard();
        PossibleActions<Position, Position> map = new PossibleActions<>();
        List<Position> unitsCurrentPlayer;
        List<Position> unitsOpponentPlayer;
        if (gameState.getCurrentPlayer() == PlayerType.FIRST_PLAYER) {
            logger.atInfo().log("Calculating possible actions for First Player");
            unitsCurrentPlayer = enumerationPlayerUnits(PlayerType.FIRST_PLAYER, board);
            unitsOpponentPlayer = enumerationPlayerUnits(PlayerType.SECOND_PLAYER, board);
            for (Position from : unitsCurrentPlayer) {
                if (board.getUnit(from.x(), from.y()).getUnitType() == UnitType.HEALER) {
                    if (!board.getUnit(from.x(), from.y()).isMoved()) {
                        for (Position to : unitsCurrentPlayer) {
                            MakeMoveEvent move = new MakeMoveEvent(from, to, board.getUnit(from.x(), from.y()));
                            if (canActMove(gameState, move)) {
                                map.put(from, to);
                            } else {
                                logger.atInfo().log(
                                        "Invalid action for Healer from ({}, {}) to ({}, {})",
                                        from.x(),
                                        from.y(),
                                        to.x(),
                                        to.y());
                            }
                        }
                    }
                } else {
                    if (!board.getUnit(from.x(), from.y()).isMoved()) {
                        for (Position to : unitsOpponentPlayer) {
                            MakeMoveEvent move = new MakeMoveEvent(from, to, board.getUnit(from.x(), from.y()));
                            if (canActMove(gameState, move)) {
                                map.put(from, to);
                            } else {
                                logger.atInfo().log(
                                        "Invalid action from ({}, {}) to ({}, {})", from.x(), from.y(), to.x(), to.y());
                            }
                        }
                    }
                }
            }
        } else if (gameState.getCurrentPlayer() == PlayerType.SECOND_PLAYER) {
            logger.atInfo().log("Calculating possible actions for Second Player");
            unitsCurrentPlayer = enumerationPlayerUnits(PlayerType.SECOND_PLAYER, board);
            unitsOpponentPlayer = enumerationPlayerUnits(PlayerType.FIRST_PLAYER, board);
            for (Position from : unitsCurrentPlayer) {
                if (board.getUnit(from.x(), from.y()).getUnitType() == UnitType.HEALER) {
                    for (Position to : unitsCurrentPlayer) {
                        MakeMoveEvent move = new MakeMoveEvent(from, to, board.getUnit(from.x(), from.y()));
                        if (canActMove(gameState, move)) {
                            map.put(from, to);
                        } else {
                            logger.atInfo().log(
                                    "Invalid action for Healer from ({}, {}) to ({}, {})",
                                    from.x(),
                                    from.y(),
                                    to.x(),
                                    to.y());
                        }
                    }
                }
                for (Position to : unitsOpponentPlayer) {
                    MakeMoveEvent move = new MakeMoveEvent(from, to, board.getUnit(from.x(), from.y()));
                    if (canActMove(gameState, move)) {
                        map.put(from, to);
                    } else {
                        logger.atInfo().log(
                                "Invalid action from ({}, {}) to ({}, {})", from.x(), from.y(), to.x(), to.y());
                    }
                }
            }
        }
        return map;
    }

    public PossibleActions<Position, Unit> unitsPossiblePlacement(GameState gameState) {
        Board board = gameState.getCurrentBoard();
        PossibleActions<Position, Unit> map = new PossibleActions<>();
        List<Position> cellsCurrentPlayer;
        List<Unit> unitList;
        if (gameState.getCurrentPlayer() == PlayerType.FIRST_PLAYER) {
            logger.atInfo().log("Calculating possible placement for First Player");
            cellsCurrentPlayer = enumerationEmptyCells(PlayerType.FIRST_PLAYER, board);
            unitList = enumerationUnit(PlayerType.FIRST_PLAYER);
            for (Position to : cellsCurrentPlayer) {
                if (board.isEmptyCell(to.x(), to.y())) {
                    boolean inProcess = true;
                    boolean general = false;
                    if (enumerationPlayerUnits(PlayerType.FIRST_PLAYER, board).size() + 1 == 6) {
                        inProcess = false;
                        general = true;
                    }
                    for (Unit randUnit : unitList) {
                        PlaceUnitEvent place =
                                new PlaceUnitEvent(
                                        to.x(), to.y(), randUnit, PlayerType.FIRST_PLAYER, inProcess, general);
                        if (canActPlace(gameState, place)) {
                            map.put(to, randUnit);
                        } else {
                            logger.atInfo().log(
                                    "Invalid placement for First Player from ({}, {}) for {})",
                                    to.x(),
                                    to.y(),
                                    randUnit.getUnitType().name());
                        }
                    }
                }
            }
        } else if (gameState.getCurrentPlayer() == PlayerType.SECOND_PLAYER) {
            logger.atInfo().log("Calculating possible placement for Second Player");
            cellsCurrentPlayer = enumerationEmptyCells(PlayerType.SECOND_PLAYER, board);
            unitList = enumerationUnit(PlayerType.SECOND_PLAYER);
            for (Position to : cellsCurrentPlayer) {
                if (board.isEmptyCell(to.x(), to.y())) {
                    boolean inProcess = true;
                    boolean general = false;
                    if (enumerationPlayerUnits(PlayerType.SECOND_PLAYER, board).size() + 1 == 6) {
                        inProcess = false;
                        general = true;
                    }
                    for (Unit randUnit : unitList) {
                        PlaceUnitEvent place =
                                new PlaceUnitEvent(
                                        to.x(), to.y(), randUnit, PlayerType.SECOND_PLAYER, inProcess, general);
                        if (canActPlace(gameState, place)) {
                            map.put(to, randUnit);
                        } else {
                            logger.atInfo().log(
                                    "Invalid placement for Second Player from ({}, {}) for {})",
                                    to.x(),
                                    to.y(),
                                    randUnit.getUnitType().name());
                        }
                    }
                }
            }
        }
        return map;
    }

    private Position intToPos(int n) {
        switch (n) {
            case 0 -> {
                return new Position(0, 0);
            }
            case 1 -> {
                return new Position(0, 1);
            }
            case 2 -> {
                return new Position(1, 0);
            }
            case 3 -> {
                return new Position(1, 1);
            }
            case 4 -> {
                return new Position(2, 0);
            }
            case 5 -> {
                return new Position(2, 1);
            }
            default -> {
                return null;
            }
        }
    }

    private boolean canActMove(GameState gameState, MakeMoveEvent move) {
        boolean result;
        try {
            gameState.isValidMove(move);
            result = true;
        } catch (GameException e) {
            logger.atError().log("Move is invalid: {}", e.getMessage());
            result = false;
        }
        return result;
    }

    private boolean canActPlace(GameState gameState, PlaceUnitEvent placeUnitEvent) {
        boolean result;
        try {
            gameState.isValidPlacement(placeUnitEvent);
            result = true;
        } catch (GameException e) {
            logger.atError().log("Place is invalid: {}", e.getMessage());
            result = false;
        }
        return result;
    }
}
