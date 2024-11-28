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

public class StartCommand extends GameCommand {

  public StartCommand(int roundNumber, String puzzle, String category) {
    super(GameCommandType.START);
    args.add(roundNumber);
    args.add(puzzle);
    args.add(category);
  }

  public int getRoundNumber()
  {
    return (int)args.getFirst();
  }

  public String getPuzzle()
  {
    return (String)args.get(1);
  }

  public String getCategory()
  {
    return (String)args.get(2);
  }

  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if(args.length != 3
    || Arrays.stream(args).anyMatch(Objects::isNull)
    || args[0].length() != 1) {
      throw new InvalidPropertiesFormatException("Command did not receive the right parameters");
    }
    
    return new StartCommand(args[0].charAt(0), args[1], args[2]);
  }
}
