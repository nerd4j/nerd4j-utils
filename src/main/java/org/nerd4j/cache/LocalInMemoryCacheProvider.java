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

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import org.nerd4j.lang.SpoolingLinkedHashMap;
import org.nerd4j.util.Require;
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
	
	/** Lock to use to serialize write operations. */
	private final ReadWriteLock lock;
	
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
		
		super();
		
		Require.toHold( size >= MIN_SIZE, "The cache size must be >= " + MIN_SIZE );
		
		this.lock = new ReentrantReadWriteLock();
		this.cache = new SpoolingLinkedHashMap<String,CacheEntry<Value>>( size, MIN_SIZE, 0.75f, true );
		
		log.info( "Created a new {} with cache size {}", LocalInMemoryCacheProvider.class.getSimpleName(), size );
		
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
	 */
	public LocalInMemoryCacheProvider( int size, float durationAdjustment )
	{
		
		super( durationAdjustment );
		
		Require.toHold( size >= MIN_SIZE, "The cache size must be >= " + MIN_SIZE );
		
		this.lock = new ReentrantReadWriteLock();
		this.cache = new SpoolingLinkedHashMap<String,CacheEntry<Value>>( size, MIN_SIZE, 0.75f, true );
		
		log.info( "Created a new {} with cache size {}", LocalInMemoryCacheProvider.class.getSimpleName(), size );
		
	}
	
	
	/* ***************** */
	/*  EXTENSION HOOKS  */
	/* ***************** */
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected CacheEntry<Value> get( String key )
	{
		
		final Lock readLock = lock.readLock();
		
		try{
        
			/* Before reading the cache we take a read lock. */
			readLock.lock();
			return cache.get( key );
			
		}catch( Exception ex )
		{
			
			log.error( "Unable to read cache for key " + key, ex );
			return null;
			
		}finally
		{
			
			/* In any case we need to release the read lock. */
			readLock.unlock();
			
		}
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected boolean touch( String key, int duration )
	{
		
		final Lock writeLock = lock.writeLock();
		
		try{
			
			/* Before updating the cache we take a write lock. */
			writeLock.lock();
						 
			
			/*
			 * By contract this method should be called when
			 * an entry is not present or has expired.
			 * If the entry exists and is not expired means that:
			 * 1. the contract has been broken.
			 * 2. the entry has been "touched" by another thread short before.
			 * In both cases the operation fails.
			 */
			final CacheEntry<Value> currentEntry = cache.get( key );
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
			cache.put( key, touchedEntry );
			return true;
									
		}catch( Exception ex )
		{
			
			log.error( "Unable to touch cache for key " + key, ex );
			return false;
			
		}finally
		{
			
			/* In any case we need to release the write lock. */
			writeLock.unlock();
			
		}
		
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void put( String key, CacheEntry<Value> entry, int duration )
	{
		
		execute(
			() -> cache.put( key, entry ),
			() -> "Unable to populate cache for key " + key
		);
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void remove( String key )
	{
		
		execute(
			() -> cache.remove( key ),
			() -> "Unable to remove key " + key
		);
		
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void empty()
	{
		
		execute(
			() -> cache.clear(),
			() -> "Unable to empty the cache"
		);
		
	}
	
	
	/* ***************** */
	/*  PRIVATE METHODS  */
	/* ***************** */
	
	
	/**
	 * Executes the given operation ensuring the write lock to be called
	 * and released and ensuring all errors to be caught and logged.
	 * 
	 * @param operation the write operation to execute.
	 * @param errorMessage the error message supplier to invoke in case of error.
	 */
	private void execute( Runnable operation, Supplier<String> errorMessage )
	{
		
		final Lock writeLock = lock.writeLock();
		
		try{
			
			/* Before updating the cache we take a write lock. */
			writeLock.lock();
			
			/* We apply the given write operation. */
			operation.run();
									
		}catch( Exception ex )
		{
			
			/* 
			 * In case of error we only log the error because errors
			 * in the cache should not break the execution. 
			 */
			log.error( errorMessage.get(), ex );
			
		}finally
		{
			
			/* In any case we need to release the write lock. */
			writeLock.unlock();
			
		}
		
	}
	
}