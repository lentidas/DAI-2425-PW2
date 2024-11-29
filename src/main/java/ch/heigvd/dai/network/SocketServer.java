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

import ch.heigvd.dai.Player;
import ch.heigvd.dai.logic.GameMatch;
import ch.heigvd.dai.logic.StatusCode;
import ch.heigvd.dai.logic.commands.GameCommand;
import ch.heigvd.dai.logic.commands.GuessCommand;
import ch.heigvd.dai.logic.commands.JoinCommand;
import ch.heigvd.dai.logic.commands.LobbyCommand;
import ch.heigvd.dai.logic.commands.StartCommand;
import ch.heigvd.dai.logic.commands.StatusCommand;
import com.google.common.net.HostAndPort;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.InvalidPropertiesFormatException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketServer extends SocketAbstract {

  // TODO Replace this static value with something coming from the game logic package
  public static final int MAX_N_CONNECTIONS = 5;
  private final GameMatch match;

  public SocketServer(HostAndPort hostAndPort, GameMatch match)
      throws NullPointerException, IllegalArgumentException, UnknownHostException {
    super(hostAndPort);
    this.match = match;
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

  class ClientHandler implements Runnable {
    private final int readTimeoutMs = 250;
    private final Socket socket;
    private Player player;

    ClientHandler(Socket socket) {
      this.socket = socket;
      player = null;

      try {
        socket.setSoTimeout(readTimeoutMs);
      } catch (SocketException e) {
        throw new RuntimeException("Failed to set timeout for socket read");
      }
    }


    /*
     * Command handlers
     */

    GameCommand parseJoin(JoinCommand joinCommand)
    {
      StatusCode joinStatus = match.addPlayer(joinCommand.getUsername());

      if(null == joinStatus) {
        joinStatus = StatusCode.KO;
      } else if (StatusCode.OK == joinStatus) {
        player = match.getPlayer(joinCommand.getUsername());
        System.out.println(player + " connected successfully");
      }

      return new StatusCommand(joinStatus);
    }


    /*
     * Socket reader / writer
     */

    @Override
    public void run() {
      try (socket;
          Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
          BufferedReader in = new BufferedReader(reader);
          Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
          BufferedWriter out = new BufferedWriter(writer)) {

        // Print message with client information.
        // TODO Check if the output includes brackets when connection from IPv6
        System.out.println(
            "[Server] New client connection from "
                + socket.getInetAddress().getHostAddress()
                + ":"
                + socket.getPort());

        // Run REPL until client disconnects.
        while (!socket.isClosed()) {

          try
          {
            // Send all remaining global commands
            if(null != player) {
              for (GameCommand pendingCommand : match.getPendingCommands(player)) {
                out.write(pendingCommand.toTcpBody() + END_OF_LINE);
                out.flush();
              }
            }

            // Read response from client, or wait for a timeout
            String clientRequest = in.readLine();
            System.out.println(clientRequest);

            // If clientRequest is null, the client has disconnected.
            // The server can close the connection and end the thread.
            // TODO Verify if this is the better behavior or if we leave the thread running until
            //  somebody reconnects.
            if (clientRequest == null) {
              socket.close();
              break;
            }

            // Parse the message we got from the player
            GameCommand command;
            try {
              command = GameCommand.fromTcpBody(clientRequest.trim());
            } catch (InvalidPropertiesFormatException format) {
              // Response is malformed (not a valid command)
              out.write(new StatusCommand(StatusCode.KO).toTcpBody() + END_OF_LINE);
              out.flush();
              continue;
            }

            // Prepare response from the server back to the client.
            String response = null;
            if(null != player)
            {
              System.out.println(player + " sent command " + command.getType());
            }
            else
            {
              System.out.println("Player from IP " + socket.getInetAddress().getHostAddress() + " sent command " + command.getType());
            }

            // Handle the request and setup appropriate response.
            switch (command.getType()) {
              case JOIN -> {
                if(null == player) {
                  response = parseJoin((JoinCommand) command).toTcpBody();
                } else {
                  response = new StatusCommand(StatusCode.KO).toTcpBody();
                  System.out.println(player + " tried to join again");
                }
              }

              case GO -> {
                if(!match.startGame()) {
                  System.out.println(player + " tried to start the match, but it was already ongoing");
                } else {
                  System.out.println(player + " started the match");
                }
              }

              case GUESS ->
              {
                if(!match.isMyTurn(player))
                {
                  System.out.println(player + " tried to guess a consonant, but it's not their turn");
                } else {
                  response = match.guessConsonant((GuessCommand) command).toTcpBody();
                }
              }

              default -> {
                System.out.println( "Command " + command.getType() + " was uncaught!");
                response = new StatusCommand(StatusCode.KO).toTcpBody();
              }
            }

            // Send the response back to the client.
            if(null != response)
            {
              out.write(response + END_OF_LINE);
              out.flush();
            }

          } catch(SocketTimeoutException e) {
            // Nothing to do but loop around and hope for an answer later on
          } catch (Exception e) {
            // TODO Same as the other TODO above
            System.err.println("[Server] Random exception: " + e);
          }
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
        System.err.println("[Server] IO exception: " + e);
      }
      System.out.println("out");
    }
  }
}
