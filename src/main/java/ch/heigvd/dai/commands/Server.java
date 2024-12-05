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

package ch.heigvd.dai.commands;

import ch.heigvd.dai.logic.server.GameMatch;
import ch.heigvd.dai.network.SocketServer;
import com.google.common.net.HostAndPort;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(
    name = "server",
    description = "Start the server and listen for client connections.")
public class Server implements Callable<Integer> {

  // TODO Remove this reference to the Root class if we do not use any root parameters/options.
  @CommandLine.ParentCommand private Root parent;

  @CommandLine.Option(
      names = {"-b", "--bind"},
      description =
          """
              IP address where the server will bind to and listen for connections. Can be passed in the format 'host:port' or simply 'host'.
              IPv4 cannot have square brackets if passed without the port number.
              IPv6 is supported, but the IP should always be enclosed by square brackets.
              Passing '0.0.0.0' or '::' means the server socket will bind to all available IPs on the machine.
              Valid inputs:
              - 10.1.1.10
              - 10.1.1.10:8912
              - [2001:0db8:85a3:0000:0000:8a2e:0370:7334]
              - [2001:0db8:85a3:0000:0000:8a2e:0370:7334]:8912
              Default: '${DEFAULT-VALUE}'""",
      defaultValue = Root.DEFAULT_HOST)
  private String bindAddress;

  @CommandLine.Option(
      names = {"-p", "--port"},
      description =
          """
              Port where the server will listen for new connections.
              If the port is specified along side the IP address, then this value is ignored.
              Default: ${DEFAULT-VALUE}""",
      defaultValue = Root.DEFAULT_PORT)
  private int serverPort;

  @Override
  public Integer call() {
    HostAndPort hostAndPort =
        HostAndPort.fromString(bindAddress).withDefaultPort(serverPort).requireBracketsForIPv6();

    GameMatch match = new GameMatch();
    SocketServer server = null;
    try {
      server = new SocketServer(hostAndPort, match);
    } catch (UnknownHostException | NullPointerException | IllegalArgumentException e) {
      // TODO Maybe handle each exception in a separate catch block
      System.err.println("[Server] Exception when creating SocketServer: " + e);
      return 1;
    }
    server.run();

    return 0;
  }
}
