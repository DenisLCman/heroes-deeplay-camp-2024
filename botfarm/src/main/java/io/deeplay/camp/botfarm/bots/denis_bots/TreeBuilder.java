package io.deeplay.camp.botfarm.bots.denis_bots;

import io.deeplay.camp.botfarm.GameAnalisys;
import io.deeplay.camp.game.Game;
import io.deeplay.camp.game.events.MakeMoveEvent;
import io.deeplay.camp.game.mechanics.GameStage;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.List;

public class TreeBuilder {

    boolean firstRootStart = true;

    @Getter
    public class Stats{
        int numNodes;
        int numTerminalNodes;
        int maxDepth;
        int depth;
        long workTimeMS;
        int winRateFirst;
        int winRateSecond;
        int winRateDraw;
        public Stats(){
            numNodes = 0;
            numTerminalNodes = 0;
            maxDepth = 0;
            depth = 0;
            workTimeMS = 0;
            winRateFirst = 0;
            winRateSecond = 0;
            winRateDraw = 0;
        }
    }
    Stats stats = new Stats();

    @SneakyThrows
    public Stats buildGameTree(final GameState root, int depth, int maxDepth){
        if(firstRootStart){
            stats.numNodes++;
            stats.maxDepth = maxDepth;
            stats.depth = depth;
            stats.workTimeMS = System.currentTimeMillis();
            firstRootStart = false;
        }
        List<MakeMoveEvent> movesRoot = root.getPossibleMoves();
        if(depth > stats.depth) {
            stats.depth = depth;
        }
        if(maxDepth != 0 && depth == stats.maxDepth){
            stats.numTerminalNodes++;
        } else if (movesRoot.isEmpty()) {
            if (root.getGameStage() != GameStage.ENDED) {
                GameState nodeGameState = root.getCopy();
                nodeGameState.changeCurrentPlayer();
                buildGameTree(nodeGameState, depth, maxDepth);
            } else {
                stats.numTerminalNodes++;
                switch (root.getWinner()) {
                    case FIRST_PLAYER -> stats.winRateFirst++;
                    case SECOND_PLAYER -> stats.winRateSecond++;
                    case DRAW -> stats.winRateDraw++;
                }
                return stats;
            }
        } else {
            for (MakeMoveEvent moveEvent : movesRoot) {
                stats.numNodes++;
                GameState nodeGameState = root.getCopy();
                nodeGameState.makeMove(moveEvent);
                buildGameTree(nodeGameState, depth+1, maxDepth);
            }
        }
        return stats;
    }
}
