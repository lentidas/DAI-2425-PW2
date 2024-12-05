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
 * Represents the command that announces to a player that it is their turn to play.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class TurnCommand extends GameCommand {

  /**
   * Default constructor.
   *
   * @param turnMoney an {@code int} representing the money the player can win in this turn
   * @param totalMoney an {@code int} representing the total money the player has already earned
   */
  public TurnCommand(int turnMoney, int totalMoney) {
    super(GameCommandType.TURN);
    args.add(turnMoney);
    args.add(totalMoney);
  }

  /**
   * Gets the money the player can win in this turn.
   *
   * @return an {@code int} with the money the player can win in this turn (obtained from the
   *     arguments of the command)
   */
  public int getTurnMoney() {
    return (int) args.getFirst();
  }

  /**
   * Gets the total money the player has.
   *
   * @return an {@code int} with the total money the player has (obtained from the arguments of the
   *     command)
   */
  public int getTotalMoney() {
    return (int) args.get(1);
  }

  /**
   * Parses the arguments of the command from a TCP message.
   *
   * @param args a {@link String} array with the arguments of the command
   * @return a {@link TurnCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args || args.length != 2 || Arrays.stream(args).anyMatch(Objects::isNull)) {
      throw new InvalidPropertiesFormatException(
          "[TurnCommand] Command did not receive the correct parameters");
    }

    int turnMoney;
    int totalMoney;
    try {
      turnMoney = Integer.parseInt(args[0]);
      totalMoney = Integer.parseInt(args[1]);
    } catch (NumberFormatException e) {
      throw new InvalidPropertiesFormatException("[TurnCommand] Invalid turn or total money");
    }

    return new TurnCommand(turnMoney, totalMoney);
  }
}
