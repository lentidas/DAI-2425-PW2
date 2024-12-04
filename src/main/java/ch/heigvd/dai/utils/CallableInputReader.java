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

package ch.heigvd.dai.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;

// TODO Document that this class is inspired from this article:
//  https://www.javaspecialists.eu/archive/Issue153-Timeout-on-Console-Input.html
// TODO Do not forget to remove this if not used
public class CallableInputReader implements Callable<String> {

  private static final int SLEEP_MS = 200;
  private final InputStream inputStream;
  private final Charset charset;

  public CallableInputReader(InputStream inputStream, Charset charset) {
    this.inputStream = inputStream;
    this.charset = charset;
  }

  public String call() throws IOException {
    try (Reader inputReader = new InputStreamReader(inputStream, charset);
        BufferedReader bir = new BufferedReader(inputReader)) {
      String input;
      do {
        try {
          // Pause the thread and wait until we have data to complete a readLine() (i.e. until we
          // have a new line character).
          while (!bir.ready()) {
            Thread.sleep(SLEEP_MS);
          }
          input = bir.readLine();
        } catch (InterruptedException e) {
          // System.out.println("ConsoleInputReadTask() cancelled"); // TODO Remove
          return null;
        }
      } while ("".equals(input));
      // System.out.println("Thank You for providing input!"); // TODO Remove
      return input;
    }
  }
}
