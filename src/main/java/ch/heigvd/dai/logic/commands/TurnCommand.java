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

public class TurnCommand extends GameCommand {

  public TurnCommand(int turnMoney, int totalMoney) {
    super(GameCommandType.TURN);
    args.add(turnMoney);
    args.add(totalMoney);
  }

  public int getTurnMoney() {
    return (int) args.getFirst();
  }

  public int getTotalMoney() {
    return (int) args.get(1);
  }

  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args
        || args.length != 2
        || Arrays.stream(args).anyMatch(Objects::isNull)
        || args[0].length() != 2
        || args[1].length() != 4) {
      throw new InvalidPropertiesFormatException("Command did not receive the correct parameters");
    }

    int turnMoney = 0;
    for (int i = 0; i < args[0].length(); i++) {
      turnMoney <<= 8;
      turnMoney |= args[0].charAt(i);
    }

    int totalMoney = 0;
    for (int i = 0; i < args[1].length(); i++) {
      totalMoney <<= 8;
      totalMoney |= args[1].charAt(i);
    }

    return new TurnCommand(turnMoney, totalMoney);
  }
}
