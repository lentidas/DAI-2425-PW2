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

package ch.heigvd.dai.logic.wheel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Wheel {

  private final List<Wedge> wedges;
  private final Random rand;

  public Wheel()
  {
    rand = new Random();
    wedges = new ArrayList<Wedge>();

    wedges.add(new Wedge(WedgeType.BANKRUPT, 0));
    wedges.add(new Wedge(WedgeType.BANKRUPT, 0));
    wedges.add(new Wedge(WedgeType.BANKRUPT, 0));
    wedges.add(new Wedge(WedgeType.BANKRUPT, 0));

    wedges.add(new Wedge(WedgeType.LOSE_A_TURN, 0));

    wedges.add(new Wedge(WedgeType.MONEY, 5000));
    wedges.add(new Wedge(WedgeType.MONEY, 900));
    wedges.add(new Wedge(WedgeType.MONEY, 1000));
    wedges.add(new Wedge(WedgeType.MONEY, 1000));
    wedges.add(new Wedge(WedgeType.MONEY, 650));
    wedges.add(new Wedge(WedgeType.MONEY, 500));
    wedges.add(new Wedge(WedgeType.MONEY, 700));
    wedges.add(new Wedge(WedgeType.MONEY, 600));
    wedges.add(new Wedge(WedgeType.MONEY, 600));
    wedges.add(new Wedge(WedgeType.MONEY, 500));
    wedges.add(new Wedge(WedgeType.MONEY, 650));
    wedges.add(new Wedge(WedgeType.MONEY, 850));
    wedges.add(new Wedge(WedgeType.MONEY, 1000));
    wedges.add(new Wedge(WedgeType.MONEY, 800));
    wedges.add(new Wedge(WedgeType.MONEY, 650));
    wedges.add(new Wedge(WedgeType.MONEY, 500));
    wedges.add(new Wedge(WedgeType.MONEY, 900));

    // Supposed to be a surprise wedge, but we don't have that implemented
    wedges.add(new Wedge(WedgeType.MONEY, 500));

    // Supposed to be a trip wedge, but we don't have that implemented
    wedges.add(new Wedge(WedgeType.MONEY, 1000));

    // Supposed to be one million dollars, but the odds would've been unfair
    wedges.add(new Wedge(WedgeType.MONEY, 10000));

    // Supposed to be a wildcard wedge
    wedges.add(new Wedge(WedgeType.MONEY, rand.nextInt(10) * 100));
  }

  public Wedge spinTheWheel()
  {
    return wedges.get(rand.nextInt(wedges.size()));
  }
}
