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

package ch.heigvd.dai.logic.puzzle;

import java.util.ArrayList;
import java.util.List;

public class Puzzle {

  public static final String FinalRoundInitialLetters = "RSTNLE";
  private final PuzzleRecord record;
  private final List<Character> lettersGuessed;
  private final int vowelCost;
  private String currentPuzzleState;

  /**
   * Constructs a new puzzle based on the provided record and initial uncovered letter set
   *
   * @param record Puzzle record
   * @param initialLetters Letters to be uncovered from the beginning
   */
  public Puzzle(PuzzleRecord record, String initialLetters, int vowelCost) {
    String upperInitialLetters = initialLetters.toUpperCase();
    lettersGuessed = new ArrayList<>();
    this.record = record;
    this.vowelCost = vowelCost;

    for (char letter : upperInitialLetters.toCharArray()) {
      lettersGuessed.add(letter);
    }

    StringBuilder sb = new StringBuilder();
    for (char letter : record.puzzle().toCharArray()) {
      if (Character.isLetter(letter) && !upperInitialLetters.contains(Character.toString(letter))) {
        sb.append('*');
      } else {
        sb.append(letter);
      }
    }

    currentPuzzleState = sb.toString();
  }

  /**
   * Checks whether the provided letter has previously been guessed
   *
   * @param letter Letter to check
   * @return True if previously checked, false if not
   */
  public boolean hasLetterBeenGuessed(char letter) {
    return lettersGuessed.contains(letter);
  }

  /**
   * Tries to place a letter in the puzzle
   *
   * @param letter Letter guessed
   * @return True if letter exists in puzzle, false if not
   */
  public boolean tryGuessLetter(char letter) {
    String upperLetter = Character.toString(letter).toUpperCase();
    char upperLetterChar = upperLetter.charAt(0);

    if (!hasLetterBeenGuessed(upperLetterChar) && record.puzzle().contains(upperLetter)) {
      StringBuilder sb = new StringBuilder();
      String fullPuzzle = record.puzzle();
      for (int i = 0; i < currentPuzzleState.length(); i++) {
        if (fullPuzzle.charAt(i) == upperLetterChar) {
          sb.append(upperLetter);
        } else {
          sb.append(currentPuzzleState.charAt(i));
        }
      }

      lettersGuessed.add(upperLetterChar);
      currentPuzzleState = sb.toString();
      return true;
    }

    return false;
  }

  /**
   * Tries to guess the full puzzle
   *
   * @param fullPuzzle Full puzzle guess
   * @return True if guess is right, false if not
   */
  public boolean guessPuzzle(String fullPuzzle) {
    if (record.puzzle().contentEquals(fullPuzzle.toUpperCase())) {
      currentPuzzleState = fullPuzzle;
      return true;
    }

    return false;
  }

  /**
   * Gets the number of times the provided letter appears in the full puzzle
   *
   * @implNote If you need to check whether a letter has been guessed yet, use hasLetterBeenGuessed
   * @param letter Letter to search
   * @return Number of times it appears. 0 means it doesn't appear, or hasn't been guessed
   */
  public int getLetterCount(char letter) {
    int count = 0;
    String upperLetter = Character.toString(letter).toUpperCase();
    for (int i = 0; i < record.puzzle().length(); i++) {
      if (record.puzzle().charAt(i) == upperLetter.charAt(0)) {
        ++count;
      }
    }
    return count;
  }

  /**
   * Returns the current state of the puzzle, with * in place of the letters that haven't been found
   * yet
   *
   * @return The current state of the puzzle
   */
  public String getCurrentPuzzleState() {
    return currentPuzzleState;
  }

  /**
   * Returns the current puzzle's category
   *
   * @return The category
   */
  public PuzzleCategory getCategory() {
    return record.category();
  }

  public char[] getGuessedLetters() {
    char[] charGuessed = new char[lettersGuessed.size()];

    for (int i = 0; i < lettersGuessed.size(); i++) {
      charGuessed[i] = lettersGuessed.get(i);
    }

    return charGuessed;
  }

  public String getFullPuzzle() {
    return record.puzzle();
  }

  public int getVowelCost() {
    return vowelCost;
  }

  public static Puzzle createNewPuzzle(String initialLetters, int vowelCost) {
    return new Puzzle(new PuzzleRecord("Test", PuzzleCategory.FOOD), initialLetters, vowelCost);
  }
}
