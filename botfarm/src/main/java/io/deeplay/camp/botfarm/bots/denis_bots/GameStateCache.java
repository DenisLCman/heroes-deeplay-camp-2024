package io.deeplay.camp.botfarm.bots.denis_bots;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.deeplay.camp.game.entities.Board;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GameStateCache implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<PossibleStartState> cache = new ArrayList<>();

    private static final ObjectMapper objectMapper = new ObjectMapper();


    public GameStateCache() {
    }

    public void addCache(GameState gameState, int countWinRound, PlayerType playerType) {
        Board enemyBoard = gameState.getCurrentBoard().getCopy();
        int shiftRow;
        if (playerType == PlayerType.FIRST_PLAYER) {
            shiftRow = 0;
        } else {
            shiftRow = 2;
        }

        for (int column = 0; column < Board.COLUMNS; column++) {
            for (int row = shiftRow; row < ((Board.ROWS / 2) + shiftRow); row++) {
                enemyBoard.setUnit(column, row, null);
            }
        }
        if(!cache.isEmpty()){
            boolean flag = false;
            for(int i = 0; i < cache.size();i++){
                if((playerType == cache.get(i).getForPlayerType())) {
                    if (equalsBoard(enemyBoard, cache.get(i).getEnemyUnits(), playerType)) {
                        if (countWinRound < cache.get(i).getCountWinRound()) {
                            cache.remove(cache.get(i));
                            cache.add(new PossibleStartState(enemyBoard, gameState.getBoard(), countWinRound, playerType));
                            flag = true;
                            break;
                        }
                    }
                }
            }
            if(!flag) {
                cache.add(new PossibleStartState(enemyBoard, gameState.getBoard(), countWinRound, playerType));
            }
        }

    }

    private boolean equalsBoard(Board enemyBoard, Board allyBoard, PlayerType playerType){
        boolean result = true;
        int shiftRow;
        if(playerType == PlayerType.FIRST_PLAYER) {
            shiftRow = 2;
        }
        else{
            shiftRow = 0;
        }
        for(int column = 0;column < Board.COLUMNS;column++) {
            for (int row = shiftRow; row < ((Board.ROWS / 2) + shiftRow); row++) {
                if((enemyBoard.getUnit(column,row).isGeneral() != allyBoard.getUnit(column,row).isGeneral()) ||
                        (enemyBoard.getUnit(column,row).getUnitType() != allyBoard.getUnit(column,row).getUnitType())){
                    result = false;
                }
            }
        }
        return result;
    }

    public void saveCacheToFile(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(JsonConverter.serialize(this));
        }
    }

    public GameStateCache loadCacheFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return JsonConverter.deserialize(in.readObject().toString(), GameStateCache.class);
        }
    }

    public List<PossibleStartState> getCache() {
        return cache;
    }
}
