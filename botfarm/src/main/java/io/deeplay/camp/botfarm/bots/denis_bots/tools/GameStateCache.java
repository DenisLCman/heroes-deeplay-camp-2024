package io.deeplay.camp.botfarm.bots.denis_bots.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.deeplay.camp.botfarm.bots.denis_bots.entities.PossibleStartState;
import io.deeplay.camp.game.entities.Board;
import io.deeplay.camp.game.mechanics.GameState;
import io.deeplay.camp.game.mechanics.PlayerType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, сохранение данных в файл для последующего использования ботами
 */
public class GameStateCache implements Serializable {

    /** Список, хранящий все данные о разыгранных играх */
    private final List<PossibleStartState> cache = new ArrayList<>();


    public GameStateCache() {
    }

    /**
     * Метод кэширования, добавляющий в Кэш новые разыгранные победные игры,
     * анализирующий, есть ли повторы, и лучше ли они себя показывали в зависимости
     * от количеста раундов, нужных для победы
     * @param gameState Игровое состояние.
     * @param countWinRound Количество раундов данного расстановки, нужных для победы
     * @param playerType Тип игрока, для которого данное сохранение игрового состояние применимо.
     */
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
    /**
     * Метод кэширования, для сравнения игровых состояний
     * @param enemyBoard Игровое состояние.
     * @param allyBoard Количество раундов данного расстановки, нужных для победы
     * @param playerType Тип игрока, для которого данное сохранение игрового состояние применимо.
     * @return Равны ли состояния или нет?
     */
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

    /**
     * Метод кэширования, для сохранения списка cache в файл.
     * @param filename Путь для сохранения кэша.
     */
    public void saveCacheToFile(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(JsonConverter.serialize(this));
        }
    }

    /**
     * Метод кэширования, для загрузки данных из файла кэша.
     * @param filename Путь для сохранения кэша.
     * @return Объект GameStateCache с списком Cache
     */
    public GameStateCache loadCacheFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            return JsonConverter.deserialize(in.readObject().toString(), GameStateCache.class);
        }
    }

    /**
     * Геттер для взятия списка cache с сохранёнными расстановками из кэша.
     */
    public List<PossibleStartState> getCache() {
        return cache;
    }
}
