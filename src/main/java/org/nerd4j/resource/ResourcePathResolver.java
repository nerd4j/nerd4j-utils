/*
 * #%L
 * Nerd4j Utils
 * %%
 * Copyright (C) 2011 - 2013 Nerd4j
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
package org.nerd4j.resource;

/**
 * Has the aim to dynamically resolve the path of a resource.
 * 
 * <p>
 *  The resource is identified by a suitable key that can be
 *  a generic object. Any implementation of this interface
 *  will define the proper key type.
 * </p>
 * 
 * @param <K> identification key type. 
 * 
 * @author Nerd4j Team
 */
public interface ResourcePathResolver<K>
{
	
	/**
	 * Returns the path to reach the resource identified
	 * by the given key.
	 *  
	 * @param key identification key for the resource.
	 * @return the path to reach the resource.
	 */
	public String getPath( K key );

}
