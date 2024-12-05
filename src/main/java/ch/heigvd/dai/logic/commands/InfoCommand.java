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
 * Represents the command that announces the puzzle and category of the current round to all the
 * players.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class InfoCommand extends GameCommand {

  /**
   * Default constructor.
   *
   * @param puzzle a {@link String} with the puzzle that the players have to solve
   * @param category a {@link String} with the category of the puzzle
   * @param usedLetters a {@link String} with the letters that have already been used
   */
  public InfoCommand(String puzzle, String category, char[] usedLetters) {
    super(GameCommandType.INFO);
    args.add(puzzle);
    args.add(category);
    args.add(new String(usedLetters));
  }

  /**
   * Gets the puzzle that the players have to solve.
   *
   * @return a {@link String} with the puzzle that the players have to solve (obtained from the
   *     arguments of the command)
   */
  public String getPuzzle() {
    return (String) args.getFirst();
  }

  /**
   * Gets the category of the puzzle.
   *
   * @return a {@link String} with the category of the puzzle (obtained from the arguments of the
   *     command)
   */
  public String getCategory() {
    return (String) args.get(1);
  }

  /**
   * Gets the letters that have already been used.
   *
   * @return a {@link String} with the letters that have already been used (obtained from the
   *     arguments of the command)
   */
  public String getUsedLetters() {
    return ((String) args.get(2));
  }

  /**
   * Parses the arguments of the command from a TCP message.
   *
   * @param args a {@link String} array with the arguments of the command
   * @return a {@link InfoCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args
        || args.length < 2
        || args.length > 3
        || Arrays.stream(args).anyMatch(Objects::isNull)) {
      throw new InvalidPropertiesFormatException(
          "[InfoCommand] Command did not receive correct parameters");
    }

    char[] usedLetters = new char[0];

    // If we received the letters that have already been used.
    if (args.length == 3) {
      usedLetters = args[2].toCharArray();
    } // if

    return new InfoCommand(args[0], args[1], usedLetters);
  }
}
