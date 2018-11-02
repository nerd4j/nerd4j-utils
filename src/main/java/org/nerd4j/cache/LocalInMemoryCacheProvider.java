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

import java.util.concurrent.locks.ReadWriteLock;

import org.nerd4j.lang.SpoolingLinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of the {@link CacheProvider} interface
 * that uses the local memory as cache storage.
 * 
 * <p>
 * This implementation uses the {@link SpoolingLinkedHashMap}
 * as cache engine and the local heap memory as storage.
 * 
 * <p>
 * This class was first implemented using {@link ReadWriteLock}
 * but benchmarks showed that, in this case, synchronization is
 * 6 times faster because the overhead of acquiring the lock is
 * dominant related to the time requested by read/write operations.
 * 
 * <p>
 * So, even if {@link ReadWriteLock}s allow concurrent reads, the time to
 * acquire the lock is greater than the time spent to perform a synchronized read.
 * 
 * 
 * @param <Value> type of values to cache.
 * 
 * @author Nerd4j Team
 */
public class LocalInMemoryCacheProvider<Value> extends AbstractCacheProvider<Value>
{
	
	/** Logging system. */
	private static final Logger log = LoggerFactory.getLogger( LocalInMemoryCacheProvider.class );
	
	/** Minimum number of entries the cache is able to store. */
	private static final int MIN_SIZE = 16;
	
	/** Default number of entries the cache is able to store. */
	private static final int DEFAULT_SIZE = 128;
	
	
	/** The cache engine, performs space occupation and eviction strategies. */
    private final SpoolingLinkedHashMap<String,CacheEntry<Value>> cache;
	
	
	/**
	 * Default constructor.
	 * 
	 */
	public LocalInMemoryCacheProvider()
	{
		
		this( DEFAULT_SIZE );
		
	}
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param size the number of entries the cache is able to store.
	 */
	public LocalInMemoryCacheProvider( int size )
	{
		
		this( size, DEFAULT_DURATION_ADJUSTMENT );
		
	}
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param durationAdjustment the cache duration adjustment.
	 */
	public LocalInMemoryCacheProvider( float durationAdjustment )
	{
		
		this( DEFAULT_SIZE, durationAdjustment );
		
	}
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param size the number of entries the cache is able to store.
	 * @param durationAdjustment the cache duration adjustment.
	 */
	public LocalInMemoryCacheProvider( int size, float durationAdjustment )
	{
		
		super( durationAdjustment );
		
		final int maxCapacity = size >= MIN_SIZE ? size : MIN_SIZE;
		
		this.cache = new SpoolingLinkedHashMap<String,CacheEntry<Value>>( maxCapacity, MIN_SIZE, 0.75f, true );
		
		log.info( "Created a new {} with cache size {}", LocalInMemoryCacheProvider.class.getSimpleName(), size );
		
	}
	
	
	/* **************** */
	/*  PUBLIC METHODS  */
	/* **************** */
	
	
	/**
	 * Returns the size of the internal {@link SpoolingLinkedHashMap}.
	 * 
	 * @return the size of the internal {@link SpoolingLinkedHashMap}.
	 */
	public synchronized int size()
	{
		
		return cache.size();
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void empty()
	{
		
		try{
			
			cache.clear();
									
		}catch( Exception ex )
		{
			
			log.error( "Unable to empty the cache", ex );
			
		}
		
	}
	
	
	/* ***************** */
	/*  EXTENSION HOOKS  */
	/* ***************** */
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected synchronized CacheEntry<Value> get( String key )
	{
		
		try{
        
			return cache.get( key );				
			
		}catch( Exception ex )
		{
			
			log.error( "Unable to read cache for key " + key, ex );
			return null;
			
		}
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean touch( String key, int duration )
	{
		
		try{
			
			/*
			 * By contract this method should be called when
			 * an entry is not present or has expired.
			 * If the entry exists and is not expired means that:
			 * 1. the contract has been broken.
			 * 2. the entry has been "touched" by another thread short before.
			 * In both cases the operation fails.
			 */
			final CacheEntry<Value> currentEntry = get( key );
			if( currentEntry != null && ! currentEntry.hasExpired() )
			{
				log.trace( "Entry for key {} has been already touched.", key );
				return false;
			}
			
			/*
			 * Otherwise the current entry (regardless if expired or nonexistent)
			 * will be replaced by an updated one.
			 */
			final CacheEntry<Value> touchedEntry = getTouched( currentEntry, duration );
			put( key, touchedEntry, duration );
			
			return true;
									
		}catch( Exception ex )
		{
			
			log.error( "Unable to touch cache for key " + key, ex );
			return false;
			
		}
		
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected synchronized void put( String key, CacheEntry<Value> entry, int duration )
	{
		
		try{
			
			cache.put( key, entry );
									
		}catch( Exception ex )
		{
			
			log.error( "Unable to populate cache for key " + key, ex );
			
		}
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected synchronized void remove( String key )
	{
		
		try{
			
			cache.remove( key );
									
		}catch( Exception ex )
		{
			
			log.error( "Unable to remove key " + key, ex );
			
		}
		
	}
	
}