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
 * Represents the command that announces a player he is going to play the last round.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class LastCommand extends GameCommand {

  /**
   * Default constructor.
   *
   * @param timeout an {@code int} representing the time the player has to play the last round
   * @param puzzle a {@link String} with the puzzle that the player has to solve
   * @param category a {@link String} with the category of the puzzle
   * @param initialLetters a {@link String} with the initial letters of the puzzle
   */
  public LastCommand(int timeout, String puzzle, String category, String initialLetters) {
    super(GameCommandType.LAST);
    args.add(timeout);
    args.add(puzzle);
    args.add(category);
    args.add(initialLetters);
  }

  /**
   * Gets the time the player has to play the last round.
   *
   * @return an {@code int} with the time the player has to play the last round (obtained from the
   *     arguments of the command)
   */
  public int getTimeout() {
    return (int) args.getFirst();
  }

  /**
   * Gets the puzzle that the player has to solve.
   *
   * @return a {@link String} with the puzzle that the player has to solve (obtained from the
   *     arguments of the command)
   */
  public String getPuzzle() {
    return (String) args.get(1);
  }

  /**
   * Gets the category of the puzzle.
   *
   * @return a {@link String} with the category of the puzzle (obtained from the arguments of the
   *     command)
   */
  public String getCategory() {
    return (String) args.get(2);
  }

  /**
   * Gets the initial letters of the puzzle.
   *
   * @return a {@link String} with the initial letters of the puzzle (obtained from the arguments of
   *     the command)
   */
  public String getInitialLetters() {
    return (String) args.get(3);
  }

  /**
   * Parses the arguments of the command from a TCP message.
   *
   * @param args a {@link String} array with the arguments of the command
   * @return a {@link LastCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args || args.length != 4 || Arrays.stream(args).anyMatch(Objects::isNull)) {
      throw new InvalidPropertiesFormatException(
          "[LastCommand] Command did not receive the right parameters");
    }

    int timeout;
    try {
      timeout = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new InvalidPropertiesFormatException("[LastCommand] Timeout is not a valid number");
    }

    return new LastCommand(timeout, args[1], args[2], args[3]);
  }
}
