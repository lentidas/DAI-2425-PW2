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

package ch.heigvd.dai.logic.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class GameCommand {


  public static String Vowels = "AEIOU"; // The original game didn't consider Y to be a vowel
  protected final GameCommandType type;
  protected List<Object> args;
  private static final Map<GameCommandType, CommandFactoryFunction> _factoryHandlers =
      new HashMap<>();

  public GameCommand(GameCommandType type) {
    this.type = type;
    this.args = new LinkedList<>();
  }

  public GameCommandType getType() {
    return type;
  }

  public int getArgCount() {
    return args.size();
  }

  public static GameCommand fromTcpBody(String body) throws InvalidPropertiesFormatException {

    String[] commandNames = body.split(" ");
    if (commandNames.length == 0) {
      throw new InvalidPropertiesFormatException("Command name is missing");
    }
    String commandName = commandNames[0];

    GameCommandType commandType;
    try {
      commandType = GameCommandType.valueOf(commandName);
    } catch (IllegalArgumentException e) {
      throw new InvalidPropertiesFormatException("Invalid command name: " + commandName);
    }

    if (!_factoryHandlers.containsKey(commandType)) {
      throw new InvalidPropertiesFormatException("No handler for command " + commandName);
    }

    // Split word by word, unless quoted
    String argsSubstr = body.substring(commandName.length()).stripLeading();
    Pattern regexPatt = Pattern.compile("([^\"]\\S*|.+?\")\\s*");
    Matcher matcher = regexPatt.matcher(argsSubstr);

    String[] commandArgs = null;
    List<String> allMatches = new ArrayList<>();
    while (matcher.find()) {
      allMatches.add(matcher.group());
    }

    if (!allMatches.isEmpty()) {
      commandArgs = new String[allMatches.size()];

      int i = 0;
      for (String arg : allMatches) {

        // Remove start and end quotes
        if (arg.startsWith("\"") && arg.endsWith("\"")) {
          arg = arg.substring(1, arg.length() - 1);
        }
        commandArgs[i] = arg;
        i++;
      }
    }

    return _factoryHandlers.get(commandType).apply(commandArgs);
  }

  protected static void addFactoryHandler(GameCommandType type, CommandFactoryFunction handler) {
    _factoryHandlers.put(type, handler);
    System.out.println("Added handler for " + type);
  }

  public static void registerHandlers() {
    GameCommand.addFactoryHandler(GameCommandType.END, EndCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.FILL, FillCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.GO, GoCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.GUESS, GuessCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.INFO, InfoCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.JOIN, JoinCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.LAST, LastCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.LOBBY, LobbyCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.QUIT, QuitCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.ROUND, RoundCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.START, StartCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.STATUS, StatusCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.TURN, TurnCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.VOWEL, VowelCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.WINNER, WinnerCommand::fromTcpBody);
    GameCommand.addFactoryHandler(GameCommandType.SKIP, SkipCommand::fromTcpBody);
  }

  private String argToString(Object arg) {
    StringBuilder sb = new StringBuilder();
    if (arg instanceof String && ((String) arg).contains(" ")) {
      sb.append('"').append(arg).append('"');
    } else {
      sb.append(arg);
    }
    return sb.toString();
  }

  public String toTcpBody() {
    StringBuilder sb = new StringBuilder();
    sb.append(type.name());

    if (null != args) {
      for (Object arg : args) {
        if (arg instanceof Object[]) {
          for (int i = 0; i < ((Object[]) arg).length; i++) {
            sb.append(' ').append(argToString(((Object[]) arg)[i]));
          }
        } else {
          sb.append(' ');
          sb.append(argToString(arg));
        }
      }
    }

    return sb.toString();
  }

  public List<Object> getArgs() {
    if (null != args) {
      return List.copyOf(args);
    } else {
      return new LinkedList<>();
    }
  }

  static protected boolean isCharAVowel(char c) {
    char lower = Character.toUpperCase(c);
    for(char vowel : Vowels.toCharArray()) {
      if(lower == vowel) {
        return true;
      }
    }
    return false;
  }
}
