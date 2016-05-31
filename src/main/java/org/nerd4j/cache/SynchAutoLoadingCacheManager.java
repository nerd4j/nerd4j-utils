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

import org.nerd4j.util.DataConsistency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple implementation of {@link AutoLoadingCacheManager}
 * that performs a synchronous update of the cache entries.
 * 
 * <p>
 *  This implementation uses the same thread that requires
 *  an entry to retrieve the related value if the key is
 *  not present or expired.
 * </p>
 * 
 * @param <Key>   type of the cache key.
 * @param <Value> type of the objects stored in the cache.
 * 
 * @author Nerd4j Team
 */
public class SynchAutoLoadingCacheManager<Key extends CacheKey,Value> implements AutoLoadingCacheManager<Key,Value>
{
	
	/** Logging system. */
	private static final Logger log = LoggerFactory.getLogger( SynchAutoLoadingCacheManager.class );
	
	/** Default duration of a cache entry in seconds (60 minutes). */
	private static final int DEFAULT_CACHE_DURATION = 60 * 60;
	
	/** Default duration in seconds used by the method {@link #touch(CacheKey, long)} (10 minutes). */
	private static final int DEFAULT_TOUCH_DURATION = 10 * 60;
	
	
	/** Actual duration of a cache entry in seconds. */
	private final int cacheDuration;
	
	/** Number of seconds to postpone a cache entry expiration. */
	private final int touchDuration;
	
	/** Name of the cache region used by this manager. */
	private final String cacheRegion;
	
	/** Underlying caching system provider. */
	private final CacheProvider<Value> cacheProvider;
	
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param cacheProvider the provider of underlying caching system.
	 */
	public SynchAutoLoadingCacheManager( CacheProvider<Value> cacheProvider )
	{
		
		this( cacheProvider, "Default" );
		
	}
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param cacheProvider the provider of underlying caching system.
	 * @param cacheRegion   name of the cache region used by this manager.
	 */
	public SynchAutoLoadingCacheManager( CacheProvider<Value> cacheProvider, String cacheRegion )
	{
		
		this( cacheProvider, cacheRegion, DEFAULT_CACHE_DURATION, DEFAULT_TOUCH_DURATION );
		
	}
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param cacheProvider the provider of underlying caching system.
	 * @param cacheRegion   name of the cache region used by this manager.
	 * @param cacheDuration number of seconds until a cache entry expiration.
	 * @param touchDuration number of seconds to postpone a cache entry expiration.
	 */
	public SynchAutoLoadingCacheManager( CacheProvider<Value> cacheProvider, String cacheRegion,
			                               int cacheDuration, int touchDuration )
	{
		
		super();

		DataConsistency.checkIfValued( "cache region", cacheRegion );
		DataConsistency.checkIfNotNull( "cache provider", cacheProvider );
		
		DataConsistency.checkIfStrictPositive( "cacheDuration", cacheDuration );
		DataConsistency.checkIfStrictPositive( "touchDuration", touchDuration );
		
		
		this.cacheRegion   = cacheRegion;
		this.cacheProvider = cacheProvider;
		this.cacheDuration = cacheDuration;
		this.touchDuration = touchDuration;
		
	}
	
	
	/* ******************* */
	/*  INTERFACE METHODS  */
	/* ******************* */
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Value get( Key key, DataProvider<Key,Value> dataProvider )
	{
		
		try{
        
			final CacheEntry<Value> entry = cacheProvider.get( cacheRegion, key );
			
			if( entry == null )
			{
				log.trace( "{} Cache MISS: for key {}", cacheRegion, key );
				
				/*
				 * If the put operation succeeds returns the updated
				 * value, otherwise returns null. In this case both
				 * values work fine.
				 */
				return put( key, dataProvider );
			}
				
			if( entry.hasExpired() )
			{
				log.trace( "{} Cache entry EXPIRED: for key {}", cacheRegion, key );
				
				/*
				 * If the put operation succeeds returns the updated
				 * value, otherwise returns null. In this case if null
				 * we need to return the old value.
				 */
				final Value newValue = put( key, dataProvider );
				return newValue != null ? newValue : entry.getValue();
				
			}
			
			log.trace( "{} Cache HIT: for key {}", cacheRegion, key );
			return entry.getValue();
			
		}catch( Exception ex )
		{
			
			log.error( "Unable to read " + cacheRegion + " Cache for key " + key, ex );
			
		}
		
		return null;
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void evict( Key key )
	{
		
		try{
	        
			log.debug( "Going to EVICT {} Cache for key {}", cacheRegion, key );
			cacheProvider.remove( cacheRegion, key );
			
		}catch( Exception ex )
		{
			
			log.error( "Unable to evict " + cacheRegion + " Cache for key " + key, ex );
			
		}
		
	}
	
	
	/* ***************** */
    /*  PRIVATE METHODS  */
    /* ***************** */

	
	/**
	 * Inserts the given key and related value into the cache.
	 * <p>
	 *  If the given key is already present will be replaced.
	 * </p>
	 * <p>
	 *  As first will book the update operation using
	 *  the method {@link CacheProvider#touch(String,CacheKey,long)}.
	 *  Only the first thread trying the booking will succeed.
	 *  This ensures that only one process retrieves the data
	 *  to cache that usually is a time demanding operation.
	 * </p>
	 * <p>
	 *  If the "touch" operation fails the method returns {@code null}.
	 *  Otherwise it proceeds with the update and returns the new value.
	 * </p>
	 * 
	 * @param key          key to cache.
	 * @param dataProvider provider of the values to be cached.
	 */
	private Value put( Key key, DataProvider<Key,Value> dataProvider )
	{
		
		try{
			
			log.trace( "Touching key {} to reserve the update.", key );
			if( ! cacheProvider.touch(cacheRegion, key, touchDuration) )
			{
				log.trace( "Touch failed, someone else is updating key {}.", key );
				return null;
			}
			
			log.trace( "Setting value for key {}.", key );
			
			/* Retrieving the data to be cached using the given provider. */
			final Value value = dataProvider.retrieve( key );
			
			/* Putting the retrieved data into cache. */
			cacheProvider.put( cacheRegion, key, value, cacheDuration );
			
			return value;

						
		}catch( Exception ex )
		{
			
			log.error( "Unable to populate " + cacheRegion + " cache for key " + key, ex );
			return null;
			
		}
		
	}

}