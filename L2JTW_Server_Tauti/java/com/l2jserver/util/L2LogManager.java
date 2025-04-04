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
package com.l2jserver.util;

import java.util.logging.LogManager;

/**
 * Specialized {@link LogManager} class.<br>
 * Prevents log devices to close before shutdown sequence so the shutdown sequence can make logging.
 */
public class L2LogManager extends LogManager
{
	public L2LogManager()
	{
		super();
	}
	
	@Override
	public void reset()
	{
		// do nothing
	}
	
	public void doReset()
	{
		super.reset();
	}
}
