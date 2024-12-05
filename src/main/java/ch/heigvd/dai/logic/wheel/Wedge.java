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

package ch.heigvd.dai.logic.wheel;

/**
 * Represents a wedge on the Wheel of Fortune. *
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class Wedge {

  private final WedgeType type;
  private final int moneyWon;

  /**
   * Default constructor.
   *
   * @param type the type of the wedge
   * @param moneyWon the amount of money that can be won with the wedge
   */
  public Wedge(WedgeType type, int moneyWon) {
    this.type = type;
    this.moneyWon = moneyWon;
  }

  /**
   * Checks if wedge is of type {@link WedgeType#LOSE_A_TURN}.
   *
   * @return {@code true} if the wedge is of type {@link WedgeType#LOSE_A_TURN}, {@code false}
   *     otherwise
   */
  public boolean skipsATurn() {
    return type == WedgeType.LOSE_A_TURN;
  }

  /**
   * Checks wedge is of type {@link WedgeType#BANKRUPT}.
   *
   * @return {@code true} if the wedge is of type {@link WedgeType#BANKRUPT}, {@code false}
   *     otherwise
   */
  public boolean bankruptsPlayer() {
    return type == WedgeType.BANKRUPT;
  }

  /**
   * Gets the amount of money that can be won with the wedge.
   *
   * @return an integer representing the amount of money that can be won with the wedge
   */
  public int getMoneyWon() {
    return moneyWon;
  }

  /**
   * Method to print the wedge type as a {@link String}.
   *
   * @return a {@link String} representing the wedge type
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    switch (type) {
      case MONEY -> sb.append("Money wedge for ").append(moneyWon);

      case BANKRUPT -> sb.append("Bankrupt wedge");

      case LOSE_A_TURN -> sb.append("Lose a turn wedge");

      default -> sb.append("Unknown wedge");
    }

    return sb.toString();
  }
}
