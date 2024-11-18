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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer extends SocketAbstract {

  // TODO Replace this static value with something coming from the game logic package
  public static final int MAX_N_CONNECTIONS = 5;

  public SocketServer(HostAndPort hostAndPort)
      throws NullPointerException, IllegalArgumentException, UnknownHostException {
    super(hostAndPort);
  }

  @Override
  public void run() {
    // TODO Implement a way to limit the maximum amount of players/connections using the backlog
    //  option of ServerSocket() in parallel with the number of cached threads
    try (ServerSocket serverSocket =
            isHostAny()
                ? new ServerSocket(getPort(), MAX_N_CONNECTIONS)
                : new ServerSocket(getPort(), MAX_N_CONNECTIONS, getHost());
        ExecutorService executor = Executors.newFixedThreadPool(MAX_N_CONNECTIONS)) {

      System.out.println("[Server] Starting server...");
      if (isHostAny()) {
        System.out.println("[Server] Listening on all interfaces");
      } else {
        System.out.println(
            "[Server] Listening on the interface with IP " + getHost().getHostAddress());
      }
      System.out.println("[Server] Listening on port " + getPort());

      while (!serverSocket.isClosed()) {
        Socket clientSocket = serverSocket.accept();
        executor.submit(new ClientHandler(clientSocket));
      }
    } catch (IOException e) {
      // TODO Maybe send this exception upwards on the Stack and manage this over there
      //  else maybe manage the outputs to console here since we also output other things here.
      System.out.println("[Server] IO exception: " + e);
    }
  }

  static class ClientHandler implements Runnable {
    private final Socket socket;

    private enum ClientCommand {
      HELLO,
    }

    private enum ServerCommand {
      HELLO_ACK,
      INVALID,
    }

    ClientHandler(Socket socket) {
      this.socket = socket;
    }

    @Override
    public void run() {
      try (socket;
          Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
          BufferedReader in = new BufferedReader(reader);
          Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
          BufferedWriter out = new BufferedWriter(writer)) {

        // Run REPL until client disconnects.
        while (!socket.isClosed()) {
          // Print message with client information.
          // TODO Check if the output includes brackets when connection from IPv6
          System.out.println(
              "[Server] New client connection from "
                  + socket.getInetAddress().getHostAddress()
                  + ":"
                  + socket.getPort());

          // Read response from client.
          String clientRequest = in.readLine();

          // If clientRequest is null, the client has disconnected.
          // The server can close the connection and end the thread.
          // TODO Verify if this is the better behavior or if we leave the thread running until
          //  somebody reconnects.
          if (clientRequest == null) {
            socket.close();
            break;
          }

          // Split user input to parse command (first part of the message is the command, second
          // part are the arguments).
          String[] clientRequestParts = clientRequest.split(" ", 2);

          ClientCommand command = null;
          try {
            command = ClientCommand.valueOf(clientRequestParts[0]);
          } catch (Exception ignore) {
            // Ignore any exception, the null case will be handed in the switch block bellow.
          }

          // Prepare response from the server back to the client.
          String response = null;

          // Handle the request and setup appropriate response.
          switch (command) {
            case HELLO -> {
              System.out.println("Received HELLO from " + socket.getInetAddress().getHostAddress());
              response = ServerCommand.HELLO_ACK.name();
            }
            case null, default -> {
              System.out.println(
                  "Invalid command from " + socket.getInetAddress().getHostAddress());

              response = ServerCommand.INVALID.name();
            }
          }

          // Send the response back to the client.
          out.write(response + END_OF_LINE);
          out.flush();
        } // end of while (!socket.isClosed())

        // Print message to say connection with client has closed.
        // TODO Check if the output includes brackets when connection from IPv6
        System.out.println(
            "[Server] Closed connection with client "
                + socket.getInetAddress().getHostAddress()
                + ":"
                + socket.getPort());
      } catch (IOException e) {
        // TODO Same as the other TODO above
        System.out.println("[Server] IO exception: " + e);
      }
    }
  }
}
