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

abstract class Socket {

  private final HostAndPort hostAndPort;
  private final InetAddress host;
  private final int port;

  public static final String END_OF_LINE = "\n";

  Socket(HostAndPort hostAndPort) throws IllegalArgumentException {
    if (hostAndPort == null) {
      throw new IllegalArgumentException("hostAndPort cannot be null");
    }
    if (!hostAndPort.hasPort()) {
      throw new IllegalArgumentException("hostAndPort needs to contain a port number");
    }
    this.hostAndPort = hostAndPort;
  }

  Socket(InetAddress host, int port) {
    if (host == null) {
      throw new NullPointerException("Socket class requires non-null InetAddress");
    }
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("Invalid port in Socket constructor");
    }
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return hostAndPort.getHost();
  }

  public int getPort() {
    return hostAndPort.getPort();
  }

  public HostAndPort getHostAndPort() {
    return hostAndPort;
  }

  // TODO
  boolean isIpAny() {
    return true;
  }
}
