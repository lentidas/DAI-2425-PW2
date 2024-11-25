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

public class InfoCommand extends GameCommand {

  // Add handler
  static {
    GameCommand.addFactoryHandler(GameCommandType.INFO, InfoCommand::fromTcpBody);
  }

  public InfoCommand(String puzzle, String category, char[] usedLetters) {
    super(GameCommandType.INFO);
    args.add(puzzle);
    args.add(category);
    args.add(usedLetters);
  }

  public String getPuzzle()
  {
    return (String)args.getFirst();
  }

  public String getCategory()
  {
    return (String)args.get(1);
  }

  public String[] getUsedLetters()
  {
    return ((String[])args.get(2)).clone();
  }

  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if(args.length != 3
    || Arrays.stream(args).anyMatch(Objects::isNull)) {
      throw new InvalidPropertiesFormatException("Command did not receive correct parameters");
    }
    
    return new InfoCommand(args[0], args[1], args[2].toCharArray());
  }
}
