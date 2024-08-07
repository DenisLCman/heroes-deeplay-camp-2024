package io.deeplay.camp.server.player;

import io.deeplay.camp.core.dto.server.GameStateDto;
import io.deeplay.camp.game.mechanics.PlayerType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Players {
  Map<PlayerType, Player> hashMap;

  public Players() {
    hashMap = new HashMap<>();
  }

  public void addPlayer(PlayerType playerType, Player player) {
    hashMap.put(playerType, player);
  }

  public boolean isFull() {
    return hashMap.size() == 2;
  }

  public void updateGameState(GameStateDto gameStateDto) {
    for (Player player : hashMap.values()) {
      player.updateGameState(gameStateDto);
    }
  }

  public PlayerType getPlayerTypeById(UUID clientId) {
    for (Player player : hashMap.values()) {
      if (player instanceof HumanPlayer) {
        if (((HumanPlayer) player).getClientId().equals(clientId)) {
          return player.getPlayerType();
        }
      } else if (player instanceof AiPlayer) {//TODO
        if (((AiPlayer) player).getGameParty().getGame().getGameState().getCurrentPlayer()
                == PlayerType.FIRST_PLAYER) {
          return player.getPlayerType();
        }
        if (((AiPlayer) player).getGameParty().getGame().getGameState().getCurrentPlayer()
            == PlayerType.SECOND_PLAYER) {
          return player.getPlayerType();
        }
      }
    }
    return null;
  }

  public UUID getPlayerByPlayerType(PlayerType playerType) {
    for (Player player : hashMap.values()) {
      if (player instanceof HumanPlayer) {
        if (((HumanPlayer) player).getPlayerType() == playerType) {
          return ((HumanPlayer) player).getClientId();
        }
      } else if (player instanceof AiPlayer) {

      }
    }
    return null;
  }

  public void clearPlayersType(){
    hashMap = new HashMap<>();
  }

  public void setPlayerById(UUID playerId, PlayerType playerType){
    for (Player player : hashMap.values()) {
      if (player instanceof HumanPlayer) {
        if (((HumanPlayer) player).getClientId().equals(playerId)) {
          player.playerType = playerType;
        }
      }
    }
  }

  public UUID getPlayerByAnotherPlayerId(UUID anotherPlayerId){
    for (Player player : hashMap.values()) {
      if (player instanceof HumanPlayer) {
        if (!((HumanPlayer) player).getClientId().equals(anotherPlayerId)) {
          return ((HumanPlayer) player).getClientId();
        }
      }
    }
    return null;
  }

}
