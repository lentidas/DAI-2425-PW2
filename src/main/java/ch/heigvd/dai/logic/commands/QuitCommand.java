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
 * Represents the command that announces that a player has decided to quit the game.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class QuitCommand extends GameCommand {

  /** Default constructor. */
  public QuitCommand() {
    super(GameCommandType.QUIT);
  }

  /**
   * Parses the arguments of the command from a TCP message.
   *
   * @param args a {@link String} array with the arguments of the command
   * @return a {@link QuitCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null != args && args.length > 0) {
      throw new InvalidPropertiesFormatException("[QuitCommand] Command does not take arguments");
    }

    return new QuitCommand();
  }
}
