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

import ch.heigvd.dai.logic.client.InteractiveConsole;
import ch.heigvd.dai.logic.commands.GameCommand;
import ch.heigvd.dai.logic.commands.GameCommandType;
import com.google.common.net.HostAndPort;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
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

  // TODO Improve this debugging by using a proper Java logging framework.
  private static final boolean DEBUG_MODE = true;

  protected final InteractiveConsole interactiveConsole;

  public SocketClient(HostAndPort hostAndPort)
      throws NullPointerException, IllegalArgumentException, UnknownHostException {
    super(hostAndPort);
    interactiveConsole = new InteractiveConsole();
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

        // Listen for command line inputs while the socket is open.
        while (!socket.isClosed()) {

          // Read user input from console.
          String userInput = bir.readLine();

          if (!interactiveConsole.needsInput()) {
            System.err.println("No input is accepted at this time!");
            continue;
          }

          try {
            // Prepare the request to send to the server.
            GameCommand command = interactiveConsole.parseUserInput(userInput);

            // Send request to server if it is non-null.
            if (command != null) {
              out.write(command.toTcpBody() + END_OF_LINE);
              out.flush();
            }
          } catch (Exception e) {
            // Send exception upwards on the call stack.
            throw new RuntimeException("[InputReaderHandler] Exception: " + e);
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

          String prompt = interactiveConsole.getPrompt();
          if (null != prompt) {
            System.out.println(prompt);
          }

          // Read response from server and parse it.
          String serverResponse = in.readLine();

          // If serverResponse is null, the server has disconnected.
          if (serverResponse == null) {
            socket.close();
            continue;
          }

          // Parse the response from the server.
          serverResponse = serverResponse.trim();
          GameCommand response;
          try {
            response = GameCommand.fromTcpBody(serverResponse);
          } catch (InvalidPropertiesFormatException format) {
            // Response is malformed (not a valid command).
            System.out.println();
            System.out.println("Invalid command sent by server, ignore...");
            if (DEBUG_MODE) {
              System.out.println("[DEBUG] Command received: " + serverResponse);
            }
            continue;
          }

          System.out.println(); // Print an empty line to improve readability on the console.
          interactiveConsole.parseServerResponse(response);
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
}
