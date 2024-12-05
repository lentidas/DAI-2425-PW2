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

package ch.heigvd.dai;

import ch.heigvd.dai.logic.PlayerState;
import ch.heigvd.dai.logic.server.wheel.Wedge;

public class Player {

  private final String username;
  private int turnOrder;
  private int money;
  private boolean isLastRoundPlayer;
  private Wedge currentWedge;
  private PlayerState state;

  public Player(String username) {
    this.username = username;
    this.turnOrder = -1;
    money = 0;
    isLastRoundPlayer = false;
    currentWedge = null;
    state = PlayerState.CHILLING;
  }

  public void setIsLastRoundPlayer(boolean lastRoundPlayer) {
    isLastRoundPlayer = lastRoundPlayer;
  }

  public void setTurnOrder(int turnOrder) {
    this.turnOrder = turnOrder;
  }

  public void incrementMoney(int money) {
    // Avoid overflow
    if (Integer.MAX_VALUE - money < this.money) {
      this.money = Integer.MAX_VALUE;
    } else {
      this.money += money;
    }
  }

  public void decrementMoney(int money) {
    // Avoid negative amounts
    if (this.money - money < 0) {
      this.money = 0;
    } else {
      this.money += money;
    }
  }

  public void goBankrupt() {
    this.money = 0;
  }

  public int getMoney() {
    return money;
  }

  public int getTurnOrder() {
    return turnOrder;
  }

  public boolean isLastRoundPlayer() {
    return isLastRoundPlayer;
  }

  public String getUsername() {
    return username;
  }

  public void setCurrentWedge(Wedge currentWedge) {
    this.currentWedge = currentWedge;
  }

  public Wedge getCurrentWedge() {
    return currentWedge;
  }

  public PlayerState getState() {
    return state;
  }

  public void setState(PlayerState state) {
    this.state = state;
  }

  @Override
  public String toString() {
    return "Player " + username;
  }
}
