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
 * Default implementation of the {@link ResourcePathResolver} interface.
 * 
 * <p>
 * This implementation allows to customize a {@code pattern}
 * that describes the path and is dynamically builded using a
 * {@link java.text.MessageFormat}.
 * 
 * <p>
 * This implementation accepts an {@link Object} array as resource identifier
 * so it is suitable for most cases.
 *  
 * @author Nerd4j Team
 */
public class DefaultResourcePathResolver extends MessageFormatResourcePathResolver<Object[]>
{

	
	/**
	 * Constructor with parameters.
	 * 
	 * @param pathPattern a {@code pattern} that can be
	 *                    construed by a {@link java.text.MessageFormat}.
	 */
	public DefaultResourcePathResolver( String pathPattern )
	{
		
		super( pathPattern );
		
	}
	
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param pathPattern a @code pattern} that can be
	 *                    construed by a {@link java.text.MessageFormat}.
	 * @param resourceRootPath common root path
	 */
	public DefaultResourcePathResolver( String resourceRootPath, String pathPattern )
	{
		
		super( resourceRootPath, pathPattern );
				
	}
	
	
	/* ***************** */
	/*  EXTENSION HOOKS  */
	/* ***************** */
	
	
	/**
	 * Extracts from the given key object the values
	 * needed to format the path.
	 *  
	 * @param key identification key for the resource.
	 * @return the arguments that match the internal pattern.
	 */
	@Override
	protected Object[] getPathValues( Object[] key )
	{
		
		return key;
		
	}
	
}
