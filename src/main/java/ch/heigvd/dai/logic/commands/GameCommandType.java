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

/** Enumerates the types of commands that can be sent to/from the server. */
public enum GameCommandType {
  END, /* 0 */
  FILL,
  GUESS,
  INFO,
  LAST,
  LOBBY,
  JOIN,
  QUIT,
  ROUND,
  START,
  STATUS, /* 10 */
  TURN,
  VOWEL,
  WINNER,
  GO,
  SKIP, /* 15 */
  LETTERS,
  HELP,
  HOST
}
