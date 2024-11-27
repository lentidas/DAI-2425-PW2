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
import ch.heigvd.dai.logic.puzzle.Puzzle;
import ch.heigvd.dai.logic.wheel.Wedge;
import ch.heigvd.dai.logic.wheel.Wheel;
import java.util.concurrent.CopyOnWriteArrayList;


public class GameMatch {

  public static int MaxPlayers = 4;
  private final CopyOnWriteArrayList<Player> connectedPlayers;
  private GamePhase currentPhase;
  private int currPlayerIndex;
  private final Wheel wheel;
  private Puzzle roundPuzzle;

  public GameMatch() {
    connectedPlayers = new CopyOnWriteArrayList<>();
    wheel = new Wheel();
    currPlayerIndex = 0;
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
    if(currentPhase == GamePhase.WAITING_FOR_PLAYERS || currentPhase == GamePhase.START_NEW_TURN) {
      currentPhase = GamePhase.NORMAL_TURN;
      roundPuzzle = Puzzle.createNewPuzzle("");
      currPlayerIndex = 0;
      return true;
    }

    return false;
  }

  public Wedge spinTheWheel()
  {
    if(currentPhase == GamePhase.NORMAL_TURN) {
      return wheel.spinTheWheel();
    }

    return null;
  }


  public String getCurrentPuzzle()
  {
    if(currentPhase == GamePhase.NORMAL_TURN || currentPhase == GamePhase.LAST_TURN) {
      return roundPuzzle.getCurrentPuzzleState();
    }

    return null;
  }

  public String getCurrentPuzzleCategory()
  {
    if(currentPhase == GamePhase.NORMAL_TURN || currentPhase == GamePhase.LAST_TURN) {
      String puzzleCatStr = roundPuzzle.getCategory().name();
      puzzleCatStr = puzzleCatStr.replace('_', ' ');
      puzzleCatStr = puzzleCatStr.toLowerCase();
      return roundPuzzle.getCurrentPuzzleState();
    }

    return null;
  }
}
