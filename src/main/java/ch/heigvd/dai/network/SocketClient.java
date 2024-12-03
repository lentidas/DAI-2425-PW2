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

import ch.heigvd.dai.logic.StatusCode;
import ch.heigvd.dai.logic.commands.GameCommand;
import ch.heigvd.dai.logic.commands.GoCommand;
import ch.heigvd.dai.logic.commands.HostCommand;
import ch.heigvd.dai.logic.commands.JoinCommand;
import ch.heigvd.dai.logic.commands.StatusCommand;
import com.google.common.net.HostAndPort;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.List;

public class SocketClient extends SocketAbstract {

  public SocketClient(HostAndPort hostAndPort)
      throws NullPointerException, IllegalArgumentException, UnknownHostException {
    super(hostAndPort);
  }

  @Override
  public void run() {
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
      System.out.println();

      // Print help message.
      help();

      // Run REPL until user quits with command QUIT.
      while (!socket.isClosed()) {
        // Output prompt.
        System.out.print("> ");

        // TODO Maybe implement a way to timeout and refresh, because the server could have sent a
        //  command saying that the game has started in the meantime.
        // Read user input from console.
        String userInput = bir.readLine();

        // Parse command from user.
        GameCommand command;
        try {
          command = GameCommand.fromTcpBody(userInput.trim());
        } catch (InvalidPropertiesFormatException ignore) {
          System.out.println("Unrecognized/Invalid command! Available commands:\n");
          help();
          continue;
        }

        try {
          // Prepare the request to send to the server.
          String request = null;

          switch (command.getType()) {
            case JOIN ->
                request = new JoinCommand((String) command.getArgs().getFirst()).toTcpBody();
            case GO -> request = new GoCommand().toTcpBody();
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
              System.out.println("Unrecognized command! Available commands:\n");
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
          System.out.println("Invalid command. Please try again.");
          continue;
        }

        // Read response from server and parse it.
        String serverResponse = in.readLine();

        // If serverResponse is null, the server has disconnected.
        if (serverResponse == null) {
          socket.close();
          continue;
        }

        // Parse the response from the server.
        // TODO Consider refactoring identical block above into a single private class function.
        GameCommand response = null;
        try {
          response = GameCommand.fromTcpBody(serverResponse.trim());
        } catch (InvalidPropertiesFormatException format) {
          // Response is malformed (not a valid command).
          System.out.println("Invalid/unknown command sent by server, ignore.");
          continue;
        }

        // Handle the response from the server and perform the appropriate action.
        // In most cases, there is, because the interpretation of the server response depends on
        // the command the client sent before.
        // switch (command.getType()) {
        //   case JOIN -> {}
        // }

        switch (response.getType()) {

          case STATUS -> {
            StatusCode status = ((StatusCommand) response).getStatus();
            switch (status) {
              case OK -> System.out.println("OK!");
              case KO -> System.out.println("Something is not OK!");
              case FULL -> {
                System.out.println("Party is full and there is no place to join.");
                socket.close();
              }
              case DUPLICATE_NAME -> System.out.println("Username is already in use.");
            }
          }

          case END -> {
            List<Object> args = response.getArgs();
            Iterator<Object> iter = args.iterator();
            System.out.println("Game ended!");
            System.out.println("===");
            System.out.println("WINNER: " + iter.next());
            System.out.println("===");
            System.out.println("Game results:");
            for (int i = 0; iter.hasNext(); ++i) {
              if (i % 2 == 0) { // If even, print username.
                System.out.print(iter.next());
              } else { // Else, print money with a new line.
                System.out.println(" - " + iter.next());
              }
            }
            System.out.println("Please use JOIN to play again or QUIT to go home.");
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

  private void help() {
    // TODO
    System.out.println("help() function output");
  }
}
