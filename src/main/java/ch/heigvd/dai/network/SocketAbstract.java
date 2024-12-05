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
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Abstract class for socket connections.
 *
 * <p>It contains the host and port information for the connection and defines the {@link
 * #END_OF_LINE} constant for the end of line character specified by the protocol.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
abstract class SocketAbstract implements Runnable {

  /** The host and port information for the connection. */
  private final HostAndPort hostAndPort;

  /** The IP information for the connection. */
  private final InetAddress host;

  /** The port information for the connection. */
  private final int port;

  /** The end of line character, as specified by the protocol. */
  public static final String END_OF_LINE = "\n";

  /**
   * Default constructor.
   *
   * @throws NullPointerException if {@code hostAndPort} is null
   * @throws IllegalArgumentException if {@code hostAndPort} does not contain a port number
   * @throws UnknownHostException if {@code hostAndPort} contains a hostname that is unresolvable to
   *     a valid IP
   */
  SocketAbstract(HostAndPort hostAndPort)
      throws NullPointerException, IllegalArgumentException, UnknownHostException {
    if (hostAndPort == null) {
      throw new NullPointerException("hostAndPort cannot be null");
    }
    if (!hostAndPort.hasPort()) {
      throw new IllegalArgumentException("hostAndPort needs to contain a port number");
    }
    this.hostAndPort = hostAndPort;
    this.host = InetAddress.getByName(hostAndPort.getHost());
    this.port = hostAndPort.getPort();
  }

  /**
   * Getter for the host and port information stored in the object.
   *
   * @return a {@link HostAndPort} object with the IP and port information for creating a socket
   */
  public HostAndPort getHostAndPort() {
    return hostAndPort;
  }

  /**
   * Getter for the host information stored in the object.
   *
   * @return an {@link InetAddress} object with the IP information for creating a socket
   */
  public InetAddress getHost() {
    return host;
  }

  /**
   * Getter for the port information stored in the object.
   *
   * @return an {@link Integer} with the port information for creating a socket
   */
  public int getPort() {
    return port;
  }

  /**
   * Checks if the host is set to any address.
   *
   * @return {@code true} if the host is set to any address, {@code false} otherwise
   */
  public boolean isHostAny() {
    return hostAndPort.getHost().equals("0.0.0.0") || hostAndPort.getHost().equals("::");
  }
}
