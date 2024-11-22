/*
 * Wheel Of Fortune - a Java server/client CLI implementation of the television game
 * Copyright (C) 2024 Pedro Alves da Silva, Gonçalo Carvalheiro Heleno
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.heigvd.dai.logic;

import ch.heigvd.dai.Player;
import ch.heigvd.dai.logic.wheel.Wheel;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameLobby {

  public static int MaxPlayers = 4;
  private final CopyOnWriteArrayList<Player> connectedPlayers;
  private GamePhase currentPhase;
  private Player currentPlayer;
  private Wheel wheel;

  public GameLobby() {
    connectedPlayers = new CopyOnWriteArrayList<>();
    wheel = new Wheel();
    currentPlayer = null;
    currentPhase = GamePhase.WAITING_FOR_PLAYERS;
  }

  public StatusCode addPlayer(String username) {
    StatusCode joinResult = StatusCode.OK;

    if(currentPhase == GamePhase.WAITING_FOR_PLAYERS && connectedPlayers.size() < MaxPlayers)
    {
      for(Player p : connectedPlayers) {
        if(p.getUsername().equals(username)) {
          joinResult = StatusCode.DUPLICATE_NAME;
          break;
        }
      }

      connectedPlayers.add(new Player(username));
    }
    else
    {
      joinResult = StatusCode.FULL;
    }


    return joinResult;
  }


  public boolean startGame()
  {
    if(currentPhase == GamePhase.WAITING_FOR_PLAYERS) {
      currentPhase = GamePhase.NORMAL_TURN;
      currentPlayer = connectedPlayers.getFirst();
      return true;
    }

    return false;
  }
}