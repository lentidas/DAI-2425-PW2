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

package ch.heigvd.dai.logic.client;

import ch.heigvd.dai.logic.PlayerState;
import ch.heigvd.dai.logic.client.parsers.DisconnectedInputParser;
import ch.heigvd.dai.logic.client.parsers.EndResponseParser;
import ch.heigvd.dai.logic.client.parsers.FillInputParser;
import ch.heigvd.dai.logic.client.parsers.GuessInputParser;
import ch.heigvd.dai.logic.client.parsers.IInputParser;
import ch.heigvd.dai.logic.client.parsers.IResponseParser;
import ch.heigvd.dai.logic.client.parsers.InfoResponseParser;
import ch.heigvd.dai.logic.client.parsers.LastRoundResponseParser;
import ch.heigvd.dai.logic.client.parsers.LettersInputParser;
import ch.heigvd.dai.logic.client.parsers.LobbyInputParser;
import ch.heigvd.dai.logic.client.parsers.RoundResponseParser;
import ch.heigvd.dai.logic.client.parsers.SecondPhaseInputParser;
import ch.heigvd.dai.logic.client.parsers.StartResponseParser;
import ch.heigvd.dai.logic.client.parsers.StatusResponseParser;
import ch.heigvd.dai.logic.client.parsers.TurnResponseParser;
import ch.heigvd.dai.logic.client.parsers.UsernameInputParser;
import ch.heigvd.dai.logic.client.parsers.VowelInputParser;
import ch.heigvd.dai.logic.client.parsers.WinnerResponseParser;
import ch.heigvd.dai.logic.commands.GameCommand;
import ch.heigvd.dai.logic.commands.GameCommandType;
import ch.heigvd.dai.logic.commands.LettersCommand;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InteractiveConsole {
  private String username;
  private PlayerState currentState;
  private boolean promptAlreadyShown;

  /** Parsers for input when in a specific player state */
  private static final Map<PlayerState, IInputParser> STATE_INPUT_PARSERS =
      Map.of(
          PlayerState.WAIT_FOR_USERNAME,
          new UsernameInputParser(),
          PlayerState.WAIT_IN_LOBBY,
          new LobbyInputParser(),
          PlayerState.WAIT_FOR_GUESS,
          new GuessInputParser(),
          PlayerState.SECOND_GUESS_PHASE,
          new SecondPhaseInputParser(),
          PlayerState.WAIT_FOR_VOWEL,
          new VowelInputParser(),
          PlayerState.WAIT_FOR_FILL,
          new FillInputParser(),
          PlayerState.SEND_LETTERS,
          new LettersInputParser(),
          PlayerState.WAIT_FOR_LAST_TURN,
          new FillInputParser(),
          PlayerState.DISCONNECTED,
          new DisconnectedInputParser());

  /** Parsers for a server response when it is of a specific command type */
  private static final Map<GameCommandType, IResponseParser> COMMAND_RESPONSE_PARSERS =
      Map.of(
          GameCommandType.START, new StartResponseParser(),
          GameCommandType.TURN, new TurnResponseParser(),
          GameCommandType.STATUS, new StatusResponseParser(),
          GameCommandType.INFO, new InfoResponseParser(),
          GameCommandType.ROUND, new RoundResponseParser(),
          GameCommandType.END, new EndResponseParser(),
          GameCommandType.WINNER, new WinnerResponseParser(),
          GameCommandType.LAST, new LastRoundResponseParser());

  /** Prompts for each one of the player states */
  private static final List<PlayerStatePrompt> STATE_PROMPTS =
      Arrays.asList(
          new PlayerStatePrompt(
              PlayerState.SECOND_GUESS_PHASE,
              new String[] {"Skip turn", "Buy a vowel", "Complete the puzzle"}),
          new PlayerStatePrompt(
              PlayerState.WAIT_FOR_USERNAME,
              new String[] {"Welcome! Please enter your username: "}),
          new PlayerStatePrompt(
              PlayerState.WAIT_FOR_FILL,
              new String[] {"Here's your chance to complete the puzzle. What is it?\n"}),
          new PlayerStatePrompt(
              PlayerState.WAIT_FOR_GUESS,
              new String[] {
                "Type a consonant to see if it is in the puzzle, or try your luck at completing the puzzle: "
              }),
          new PlayerStatePrompt(
              PlayerState.WAIT_FOR_VOWEL, new String[] {"Type the vowel to buy: "}),
          new PlayerStatePrompt(
              PlayerState.WAIT_FOR_LAST_TURN, new String[] {"Type the full puzzle: "}),
          new PlayerStatePrompt(
              PlayerState.SEND_LETTERS,
              new String[] {
                "Type " + LettersCommand.NumberOfLetters + " letters to complete the last puzzle: "
              }),
          new PlayerStatePrompt(
              PlayerState.WAIT_IN_LOBBY,
              new String[] {
                "Wait for someone to start the game, or type 'Go' to start it yourself\n"
              }));

  /**
   * Creates a new interactive console instance to make it easier for players to take part in the
   * game
   */
  public InteractiveConsole() {
    currentState = PlayerState.WAIT_FOR_USERNAME;
    promptAlreadyShown = false;
  }

  /**
   * Returns whether the client handler must wait for a user input, given the current player state
   *
   * @return True if input is needed or optional, false if handler should not receive it
   */
  public boolean needsInput() {
    switch (currentState) {
      case WAIT_FOR_USERNAME,
          WAIT_FOR_GUESS,
          WAIT_FOR_VOWEL,
          WAIT_FOR_FILL,
          WAIT_FOR_LAST_TURN,
          SEND_LETTERS,
          WAIT_IN_LOBBY,
          SECOND_GUESS_PHASE -> {
        return true;
      }

      case CHILLING, WAIT_FOR_ENDING, WAIT_FOR_TURN, DISCONNECTED -> {
        return false;
      }

      default -> {
        System.err.println("Invalid state " + currentState);
        return false;
      }
    }
  }

  /**
   * Gets the console prompt for the current player state
   *
   * @return The prompt to show on the console, or null if no prompt is to be shown
   */
  public String getPrompt() {
    String prompt = null;

    if (!promptAlreadyShown) {
      for (PlayerStatePrompt p : STATE_PROMPTS) {
        if (p.validFor() == currentState) {
          StringBuilder sb = new StringBuilder();

          if (p.prompts().length > 1) {
            sb.append("Choose one of the following:\n");
          }

          for (int i = 0; i < p.prompts().length; i++) {
            String state_prompt = p.prompts()[i];
            if (p.prompts().length > 1) {
              sb.append(i + 1).append(" - ");
            }

            sb.append(state_prompt);

            if (p.prompts().length > 1) {
              sb.append('\n');
            }
          }

          prompt = sb.toString();
          break;
        }
      }

      promptAlreadyShown = true;
    }

    return prompt;
  }

  /**
   * Parses the provided user input and returns the corresponding game command, if any needs to be
   * sent
   *
   * @return Game command to be sent to the server, or null if no command needs to be sent
   */
  public GameCommand parseUserInput(String input) {
    GameCommand command = null;
    IInputParser parser = STATE_INPUT_PARSERS.getOrDefault(currentState, null);

    if (null != parser) {
      command = parser.parse(this, input);
    } else {
      System.err.println("No parser for state " + currentState);
    }

    return command;
  }

  /**
   * Gets the current player's state
   *
   * @return The current state
   */
  public PlayerState getCurrentState() {
    return currentState;
  }

  /**
   * Sets the current player's state
   *
   * @param state The new state
   */
  public void setCurrentState(PlayerState state) {
    currentState = state;
    promptAlreadyShown = false;
  }

  /**
   * Returns the current player's username
   *
   * @return This player's username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets a new username
   *
   * @param username New username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * Parses a command received from the server and, if needed, shows things on the console
   *
   * @param command Command to parse
   */
  public void parseServerResponse(GameCommand command) {
    IResponseParser parser = COMMAND_RESPONSE_PARSERS.getOrDefault(command.getType(), null);

    if (null != parser) {
      parser.parse(this, command);
    } else {
      System.err.println("No parser for command type " + command.getType());
    }
  }
}
