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

import picocli.CommandLine;

/**
 * Implements the root command for executing the program on a CLI.
 *
 * <p>For checking the usage of the command, use the {@code --help} option. No specific parameters
 * or arguments are saved on this class, the logic is implemented separately in the {@link Server}
 * and {@link Client} classes. The default values for the host and port are saved here as static
 * attributes.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
@CommandLine.Command(
    description =
        "Wheel Of Fortune - a Java server/client CLI implementation of the television game",
    version = "1.0.0", // x-release-please-version
    subcommands = {Server.class, Client.class},
    scope = CommandLine.ScopeType.INHERIT,
    mixinStandardHelpOptions = true)
public class Root {

  public static final String DEFAULT_PORT = "1234";
  public static final String DEFAULT_HOST = "127.0.0.1";
}
