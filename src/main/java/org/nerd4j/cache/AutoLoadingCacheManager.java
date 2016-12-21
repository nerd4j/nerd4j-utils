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
 * Represents a cache manager able to query and
 * populate the underlying caching system.
 * <p>
 * This manager queries the underlying {@link CacheProvider}
 * for the given key, if the related entry is {@code null}
 * or expired uses the given {@link DataProvider} to retrieve
 * the related data and populate the cache.
 * 
 * <p>
 * The assumption behind this interface is that the
 * given key is able to uniquely identify the related
 * data and therefore contains all the information 
 * needed to retrieve it.
 * 
 * <p>
 * If a given key is not present in the cache uses the given 
 * {@link DataProvider} to retrieve the related data, if the
 * key is present but expired will be marked as still valid
 * and the {@link DataProvider} will be used to update the
 * related value.
 * 
 * @param <Key>   type of the cache key.
 * @param <Value> type of the related data.
 * 
 * @author Nerd4j Team
 */
public interface AutoLoadingCacheManager<Key extends CacheKey, Value>
{
	
	/**
	 * Returns the value related to the given key.
	 * <p>
	 * If the given key is not present in cache or
	 * the related entry is expired the given
	 * {@link DataProvider} will be used to retrieve
	 * the data to store into cache.
	 * 
	 * @param key          the key to search for.
	 * @param dataProvider the provider of the information to cache.
	 * @return the value related to the given key, can be {@code null}.
	 */
	public Value get( Key key, DataProvider<Key,Value> dataProvider );
	
	/**
	 * Removes the given key from the underlying cache system
	 * forcing a reload the next time {@link #get(CacheKey, DataProvider)}
	 * is invoked.
	 * 
	 * @param key key to be evicted.
	 */
	public void evict( Key key );

}