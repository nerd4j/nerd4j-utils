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

import java.util.concurrent.Executor;

import org.nerd4j.util.Require;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple implementation of {@link AutoLoadingCacheManager}
 * that performs an asynchronous update of the cache entries.
 * 
 * <p>
 * This implementation uses an {@link java.util.concurrent.ExecutorService} to
 * execute cache updates asynchronously.
 * <b>Note</b>: only updates are executed asynchronously
 * because the requested data is already present in cache.
 * The insertions are still executed in a synchronous way.
 *  
 * <p>
 * This allows cache to have short response time even
 * if the required entry has expired and need to be updated.
 * 
 * @param <Key>   type of the cache key.
 * @param <Value> type of the related data.
 * 
 * @author Nerd4j Team
 */
public class AsynchAutoLoadingCacheManager<Key extends CacheKey,Value> implements AutoLoadingCacheManager<Key,Value>
{
	
	/** Logging system. */
	private static final Logger log = LoggerFactory.getLogger( AsynchAutoLoadingCacheManager.class );
	
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
	 * Implementation of {@link java.util.concurrent.Executor} for
	 * the asynchronous execution of the updates.
	 */
	private final Executor updatesExecutor;

	
	/**
	 * Constructor with parameters.
	 * 
	 * @param updatesExecutor the asynchronous executor of the updates.
	 * @param cacheProvider   the provider of underlying caching system.
	 */
	public AsynchAutoLoadingCacheManager( Executor updatesExecutor, CacheProvider<Value> cacheProvider )
	{
		
		this( updatesExecutor, cacheProvider, "Default" );
		
	}
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param updatesExecutor the asynchronous executor of the updates.
	 * @param cacheProvider   the provider of underlying caching system.
	 * @param cacheRegion     name of the cache region used by this manager.
	 */
	public AsynchAutoLoadingCacheManager( Executor updatesExecutor,
			                              CacheProvider<Value> cacheProvider,
			                              String cacheRegion )
	{
		
		this( updatesExecutor, cacheProvider, cacheRegion, DEFAULT_CACHE_DURATION, DEFAULT_TOUCH_DURATION );
		
	}
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param updatesExecutor the asynchronous executor of the updates.
	 * @param cacheProvider   the provider of underlying caching system.
	 * @param cacheRegion     name of the cache region used by this manager.
	 * @param cacheDuration   number of seconds until a cache entry expiration.
	 * @param touchDuration   number of seconds to postpone a cache entry expiration.
	 */
	public AsynchAutoLoadingCacheManager( Executor updatesExecutor, CacheProvider<Value> cacheProvider,
			                              String cacheRegion, int cacheDuration, int touchDuration )
	{
		
		super();

		this.cacheRegion     = Require.nonEmpty( cacheRegion, "The cache region is mandatory" );
		this.cacheProvider   = Require.nonNull( cacheProvider, "The cache provier is mandatory" );
		this.updatesExecutor = Require.nonNull( updatesExecutor, "The updates executor is mandatory" );
		this.cacheDuration   = Require.trueFor( cacheDuration, cacheDuration > 0, "The cache duration must be > 0" );
		this.touchDuration   = Require.trueFor( touchDuration, touchDuration > 0, "The touch duration must be > 0" );
		
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
				
				log.debug( "{} Cache MISS: for key {}", cacheRegion, key );
				
				/*
				 * If the key is not present in the cache the insert
				 * will be made in a synchronous way using the current
				 * thread.
				 */
				return insert( key, dataProvider );
				
			}
				
			if( entry.hasExpired() )
			{
				
				log.debug( "{} Cache entry EXPIRED: for key {}", cacheRegion, key );
				
				/*
				 * The update will be executed in an asynchronous way so this
				 * method returns what is currently present in cache and start
				 * a task to perform the update.
				 */
				update( key, dataProvider );
				return entry.getValue();
				
			}
			
			log.debug( "{} Cache HIT: for key {}", cacheRegion, key );
			return entry.getValue();
			
		}catch( Exception ex )
		{
			
			log.warn( "Unable to read " + cacheRegion + " Cache for key " + key, ex );
			
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
			
			log.warn( "Unable to evict " + cacheRegion + " Cache for key " + key, ex );
			
		}
		
	}
	
	
	/* ***************** */
    /*  PRIVATE METHODS  */
    /* ***************** */

	
	/**
	 * Inserts the given key and related value into the cache.
	 * <p>
	 * As first will book the update operation using
	 * the method {@link CacheProvider#touch(String,CacheKey,long)}.
	 * Only the first thread trying the booking will succeed.
	 * This ensures that only one process retrieves the data
	 * to cache that usually is a time demanding operation.
	 * <p>
	 * If the "touch" operation fails the method returns {@code null}.
	 * Otherwise it proceeds with the update and returns the new value.
	 * 
	 * @param key          key to cache.
	 * @param dataProvider provider of the values to be cached.
	 */
	private Value insert( Key key, DataProvider<Key,Value> dataProvider )
	{
		
		try{
			
			log.trace( "Touching key {} to reserve the insert.", key );
			if( cacheProvider.touch(cacheRegion, key, touchDuration) )
			{
				
				log.trace( "Inserting value for key {}.", key );
				
				/* Retrieving data to cache. */
				final Value value = dataProvider.retrieve( key );
				
				/* Inserting new entry into cache. */
				cacheProvider.put( cacheRegion, key, value, cacheDuration );
				
				return value;
				
			}
			else
				log.trace( "Touch failed, someone else is handling key {}.", key );
						
		}catch( Exception ex )
		{
			
			log.warn( "Unable to insert " + cacheRegion + " cache for key " + key, ex );
			
		}
		
		return null;
		
	}
	
	/**
	 * Updates the entry related to the given cache key.
	 * <p>
	 * As first will book the update operation using
	 * the method {@link CacheProvider#touch(String,CacheKey,long)}.
	 * Only the first thread trying the booking will succeed.
	 * This ensures that only one process retrieves the data
	 * to cache that usually is a time demanding operation.
	 * <p>
	 * If the "touch" operation succeeds an update task will be created
	 * and submitted to the {@link #updatesExecutor} while the method
	 * returns immediately ensuring real-time performance.
	 * 
	 * @param key          key to cache.
	 * @param dataProvider provider of the values to be cached.
	 */
	private void update( Key key, DataProvider<Key,Value> dataProvider )
	{
		
		try{
			
			log.trace( "Touching key {} to reserve the update.", key );
			if( cacheProvider.touch(cacheRegion, key, touchDuration) )
			{
				
				log.trace( "Scheduling new update task for key {}.", key );
				
				final UpdateTask updateTask = new UpdateTask( key, dataProvider );
				updatesExecutor.execute( updateTask );
				
			}
			else
				log.trace("Touch failed, someone else is updating key {}.", key );
			
		}catch( Exception ex )
		{
			
			log.warn( "Unable to update " + cacheRegion + " cache for key " + key, ex );
			
		}
		
	}
	
	
	/* *************** */
	/*  INNER CLASSES  */
	/* *************** */

    
	/**
	 * Represents an execution task able to update a cache entry.
	 * 
	 * @author Nerd4j Team
	 */
	private class UpdateTask implements Runnable
	{
		
		/** Cache key to be updated. */
		private final Key key;
		
		/** Provider of the data to be cached. */
		private final DataProvider<Key,Value> dataProvider;

		
		/**
		 * Constructor with parameters.
		 * 
		 * @param key          key to cache.
	     * @param dataProvider provider of the values to be cached.
		 */
		public UpdateTask( Key key, DataProvider<Key,Value> dataProvider )
		{
			
			super();
			
			this.key = key;
			this.dataProvider = dataProvider;
			
		}
		
		
		/* ******************* */
		/*  INTERFACE METHODS  */
		/* ******************* */
		
		
		/**
		 * Retrieves the data related to the given key
		 * and creates a new entry to store into cache.
		 * 
		 */
		@Override
		public void run()
		{
			
			try{
				
				log.trace( "Updating value for key {}.", key );
				
				/* Retrieving data to cache. */
				final Value value = dataProvider.retrieve( key );
				
				/* Inserting new entry into cache. */
				cacheProvider.put( cacheRegion, key, value, cacheDuration );
				
			}catch( Exception ex )
			{
				
				log.warn( "Unable to update " + cacheRegion + " cache for key " + key, ex );
				
			}
			
		}
		
		
	}
	
}