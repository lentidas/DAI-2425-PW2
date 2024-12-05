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
 * Represents the command that announces the selection of a vowel by a player.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class VowelCommand extends GameCommand {

  /**
   * Default constructor.
   *
   * @param vowel a {@code char} that is the vowel selected by the player
   */
  public VowelCommand(char vowel) {
    super(GameCommandType.VOWEL);
    args.add(vowel);
  }

  /**
   * Gets the vowel selected by the player, as stored in this object.
   *
   * @return a {@code char} with the vowel selected by the player (obtained from the arguments of
   *     the command)
   */
  public char getVowel() {
    return (char) args.getFirst();
  }

  /**
   * Parses the arguments of the command from a TCP message.
   *
   * @param args a {@link String} array with the arguments of the command
   * @return a {@link VowelCommand} with the parsed arguments
   * @throws InvalidPropertiesFormatException if the arguments are invalid for this command
   */
  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args
        || args.length != 1
        || Arrays.stream(args).anyMatch(Objects::isNull)
        || args[0].length() != 1) {
      throw new InvalidPropertiesFormatException(
          "[VowelCommand] Command did not receive the correct parameters");
    }

    char letter = args[0].charAt(0);
    if (!Character.isLetter(letter)) {
      throw new InvalidPropertiesFormatException("[VowelCommand] Character is not a letter");
    } else if (!isCharAVowel(letter)) {
      throw new InvalidPropertiesFormatException("[VowelCommand] Letter is a not a vowel");
    }

    return new VowelCommand(args[0].charAt(0));
  }
}
