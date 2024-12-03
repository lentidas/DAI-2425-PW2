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
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class LettersCommand extends GameCommand {

  public static final int NumberOfLetters = 5;

  public LettersCommand(char[] letters) {
    super(GameCommandType.LETTERS);
    args.add(letters);
  }

  public Character[] getGuessedLetters() {
    char[] letters = ((char[]) args.getFirst());
    Character[] argsCopy = new Character[letters.length];

    for (int i = 0; i < letters.length; ++i) {
      argsCopy[i] = letters[i];
    }
    return argsCopy;
  }

  public boolean hasRepeatedLetters() {
    List<Character> list = List.of(getGuessedLetters());
    Set<Character> set = new HashSet<>(list);
    return set.size() < list.size();
  }

  public boolean hasAnyOf(String letters) {
    for (Object arg : args) {
      for (int j = 0; j < letters.length(); ++j) {
        if (arg.equals(letters.charAt(j))) {
          return true;
        }
      }
    }

    return false;
  }

  public static GameCommand fromTcpBody(String[] args) throws InvalidPropertiesFormatException {
    if (null == args
        || args.length != 1
        || Arrays.stream(args).anyMatch(Objects::isNull)
        || args[0].length() != NumberOfLetters) {
      throw new InvalidPropertiesFormatException("Command did not receive 5 letters");
    }

    char[] letters = new char[NumberOfLetters];
    for (int i = 0; i < NumberOfLetters; i++) {
      letters[i] = args[0].charAt(i);
    }

    return new LettersCommand(letters);
  }
}
