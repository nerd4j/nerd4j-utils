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

import java.util.Random;

import org.nerd4j.util.DataConsistency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract class common to all the implementations of the {@link CacheProvider} interface.
 * 
 * <p>
 * This class provides an implementation of the common operations
 * for example the eviction strategies.
 * 
 * <p>
 * To prevent bulk operation to force all keys to expire in the same moment,
 * this implementation provides a way to randomize the cache duration within
 * configurable parameters.
 * 
 * @param <Value> type of data in the cache entry.
 * 
 * @author Nerd4j Team
 */
public abstract class AbstractCacheProvider<Value> implements CacheProvider<Value>
{
	
	/** Logging system. */
	private static final Logger log = LoggerFactory.getLogger( AbstractCacheProvider.class );
	
	/** Random numbers generator (threadsafe) */
	private static final Random RANDOM = new Random();
	
	/** Maximum cache duration adjustment. */
	private static final float MAX_DURATION_ADJUSTMENT = 0.5f;
	
	/** Minimum cache duration adjustment. */
	private static final float MIN_DURATION_ADJUSTMENT = 0f;
		
	/** Default cache duration adjustment. */
	private static final float DEFAULT_DURATION_ADJUSTMENT = 0.25f;
	
	
	/** Cache duration adjustment. */
	private final float durationAdjustment;
	
	
	/**
	 * Default constructor.
	 * 
	 */
	public AbstractCacheProvider()
	{
		
		super();
		
		this.durationAdjustment = DEFAULT_DURATION_ADJUSTMENT;
		
	}
	
	/**
	 * Constructor with parameters.
	 * <p>
	 * The provided value must be between {@link #MIN_DURATION_ADJUSTMENT}
	 * and {@link #MAX_DURATION_ADJUSTMENT}.
	 * 
	 * @param durationAdjustment the cache duration adjustment.
	 */
	public AbstractCacheProvider( float durationAdjustment )
	{
		
		super();
		
		DataConsistency.checkIfTrue( "durationAdjustment >= " + MIN_DURATION_ADJUSTMENT, durationAdjustment >= MIN_DURATION_ADJUSTMENT );
		DataConsistency.checkIfTrue( "durationAdjustment <= " + MAX_DURATION_ADJUSTMENT, durationAdjustment <= MAX_DURATION_ADJUSTMENT );
		
		this.durationAdjustment = durationAdjustment;
		
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
		
		try{
		
			/* 
			 * The data consistency checks are made into
			 * the try-catch block because a failure in
			 * the cache must not lead to a system failure.
			 * From the point of view of the system the
			 * cache must be invisible.
			 */
			DataConsistency.checkIfNotNull( "cache key", key );

			final String actualKey = buildActualKey( region, key );
			return get( actualKey );
			
		}catch( Exception ex )
		{
			
			log.error( "Unable to read cache for key " + key + " in region " + region, ex );
			return null;
			
		}
		
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void put( String region, CacheKey key, Value value, int duration )
	{
		
		try{

			/* 
			 * The data consistency checks are made into
			 * the try-catch block because a failure in
			 * the cache must not lead to a system failure.
			 * From the point of view of the system the
			 * cache must be invisible.
			 */
			DataConsistency.checkIfNotNull( "cache key", key );
			DataConsistency.checkIfStrictPositive( "duration", duration );
					
			final String actualKey = buildActualKey( region, key );
			final int actualDuration = random( duration );
			
			/* We create a new entry for the given key. */
			final CacheEntry<Value> entry = new CacheEntry<Value>( value, actualDuration );
			
			/*
			 * We insert the new entry into the cache.
			 * Usually the underlying cache system requires
			 * a duration (or expiration) parameter by
			 * itself. To get a good balance between
			 * data availability and space occupation we
			 * send to the underlying caching system
			 * a duration double than the one requested. 
			 */
			put( actualKey, entry, actualDuration << 1 );
									
		}catch( Exception ex )
		{
			
			log.error( "Unable to populate cache for key " + key + " in region " + region, ex );
			
		}
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean touch( String region, CacheKey key, int duration )
	{
		
		try{
			
			/* 
			 * The data consistency checks are made into
			 * the try-catch block because a failure in
			 * the cache must not lead to a system failure.
			 * From the point of view of the system the
			 * cache must be invisible.
			 */
			DataConsistency.checkIfNotNull( "cache key", key );
			DataConsistency.checkIfStrictPositive( "duration", duration );
			
			final String actualKey = buildActualKey( region, key );
			
			/* We update the underlying caching system expiration. */
			return touch( actualKey, duration );
									
		}catch( Exception ex )
		{
			
			log.error( "Unable to touch cache entry for key " + key + " in region " + region, ex );
			throw ex;
			
		}
		
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove( String region, CacheKey key )
	{
		
		try{
			
			/* 
			 * The data consistency checks are made into
			 * the try-catch block because a failure in
			 * the cache must not lead to a system failure.
			 * From the point of view of the system the
			 * cache must be invisible.
			 */
			DataConsistency.checkIfNotNull( "cache key", key );
			
			final String actualKey = buildActualKey( region, key );
			remove( actualKey );
									
		}catch( Exception ex )
		{
			
			log.error( "Unable to remove key " + key + " in region " + region, ex );
			
		}
		
	}
	
    
	/* ***************** */
	/*  PRIVATE METHODS  */
	/* ***************** */
	
	
	/**
	 * Builds the actual key used in the underlying cache system
	 * combining the region and the provided cache key.
	 * 
	 * @param region the cache region to scan.
	 * @param key    the key to search for.
	 * @return the actual key to use in the underlying system.
	 */
	private String buildActualKey( String region, CacheKey key )
	{
		
		final String serializedKey = key.serialize();
		if( region != null )
			return serializedKey + "-" + region;
		else
			return serializedKey;
		
	}
	
	/**
	 * Returns the duration adjusted by a random value
	 * within the configured bounds.
	 * <p>
	 * The returned duration belongs to the interval:
	 * <pre>
	 * [ duration-durationAdjustment, duration+durationAdjustment ]
	 * </pre>
	 * 
	 * @param duration the duration to adjust.
	 * @return the modified duration.
	 */
	private int random( int duration )
	{
		
		if( durationAdjustment <= 0 ) return duration;
		
		/* A random value between durationAdjustment and 2 * durationAdjustment. */
		final float randomAdjustment = RANDOM.nextFloat() * durationAdjustment * 2;
		
		/* A random value between -durationAdjustment and +durationAdjustment. */
		final float actualAdjustment = randomAdjustment - durationAdjustment;
		
		/* The adjustment to add to the duration. */
		final int durationDelta = (int)(actualAdjustment * duration);
		
		/* The duration properly adjusted. */
		return duration + durationDelta;
		
	}
	
	
	/* ******************* */
	/*  PROTECTED METHODS  */
	/* ******************* */
	
	
	/**
	 * Returns a new cache entry which value is the same
	 * as the given one and the expiration time is postponed
	 * by the given amount of seconds.
	 *  
	 * @param entry    the cache entry to update.
	 * @param duration the amount of seconds to postpone expiration.
	 * @return the updated entry.
	 */
	protected CacheEntry<Value> getTouched( CacheEntry<Value> entry, int duration )
	{
		
		DataConsistency.checkIfStrictPositive( "duration", duration );
		
		final Value value = entry != null ? entry.getValue() : null; 
		return new CacheEntry<Value>( value, duration );
		
	}
	
	
	/* ***************** */
	/*  EXTENSION HOOKS  */
	/* ***************** */
	
	
	/**
	 * Returns the cache entry related to the given key.
	 * <p>
	 * If the given key was never being cached {@code null}
	 * will be returned, otherwise will be returned a
	 * {@link CacheEntry} containing the cached value 
	 * and the expiration time.
	 * 
	 * @param key the cache key to search for.
	 * @return the entry related to the given key if any, {@code null} otherwise.
	 */
	protected abstract CacheEntry<Value> get( String key );
	
	/**
	 * Binds the given entry to the given key and
	 * put it into the underlying cache.
	 * <p>
	 * If the given key is already present into the cache
	 * will be replaced.
	 * 
	 * @param key      key to be cached.
	 * @param entry    entry to be cached.
	 * @param duration number of seconds until expiration.
	 */
	protected abstract void put( String key, CacheEntry<Value> entry, int duration );
	
	/**
	 * Postpones the expiration time of the cache entry related to the given key.
	 * <p>
	 * If no entry is present for the given key a new one will be created with value {@code null}.
	 * <p>
	 * Only the first thread calling this method for the given key should be successful,
	 * any other thread should fail. This method returns {@code true} if the operation
	 * was successful and {@code false} otherwise.
	 * 
	 * @param key      key to update.
	 * @param duration number of seconds until expiration.
	 * @return {@code true} if the related entry has been updated.
	 */
	protected abstract boolean touch( String key, int duration );
	
	/**
	 * Removes the given key and the related entry from the cache.
	 * <p>
	 * If the key is not present nothing will be done.
	 * 
	 * @param key key to be removed.
	 */
	protected abstract void remove( String key );
	
}