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

package ch.heigvd.dai.logic.client.parsers;

import ch.heigvd.dai.logic.PlayerState;
import ch.heigvd.dai.logic.StatusCode;
import ch.heigvd.dai.logic.client.InteractiveConsole;
import ch.heigvd.dai.logic.commands.GameCommand;
import ch.heigvd.dai.logic.commands.GameCommandType;
import ch.heigvd.dai.logic.commands.LettersCommand;
import ch.heigvd.dai.logic.commands.StatusCommand;

public class StatusResponseParser implements IResponseParser {

  @Override
  public void parse(InteractiveConsole interactiveConsole, GameCommand response) {
    if (response.getType() != GameCommandType.STATUS) {
      System.err.println("Unexpected response " + response.getType() + " for this parser");
      return;
    }

    StatusCommand cmd = (StatusCommand) response;

    // Always run these first
    if (cmd.getStatus() == StatusCode.PLAYER_JOINED || cmd.getStatus() == StatusCode.PLAYER_QUIT) {
      parseLobbyChange(interactiveConsole, cmd);
      return;
    }

    switch (interactiveConsole.getCurrentState()) {
      case WAIT_FOR_USERNAME -> parseJoinResponse(interactiveConsole, cmd);
      case WAIT_FOR_GUESS, WAIT_FOR_VOWEL -> parseLetterGuessResponse(interactiveConsole, cmd);
      case WAIT_FOR_FILL, WAIT_FOR_LAST_TURN -> parseAnswerResponse(interactiveConsole, cmd);
      case SEND_LETTERS -> parseLettersResponse(interactiveConsole, cmd);
      case WAIT_FOR_TURN -> parseTurnWaitingResponse(interactiveConsole, cmd);
    }
  }

  /**
   * Parses a response to a join request
   *
   * @param interactiveConsole Interactice CLI console
   * @param response Response received
   */
  private void parseJoinResponse(InteractiveConsole interactiveConsole, StatusCommand response) {
    switch (response.getStatus()) {
      case OK -> {
        System.out.println("Welcome to the game!");
        interactiveConsole.setCurrentState(PlayerState.WAIT_IN_LOBBY);
      }
      case DUPLICATE_NAME ->
          System.err.println("Someone else has already joined the game with that name!");
      case FULL -> System.err.println("The lobby is full or game has already started!");
      case KO -> System.err.println("Invalid username!");
      default -> System.err.println("Unexpected server response!");
    }
  }

  /**
   * Parses a response to a letter guess request
   *
   * @param interactiveConsole Interactice CLI console
   * @param response Response received
   */
  private void parseLetterGuessResponse(
      InteractiveConsole interactiveConsole, StatusCommand response) {
    switch (response.getStatus()) {
      case LETTER_EXISTS -> {
        System.out.println("You have guessed correctly!");
        if (interactiveConsole.getCurrentState() == PlayerState.WAIT_FOR_GUESS) {
          interactiveConsole.setCurrentState(PlayerState.SECOND_GUESS_PHASE);
        } else if (interactiveConsole.getCurrentState() == PlayerState.WAIT_FOR_VOWEL) {
          interactiveConsole.setCurrentState(PlayerState.WAIT_FOR_TURN);
        } else {
          System.err.println("Invalid current state!");
        }
      }

      case LETTER_MISSING -> {
        System.out.println("Unfortunately, that letter does not exist in this puzzle");
        interactiveConsole.setCurrentState(PlayerState.WAIT_FOR_TURN);
      }

      case KO -> System.err.println("Invalid consonant!");
      case ALREADY_TRIED -> System.out.println("Someone has already tried to guess this letter!");
      case TIMEOUT -> System.out.println("Unfortunately, you took too long to answer!");
      case NO_FUNDS -> {
        System.out.println("You don't have enough money to buy a vowel!");
        interactiveConsole.setCurrentState(PlayerState.SECOND_GUESS_PHASE);
      }
      default -> System.err.println("Unexpected server response!");
    }
  }

  /**
   * Parses a response to an answer request
   *
   * @param interactiveConsole Interactice CLI console
   * @param response Response received
   */
  private void parseAnswerResponse(InteractiveConsole interactiveConsole, StatusCommand response) {
    switch (response.getStatus()) {
      case RIGHT_ANSWER -> System.out.println("Congratulations, you have completed this puzzle!");

      case WRONG_ANSWER -> System.out.println("Unfortunately, that was not the right answer");

      default -> System.err.println("Unexpected server response!");
    }

    interactiveConsole.setCurrentState(PlayerState.WAIT_FOR_TURN);
  }

  /**
   * Parses a response to a "guess n letters" request
   *
   * @param interactiveConsole Interactice CLI console
   * @param response Response received
   */
  private void parseLettersResponse(InteractiveConsole interactiveConsole, StatusCommand response) {
    switch (response.getStatus()) {
      case ALREADY_TRIED ->
          System.out.println("One of more of the provided letters have already been used");
      case KO ->
          System.out.println(
              "Invalid letters provided! You must provide "
                  + LettersCommand.NumberOfLetters
                  + " letters in total, with no spaces in between");
    }
  }

  /**
   * Parses a response to a lost turn
   *
   * @param interactiveConsole Interactice CLI console
   * @param response Response received
   */
  private void parseTurnWaitingResponse(
      InteractiveConsole interactiveConsole, StatusCommand response) {
    switch (response.getStatus()) {
      case BANKRUPT ->
          System.out.println("Bad luck... The wheel says you're BANKRUPT! You lose the turn.");
      case LOST_A_TURN -> System.out.println("Bad luck... The wheel says you miss the next turn!");
    }
  }

  /**
   * Parses a response to a lobby change
   *
   * @param interactiveConsole Interactice CLI console
   * @param response Response received
   */
  private void parseLobbyChange(InteractiveConsole interactiveConsole, StatusCommand response) {
    switch (response.getStatus()) {
      case PLAYER_JOINED -> System.out.println("Another player joined the game!");
      case PLAYER_QUIT -> System.out.println("Another player quit the game...");
    }
  }
}
