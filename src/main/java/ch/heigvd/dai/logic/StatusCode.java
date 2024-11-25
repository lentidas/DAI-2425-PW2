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

public enum StatusCode {
  OK, /* 0 */
  KO,
  WRONG_FORMAT,
  PLAYER_JOINED,
  PLAYER_QUIT,
  GAME_START,
  LETTER_EXISTS,
  LETTER_MISSING,
  TIMEOUT,
  ALREADY_TRIED,
  WRONG_ANSWER, /* 10 */
  RIGHT_ANSWER,
  DUPLICATE_NAME,
  CLOSING,
  NO_FUNDS,
  SKIP,
  FULL /* 16 */
}
