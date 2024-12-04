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
import ch.heigvd.dai.logic.commands.EndCommand;
import ch.heigvd.dai.logic.commands.FillCommand;
import ch.heigvd.dai.logic.commands.GameCommand;
import ch.heigvd.dai.logic.commands.GuessCommand;
import ch.heigvd.dai.logic.commands.InfoCommand;
import ch.heigvd.dai.logic.commands.LastCommand;
import ch.heigvd.dai.logic.commands.LobbyCommand;
import ch.heigvd.dai.logic.commands.RoundCommand;
import ch.heigvd.dai.logic.commands.StartCommand;
import ch.heigvd.dai.logic.commands.StatusCommand;
import ch.heigvd.dai.logic.commands.TurnCommand;
import ch.heigvd.dai.logic.commands.VowelCommand;
import ch.heigvd.dai.logic.commands.WinnerCommand;
import ch.heigvd.dai.logic.puzzle.Puzzle;
import ch.heigvd.dai.logic.wheel.Wedge;
import ch.heigvd.dai.logic.wheel.Wheel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameMatch {

  public static final int VOWEL_COST = 250;
  public static final int NORMAL_ROUNDS_BEFORE_LAST_ROUND = 5;
  public static final int LAST_ROUND_TIMEOUT = 15;
  public static final int MAX_PLAYERS = 4;
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

    if (currentPhase == GamePhase.WAITING_FOR_PLAYERS && connectedPlayers.size() < MAX_PLAYERS) {
      for (Player p : connectedPlayers) {
        if (p.getUsername().equals(username)) {
          joinResult = StatusCode.DUPLICATE_NAME;
          break;
        }
      }

      Player newPlayer = new Player(username);
      connectedPlayers.add(newPlayer);
      pendingCommands.put(newPlayer, new ArrayList<>());

      // Let the other players know someone joined.
      queueOthersGlobalCommand(newPlayer, new StatusCommand(StatusCode.PLAYER_JOINED));
      queueOthersGlobalCommand(newPlayer, new LobbyCommand(getPlayers()));
    } else {
      joinResult = StatusCode.FULL;
    }

    return joinResult;
  }

  public void quitPlayer(String username) {
    boolean playerFound = false;
    int playerIndex = 0;
    for (; playerIndex < connectedPlayers.size(); playerIndex++) {
      if (connectedPlayers.get(playerIndex).getUsername().equals(username)) {
        playerFound = true;
        break;
      }
    }

    if (playerFound) {
      connectedPlayers.remove(playerIndex);
      queueGlobalCommand(new LobbyCommand(getPlayers()));

      if (connectedPlayers.isEmpty()) {
        currentPhase = GamePhase.WAITING_FOR_PLAYERS;
        System.out.println("No more players. Restarting match");
        return;
      }

      // Player was playing
      if (currPlayerIndex == playerIndex) {
        if (currentPhase == GamePhase.NORMAL_TURN) {
          --currPlayerIndex; // Will be readjusted when calling function below
          advanceTurn();
        } else if (currentPhase == GamePhase.LAST_TURN) {
          announceResults("-");
        }
      }
    }
  }

  public String[] getPlayers() {
    int i = 0;
    String[] players = new String[connectedPlayers.size()];
    for (Player p : connectedPlayers) {
      players[i] = p.getUsername();
      i++;
    }
    return players;
  }

  public Player getPlayer(String username) {
    Player player = null;
    for (Player p : connectedPlayers) {
      if (p.getUsername().equals(username)) {
        player = p;
        break;
      }
    }
    return player;
  }

  public GameCommand[] getPendingCommands(Player player) {
    if (pendingCommands.containsKey(player)) {
      GameCommand[] commands = new GameCommand[pendingCommands.get(player).size()];
      pendingCommands.get(player).toArray(commands);
      pendingCommands.get(player).clear();
      return commands;
    } else {
      return null;
    }
  }

  public boolean isNotMyTurn(Player player) {
    return !connectedPlayers.get(currPlayerIndex).getUsername().equals(player.getUsername());
  }

  public boolean startGame() {
    if (!connectedPlayers.isEmpty()
        && (currentPhase == GamePhase.WAITING_FOR_PLAYERS
            || currentPhase == GamePhase.START_NEW_TURN)) {
      currentPhase = GamePhase.NORMAL_TURN;
      currPlayerIndex = -1; // Will change when calling advanceRound()
      currentRound = 0; // Will change when calling advanceRound()
      advanceRound();
      return true;
    }

    return false;
  }

  public Wedge spinTheWheel() {
    if (currentPhase == GamePhase.NORMAL_TURN) {
      return wheel.spinTheWheel();
    }

    return null;
  }

  public String getCurrentPuzzle() {
    if (currentPhase == GamePhase.NORMAL_TURN || currentPhase == GamePhase.LAST_TURN) {
      return roundPuzzle.getCurrentPuzzleState();
    }

    return null;
  }

  public String getCurrentPuzzleCategory() {
    if (currentPhase == GamePhase.NORMAL_TURN || currentPhase == GamePhase.LAST_TURN) {
      String puzzleCatStr = roundPuzzle.getCategory().name();
      puzzleCatStr = puzzleCatStr.replace('_', ' ');
      puzzleCatStr = puzzleCatStr.toLowerCase();
      return puzzleCatStr;
    }

    return null;
  }

  public char[] getGuessedLetters() {
    if (currentPhase == GamePhase.NORMAL_TURN || currentPhase == GamePhase.LAST_TURN) {
      return roundPuzzle.getGuessedLetters();
    }

    return null;
  }

  public GameCommand guessConsonant(GuessCommand command) {

    GameCommand response;
    Player player;

    if (currentPhase == GamePhase.NORMAL_TURN
        && (player = connectedPlayers.get(currPlayerIndex)).getState() == PlayerState.CHILLING) {

      int repetitions = roundPuzzle.getLetterCount(command.getGuessedLetter());

      if (roundPuzzle.hasLetterBeenGuessed(command.getGuessedLetter())) {
        response = new StatusCommand(StatusCode.ALREADY_TRIED);
        System.out.println(player + " guessed a consonant that has already been guessed");
      } else if (!roundPuzzle.tryGuessLetter(command.getGuessedLetter())) {
        response = new StatusCommand(StatusCode.LETTER_MISSING);
        System.out.println(player + " guessed a consonant that does not exist in the puzzle");
        player.setCurrentWedge(null);
        advanceTurn();
      } else {
        int moneyWon = player.getCurrentWedge().getMoneyWon() * repetitions;
        player.incrementMoney(moneyWon);
        System.out.println(player + " got " + moneyWon + "$ for a correct guess");
        player.setCurrentWedge(null);
        response = new StatusCommand(StatusCode.LETTER_EXISTS);

        if (roundPuzzle.getFullPuzzle().equals(getCurrentPuzzle())) {
          System.out.println(player + " finished round");
          advanceRound();
        } else {
          player.setState(PlayerState.SECOND_GUESS_PHASE);
          queueSpecificGlobalCommand(
              player,
              new InfoCommand(getCurrentPuzzle(), getCurrentPuzzleCategory(), getGuessedLetters()));
        }
      }
    } else {
      response = new StatusCommand(StatusCode.KO);
    }

    return response;
  }

  public GameCommand guessVowel(VowelCommand command) {

    boolean endTurn = false;
    GameCommand response;
    Player player;

    if (currentPhase == GamePhase.NORMAL_TURN
        && (player = connectedPlayers.get(currPlayerIndex)).getState()
            == PlayerState.SECOND_GUESS_PHASE) {

      if (roundPuzzle.hasLetterBeenGuessed(command.getVowel())) {
        response = new StatusCommand(StatusCode.ALREADY_TRIED);
        System.out.println(player + " guessed a vowel that has already been guessed");
      } else if (!roundPuzzle.tryGuessLetter(command.getVowel())) {
        response = new StatusCommand(StatusCode.LETTER_MISSING);
        System.out.println(player + " guessed a vowel that does not exist in the puzzle");
        endTurn = true;
      } else {
        response = new StatusCommand(StatusCode.LETTER_EXISTS);
        System.out.println(player + " guessed the vowel " + command.getVowel());
        endTurn = true;
      }

      if (endTurn) {
        player.decrementMoney(roundPuzzle.getVowelCost());
        player.setCurrentWedge(null);

        if (roundPuzzle.getFullPuzzle().equals(getCurrentPuzzle())) {
          System.out.println(player + " finished round");
          advanceRound();
        } else {
          advanceTurn();
          player.setState(PlayerState.CHILLING);
        }
      }
    } else {
      response = new StatusCommand(StatusCode.KO);
    }

    return response;
  }

  public GameCommand solvePuzzle(FillCommand command) {

    GameCommand response;
    Player player;

    if (currentPhase == GamePhase.NORMAL_TURN
        && (player = connectedPlayers.get(currPlayerIndex)) != null) {

      if (roundPuzzle.guessPuzzle(command.getPuzzle())) {
        System.out.println(player + " successfully solved the puzzle");
        response = new StatusCommand(StatusCode.RIGHT_ANSWER);
        queueGlobalCommand(new RoundCommand(getCurrentPuzzle()));
        advanceRound();
      } else {
        System.out.println(player + " did not solve the puzzle");
        response = new StatusCommand(StatusCode.WRONG_ANSWER);
        advanceTurn();
      }

      player.setState(PlayerState.CHILLING);
    } else if (currentPhase == GamePhase.LAST_TURN
        && (player = connectedPlayers.get(currPlayerIndex)).getState() == PlayerState.CHILLING) {

      boolean playerWon = false;

      if (roundPuzzle.guessPuzzle(command.getPuzzle())) {
        System.out.println(player + " successfully solved the puzzle");
        response = new StatusCommand(StatusCode.RIGHT_ANSWER);
        playerWon = true;
      } else {
        System.out.println(player + " did not solve the puzzle");
        response = new StatusCommand(StatusCode.WRONG_ANSWER);
      }

      announceResults(playerWon ? player.getUsername() : "-");
    } else {
      response = new StatusCommand(StatusCode.KO);
    }

    return response;
  }

  private void announceResults(String winner) {
    String[] playerUsernames = new String[connectedPlayers.size()];
    int[] playerMoney = new int[connectedPlayers.size()];
    for (int i = 0; i < connectedPlayers.size(); i++) {
      playerUsernames[i] = connectedPlayers.get(i).getUsername();
      playerMoney[i] = connectedPlayers.get(i).getMoney();
    }
    queueGlobalCommand(new EndCommand(winner, playerUsernames, playerMoney));
    currentPhase = GamePhase.WAITING_FOR_PLAYERS;
  }

  private void queueGlobalCommand(GameCommand command) {
    for (Player p : connectedPlayers) {
      pendingCommands.get(p).add(command);
    }
  }

  private void queueOthersGlobalCommand(Player unmatchingPlayer, GameCommand othersCommand) {
    for (Player p : connectedPlayers) {
      if (!p.getUsername().equals(unmatchingPlayer.getUsername())) {
        pendingCommands.get(p).add(othersCommand);
      }
    }
  }

  private void queueSpecificGlobalCommand(Player matchingPlayer, GameCommand theirCommand) {
    for (Player p : connectedPlayers) {
      if (p.getUsername().equals(matchingPlayer.getUsername())) {
        pendingCommands.get(p).add(theirCommand);
      }
    }
  }

  private void advanceRound() {
    currentRound++;
    if (currentRound > NORMAL_ROUNDS_BEFORE_LAST_ROUND) {
      currentPhase = GamePhase.START_LAST_TURN;
      startLastRound();
    } else {
      roundPuzzle = Puzzle.createNewPuzzle("", VOWEL_COST);
      queueGlobalCommand(
          new StartCommand(currentRound, getCurrentPuzzle(), getCurrentPuzzleCategory()));
      System.out.println("New game started. Full puzzle: " + roundPuzzle.getFullPuzzle());
      advanceTurn();
    }
  }

  public void advanceTurn() {
    currPlayerIndex = (currPlayerIndex + 1) % connectedPlayers.size();
    Player currentPlayer = connectedPlayers.get(currPlayerIndex);
    Wedge turnWedge = spinTheWheel();
    System.out.println(currentPlayer + " got " + turnWedge);
    GameCommand playerResponse;
    boolean endsTurn = turnWedge.skipsATurn() || turnWedge.bankruptsPlayer();

    System.out.println("It is " + currentPlayer + "'s turn. Spin is " + turnWedge);

    if (turnWedge.bankruptsPlayer()) {
      currentPlayer.goBankrupt();
      playerResponse = new StatusCommand(StatusCode.BANKRUPT);
    } else if (turnWedge.skipsATurn()) {
      playerResponse = new StatusCommand(StatusCode.LOST_A_TURN);
    } else {
      playerResponse = new TurnCommand(turnWedge.getMoneyWon(), currentPlayer.getMoney());
      currentPlayer.setCurrentWedge(turnWedge);
    }

    queueSpecificGlobalCommand(
        currentPlayer,
        new InfoCommand(getCurrentPuzzle(), getCurrentPuzzleCategory(), getGuessedLetters()));
    queueSpecificGlobalCommand(currentPlayer, playerResponse);

    if (endsTurn) {
      advanceTurn();
    }
  }

  private void startLastRound() {
    if (this.currentPhase != GamePhase.START_LAST_TURN) {
      return;
    }

    Player winningPlayer = null;
    int i = 0;
    int winningPlayerIndex = 0;
    for (Player p : connectedPlayers) {
      if (winningPlayer == null || p.getMoney() > winningPlayer.getMoney()) {
        winningPlayer = p;
        winningPlayerIndex = i;
      }
      ++i;
    }

    if (null == winningPlayer) {
      throw new RuntimeException("No winning player!");
    }

    currPlayerIndex = winningPlayerIndex;
    System.out.println(winningPlayer + " is the winner, and goes to the last round");
    roundPuzzle = Puzzle.createNewPuzzle(Puzzle.FinalRoundInitialLetters, VOWEL_COST);
    currentPhase = GamePhase.LAST_TURN;
    queueGlobalCommand(new WinnerCommand(winningPlayer.getUsername()));
    queueSpecificGlobalCommand(
        winningPlayer,
        new LastCommand(LAST_ROUND_TIMEOUT, getCurrentPuzzle(), getCurrentPuzzleCategory()));
  }
}
