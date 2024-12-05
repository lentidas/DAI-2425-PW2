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

import ch.heigvd.dai.logic.client.InteractiveConsole;
import ch.heigvd.dai.logic.commands.GameCommand;

public interface IInputParser {

  /**
   * Makes an input parser parse the provided user input
   *
   * @param interactiveConsole Interactive console to manage
   * @param input User input
   * @return Game command to be sent to the server, or null if no command needs to be sent
   */
  public GameCommand parse(InteractiveConsole interactiveConsole, String input);
}
