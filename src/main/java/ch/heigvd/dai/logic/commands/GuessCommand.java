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

package ch.heigvd.dai.logic.commands;

import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.Objects;

/**
 * Represents the command the player uses to guess a consonant.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class GuessCommand extends GameCommand {

  /**
   * Default constructor.
   *
   * @param letter a {@link char} with the consonant the player is guessing
   */
  public GuessCommand(char letter) {
    super(GameCommandType.GUESS);
    args.add(letter);
  }

  /**
   * Gets the consonant the player is guessing.
   *
   * @return a {@link char} with the consonant the player is guessing (obtained from the arguments
   *     of the command)
   */
  public char getGuessedLetter() {
    return (char) args.getFirst();
  }

  /**
   * Parses the arguments of the command from a TCP message.
   *
   * @param args a {@link String} array with the arguments of the command
   * @return a {@link GuessCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args
        || args.length != 1
        || Arrays.stream(args).anyMatch(Objects::isNull)
        || args[0].length() != 1) {
      throw new InvalidPropertiesFormatException(
          "[GuessCommand] Command did not receive a single letter");
    }

    char letter = args[0].charAt(0);
    if (!Character.isLetter(letter)) {
      throw new InvalidPropertiesFormatException("[GuessCommand] Character is not a letter");
    } else if (isCharAVowel(letter)) {
      throw new InvalidPropertiesFormatException("[GuessCommand] Letter is a vowel");
    }

    return new GuessCommand(letter);
  }
}
