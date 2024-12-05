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

package ch.heigvd.dai.logic;

public enum PlayerState {
  WAIT_FOR_USERNAME,
  WAIT_IN_LOBBY,
  WAIT_FOR_TURN,
  WAIT_FOR_GUESS,
  WAIT_FOR_VOWEL,
  WAIT_FOR_FILL,
  WAIT_FOR_ENDING,
  WAIT_FOR_LAST_TURN,
  SEND_LETTERS,
  CHILLING,
  SECOND_GUESS_PHASE,
  DISCONNECTED
}
