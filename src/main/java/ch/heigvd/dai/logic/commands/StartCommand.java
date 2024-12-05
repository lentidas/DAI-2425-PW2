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
 * Represents the command that announces the start of a new round.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class StartCommand extends GameCommand {

  /**
   * Default constructor.
   *
   * @param roundNumber an {@code int} representing the number of the round that is starting
   * @param puzzle a {@link String} with the puzzle that the players have to solve
   * @param category a {@link String} with the category of the puzzle
   */
  public StartCommand(int roundNumber, String puzzle, String category) {
    super(GameCommandType.START);
    args.add(roundNumber);
    args.add(puzzle);
    args.add(category);
  }

  /**
   * Gets the number of the round that is starting.
   *
   * @return an {@code int} with the number of the round that is starting (obtained from the
   *     arguments of the command)
   */
  public int getRoundNumber() {
    return (int) args.getFirst();
  }

  /**
   * Gets the puzzle that the players have to solve.
   *
   * @return a {@link String} with the puzzle that the players have to solve (obtained from the
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
   * Parses the arguments of the command from a TCP message.
   *
   * @param args a {@link String} array with the arguments of the command
   * @return a {@link StartCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args
        || args.length != 3
        || Arrays.stream(args).anyMatch(Objects::isNull)
        || args[0].length() != 1) {
      throw new InvalidPropertiesFormatException(
          "[StartCommand] Command did not receive the right parameters");
    }

    int roundNumber;
    try {
      roundNumber = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new InvalidPropertiesFormatException("[StartCommand] Invalid round number");
    }

    return new StartCommand(roundNumber, args[1], args[2]);
  }
}
