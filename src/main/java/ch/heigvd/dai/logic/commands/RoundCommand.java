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
 * Represents the command that announces the start of the winner's round
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class RoundCommand extends GameCommand {

  /**
   * Default constructor.
   *
   * @param puzzle a {@link String} with the new puzzle to guess
   */
  public RoundCommand(String puzzle) {
    super(GameCommandType.ROUND);
    args.add(puzzle);
  }

  /**
   * Gets the new puzzle to guess.
   *
   * @return a {@link String} with the new puzzle to guess (obtained from the arguments of the
   *     command)
   */
  public String getPuzzle() {
    return (String) args.getFirst();
  }

  /**
   * Parses the arguments of the command from a TCP message.
   *
   * @param args a {@link String} array with the arguments of the command
   * @return a {@link RoundCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args
        || args.length != 1
        || Arrays.stream(args).anyMatch(Objects::isNull)
        || args[0].isEmpty()) {
      throw new InvalidPropertiesFormatException(
          "[RoundCommand] Command did not receive the new puzzle");
    }

    return new RoundCommand(args[0]);
  }
}
