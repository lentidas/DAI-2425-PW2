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
import java.net.ServerSocket;

public class SocketServer extends Socket implements Runnable {

  public SocketServer(HostAndPort hostAndPort) {
    super(hostAndPort);
  }

  @Override
  public void run() {
    // TODO Implement a way to limit the maximum amount of players/connections using the backlog
    //  option of ServerSocket()
    // TODO Implement a way of binding to a single IP instead of 0.0.0.0 by default
    try (ServerSocket serverSocket = new ServerSocket(getPort())) {

      // TODO Remove this test output
      System.out.println("Output from ServerSocket");

    } catch (IOException e) {
      System.out.println("[Server] IO exception: " + e);
    }
  }
}
