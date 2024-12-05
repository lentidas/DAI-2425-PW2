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

public class LastCommand extends GameCommand {

  public LastCommand(int timeout, String puzzle, String category, String initialLetters) {
    super(GameCommandType.LAST);
    args.add(timeout);
    args.add(puzzle);
    args.add(category);
    args.add(initialLetters);
  }

  public int getTimeout() {
    return (int) args.getFirst();
  }

  public String getPuzzle() {
    return (String) args.get(1);
  }

  public String getCategory() {
    return (String) args.get(2);
  }

  public String getInitialLetters() {
    return (String) args.get(3);
  }

  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args || args.length != 4 || Arrays.stream(args).anyMatch(Objects::isNull)) {
      throw new InvalidPropertiesFormatException("Command did not receive the right parameters");
    }

    int timeout;
    try {
      timeout = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      throw new InvalidPropertiesFormatException("Timeout is not a valid number");
    }

    return new LastCommand(timeout, args[1], args[2], args[3]);
  }
}
