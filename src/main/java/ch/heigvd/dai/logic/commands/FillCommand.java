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

import java.util.InvalidPropertiesFormatException;

/**
 * Represents the command the player uses to entirely guess the puzzle.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class FillCommand extends GameCommand {

  /**
   * Default constructor.
   *
   * @param puzzle a {@link String} with the guess of the player
   */
  public FillCommand(String puzzle) {
    super(GameCommandType.FILL);
    args.add(puzzle);
  }

  /**
   * Gets the guess of the player.
   *
   * @return a {@link String} with the guess of the player (obtained from the arguments of the
   *     command)
   */
  public String getPuzzle() {
    return (String) args.getFirst();
  }

  /**
   * Parses the arguments of the command from a TCP message.
   *
   * @param args a {@link String} array with the arguments of the command
   * @return a {@link FillCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args || args.length != 1 || args[0] == null || args[0].isEmpty()) {
      throw new InvalidPropertiesFormatException("[FillCommand] Command did not receive the completed puzzle");
    }

    return new FillCommand(args[0]);
  }
}
