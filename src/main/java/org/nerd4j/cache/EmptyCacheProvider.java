/*
 * #%L
 * Nerd4j Utils
 * %%
 * Copyright (C) 2011 - 2016 Nerd4j
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
package org.nerd4j.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Dummy implementation of the {@link CacheProvider} interface
 * that implements the design pattern {@code Empty Object}.
 * <p>
 * It provides a caching system where all insertions
 * will take no effect and the cache will always be empty.
 * 
 * @param <Value> data type to be returned by the cache.
 * 
 * @author Nerd4j Team
 */
public class EmptyCacheProvider<Value> implements CacheProvider<Value>
{
	
	/** Logging system. */
	private static final Logger log = LoggerFactory.getLogger( EmptyCacheProvider.class );
	
	
	/**
	 * Default constructor.
	 * 
	 */
	public EmptyCacheProvider()
	{
		
		super();
		
		log.info( "Created a new EmptyCacheProvider." );
		
	}
	
	
	/* ******************* */
	/*  INTERFACE METHODS  */
	/* ******************* */
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public CacheEntry<Value> get( String region, CacheKey key )
	{
		
		log.debug( "Retrieving key {} from region {} in Empty Cache.", key, region );
		return null;
		
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put( String region, CacheKey key, Value value, int duration )
	{
		
		log.debug( "Inserting key {} into region {} in Empty Cache.", key, region );
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean touch( String region, CacheKey key, int duration )
	{
		
		log.debug( "Touching key {} into regin {} in Empty Cache.", key, region );
		return true;
		
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove( String region, CacheKey key )
	{
		
		log.debug( "Removing key {} from region {} in Empty Cache.", key, region );
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void empty()
	{
		
		log.debug( "Removing all elements from all regions" );
		
	}

}