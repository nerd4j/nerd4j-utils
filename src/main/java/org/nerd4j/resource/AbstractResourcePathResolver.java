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
 * Common abstract implementation of the <code>ResourcePathResolver</code> interface.
 * 
 * <p>
 * This class represents the base implementation common to all
 * the resource path resolvers.
 * 
 * @param <K> identification key type. 
 * 
 * @author Nerd4j Team
 */
public abstract class AbstractResourcePathResolver<K> implements ResourcePathResolver<K>
{
	
	/** Root common to all the paths that lead to a specific resource set. */
	private String resourceRootPath;
	
	
	/**
	 * Default constructor.
	 * 
	 */
	public AbstractResourcePathResolver()
	{
		
		super();
		
		this.resourceRootPath = "";
		
	}
	

	/**
	 * Constructor with parameters.
	 * 
	 * @param resourceRootPath common root path
	 */
	public AbstractResourcePathResolver( String resourceRootPath )
	{
		
		super();
		
		if( resourceRootPath != null && ! resourceRootPath.isEmpty() )
		{
			
			this.resourceRootPath = resourceRootPath;
			if( ! this.resourceRootPath.endsWith("/") )
				this.resourceRootPath += "/";
			
		}
		else
			this.resourceRootPath = "";
		
	}
	
	
	/* ******************* */
	/*  PROTECTED METHODS  */
	/* ******************* */
	
	
	/**
	 * Given the partial path, specific for one resource,
	 * this method returns the complete (absolute or relative) path.
	 *  
	 * @param resourcePartialPath partial path, specific for one resource.
	 * @return resource complete path.
	 */
	protected String buildPath( String resourcePartialPath )
	{
		
		return this.resourceRootPath + resourcePartialPath;
		
	}
	
}
