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

public class VowelCommand extends GameCommand {

  public VowelCommand(char vowel) {
    super(GameCommandType.VOWEL);
    args.add(vowel);
  }

  public char getVowel() {
    return (char) args.getFirst();
  }

  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args
        || args.length != 1
        || Arrays.stream(args).anyMatch(Objects::isNull)
        || args[0].length() != 1) {
      throw new InvalidPropertiesFormatException("Command did not receive the correct parameters");
    }

    char letter = args[0].charAt(0);
    if(!Character.isLetter(letter)) {
      throw new InvalidPropertiesFormatException("Character is not a letter");
    } else if(!isCharAVowel(letter)) {
      throw new InvalidPropertiesFormatException("Letter is a not a vowel");
    }

    return new VowelCommand(args[0].charAt(0));
  }
}
