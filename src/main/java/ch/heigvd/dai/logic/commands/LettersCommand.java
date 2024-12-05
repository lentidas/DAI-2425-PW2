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
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents the command that the player sends with the letters they are trying to guess on the
 * final round.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class LettersCommand extends GameCommand {

  public static final int NUMBER_OF_LETTERS = 5;

  /**
   * Default constructor.
   *
   * @param letters a {@link String} with the letters the player is trying to guess
   */
  public LettersCommand(String letters) {
    super(GameCommandType.LETTERS);
    args.add(letters);
  }

  /**
   * Gets the letters the player is trying to guess.
   *
   * @return a {@link String} with the letters the player is trying to guess (obtained from the
   *     arguments of the command)
   */
  public Character[] getGuessedLetters() {
    char[] letters = ((String) args.getFirst()).toCharArray();
    Character[] argsCopy = new Character[letters.length];

    for (int i = 0; i < letters.length; ++i) {
      argsCopy[i] = letters[i];
    }
    return argsCopy;
  }

  /**
   * Checks if the string of letters has any repeated letters.
   *
   * @return {@code true} if the string of letters has any repeated letters, {@code false} otherwise
   */
  public boolean hasRepeatedLetters() {
    List<Character> list = List.of(getGuessedLetters());
    Set<Character> set = new HashSet<>(list);
    return set.size() < list.size();
  }

  /**
   * Checks if the string of letters has any of the letters in the given string.
   *
   * @param letters a {@link String} with the letters to check for
   * @return {@code true} if the string of letters has any of the letters in the given string,
   *     {@code false} otherwise
   */
  public boolean hasAnyOf(String letters) {
    for (Object arg : args) {
      for (int j = 0; j < letters.length(); ++j) {
        if (arg.equals(letters.charAt(j))) {
          return true;
        }
      }
    }

    return false;
  }

  /**
   * Parses the arguments of the command from a TCP message.
   *
   * @param args a {@link String} array with the arguments of the command
   * @return a {@link LettersCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args
        || args.length != 1
        || Arrays.stream(args).anyMatch(Objects::isNull)
        || args[0].length() != NUMBER_OF_LETTERS) {
      throw new InvalidPropertiesFormatException(
          "[LettersCommand] Command did not receive 5 letters");
    }

    char[] letters = new char[NUMBER_OF_LETTERS];
    for (int i = 0; i < NUMBER_OF_LETTERS; i++) {
      letters[i] = args[0].charAt(i);
    }

    return new LettersCommand(new String(letters));
  }
}
