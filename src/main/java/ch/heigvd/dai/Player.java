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
import ch.heigvd.dai.logic.wheel.Wedge;

/**
 * Implements a Wheel of Fortune with the required attributes to describe the state of the player in
 * the current game.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class Player {

  private final String username;
  private int turnOrder;
  private int money;
  private boolean isLastRoundPlayer;
  private Wedge currentWedge;
  private PlayerState state;

  /**
   * Default constructor.
   *
   * @param username a String containing the username of the player
   */
  public Player(String username) {
    this.username = username;
    this.turnOrder = -1;
    money = 0;
    isLastRoundPlayer = false;
    currentWedge = null;
    state = PlayerState.CHILLING;
  }

  /**
   * Getter for the username of the player.
   *
   * @return a {@link String} with username of the player
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the boolean that says this player is the winner of the first rounds and goes to the final
   * round.
   *
   * @param lastRoundPlayer a boolean at {@code true} if the player is going to the last round
   */
  public void setIsLastRoundPlayer(boolean lastRoundPlayer) {
    isLastRoundPlayer = lastRoundPlayer;
  }

  /**
   * TODO
   *
   * @return
   */
  public int getTurnOrder() {
    return turnOrder;
  }

  /**
   * Getter for the boolean that says the user goes to the last round.
   *
   * @return the value stored in {@link Player#isLastRoundPlayer}
   */
  public boolean isLastRoundPlayer() {
    return isLastRoundPlayer;
  }

  /**
   * TODO
   *
   * @param turnOrder
   */
  public void setTurnOrder(int turnOrder) {
    this.turnOrder = turnOrder;
  }

  /**
   * Increments the amount of money the player as accumulated during the game.
   *
   * @param money an integer with the amount the player has earned
   */
  public void incrementMoney(int money) {
    // Avoid overflow
    if (Integer.MAX_VALUE - money < this.money) {
      this.money = Integer.MAX_VALUE;
    } else {
      this.money += money;
    }
  }

  /**
   * Decreases the amount of money the player as accumulated during the game.
   *
   * @param money an integer with the amount the player has lost/paid
   */
  public void decrementMoney(int money) {
    // Avoid negative amounts
    if (this.money - money < 0) {
      this.money = 0;
    } else {
      this.money += money;
    }
  }

  /** Resets the money of the user at 0 (bankrupts the user). */
  public void goBankrupt() {
    this.money = 0;
  }

  /**
   * Getter for the {@link Player#money} attribute.
   *
   * @return an integer with the amount of money the player has earned
   */
  public int getMoney() {
    return money;
  }

  /**
   * Setter to set the current wedge that the player got from the wheel.
   *
   * @param currentWedge a {@link Wedge} that was randomly chosen to attribute to the user
   */
  public void setCurrentWedge(Wedge currentWedge) {
    this.currentWedge = currentWedge;
  }

  /**
   * Getter to get the current wedge that the player got from the wheel.
   *
   * @return a {@link Wedge} that is stored in the attribute {@link Player#currentWedge}
   */
  public Wedge getCurrentWedge() {
    return currentWedge;
  }

  /**
   * Setter to set the current state of player in the current game (p.e. it is the player's turn or
   * the player is waiting for other players to finish their turn).
   *
   * @param state the {@link PlayerState} that the player will be in
   */
  public void setState(PlayerState state) {
    this.state = state;
  }

  /**
   * Getter to get the current state the player is in (p.e. it is the player's turn or the player is
   * waiting for other players to finish their turn).
   *
   * @return
   */
  public PlayerState getState() {
    return state;
  }

  /**
   * Method to output the {@link String} value of the instance, to be used in console outputs.
   *
   * @return a {@link String} containing the username of the player
   */
  @Override
  public String toString() {
    return "Player " + username;
  }
}
