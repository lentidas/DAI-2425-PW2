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

import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class GameCommand {
  
  protected final GameCommandType type;
  protected List<Object> args;
  private static final Map<GameCommandType, CommandFactoryFunction> _factoryHandlers = new HashMap<>();
  
  public GameCommand(GameCommandType type) {
    this.type = type;
    this.args = new LinkedList<>();
  }

  public GameCommandType getType()
  {
    return type;
  }
  
  public int getArgCount()
  {
    return args.size();
  }

  public static GameCommand fromTcpBody(String body) throws InvalidPropertiesFormatException {
    // Command names are always all-capital
    String[] commandNames = body.split("([A-Z]+) ");
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

    if(!_factoryHandlers.containsKey(commandType)) {
      throw new InvalidPropertiesFormatException("No handler for command " + commandName);
    }
    
    // Split word by word, unless quoted
    String[] commandArgs = body.substring(commandName.length() + 1)
                        .split("([^\"]\\S*|.+?\")\\s*");
    
    // Remove start and end quotes
    for(int i = 0; i < commandArgs.length; i++) {
      if(commandArgs[i].startsWith("\"") && commandArgs[i].endsWith("\"")) {
        commandArgs[i] = commandArgs[i].substring(1, commandArgs[i].length() - 1);
      }
    }
    
    return _factoryHandlers.get(commandType).apply(commandArgs);
  }
  
  protected static void addFactoryHandler(GameCommandType type, CommandFactoryFunction handler)
  {
    _factoryHandlers.put(type, handler);
    System.out.println("Added handler for " + type);
  }
  
  public String toTcpBody()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(type.name());
    
    for(Object arg : args) {
      sb.append(' ');
      if(arg instanceof String && ((String) arg).contains(" ")) {
        sb.append('"')
          .append(arg)
          .append('"');
      } else {
        sb.append(arg);
      }
    }
    
    return sb.toString();
  }
  
  public List<Object> getArgs()
  {
    return List.copyOf(args);
  }
}
