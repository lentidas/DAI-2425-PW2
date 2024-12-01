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

package ch.heigvd.dai;

import ch.heigvd.dai.commands.Root;
import ch.heigvd.dai.logic.commands.GameCommand;
import java.io.File;
import picocli.CommandLine;

/**
 * Main class.
 *
 * @author Pedro Alves da Silva
 * @author Gonçalo Carvalheiro Heleno
 */
public class Main {

  public static void main(String[] args) {
    // Example from DAI classes -
    // https://github.com/heig-vd-dai-course/heig-vd-dai-course-java-ios-practical-content-template/blob/778e1934a64f338e93613afbb31dd9e92356d7c4/src/main/java/ch/heigvd/dai/Main.java#L10
    // Define command name - source: https://stackoverflow.com/a/11159435
    String jarFilename =
        new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath())
            .getName();

    // Register game command handlers
    GameCommand.registerHandlers();

    // Create root command and CommandLine
    CommandLine command = new CommandLine(new Root());
    command.setCommandName(jarFilename).setCaseInsensitiveEnumValuesAllowed(true);
    int exitCode = command.execute(args);

    // Exit the program
    System.exit(exitCode);
  }
}
