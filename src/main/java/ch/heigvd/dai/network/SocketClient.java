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
import ch.heigvd.dai.utils.CallableInputReader;
import com.google.common.net.HostAndPort;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SocketClient extends SocketAbstract {

  // TODO Consider if this should be final or not.
  // TODO Comment that this is an attribute because the HELP is dynamic depending on the available
  //  commands.
  private final HashSet<GameCommandType> availableCommands = new HashSet<>();

  public SocketClient(HostAndPort hostAndPort)
      throws NullPointerException, IllegalArgumentException, UnknownHostException {
    super(hostAndPort);
  }

  @Override
  public void run() {
    // Prepare a list of available commands for the first run.
    // FIXME Adjust this. Temporarily, this adds all the possible commands possible for the client.
    availableCommands.addAll(
        Arrays.asList(
            GameCommandType.JOIN,
            GameCommandType.GO,
            GameCommandType.FILL,
            GameCommandType.GUESS,
            GameCommandType.VOWEL,
            GameCommandType.HOST,
            GameCommandType.HELP,
            GameCommandType.QUIT));

    System.out.println(
        "[Client] Connecting to server at " + getHost().getHostAddress() + ":" + getPort() + "...");

    try (Socket socket = new Socket(getHost(), getPort());
        Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(reader);
        Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
        BufferedWriter out = new BufferedWriter(writer);
        Reader inputReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
        BufferedReader bir = new BufferedReader(inputReader)) {
      // Print message to acknowledge successful connection to the server.
      System.out.println(
          "[Client] Connected to server at " + getHost().getHostAddress() + ":" + getPort());

      // Print help message.
      help();

      // Run REPL until user quits with command QUIT.
      while (!socket.isClosed()) {

        // Skip the prompt from the client if there is no command possible.
        if (availableCommands.isEmpty()) {
          System.out.println("Waiting for server response...");
        } else {
          // Output prompt.
          System.out.print("> ");

          // Read user input from console.
          String userInput = bir.readLine();

          // TODO Check if we keep this behaviour...
          // If user simply uses ENTER, skip command parsing and go check for a server message.
          // WARNING: this could be blocking if the user repeats this multiple times.
          if (!"".equals(userInput)) {
            // Parse command from user and throw an error if not supported.
            GameCommand command;
            try {
              command = GameCommand.fromTcpBody(userInput.trim());
              // TODO Do not forget to remove this if unnecessary in the end
              if (!availableCommands.contains(command.getType())) {
                throw new InvalidPropertiesFormatException("Invalid command in the current state.");
              }
            } catch (InvalidPropertiesFormatException ignore) {
              System.out.println("Unrecognized/Invalid command! Please try again.");
              help();
              continue;
            }

            try {
              // Prepare the request to send to the server.
              String request = null;

              switch (command.getType()) {
                case JOIN, GO, FILL, GUESS, VOWEL -> request = command.toTcpBody();
                case HOST -> {
                  System.out.println(((HostCommand) command).getHost());
                  continue;
                }
                case HELP -> {
                  help();
                  continue;
                }
                case QUIT -> {
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
              // TODO Consider sending this exception upstream.
              System.out.println("Invalid command. Please try again.");
              continue;
            }
          }
        }

        // Read response from server and parse it.
        String serverResponse = in.readLine();

        // If serverResponse is null, the server has disconnected.
        if (serverResponse == null) {
          socket.close();
          continue;
        }

        // Parse the response from the server.
        GameCommand response = null;
        try {
          response = GameCommand.fromTcpBody(serverResponse.trim());
        } catch (InvalidPropertiesFormatException format) {
          // Response is malformed (not a valid command).
          System.out.println("Invalid command sent by server, ignore.");
          continue;
        }

        switch (response.getType()) {
          case STATUS -> {
            switch (((StatusCommand) response).getStatus()) {
              case OK -> System.out.println("OK!");
              case KO ->
                  System.out.println(
                      // FIXME Is there a way to have a less generic message?
                      "Something is not OK!");
              case FULL -> {
                // We should never arrive at the FULL case, because we are limited by the number of
                // client threads in the SocketServer class. It is conserved here for consistency
                // with the protocol specification.
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
              case BANKRUPT -> System.out.println("Bad luck... The wheel says you're BANKRUPT!");
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
            int round = Integer.parseInt((String) iter.next());
            String puzzle = (String) iter.next();
            String category = (String) iter.next();
            System.out.println("=== ROUND #" + round + " ===");
            System.out.println("Category: " + category);
            System.out.println("Puzzle: " + puzzle);
            // Put the client waiting from a response from the server.
            // availableCommands.clear(); // TODO
          }

          case TURN -> {
            System.out.println("It's your turn!");
            System.out.println(
                "You're in luck! The wheel gave you " + response.getArgs().getFirst() + "€");
            System.out.println("Total money: " + response.getArgs().getLast() + "€");
            // TODO Set available commands
          }

            // FIXME This command is not really used by the server...
          case WINNER -> {
            System.out.println("WINNER"); // TODO Remove debug line
          }

          default -> System.out.println("Invalid/unknown command sent by server, ignore.");
        }
      } // end of while (!socket.isClosed())

      System.out.println("[Client] Closing connection and quitting...");
    } catch (IOException e) {
      // TODO Maybe send this exception upwards on the Stack and manage this over there
      //  else maybe manage the outputs to console here since we also output other things here.
      System.out.println("[Client] IO exception: " + e);
    }
  }

  // TODO Complete these outputs
  private void help() {
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

  // TODO Document that this class is inspired from this article:
  //  https://www.javaspecialists.eu/archive/Issue153-Timeout-on-Console-Input.html
  // TODO Do not forget to remove this if not used
  private String readLine() throws InterruptedException {
    String input = null;
    try (ExecutorService ex = Executors.newSingleThreadExecutor()) {
      try {
        Future<String> result =
            ex.submit(new CallableInputReader(System.in, StandardCharsets.UTF_8));
        try {
          // TODO Create static attributes for these parameters maybe
          input = result.get(5, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
          e.getCause().printStackTrace();
        } catch (TimeoutException e) {
          System.out.println("Cancelling reading task");
          result.cancel(true);
          System.out.println("\nThread cancelled. input is null");
        }
      } finally {
        ex.shutdownNow();
      }
    }
    return input;
  }

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
  //
  //   }
  // }
}
