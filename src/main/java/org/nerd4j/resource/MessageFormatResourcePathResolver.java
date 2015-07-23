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

import java.text.MessageFormat;

/**
 * Implementation of the <code>ResourcePathResolver</code> interface
 * based on a {@link MessageFormat} resolver.
 * 
 * <p>
 *  This class handles the case where the path to each resource
 *  can be described using a <code>pattern</code> that can be
 *  construed by a {@link MessageFormat}.
 * </p>
 * 
 * <p>
 *  This class actually contains a {@link MessageFormat} that builds
 *  the path based on the provided arguments.
 * </p>
 * 
 * @param <K> identification key type.
 * 
 * @author Nerd4j Team
 */
public abstract class MessageFormatResourcePathResolver<K> extends AbstractResourcePathResolver<K>
{
	
	/** Internal structure used to format the path. */
	private MessageFormat pathFormat;
	
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param pathPattern a <code>pattern</code> that can be
	 *                    construed by a {@link MessageFormat}.
	 */
	public MessageFormatResourcePathResolver( String pathPattern )
	{
		
		super();
		
		this.pathFormat = new MessageFormat( pathPattern );
		
	}
	
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param pathPattern a <code>pattern</code> that can be
	 *                    construed by a {@link MessageFormat}.
	 * @param resourceRootPath common root path
	 */
	public MessageFormatResourcePathResolver( String resourceRootPath, String pathPattern )
	{
		
		super( resourceRootPath );
		
		this.pathFormat = new MessageFormat( pathPattern );
		
	}


	/* ******************* */
	/*  INTERFACE METHODS  */
	/* ******************* */
	
	
	/**
	 * Returns the path to reach the resource identified
	 * by the given key.
	 *  
	 * @param key identification key for the resource.
	 * @return the path to reach the resource.
	 */
	@Override
	public String getPath( K key )
	{
		
		final Object[] arguments = getPathValues( key );
		return buildPath( pathFormat.format(arguments) );
		
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
	protected abstract Object[] getPathValues( K key );
	
}
