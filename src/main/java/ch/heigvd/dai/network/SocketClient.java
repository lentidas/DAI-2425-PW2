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

import com.google.common.net.HostAndPort;
import java.io.*;
import java.net.*;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class SocketClient extends SocketAbstract {
  public enum ClientCommand {
    HELLO,
    INVALID,
    HELP,
    QUIT
  }

  public enum ServerCommand {
    HELLO_ACK,
    INVALID
  }

  public SocketClient(HostAndPort hostAndPort)
      throws NullPointerException, IllegalArgumentException, UnknownHostException {
    super(hostAndPort);
  }

  @Override
  public void run() {
    System.out.println(
        // TODO Check if the output includes brackets when connecting to IPv6
        "[Client] Connecting to server at " + getHost().getHostAddress() + ":" + getPort() + "...");

    try (Socket socket = new Socket(getHost(), getPort());
        Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
        BufferedReader in = new BufferedReader(reader);
        Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
        BufferedWriter out = new BufferedWriter(writer)) {
      // Print message to acknowledge successful connection to the server.
      // TODO Check if the output includes brackets when connecting to IPv6
      System.out.println(
          "[Client] Connected to server at " + getHost().getHostAddress() + ":" + getPort());
      System.out.println();

      // Print help message.
      help();

      // Run REPL until user quits with command QUIT.
      while (!socket.isClosed()) {
        // Output prompt.
        System.out.print("> ");

        // Read user input from console.
        Reader inputReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
        BufferedReader bir = new BufferedReader(inputReader);
        String userInput = bir.readLine();

        try {
          // Split user input to parse command (first part of the message is the command, second
          // part are the arguments).
          String[] userInputParts = userInput.split(" ", 2);
          ClientCommand command = ClientCommand.valueOf(userInputParts[0].toUpperCase());

          // Prepare the request to send to the server.
          String request = null;

          switch (command) {
            case HELLO -> {
              System.out.println("Sent HELLO command to server");
              request = ClientCommand.HELLO.name();
            }
            case QUIT -> {
              socket.close();
              continue;
            }
            case HELP -> help();
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

        // If serverResponse is null, the server has disconnected
        if (serverResponse == null) {
          socket.close();
          continue;
        }

        // Split server response to parse command (first part of the message is the command, second
        // part are the arguments).
        String[] serverResponseParts = serverResponse.split(" ", 2);

        //
        ServerCommand message = null;
        try {
          message = ServerCommand.valueOf(serverResponseParts[0]);
        } catch (Exception ignore) {
          // Ignore any exception, the null case will be handed in the switch block bellow.
        }

        // Handle the response from the server and perform the appropriate action.
        switch (message) {
          case HELLO_ACK -> {
            System.out.println("Server responded HELLO_ACK");
          }
          case INVALID -> {
            if (serverResponseParts.length < 2) {
              System.out.println("Invalid message. Please try again.");
              break;
            }
            // Print invalid message from server if there is any.
            String invalidMessage = serverResponseParts[1];
            System.out.println(invalidMessage);
          }
          case null, default ->
              System.out.println("Invalid/unknown command sent by server, ignore.");
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
