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
import ch.heigvd.dai.logic.commands.GameCommand;
import ch.heigvd.dai.logic.commands.GuessCommand;
import ch.heigvd.dai.logic.commands.LobbyCommand;
import ch.heigvd.dai.logic.commands.RoundCommand;
import ch.heigvd.dai.logic.commands.StartCommand;
import ch.heigvd.dai.logic.commands.StatusCommand;
import ch.heigvd.dai.logic.commands.TurnCommand;
import ch.heigvd.dai.logic.puzzle.Puzzle;
import ch.heigvd.dai.logic.wheel.Wedge;
import ch.heigvd.dai.logic.wheel.Wheel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class GameMatch {

  public static int NormalRoundsBeforeLastRound = 4;
  public static int MaxPlayers = 4;
  private final CopyOnWriteArrayList<Player> connectedPlayers;
  private final ConcurrentHashMap<Player, ArrayList<GameCommand>> pendingCommands;
  private GamePhase currentPhase;
  private int currPlayerIndex;
  private final Wheel wheel;
  private Puzzle roundPuzzle;
  private int currentRound;

  public GameMatch() {
    connectedPlayers = new CopyOnWriteArrayList<>();
    wheel = new Wheel();
    currPlayerIndex = 0;
    currentPhase = GamePhase.WAITING_FOR_PLAYERS;
    pendingCommands = new ConcurrentHashMap<>();
    currentRound = 0;
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

      Player newPlayer = new Player(username);
      connectedPlayers.add(newPlayer);
      pendingCommands.put(newPlayer, new ArrayList<>());

      // Let the other players know someone joined
      queueOthersGlobalCommand(newPlayer, new StatusCommand(StatusCode.PLAYER_JOINED));
      queueOthersGlobalCommand(newPlayer, new LobbyCommand(getPlayers()));
    }
    else
    {
      joinResult = StatusCode.FULL;
    }

    return joinResult;
  }

  public String[] getPlayers()
  {
    int i = 0;
    String[] players = new String[connectedPlayers.size()];
    for(Player p : connectedPlayers) {
      players[i] = p.getUsername();
      i++;
    }
    return players;
  }

  public Player getPlayer(String username)
  {
    Player player = null;
    for(Player p : connectedPlayers) {
      if(p.getUsername().equals(username)) {
        player = p;
        break;
      }
    }
    return player;
  }

  public GameCommand[] getPendingCommands(Player player) {
    if(pendingCommands.containsKey(player)) {
      GameCommand[] commands = new GameCommand[pendingCommands.get(player).size()];
      pendingCommands.get(player).toArray(commands);
      pendingCommands.get(player).clear();
      System.out.println(Arrays.toString(commands));
      return commands;
    } else {
      return null;
    }
  }

  public boolean isMyTurn(Player player)
  {
    return connectedPlayers.get(currPlayerIndex).getUsername().equals(player.getUsername());
  }

  public boolean startGame() {
    if (currentPhase == GamePhase.WAITING_FOR_PLAYERS || currentPhase == GamePhase.START_NEW_TURN) {
      currentPhase = GamePhase.NORMAL_TURN;
      roundPuzzle = Puzzle.createNewPuzzle("");
      currPlayerIndex = -1; // Will change when calling advanceTurn()
      currentRound = 0; // Will change when calling advanceTurn()
      queueGlobalCommand(new StartCommand(currentRound, getCurrentPuzzle(), getCurrentPuzzleCategory()));
      advanceTurn();
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
      return puzzleCatStr;
    }

    return null;
  }

  public GameCommand guessConsonant(GuessCommand command) {
    int repetitions = roundPuzzle.getLetterCount(command.getGuessedLetter());
    Player player = connectedPlayers.get(currPlayerIndex);
    GameCommand response = null;

    if (0 == repetitions) {
      response = new StatusCommand(StatusCode.LETTER_MISSING);
      System.out.println(player + " guessed a consonant that does not exist in the puzzle");
      advanceTurn();
    } else {
      int moneyWon = player.getCurrentWedge().getMoneyWon() * repetitions;
      player.incrementMoney(moneyWon);
      System.out.println(player + " got " + moneyWon + "$ for a correct guess");
    }

    player.setCurrentWedge(null);
    return response;
  }

  private void queueGlobalCommand(GameCommand command)
  {
    for(Player p : connectedPlayers) {
      pendingCommands.get(p).add(command);
    }
  }

  private void queueOthersGlobalCommand(Player unmatchingPlayer, GameCommand othersCommand)
  {
    for(Player p : connectedPlayers) {
      if(!p.getUsername().equals(unmatchingPlayer.getUsername())) {
        pendingCommands.get(p).add(othersCommand);
      }
    }
  }

  private void queueSpecificGlobalCommand(Player matchingPlayer, GameCommand theirCommand)
  {
    for(Player p : connectedPlayers) {
      if(p.getUsername().equals(matchingPlayer.getUsername())) {
        pendingCommands.get(p).add(theirCommand);
      }
    }
  }

  private void queueDifferentGlobalCommand(Player matchingPlayer, GameCommand othersCommand, GameCommand theirCommand)
  {
    for(Player p : connectedPlayers) {
      if(p.getUsername().equals(matchingPlayer.getUsername())) {
        pendingCommands.get(p).add(theirCommand);
      } else {
        pendingCommands.get(p).add(othersCommand);
      }
    }
  }

  private void advanceRound()
  {
    currentRound++;
    if(currentRound > NormalRoundsBeforeLastRound) {
      currentPhase = GamePhase.LAST_TURN;
    }
    else
    {
      advanceTurn();
    }
  }

  private void advanceTurn()
  {
    currPlayerIndex = (currPlayerIndex + 1) % connectedPlayers.size();
    Player currentPlayer = connectedPlayers.get(currPlayerIndex);
    Wedge turnWedge = spinTheWheel();
    System.out.println(currentPlayer + " got " + turnWedge);
    boolean endsTurn = turnWedge.bankruptsPlayer() || turnWedge.bankruptsPlayer();
    GameCommand playerResponse = null;

    if(turnWedge.bankruptsPlayer())
    {
      currentPlayer.goBankrupt();
      playerResponse = new StatusCommand(StatusCode.BANKRUPT);
    }
    else if(turnWedge.skipsATurn())
    {
      playerResponse = new StatusCommand(StatusCode.LOST_A_TURN);
    }
    else
    {
      playerResponse = new TurnCommand(turnWedge.getMoneyWon(), currentPlayer.getMoney());
      currentPlayer.setCurrentWedge(turnWedge);
    }

    queueSpecificGlobalCommand(currentPlayer, playerResponse);
  }
}
