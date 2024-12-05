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
 * Represents the command the server sends to announce the end of the game.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class EndCommand extends GameCommand {

  /**
   * Default constructor.
   *
   * @param winningPlayer the name of the player that won the game
   * @param players the names of the players
   * @param money the money of the players
   */
  public EndCommand(String winningPlayer, String[] players, int[] money) {
    super(GameCommandType.END);
    args.add(winningPlayer);
    for (int i = 0; i < players.length; i++) {
      args.add(players[i]);
      args.add(money[i]);
    }
  }

  /**
   * Parses the arguments of the command from a TCP message.
   *
   * @param args a {@link String} array with the arguments of the command
   * @return a {@link EndCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args || args.length % 2 == 0) {
      throw new InvalidPropertiesFormatException(
          "Command did not receive enough, or a wrong number of arguments.");
    }

    int playerCount = (args.length - 1) / 2;
    String[] winningPlayers = new String[playerCount];
    int[] playerMoney = new int[playerCount];

    for (int i = 0; i < playerCount; i++) {
      winningPlayers[i] = args[i * 2 + 1];
      playerMoney[i] = Integer.parseInt(args[i * 2 + 2]);
    }

    return new EndCommand(args[0], winningPlayers, playerMoney);
  }
}
