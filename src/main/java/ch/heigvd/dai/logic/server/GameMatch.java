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

package ch.heigvd.dai.logic.server;

import ch.heigvd.dai.Player;
import ch.heigvd.dai.logic.PlayerState;
import ch.heigvd.dai.logic.StatusCode;
import ch.heigvd.dai.logic.commands.EndCommand;
import ch.heigvd.dai.logic.commands.FillCommand;
import ch.heigvd.dai.logic.commands.GameCommand;
import ch.heigvd.dai.logic.commands.GuessCommand;
import ch.heigvd.dai.logic.commands.InfoCommand;
import ch.heigvd.dai.logic.commands.LastCommand;
import ch.heigvd.dai.logic.commands.LettersCommand;
import ch.heigvd.dai.logic.commands.LobbyCommand;
import ch.heigvd.dai.logic.commands.RoundCommand;
import ch.heigvd.dai.logic.commands.StartCommand;
import ch.heigvd.dai.logic.commands.StatusCommand;
import ch.heigvd.dai.logic.commands.TurnCommand;
import ch.heigvd.dai.logic.commands.VowelCommand;
import ch.heigvd.dai.logic.commands.WinnerCommand;
import ch.heigvd.dai.logic.server.puzzle.Puzzle;
import ch.heigvd.dai.logic.server.wheel.Wedge;
import ch.heigvd.dai.logic.server.wheel.Wheel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Core class that represents a game match, with all its players, the wheel, the current round, the
 * current puzzle, and the current phase.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class GameMatch {

  public static final int VOWEL_COST = 250;
  public static final int NORMAL_ROUNDS_BEFORE_LAST_ROUND = 5;
  public static final int LAST_ROUND_TIMEOUT = 15;
  public static final int MAX_PLAYERS = 5;
  private final CopyOnWriteArrayList<Player> connectedPlayers;
  private final ConcurrentHashMap<Player, ArrayList<GameCommand>> pendingCommands;
  private GamePhase currentPhase;
  private int currPlayerIndex;
  private final Wheel wheel;
  private Puzzle roundPuzzle;
  private int currentRound;

  /**
   * Default constructor for a game match. Initializes the attributes of the game match with default
   * that are adequate for a new game.
   */
  public GameMatch() {
    connectedPlayers = new CopyOnWriteArrayList<>();
    wheel = new Wheel();
    currPlayerIndex = 0;
    currentPhase = GamePhase.WAITING_FOR_PLAYERS;
    pendingCommands = new ConcurrentHashMap<>();
    currentRound = 0;
  }

  /**
   * Adds a player to the game match. If the player is successfully added, a message is sent to all
   * other players to inform them of the new player.
   *
   * @param username the username of the player to be added
   * @return the status code of the operation
   */
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

    // Force start game if lobby is full
    if (connectedPlayers.size() == MAX_PLAYERS) {
      startGame();
    }

    return joinResult;
  }

  /**
   * Removes a player from the game match. If the player is successfully removed, a message is sent
   * to all other players with the new game lobby. If no more players are connected, the game match
   * is restarted.
   *
   * @param username the username of the player to be removed from the game match
   */
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
        System.out.println("No more players. Restarting match...");
        return;
      }

      // Player was playing
      if (currPlayerIndex == playerIndex) {
        if (currentPhase == GamePhase.NORMAL_TURN) {
          --currPlayerIndex; // Will be readjusted when calling function below.
          advanceTurn();
        } else if (currentPhase == GamePhase.LAST_TURN) {
          announceResults("-");
        }
      }
    }
  }

  /**
   * Returns the usernames of all the players connected to the game match.
   *
   * @return an array of {@link String} with the usernames of all the players connected to the game
   *     match
   */
  public String[] getPlayers() {
    int i = 0;
    String[] players = new String[connectedPlayers.size()];
    for (Player p : connectedPlayers) {
      players[i] = p.getUsername();
      i++;
    }
    return players;
  }

  /**
   * Returns the {@link Player} object associated with the given username.
   *
   * @param username the username of the player to be returned
   * @return a {@link Player} object associated with the given username
   */
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

  /**
   * Gets the pending commands for a given player.
   *
   * @param player the player to get the pending commands for
   * @return an array of {@link GameCommand} objects with the pending commands for the given player
   *     or a {@code null} if there are no pending commands
   */
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

  /**
   * Checks if the given player has the turn to play.
   *
   * @param player the player to check if it is the current player
   * @return {@code true} if the given player is the current player, {@code false} otherwise
   */
  public boolean isNotMyTurn(Player player) {
    return !connectedPlayers.get(currPlayerIndex).getUsername().equals(player.getUsername());
  }

  /**
   * Starts the game. If there are no players connected or the game is not in the correct phase, the
   * game is not started.
   *
   * @return {@code true} if the game was successfully started, {@code false} otherwise
   */
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

  /**
   * Spins the wheel and returns the wedge that was spun. If the game is not in the correct phase,
   * the wheel is not spun.
   *
   * @return the {@link Wedge} that was spun or {@code null} if the wheel was not spun
   */
  public Wedge spinTheWheel() {
    if (currentPhase == GamePhase.NORMAL_TURN) {
      return wheel.spinTheWheel();
    }

    return null;
  }

  /**
   * Gets the current puzzle that is being played.
   *
   * @return a {@link String} with the current puzzle that is being played or {@code null} if it's
   *     not a guessing phase
   */
  public String getCurrentPuzzle() {
    if (currentPhase == GamePhase.NORMAL_TURN || currentPhase == GamePhase.LAST_TURN) {
      return roundPuzzle.getCurrentPuzzleState();
    }

    return null;
  }

  /**
   * Gets the current puzzle category that is being played.
   *
   * @return a {@link String} with the current puzzle category that is being played or {@code null}
   *     if it's not a guessing phase
   */
  public String getCurrentPuzzleCategory() {
    if (currentPhase == GamePhase.NORMAL_TURN || currentPhase == GamePhase.LAST_TURN) {
      String puzzleCatStr = roundPuzzle.getCategory().name();
      puzzleCatStr = puzzleCatStr.replace('_', ' ');
      puzzleCatStr = puzzleCatStr.toLowerCase();
      return puzzleCatStr;
    }

    return null;
  }

  /**
   * Gets the letters that have already been used for the current puzzle that is being played.
   *
   * @return an array of {@code char} with the guessed letters of the current puzzle that is being
   *     played or {@code null} if it's not a guessing phase
   */
  public char[] getGuessedLetters() {
    if (currentPhase == GamePhase.NORMAL_TURN || currentPhase == GamePhase.LAST_TURN) {
      return roundPuzzle.getGuessedLetters();
    }

    return null;
  }

  /**
   * Method that handles the guessing of a consonant by a player. If the player is not in the
   * correct phase, a {@link StatusCommand} with the {@link StatusCode#KO} is returned. If the
   * player is in the correct phase, the guessed letter is checked against the current puzzle. A
   * response to give the player is then generated and returned, depending on the correctness of the
   * guess.
   *
   * @param command the {@link GuessCommand} with the guessed consonant
   * @return a {@link GameCommand} with the response send back to the player's client socket
   */
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

  /**
   * Method that handles the guessing of a vowel by a player. If the player is not in the correct
   * phase, a {@link StatusCommand} with the {@link StatusCode#KO} is returned. If the player is in
   * the correct phase, the guessed letter is checked against the current puzzle. A response to give
   * the player is then generated and returned, depending on the correctness of the guess.
   *
   * <p>Since the guessing of a vowel comes with a cost, the player's money is decremented by the
   * cost. Also note that the guessing of vowels only comes on the first phase of the game.
   *
   * @param command the {@link VowelCommand} with the guessed vowel
   * @return a {@link GameCommand} with the response send back to the player's client socket
   */
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

        // Small hack so next turn is played by the same player
        currPlayerIndex -= 1;
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

  /**
   * Method that handles the allows the player to try to solve the puzzle.
   *
   * <p>If the player guesses correctly, the game advances to the next round and all players are
   * notified.
   *
   * @param command the {@link FillCommand} with the guessed puzzle
   * @return a {@link GameCommand} with the response send back to the player's client socket
   */
  public GameCommand solvePuzzle(FillCommand command) {

    GameCommand response;
    Player player;

    if ((player = connectedPlayers.get(currPlayerIndex)) != null) {

      if (currentPhase == GamePhase.NORMAL_TURN) {

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
          && player.getState() == PlayerState.SECOND_GUESS_PHASE) {

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
    } else {
      response = new StatusCommand(StatusCode.KO);
    }

    return response;
  }

  /**
   * Announces the results of a round to all players by queueing a {@link EndCommand} with the
   * results to be sent to all players.
   *
   * @param winner the username of the player that won the round
   */
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

  /**
   * Queues a global command to be sent to all players.
   *
   * @param command the {@link GameCommand} to be sent to all players
   */
  private void queueGlobalCommand(GameCommand command) {
    for (Player p : connectedPlayers) {
      pendingCommands.get(p).add(command);
    }
  }

  /**
   * Queues a global command to be sent to all players except the one specified.
   *
   * @param unmatchingPlayer the {@link Player} to not send the command to
   * @param othersCommand the {@link GameCommand} to be sent to all other players
   */
  private void queueOthersGlobalCommand(Player unmatchingPlayer, GameCommand othersCommand) {
    for (Player p : connectedPlayers) {
      if (!p.getUsername().equals(unmatchingPlayer.getUsername())) {
        pendingCommands.get(p).add(othersCommand);
      }
    }
  }

  /**
   * Queues a specific global command to be sent to a specific player.
   *
   * @param matchingPlayer the {@link Player} to send the command to
   * @param theirCommand the {@link GameCommand} to be sent to the specified player
   */
  private void queueSpecificGlobalCommand(Player matchingPlayer, GameCommand theirCommand) {
    for (Player p : connectedPlayers) {
      if (p.getUsername().equals(matchingPlayer.getUsername())) {
        pendingCommands.get(p).add(theirCommand);
      }
    }
  }

  /**
   * Advances the round to the next one. If the current round is the last one, the game is set to
   * the last turn phase.
   */
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

  /**
   * Skips the turn of a player. If it's not the player's turn, the turn is not skipped.
   *
   * @param player the {@link Player} to skip the turn
   */
  public void skipTurn(Player player) {
    // Reset player state for next turns.
    if (!isNotMyTurn(player)) {
      player.setState(PlayerState.CHILLING);
      advanceTurn();
    }
  }

  /** Advances the turn to the next player. */
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

  /**
   * Method that handles the guessing of the last round puzzle by a player. If the player is not in
   * the correct phase, a {@link StatusCommand} with the {@link StatusCode#KO} is returned. If the
   * player is in the correct phase, the guessed letters are checked against the current puzzle. A
   * response to give the player is then generated and returned, depending on the correctness of the
   * guess.
   *
   * @param command the {@link LettersCommand} with the guessed letters
   * @return a {@link GameCommand} with the response send back to the player's client socket
   */
  public GameCommand guessLastRoundLetters(LettersCommand command) {
    GameCommand response;
    Player player;

    if (this.currentPhase == GamePhase.LAST_TURN
        && (player = connectedPlayers.get(currPlayerIndex)).getState() == PlayerState.CHILLING) {
      if (command.hasRepeatedLetters() || command.hasAnyOf(Puzzle.FinalRoundInitialLetters)) {
        System.out.println(player + " guessed letters that have already been guessed");
        response = new StatusCommand(StatusCode.ALREADY_TRIED);
      } else {
        for (Character c : command.getGuessedLetters()) {
          // Ignore the result as intended
          roundPuzzle.tryGuessLetter(c);
        }

        player.setState(PlayerState.SECOND_GUESS_PHASE);
        System.out.println(
            player
                + " is playing with guessed letters "
                + new String(roundPuzzle.getGuessedLetters()));
        response = new RoundCommand(roundPuzzle.getCurrentPuzzleState());
      }
    } else {
      response = new StatusCommand(StatusCode.KO);
    }

    return response;
  }

  /**
   * Method that handles the startup of the last round. The player with the most money is selected
   * as the winner and the last round puzzle is set.
   */
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
    System.out.println("Full puzzle: " + roundPuzzle.getFullPuzzle());
    currentPhase = GamePhase.LAST_TURN;
    queueGlobalCommand(new WinnerCommand(winningPlayer.getUsername()));
    queueSpecificGlobalCommand(
        winningPlayer,
        new LastCommand(
            LAST_ROUND_TIMEOUT,
            getCurrentPuzzle(),
            getCurrentPuzzleCategory(),
            Puzzle.FinalRoundInitialLetters));
  }
}
