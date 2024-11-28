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

import ch.heigvd.dai.logic.StatusCode;
import java.util.Arrays;
import java.util.InvalidPropertiesFormatException;
import java.util.Objects;

public class StatusCommand extends GameCommand {

  public StatusCommand(StatusCode status) {
    super(GameCommandType.STATUS);
    args.add(status);
  }

  public StatusCode getStatus()
  {
    return (StatusCode)args.getFirst();
  }

  public static GameCommand fromTcpBody(String[] args) throws IllegalArgumentException, InvalidPropertiesFormatException {
    if(args.length != 1
    || Arrays.stream(args).anyMatch(Objects::isNull)) {
      throw new InvalidPropertiesFormatException("Command does not take arguments");
    }

    StatusCode status;
    try
    {
      status = StatusCode.valueOf(args[0]);
    } catch(IllegalArgumentException e)
    {
      throw new IllegalArgumentException("Unknown status code");
    }

    return new StatusCommand(status);
  }
}
