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

import ch.heigvd.dai.network.SocketClient;
import com.google.common.net.*;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import picocli.CommandLine;

/**
 * Implements the {@code client} subcommand for executing the program on a CLI.
 *
 * <p>This class defines the parameters and options that are only specific for this subcommand. For
 * checking the usage of the subcommand, use the {@code --help} option.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
@CommandLine.Command(
    name = "client",
    description = "Start the client and connect to a given server.")
public class Client implements Callable<Integer> {

  @CommandLine.Option(
      names = {"-h", "--host"},
      description =
          """
              IP address where the server will bind to and listen for connections. Can be passed in the format '[host]:[port]' or simply 'host'.
              IPv4 cannot have square brackets if passed without the port number.
              IPv6 is supported, but the IP should always be enclosed by square brackets.
              Valid inputs:
              - 10.1.1.10
              - 10.1.1.10:8912
              - [2001:0db8:85a3:0000:0000:8a2e:0370:7334]
              - [2001:0db8:85a3::8a2e:0370:7334]:8912
              Default: '${DEFAULT-VALUE}'""",
      defaultValue = Root.DEFAULT_HOST)
  private String serverAddress;

  @CommandLine.Option(
      names = {"-p", "--port"},
      description =
          """
          Port where the server is expecting client connections.
          If the port is specified along side the IP address, then this value is ignored.
          Default: ${DEFAULT-VALUE}""",
      defaultValue = Root.DEFAULT_PORT)
  private int serverPort;

  /**
   * Call function that contains the logic of the subcommand.
   *
   * <p>This function performs the multiple input validation checks and outputs an exit code
   * accordingly. An error message is displayed to explain why the command failed.
   *
   * @return exit code 1 if there was an error, 0 otherwise
   */
  @Override
  public Integer call() {
    HostAndPort hostAndPort =
        HostAndPort.fromString(serverAddress).withDefaultPort(serverPort).requireBracketsForIPv6();

    SocketClient client = null;
    try {
      client = new SocketClient(hostAndPort);
    } catch (UnknownHostException | NullPointerException | IllegalArgumentException e) {
      System.err.println("[Client] Exception when creating SocketClient: " + e);
      return 1;
    }
    client.run();

    return 0;
  }
}
