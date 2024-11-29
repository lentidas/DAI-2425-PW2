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

public class Wedge {

  private final WedgeType type;
  private final int moneyWon;

  public Wedge(WedgeType type, int moneyWon) {
    this.type = type;
    this.moneyWon = moneyWon;
  }

  public boolean skipsATurn() {
    return type == WedgeType.LOSE_A_TURN;
  }

  public boolean bankruptsPlayer() {
    return type == WedgeType.BANKRUPT;
  }

  public int getMoneyWon() {
    return moneyWon;
  }

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
