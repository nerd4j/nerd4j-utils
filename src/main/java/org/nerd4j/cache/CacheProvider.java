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



/**
 * Represents a provider of a caching system.
 * 
 * <p>
 *  Usually a caching system returns the value related to a given key
 *  if it is present ad still valid and {@code null} if it is not
 *  present or expired. The implementations of this interface need
 *  to return {@code null} if the given key is not present and an
 *  object of type {@link CacheEntry} otherwise. The {@link CacheEntry}
 *  need to be returned even if expired.
 * </p>
 * <p>
 *  The concept behind this choice is to perform the following behaviour:
 *  <ol>
 *   <li>
 *     if the key is not present (i.e. is never been cached)
 *     the related value will be retrieved and inserted.
 *   </li>
 *   <li>
 *     if the key is present and still valid, the related value will be returned.
 *   </li> 
 *   <li>
 *     if the key is present but expired the expiration will be updated
 *     (calling {@link CacheProvider#touch(CacheKey, long)}) and the value
 *     will be updated.
 *   </li> 
 *  </ol>
 * </p>
 * <p>
 *  If more threads are requesting the same key and the key is expired, only the
 *  first thread invoking {@link CacheProvider#touch(CacheKey, long)} will
 *  succeed, for the other threads the invocation will fail.
 *  This way the first thread will update the value related to the key
 *  while the other threads will consider the cached value as still valid.
 * </p>
 * <p>
 *  If {@link CacheProvider#touch(CacheKey, long)} is called for a key
 *  not present in the cache an empty {@link CacheEntry} will be cached
 *  in relation to the given key.
 * </p>
 * 
 * @param <Value> type of the objects stored in the cache.
 * 
 * @author Nerd4j Team
 */
public interface CacheProvider<Value>
{
	
	/**
	 * Returns the cache entry related to the given key and region.
	 * <p>
	 *  If the given key is never been cached {@code null} will be returned,
	 *  otherwise an object of type {@link CacheEntry} will be returned
	 *  containing the related value and expiration time.
	 *  The value in the {@link CacheEntry} can be {@code null}.
	 * </p>
	 * <p>
	 *  Unlike other caching systems, this version returns an object
	 *  even if the related key is expired. This choice is made to
	 *  allow a given key to be available in cache during the update.
	 * </p>
	 * 
	 * @param region the cache region to scan, can be {@code null}.
	 * @param key    the key to search for, is mandatory.
	 * @return the related entry if present, {@value null} otherwise.
	 */
	public CacheEntry<Value> get( String region, CacheKey key );
	
	/**
	 * Inserts the given key and the related value into the 
	 * given cache region. 
	 * <p>
	 *  If the given key is already present in the given region
	 *  will be replaced.
	 * </p> 
	 * 
	 * @param region   the cache region where to put the key, can be {@code null}.
	 * @param key      key to be cached.
	 * @param value    value to be cached.
	 * @param duration number of seconds until expiration.
	 */
	public void put( String region, CacheKey key, Value value, int duration );
	
	/**
	 * Updates the expiration time of the entry related to the given
	 * key and region.
	 * <p>
	 *  If the key is not present in the given region an empty 
	 *  entry will be created and cached.
	 * </p>
	 * 
	 * @param region   the cache region to scan, can be {@code null}.
	 * @param key      the key to search for, is mandatory.
	 * @param duration number of seconds until expiration.
	 * @return {@code true} if the operation was successful.
	 */
	public boolean touch( String region, CacheKey key, int duration );
	
	/**
	 * Removes the given key from the given cache region.
	 * <p>
	 *  If the key is not present nothing will be done.
	 * </p> 
	 * 
	 * @param region the cache region where to remove the key, can be {@code null}.
	 * @param key    the key to be removed, is mandatory.
	 */
	public void remove( String region, CacheKey key );
	

}