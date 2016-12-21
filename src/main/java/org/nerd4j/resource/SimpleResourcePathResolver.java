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
 * Trivial implementation of the {@code ResourcePathResolver} interface.
 * 
 * <p>
 * This class handles the base case where the key used to identify
 * the resource is the resource name itself, and the path is fixed.
 * 
 * @author Nerd4j Team
 */
public class SimpleResourcePathResolver extends AbstractResourcePathResolver<String>
{
	
	
	/**
	 * Default constructor.
	 * 
	 */
	public SimpleResourcePathResolver()
	{
		
		super();
		
	}
	
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param resourceRootPath common root path
	 */
	public SimpleResourcePathResolver( String resourceRootPath )
	{
		
		super( resourceRootPath );
		
	}
	

	/* ******************* */
	/*  INTERFACE METHODS  */
	/* ******************* */
	
	
	/**
	 * Given the resource name it returns the related path.
	 *  
	 * @param key identification key for the resource.
	 * @return the path to reach the resource.
	 */
	@Override
	public String getPath( String key )
	{
		
		return buildPath( key );
		
	}
	
}
