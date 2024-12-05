/*
 * Wheel Of Fortune - a Java server/client implementation of the television game
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

package ch.heigvd.dai.network;

import ch.heigvd.dai.logic.commands.GameCommand;
import ch.heigvd.dai.logic.commands.GameCommandType;
import ch.heigvd.dai.logic.commands.HostCommand;
import ch.heigvd.dai.logic.commands.StatusCommand;
import com.google.common.net.HostAndPort;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketClient extends SocketAbstract {
  // TODO Consider if this should be final or not.
  // TODO Comment that this is an attribute because the HELP is dynamic depending on the available
  //  commands.
  // TODO Find a proper way to thread-safe this if we make it dynamic after all
  private final HashSet<GameCommandType> availableCommands = new HashSet<>();

  private final AtomicBoolean inputBlocked = new AtomicBoolean(false);
  private final AtomicBoolean expectedQuit = new AtomicBoolean(false);
  private final Semaphore quit = new Semaphore(0);

  public SocketClient(HostAndPort hostAndPort)
      throws NullPointerException, IllegalArgumentException, UnknownHostException {
    super(hostAndPort);

    // Prepare a list of available commands for the first run.
    // FIXME Adjust this. Temporarily, this adds all the possible commands possible for the
    //  client. For now, only used for the help.
    availableCommands.addAll(
        Arrays.asList(
            GameCommandType.JOIN,
            GameCommandType.GO,
            GameCommandType.FILL,
            GameCommandType.GUESS,
            GameCommandType.VOWEL,
            GameCommandType.LETTERS,
            GameCommandType.HOST,
            GameCommandType.HELP,
            GameCommandType.QUIT));
  }

  class InputReaderHandler implements Runnable {
    private final Socket socket;

    InputReaderHandler(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      try (socket;
          Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
          BufferedWriter out = new BufferedWriter(writer);
          Reader inputReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
          BufferedReader bir = new BufferedReader(inputReader)) {

        help();

        // TODO Implement this wait through the availableCommands attribute if it is empty.
        // Listen for command line inputs while the socket is open.
        while (!socket.isClosed()) {

          // Block the thread if there is no command possible.
          try {
            if (inputBlocked.get()) {
              System.out.println("\nWaiting for server response...");
              // Call to Thread.sleep() inside a loop is considered busy-waiting, but I've yet to
              // learn proper thread synchronization in Java :)
              while (inputBlocked.get()) {
                Thread.sleep(200);
              }
            }
            System.out.println("\nReady for command!");
          } catch (InterruptedException e) {
            // TODO Decide what to do with this exception
          }

          // Read user input from console.
          String userInput = bir.readLine();

          // Parse command from user and throw an error if not supported.
          GameCommand command;
          userInput = userInput.trim();
          try {
            command = GameCommand.fromTcpBody(userInput);
          } catch (InvalidPropertiesFormatException ignore) {
            System.out.println();
            System.out.println("Unrecognized/Invalid command! Please try again.");
            System.out.println("[DEBUG] Command: " + userInput);
            help();
            continue;
          }

          try {
            // Prepare the request to send to the server.
            String request = null;

            // Perform action appropriate to each command
            switch (command.getType()) {
              case JOIN, GO, FILL, GUESS, VOWEL, LETTERS -> request = command.toTcpBody();
              case HOST -> {
                System.out.println(((HostCommand) command).getHost());
                continue;
              }
              case HELP -> {
                help();
                continue;
              }
              case QUIT -> {
                expectedQuit.set(true);
                socket.close();
                continue;
              }
              default -> {
                System.out.println("Unrecognized/Invalid command! Please try again.");
                help();
                continue;
              }
            }

            // Send request to server if it is non-null.
            if (request != null) {
              out.write(request + END_OF_LINE);
              out.flush();
            }
          } catch (Exception e) {
            // Send exception upwards on the call stack.
            throw new RuntimeException(
                "[InputReaderHandler] Exception: " + e);
          }
        } // end of while (!socket.isClosed())

        // Release the parent thread to print the quit message and finish the program.
        quit.release();

      } catch (IOException e) {
        // Send 2 releases to make sure the parent thread is able to quit. Probably unnecessary
        // since we send the exception upwards in the call stack.
        quit.release(2);
        throw new RuntimeException("[InputReaderHandler] IOException: " + e);
      }
    }
  }

  class ServerResponseHandler implements Runnable {
    private final Socket socket;

    ServerResponseHandler(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      try (socket;
          Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
          BufferedReader in = new BufferedReader(reader)) {

        // Listen for server responses while the socket is open.
        while (!socket.isClosed()) {

          // Read response from server and parse it.
          String serverResponse = in.readLine();

          // If serverResponse is null, the server has disconnected.
          if (serverResponse == null) {
            socket.close();
            continue;
          }

          // Parse the response from the server.
          serverResponse = serverResponse.trim();
          GameCommand response = null;
          try {
            response = GameCommand.fromTcpBody(serverResponse);
          } catch (InvalidPropertiesFormatException format) {
            // Response is malformed (not a valid command).
            System.out.println();
            System.out.println("Invalid command sent by server, ignore...");
            // TODO Disable this debug message conditionally
            System.out.println("[DEBUG] Command received: " + serverResponse);
            continue;
          }

          System.out.println(); // Print an empty line to improve readability on the console.
          switch (response.getType()) {
            // TODO Remove the commented cases that are not used.
            case STATUS -> {
              switch (((StatusCommand) response).getStatus()) {
                case OK -> System.out.println("OK!");
                case KO ->
                    System.out.println(
                        // FIXME Is there a way to have a less generic message?
                        "Something is not OK!");
                case FULL -> {
                  // We should never arrive at the FULL case, because we are limited by the number
                  // of client threads in the SocketServer class. It is conserved here for
                  // consistency with the protocol specification.
                  System.out.println("Party is full and there is no place to join.");
                  socket.close();
                }
                case PLAYER_JOINED -> System.out.println("Another player joined the game!");
                case PLAYER_QUIT -> System.out.println("Another player quit the game...");
                case DUPLICATE_NAME -> System.out.println("Username is already in use.");
                case WRONG_ANSWER -> System.out.println("Your guess is WRONG!");
                case RIGHT_ANSWER -> System.out.println("CONGRATULATIONS! Your guess is RIGHT!");
                  // case TIMEOUT -> System.out.println("TIMEOUT! Answer faster next time!");
                case LETTER_MISSING ->
                    System.out.println("MISSING! The puzzle does not contain that letter.");
                case LETTER_EXISTS -> System.out.println("RIGHT! The puzzle contains that letter.");
                  // case WRONG_FORMAT -> System.out.println("You can only guess consonants.");
                case ALREADY_TRIED -> System.out.println("That letter was already tried before.");
                  // case NO_FUNDS ->
                  //     System.out.println("You do not have enough money to buy that letter.");
                case BANKRUPT ->
                    System.out.println(
                        "Bad luck... The wheel says you're BANKRUPT! You lose the turn.");
                case LOST_A_TURN ->
                    System.out.println("Bad luck... The wheel says you miss the next turn!");
              }
            }

            case END -> {
              List<Object> args = response.getArgs();
              Iterator<Object> iter = args.iterator();
              System.out.println("Game ended!");
              System.out.println("*** WINNER: " + iter.next() + " ***");
              System.out.println("=== GAME RESULTS ===");
              for (int i = 0; iter.hasNext(); ++i) {
                if (i % 2 == 0) { // If even, print username.
                  System.out.print(iter.next());
                } else { // Else, print money with a new line.
                  System.out.println(" - " + iter.next());
                }
              }
              System.out.println("Please use JOIN to play again or QUIT to go home.");
            }

            case INFO -> {
              System.out.println("=== CURRENT PUZZLE ===");
              List<Object> args = response.getArgs();
              Iterator<Object> iter = args.iterator();
              String puzzle = (String) iter.next();
              String category = (String) iter.next();
              String usedLetters = (String) iter.next();
              System.out.println("Category: " + category);
              System.out.println("Puzzle: " + puzzle);
              System.out.println("Used letters: " + usedLetters);
            }

            case LAST -> {
              System.out.println("CONGRATULATIONS! You go to the last round!");
              System.out.println("=== LAST PUZZLE ===");
              List<Object> args = response.getArgs();
              Iterator<Object> iter = args.iterator();
              String timeout = (String) iter.next();
              String puzzle = (String) iter.next();
              String category = (String) iter.next();
              System.out.println("Category: " + category);
              System.out.println("Puzzle: " + puzzle);
              System.out.println("Timeout: " + timeout + "seconds");
              System.out.println("What letters do you want to add?");
            }

            case LOBBY -> {
              System.out.println("=== LOBBY ===");
              // FIXME does not work yet
              // for (Object player : response.getArgs()) {
              //   System.out.println((String) player);
              // }
            }

            case START -> {
              List<Object> args = response.getArgs();
              Iterator<Object> iter = args.iterator();
              String round = Character.toString((Integer) iter.next());
              String puzzle = (String) iter.next();
              String category = (String) iter.next();
              System.out.println("=== ROUND #" + round + " ===");
              System.out.println("Category: " + category);
              System.out.println("Puzzle: " + puzzle);
            }

            case TURN -> {
              System.out.println("It's your turn!");
              System.out.println(
                  "You're in luck! The wheel gave you "
                      + response.getArgs().getFirst()
                      + "$ for this round.");
              System.out.println("Earned money overall: " + response.getArgs().getLast() + "$");
            }

              // FIXME This command is not really used by the server...
            case WINNER -> {
              System.out.println("WINNER"); // TODO Remove debug line
            }

            default -> System.out.println("Invalid/unknown command sent by server, ignore.");
          }
        } // end of while (!socket.isClosed())

        // Release the parent thread to print the quit message and finish the program.
        quit.release();

      } catch (IOException e) {
        // Exit the thread cleanly because the socket was probably closed by the other handler.
        if (expectedQuit.get()) {
          // Release the parent thread to print the quit message and finish the program.
          quit.release();
        }
        // Else this was a true IOException.
        else {
          System.out.println("[ServerResponseHandler] IOException: " + e);
        }
      }
    }
  }

  @Override
  public void run() {

    System.out.println(
        "[Client] Connecting to server at " + getHost().getHostAddress() + ":" + getPort() + "...");

    try (Socket socket = new Socket(getHost(), getPort());
        ExecutorService executor = Executors.newFixedThreadPool(2)) {
      // Print message to acknowledge successful connection to the server.
      System.out.println(
          "[Client] Connected to server at " + getHost().getHostAddress() + ":" + getPort());

      // Create child threads, one for getting user inputs, another for listening to the server
      // responses.
      executor.submit(new ServerResponseHandler(socket));
      executor.submit(new InputReaderHandler(socket));

      // Block the parent thread until released by one of the children.
      quit.acquire(2);

      // Print the quit message.
      System.out.println("[Client] Closing connection and quitting...");
    } catch (IOException e) {
       System.out.println("[Client] IOException: " + e);
    } catch (InterruptedException e) {
      System.out.println("[Client] InterruptedException: " + e);
    }
  }

  private void help() {
    System.out.println();
    System.out.println("=== AVAILABLE COMMANDS ===");
    for (GameCommandType command : availableCommands) {
      switch (command) {
        case JOIN ->
            System.out.print(
                """
                * JOIN - Provide a username and join the game.
                  - JOIN user1
                  - JOIN "My Name"
                """);
        case GO ->
            System.out.print(
                """
               * GO - Start the game right away without waiting for the maximum number of players.
               """);
        case GUESS ->
            System.out.print(
                """
              * GUESS - Guess a possible consonant in the puzzle (use capital letters only).
                - GUESS P
                - GUESS G
              """);
        case VOWEL ->
            System.out.print(
                """
            * VOWEL - Buy a vowel (AEIOU) if you have the money to buy one (use capital letters only).
              - GUESS A
              - GUESS E
            """);
        case FILL ->
            System.out.print(
                """
            * FILL - Try to guess the full puzzle (should be in capitals and between quotes.
              - FILL "THIS IS A PUZZLE"
            """);
        case SKIP -> System.out.print("""
            * SKIP - Skip your turn.
            """);
        case LETTERS -> System.out.print("""
            * LETTERS - TODO
            """); // TODO
        case HELP ->
            System.out.print(
                """
                * HELP - Print this help message (the message is dynamic depending on the available commands).
                """);
        case QUIT ->
            System.out.print(
                """
                * QUIT - Quit the game and disconnect from the server.
                """);
      }
    }
  }

  // TODO Decide if we will still use this function
  // private void computeAvailableCommands(
  //     GameCommand previousRequest, GameCommand serverResponse) {
  //   availableCommands.clear();
  //
  //   switch (previousRequest.getType()) {
  //
  //     case JOIN -> {
  //       availableCommands.addAll(Arrays.asList(GameCommandType.HOST, GameCommandType.HELP,
  // GameCommandType.QUIT));
  //       if (serverResponse.getType() == GameCommandType.STATUS && ((StatusCommand)
  // serverResponse).getStatus() == StatusCode.OK) {
  //         availableCommands.add(GameCommandType.GO);
  //       } else {
  //         availableCommands.add(GameCommandType.JOIN);
  //       }
  //     }
  //
  //     default -> {
  //       switch (serverResponse.getType()) {
  //         case TURN -> {
  //           availableCommands.addAll(Arrays.asList(
  //               GameCommandType.HOST,
  //               GameCommandType.QUIT,
  //               GameCommandType.HELP,
  //               GameCommandType.GUESS,
  //               GameCommandType.FILL));
  //         }
  //       }
  //     }
  //   }
  // }
}
