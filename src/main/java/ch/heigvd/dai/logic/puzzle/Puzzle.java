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
import java.util.Random;

/**
 * Implements the logic for a Wheel of Fortune puzzle and contains all the possible puzzles that can
 * be used in the game.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class Puzzle {

  public static final String FinalRoundInitialLetters = "RSTNLE";
  private final PuzzleRecord record;
  private final List<Character> lettersGuessed;
  private final int vowelCost;
  private String currentPuzzleState;

  /**
   * Attribute containing all the possible puzzles that can be used in the game. Puzzles are based
   * on some of the actual Wheel of Fortune puzzles, retrieved from <a
   * href="http://google.com">https://wheeloffortuneanswer.com/</a>.
   */
  private static final PuzzleRecord[] PossiblePuzzles =
      new PuzzleRecord[] {
        new PuzzleRecord("A Bag Full Of Blue M&M's", PuzzleCategory.FOOD),
        new PuzzleRecord("All-Natural Ingredients", PuzzleCategory.FOOD),
        new PuzzleRecord("All-You-Can-Eat Shrimp", PuzzleCategory.FOOD),
        new PuzzleRecord("An Assortment Of Colorful Hard Candy", PuzzleCategory.FOOD),
        new PuzzleRecord("Antipasto Salad With Oil & Vinegar", PuzzleCategory.FOOD),
        new PuzzleRecord("Bacon And Soft-Boiled Eggs", PuzzleCategory.FOOD),
        new PuzzleRecord("Bacon Wrapped Barbecue Shrimp", PuzzleCategory.FOOD),
        new PuzzleRecord("Baked Apples With Cinnamon", PuzzleCategory.FOOD),
        new PuzzleRecord("Banana Pudding", PuzzleCategory.FOOD),
        new PuzzleRecord("Beef & Broccoli Stir Fry", PuzzleCategory.FOOD),
        new PuzzleRecord("Best Pizza In Town", PuzzleCategory.FOOD),
        new PuzzleRecord("Black-Eyed Pea Soup", PuzzleCategory.FOOD),
        new PuzzleRecord("Buttered Croissant With Jam", PuzzleCategory.FOOD),
        new PuzzleRecord("California Roll With Wasabi", PuzzleCategory.FOOD),
        new PuzzleRecord("Champagne & Caviar", PuzzleCategory.FOOD),
        new PuzzleRecord("Chicken & Waffles", PuzzleCategory.FOOD),
        new PuzzleRecord("Chocolate Covered Marshmallows", PuzzleCategory.FOOD),
        new PuzzleRecord("Drizzling Melted Butter On My Food", PuzzleCategory.FOOD),
        new PuzzleRecord("Dulce De Leche On Ice Cream", PuzzleCategory.FOOD),
        new PuzzleRecord("Fresh Fruits & Vegetables", PuzzleCategory.FOOD),
        new PuzzleRecord("Frozen Berries", PuzzleCategory.FOOD),
        new PuzzleRecord("Gingerbread Men Cookies", PuzzleCategory.FOOD),
        new PuzzleRecord("Glass Of Ice Tea", PuzzleCategory.FOOD),
        new PuzzleRecord("Iced Coffee With Cream", PuzzleCategory.FOOD),
        new PuzzleRecord("Large Pizza With Everything On It", PuzzleCategory.FOOD),
        new PuzzleRecord("Light & Fluffy Deep Fried Doughnuts", PuzzleCategory.FOOD),
        new PuzzleRecord("Lime Wedge", PuzzleCategory.FOOD),
        new PuzzleRecord("Lobster Tail & Butter Sauce", PuzzleCategory.FOOD),
        new PuzzleRecord("Maine Lobster", PuzzleCategory.FOOD),
        new PuzzleRecord("Marshmallow Peeps", PuzzleCategory.FOOD),
        new PuzzleRecord("Mashed Potatoes With Chives", PuzzleCategory.FOOD),
        new PuzzleRecord("Peanut Butter & Jelly Sandwich", PuzzleCategory.FOOD),
        new PuzzleRecord("Raspberries, Strawberries, And Cherries", PuzzleCategory.FOOD),
        new PuzzleRecord("Red Sangria With Sweet Orange Slices", PuzzleCategory.FOOD),
        new PuzzleRecord("Sponge Cake Topped With Sugary Icing", PuzzleCategory.FOOD),
        new PuzzleRecord("Sugar-Free Cola", PuzzleCategory.FOOD),
        new PuzzleRecord("Alarm Clock With Nature Sounds", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Antique Clock", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Backyard Furniture", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Bermuda Shorts", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Black-And White Family Photograph", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Cashmere Blanket", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Clear Plastic Shower Curtain", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Creaking Door", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Egyptian Cotton Sheets", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Electrical Outlets", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Family Heirlooms", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Floor-To-Ceiling Views", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Fresh-Smelling Bathroom Drain", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Memory-Foam Pet Mat", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Moisturizing Cream", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Pet's Water Bowl", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Scented Candles", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Stackable Baskets", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("USB Power Adapter", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Welcome Doormat", PuzzleCategory.AROUND_THE_HOUSE),
        new PuzzleRecord("Blackjack", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Bodysurfing", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Bowling", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Bowling Night", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Buying A Vowel", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Deck Of Playing Cards", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Egg Toss", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Etch A Sketch", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Fake Snake Popping Out Of A Nut Can", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Halloween", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Marco! Polo!", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Painting By Numbers", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Playing A Round Of Golf", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Playing Tic-Tac-Toe In The Snow", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Riddles And Jokes", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Scoring The Winning Goal", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Sidewalk Chalk & Hula Hoops", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Skateboarding & Snowboarding", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Snowball Fights", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("The First Game Of The World Cup", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Truth Or Dare & Simon Says", PuzzleCategory.FUN_AND_GAMES),
        new PuzzleRecord("Abbey Library Of St. Gall", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Acropolis Museum In Athens, Greece", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Big Ben", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Boston Harbor", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Castle Of Lisbon", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Colosseum", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Dolby Theater", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Egyptian Pyramids", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Eiffel Tower", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Empire State Building", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Grand Canyon National Park", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Great Wall Of China", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Hawaii Volcanoes National Park", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Hollywood Walk Of Fame", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Joshua Tree National Park", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Lisbon Oceanarium", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Lisbon Portela Airport", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Monument Valley Navajo Tribal Park", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Piccadilly Circus", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Serra De Estrela", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Sidney Harbour", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Stonehenge", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Taj Mahal", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("The Cathedral Of Our Lady Of Chartres", PuzzleCategory.LANDMARKS),
        new PuzzleRecord("Abbeys And Convents", PuzzleCategory.PLACES),
        new PuzzleRecord("Amazing Cities", PuzzleCategory.PLACES),
        new PuzzleRecord("Amazing Water Park", PuzzleCategory.PLACES),
        new PuzzleRecord("Car Show Room", PuzzleCategory.PLACES),
        new PuzzleRecord("Computer-Generated World", PuzzleCategory.PLACES),
        new PuzzleRecord("Exclusive Waterfront Hotel", PuzzleCategory.PLACES),
        new PuzzleRecord("Foreign Trade Routes", PuzzleCategory.PLACES),
        new PuzzleRecord("Fortune Teller's Tent", PuzzleCategory.PLACES),
        new PuzzleRecord("Four-Lane, Heavily-Congested Highway", PuzzleCategory.PLACES),
        new PuzzleRecord("Gourmet Specialty Restaurants", PuzzleCategory.PLACES),
        new PuzzleRecord("Grasslands National Park", PuzzleCategory.PLACES),
        new PuzzleRecord("Historic Downtown District", PuzzleCategory.PLACES),
        new PuzzleRecord("Hogwarts School Of Witchcraft And Wizardry", PuzzleCategory.PLACES),
        new PuzzleRecord("Home To Millions Of People", PuzzleCategory.PLACES),
        new PuzzleRecord("Houses Made Of Concrete & Brick", PuzzleCategory.PLACES),
        new PuzzleRecord("Japanese Garden", PuzzleCategory.PLACES),
        new PuzzleRecord("Jazz Club", PuzzleCategory.PLACES),
        new PuzzleRecord("Korean Barbecue Restaurant", PuzzleCategory.PLACES),
        new PuzzleRecord("Lake Geneva, Switzerland", PuzzleCategory.PLACES),
        new PuzzleRecord("Luxury-Cruise Destination", PuzzleCategory.PLACES),
        new PuzzleRecord("Multipurpose Gym", PuzzleCategory.PLACES),
        new PuzzleRecord("Prime Whale-Watching Site", PuzzleCategory.PLACES),
        new PuzzleRecord("Quaint Harbor Town", PuzzleCategory.PLACES),
        new PuzzleRecord("Rural & Mountainous Region", PuzzleCategory.PLACES),
        new PuzzleRecord("Security Area", PuzzleCategory.PLACES),
        new PuzzleRecord("Underground Cave", PuzzleCategory.PLACES),
        new PuzzleRecord("Where The Sidewalk Ends", PuzzleCategory.PLACES),
        new PuzzleRecord("Your Neighbor's Driveway", PuzzleCategory.PLACES),
        new PuzzleRecord("Zen Garden", PuzzleCategory.PLACES),
        new PuzzleRecord("Africa By Toto", PuzzleCategory.SONGS),
        new PuzzleRecord("All I Want For Christmas Is You By Mariah Carey", PuzzleCategory.SONGS),
        new PuzzleRecord("Another Day In Paradise By Phil Collins", PuzzleCategory.SONGS),
        new PuzzleRecord("Billie Jean By Michael Jackson", PuzzleCategory.SONGS),
        new PuzzleRecord("Call Me By Blondie", PuzzleCategory.SONGS),
        new PuzzleRecord("Cold Heart By Dua Lipa & Elton John", PuzzleCategory.SONGS),
        new PuzzleRecord("Don't Stop The Music By Rihanna", PuzzleCategory.SONGS),
        new PuzzleRecord("Down Under By Men At Work", PuzzleCategory.SONGS),
        new PuzzleRecord("Fields Of Gold By Sting", PuzzleCategory.SONGS),
        new PuzzleRecord("Happy Together By The Turtles", PuzzleCategory.SONGS),
        new PuzzleRecord("Here Comes The Sun By The Beatles", PuzzleCategory.SONGS),
        new PuzzleRecord("Imagine By John Lennon", PuzzleCategory.SONGS),
        new PuzzleRecord("Jammin' By Bob Marley", PuzzleCategory.SONGS),
        new PuzzleRecord("Monster By Lady Gaga", PuzzleCategory.SONGS),
        new PuzzleRecord("My Heart Will Go On By Celine Dion", PuzzleCategory.SONGS),
        new PuzzleRecord("One Love By Bob Marley And The Wailers", PuzzleCategory.SONGS),
        new PuzzleRecord("Royals By Lorde", PuzzleCategory.SONGS),
        new PuzzleRecord("Same Old Love By Selena Gomez", PuzzleCategory.SONGS),
        new PuzzleRecord("Seven Nation Army By The White Stripes", PuzzleCategory.SONGS),
        new PuzzleRecord("Waiting For A Girl Like You By Foreigner", PuzzleCategory.SONGS),
        new PuzzleRecord("We Found Love By Rihanna", PuzzleCategory.SONGS),
        new PuzzleRecord("A Word Search", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Adding A New Show To My Watchlist", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Admiring The Gorgeous Lake", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Applying For A New Job", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord(
            "Asking A Girl If I Can Carry Her Books", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Asking My Girlfriend To Homecoming", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Breaking All The Rules", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Brewing Ice Tea", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord(
            "Carrying My Girlfriend's Books Home From School", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Catching Snowflakes On My Tongue", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Celebrating Chinese New Year", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Dining At A Popular Local Restaurant", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord(
            "Dipping My Toe Into The Mediterranean Sea", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Drinking Champagne", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Drinking Fresh-Squeezed Orange Juice", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Enjoying The Ocean Breeze", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Giving You My Undivided Attention", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Going Away For A Relaxing Weekend", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Having A Good Laugh", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Lying Down", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord(
            "Participating In A National Spelling Bee", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Reading The Local Paper", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Riding Shotgun", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Roasting Hot Dogs Over A Campfire", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord(
            "Running Into A Former High School Classmate", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Saving A Few Dollars", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Sipping Hot Chocolate", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Swimming In A Pool", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Taking A Ferry To A Small Island", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Visiting The North Pole", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Walking Around On A Roof", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Yelling Timber!", PuzzleCategory.WHAT_ARE_YOU_DOING),
        new PuzzleRecord("Zooming Around", PuzzleCategory.WHAT_ARE_YOU_DOING),
      };

  private static final ArrayList<Integer> PlayedPuzzles = new ArrayList<>();

  /**
   * Default constructor. Constructs a new puzzle based on the provided record and initial uncovered
   * letter set.
   *
   * @param record a {@link PuzzleRecord} with the puzzle record
   * @param initialLetters a {@link String} with the initial letters to uncover (e.g., "RSTLNE" for
   *     the final round)
   * @param vowelCost an integer with the cost of guessing a vowel
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
   * Checks whether the provided letter has previously been guessed.
   *
   * @param letter a {@code char} with the letter to check
   * @return {@code true} if the letter has been used, {@code false} otherwise
   */
  public boolean hasLetterBeenGuessed(char letter) {
    return lettersGuessed.contains(letter);
  }

  /**
   * Tries to place a letter in the puzzle.
   *
   * @param letter a {@code char} with the letter to try
   * @return {@code true} if the letter exists in the puzzle, {@code false} otherwise
   */
  public boolean tryGuessLetter(char letter) {
    String upperLetter = Character.toString(letter).toUpperCase();
    boolean letterGuessed = false;
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

      currentPuzzleState = sb.toString();
      letterGuessed = true;
    }

    if (!lettersGuessed.contains(upperLetterChar)) {
      lettersGuessed.add(upperLetterChar);
    }

    return letterGuessed;
  }

  /**
   * Tries to guess the full puzzle.
   *
   * @param fullPuzzle a {@link String} with the full guess
   * @return {@code true} if the guess is correct, {@code false} otherwise
   */
  public boolean guessPuzzle(String fullPuzzle) {
    if (record.puzzle().contentEquals(fullPuzzle.toUpperCase())) {
      currentPuzzleState = fullPuzzle;
      return true;
    }

    return false;
  }

  /**
   * Gets the number of times the provided letter appears in the full puzzle.
   *
   * <p>NOTE: If you need to check whether a letter has been guessed yet, use {@link
   * Puzzle#hasLetterBeenGuessed(char)}.
   *
   * @param letter a {@code char} with the letter to search
   * @return an integer with number of times the character appears (0 means it doesn't appear, or
   *     hasn't been guessed yet)
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
   * yet.
   *
   * @return a {@link String} representing the current puzzle state
   */
  public String getCurrentPuzzleState() {
    return currentPuzzleState;
  }

  /**
   * Returns the current puzzle's category.
   *
   * @return a {@link PuzzleCategory} with the puzzle's category
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

  /**
   * Returns the full puzzle string.
   *
   * @return a {@link String} with the full puzzle
   */
  public String getFullPuzzle() {
    return record.puzzle();
  }

  /**
   * Returns the cost of guessing a vowel.
   *
   * @return an integer with the cost of guessing a vowel
   */
  public int getVowelCost() {
    return vowelCost;
  }

  /**
   * Creates a new puzzle with a random index from the list of possible puzzles.
   *
   * @param initialLetters a {@link String} with the initial letters to uncover
   * @param vowelCost an integer with the cost of guessing a vowel
   * @return a new {@link Puzzle} with the random puzzle
   */
  public static Puzzle createNewPuzzle(String initialLetters, int vowelCost) {
    Random random = new Random();
    int nextIndex = 0;
    boolean validIndex = false;

    if (PlayedPuzzles.size() == PossiblePuzzles.length) {
      throw new RuntimeException("No more puzzles available");
    }

    while (!validIndex) {
      nextIndex = random.nextInt(PossiblePuzzles.length);
      validIndex = !PlayedPuzzles.contains(nextIndex);
    }

    PlayedPuzzles.add(nextIndex);
    return new Puzzle(PossiblePuzzles[nextIndex], initialLetters, vowelCost);
  }
}
