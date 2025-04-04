/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.l2jserver.gameserver.util;

import java.util.Iterator;

import com.l2jserver.Config;
import com.l2jserver.gameserver.model.L2Object;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 * @param <T>
 */
public abstract class L2ObjectMap<T extends L2Object> implements Iterable<T>
{
	public abstract int size();
	
	public abstract boolean isEmpty();
	
	public abstract void clear();
	
	public abstract void put(T obj);
	
	public abstract void remove(T obj);
	
	public abstract T get(int id);
	
	public abstract boolean contains(T obj);
	
	@Override
	public abstract Iterator<T> iterator();
	
	public static L2ObjectMap<L2Object> createL2ObjectMap()
	{
		switch (Config.MAP_TYPE)
		{
			case WorldObjectMap:
				return new WorldObjectMap<>();
			default:
				return new WorldObjectTree<>();
		}
	}
}