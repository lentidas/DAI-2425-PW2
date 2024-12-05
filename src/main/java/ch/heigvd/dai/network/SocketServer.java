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
import ch.heigvd.dai.logic.StatusCode;
import ch.heigvd.dai.logic.commands.FillCommand;
import ch.heigvd.dai.logic.commands.GameCommand;
import ch.heigvd.dai.logic.commands.GuessCommand;
import ch.heigvd.dai.logic.commands.JoinCommand;
import ch.heigvd.dai.logic.commands.LettersCommand;
import ch.heigvd.dai.logic.commands.StatusCommand;
import ch.heigvd.dai.logic.commands.VowelCommand;
import ch.heigvd.dai.logic.server.GameMatch;
import com.google.common.net.HostAndPort;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.InvalidPropertiesFormatException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implements a network server for the Wheel of Fortune game.
 *
 * <p>It listens for incoming connections and creates a new thread for each client that connects.
 * The number of clients is limited to the maximum number of players allowed in a game match, and
 * this limit is enforced both through a maximum number of threads in the thread pool and a maximum
 * number of connections on the server socket.
 *
 * <p>Each client thread reads commands from the client and sends responses back.
 */
public class SocketServer extends SocketAbstract {

  /** Attribute containing the instance of the game match. */
  private final GameMatch match;

  /**
   * Default constructor.
   *
   * @param hostAndPort a {@link HostAndPort} object containing the host and port information
   * @param match a {@link GameMatch} object containing the instance of the game match
   * @throws NullPointerException if {@code hostAndPort} or {@code match} is null
   * @throws IllegalArgumentException if {@code hostAndPort} does not contain a port number
   * @throws UnknownHostException if {@code hostAndPort} contains a hostname that is unresolvable to
   *     a valid IP
   */
  public SocketServer(HostAndPort hostAndPort, GameMatch match)
      throws NullPointerException, IllegalArgumentException, UnknownHostException {
    super(hostAndPort);
    this.match = match;
  }

  /**
   * Inner class that implements the client handler for each of the threads of the clients.
   * Implements {@link Runnable} to be able to run in a separate thread.
   */
  class ClientHandler implements Runnable {
    private static final int READ_TIMEOUT_MS = 250;
    private final Socket socket;
    private Player player;

    /**
     * Default constructor.
     *
     * @param socket a {@link Socket} object containing the socket connection to the client
     * @throws RuntimeException if the timeout for the socket read cannot be set
     */
    ClientHandler(Socket socket) throws RuntimeException {
      this.socket = socket;
      player = null;

      try {
        socket.setSoTimeout(READ_TIMEOUT_MS);
      } catch (SocketException e) {
        throw new RuntimeException(
            "[ClientHandler] SocketException: failed to set timeout for socket read");
      }
    }

    /**
     * Method to parse the JOIN command from the client and to add the player to the game match
     * object.
     *
     * @param joinCommand a {@link JoinCommand} object containing the JOIN command from the client
     * @return a {@link GameCommand} object with the {@link StatusCommand} response to the JOIN
     *     command
     */
    GameCommand parseJoin(JoinCommand joinCommand) {
      StatusCode joinStatus = match.addPlayer(joinCommand.getUsername());

      if (null == joinStatus) {
        joinStatus = StatusCode.KO;
      } else if (StatusCode.OK == joinStatus) {
        player = match.getPlayer(joinCommand.getUsername());
        System.out.println(player + " connected successfully");
      }

      return new StatusCommand(joinStatus);
    }

    /**
     * Run method (implements {@link Runnable}) for the client handler that reads commands from the
     * client and sends responses back.
     *
     * <p>It reads commands from the client and sends responses back while the client socket is not
     * closed.
     *
     * <p>NOTE: Use of try-with-resources automatically closes the resources when the try block
     * ends. Most of the exceptions are caught and handled, only critical exceptions are sent
     * upwards through the call stack.
     */
    @Override
    public void run() {
      try (socket;
          Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
          BufferedReader in = new BufferedReader(reader);
          Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
          BufferedWriter out = new BufferedWriter(writer)) {

        // Print message with client information.
        System.out.println(
            "[Server] New client connection from "
                + socket.getInetAddress().getHostAddress()
                + ":"
                + socket.getPort());

        // Run REPL until client disconnects.
        while (!socket.isClosed()) {

          try {
            // Send all remaining global commands.
            if (null != player) {
              for (GameCommand pendingCommand : match.getPendingCommands(player)) {
                out.write(pendingCommand.toTcpBody() + END_OF_LINE);
                out.flush();
              }
            }

            // Read response from client, or wait for a timeout.
            String clientRequest = in.readLine();

            // If clientRequest is null, the client has disconnected.
            // The server can close the connection and end the thread.
            if (clientRequest == null) {
              socket.close();
              break;
            }

            // Parse the message we got from the player.
            GameCommand command;
            try {
              command = GameCommand.fromTcpBody(clientRequest.trim());
            } catch (InvalidPropertiesFormatException format) {
              // Response is malformed (not a valid command).
              out.write(new StatusCommand(StatusCode.KO).toTcpBody() + END_OF_LINE);
              out.flush();
              continue;
            }

            // Prepare response from the server back to the client.
            String response = null;
            if (null != player) {
              System.out.println(player + " sent command " + command.getType());
            } else {
              System.out.println(
                  "Player from IP "
                      + socket.getInetAddress().getHostAddress()
                      + " sent command "
                      + command.getType());
            }

            // Handle the request and setup appropriate response.
            switch (command.getType()) {
              case JOIN -> {
                if (null == player) {
                  response = parseJoin((JoinCommand) command).toTcpBody();
                } else {
                  response = new StatusCommand(StatusCode.KO).toTcpBody();
                  System.out.println(player + " tried to join again");
                }
              }

              case GO -> {
                if (!match.startGame()) {
                  System.out.println(
                      player + " tried to start the match, but it was already ongoing");
                } else {
                  System.out.println(player + " started the match");
                }
              }

              case LETTERS -> {
                if (match.isNotMyTurn(player)) {
                  System.out.println(
                      player + " tried to play the last round, but it's not their turn");
                  response = new StatusCommand(StatusCode.KO).toTcpBody();
                } else {
                  response = match.guessLastRoundLetters((LettersCommand) command).toTcpBody();
                }
              }

              case GUESS -> {
                if (match.isNotMyTurn(player)) {
                  System.out.println(
                      player + " tried to guess a consonant, but it's not their turn");
                  response = new StatusCommand(StatusCode.KO).toTcpBody();
                } else {
                  response = match.guessConsonant((GuessCommand) command).toTcpBody();
                }
              }

              case FILL -> {
                if (match.isNotMyTurn(player)) {
                  System.out.println(
                      player + " tried to fill in the puzzle, but it's not their turn");
                  response = new StatusCommand(StatusCode.KO).toTcpBody();
                } else {
                  response = match.solvePuzzle((FillCommand) command).toTcpBody();
                }
              }

              case SKIP -> {
                if (match.isNotMyTurn(player)) {
                  System.out.println(player + " tried to skip their turn, but it's not their turn");
                  response = new StatusCommand(StatusCode.KO).toTcpBody();
                } else {
                  System.out.println(player + " skipped their turn");
                  match.skipTurn(player);
                }
              }

              case VOWEL -> {
                if (match.isNotMyTurn(player)) {
                  System.out.println(player + " tried to buy a vowel, but it's not their turn");
                  response = new StatusCommand(StatusCode.KO).toTcpBody();
                } else {
                  System.out.println(player + " bought a vowel");
                  response = match.guessVowel((VowelCommand) command).toTcpBody();
                }
              }

              case QUIT -> {
                if (null == player) {
                  System.out.println("Peer tried to quit lobby before even logging it");
                } else {
                  match.quitPlayer(player.getUsername());
                  System.out.println(player + " quit");
                  player = null;
                }
              }

              default -> {
                System.out.println("Command " + command.getType() + " was uncaught!");
                response = new StatusCommand(StatusCode.KO).toTcpBody();
              }
            }

            // Send the response back to the client.
            if (null != response) {
              out.write(response + END_OF_LINE);
              out.flush();
            }

          } catch (SocketTimeoutException e) {
            // Nothing to do but loop around and hope for an answer later on.
          } catch (Exception e) {
            System.err.println("[Server] Random exception: " + e);
            socket.close();
            break;
          }
        } // end of while (!socket.isClosed())

        // Print message to say connection with client has closed.
        System.out.println(
            "[Server] Closed connection with client "
                + socket.getInetAddress().getHostAddress()
                + ":"
                + socket.getPort());
      } catch (IOException e) {
        System.err.println("[Server] IOException: " + e);
      }

      // Disconnect player from match if that's not yet the case.
      if (null != player) {
        match.quitPlayer(player.getUsername());
      }
    }
  }

  /**
   * Run method (implements {@link Runnable}) for the server that creates a new thread pool and a
   * thread for each client that connects to the server.
   *
   * <p>It listens for incoming connections and creates a new thread for each client that connects
   * while the server socket is not closed. The number of clients is limited to the maximum number
   * of players allowed in a game match, and this limit is enforced both through a maximum number of
   * threads in the thread pool and a maximum number of connections on the server socket.
   */
  @Override
  public void run() {
    try (ServerSocket serverSocket =
            isHostAny()
                ? new ServerSocket(getPort(), GameMatch.MAX_PLAYERS)
                : new ServerSocket(getPort(), GameMatch.MAX_PLAYERS, getHost());
        ExecutorService executor = Executors.newFixedThreadPool(GameMatch.MAX_PLAYERS)) {

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
      System.out.println("[Server] IOException: " + e);
    }
  }
}
