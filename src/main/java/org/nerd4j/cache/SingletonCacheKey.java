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

import org.nerd4j.util.EqualsUtils;
import org.nerd4j.util.HashCoder;
import org.nerd4j.util.Require;



/**
 * Implementation of the {@link CacheKey} interface
 * intended to be used if there are a singleton
 * value to be cached.
 * 
 * <p>
 * In this case there is no need to distinguish cache
 * entries because there is only one entry available.
 * 
 * @author Nerd4j Team
 */
public class SingletonCacheKey implements CacheKey
{
	
	/**
	 * This value is intended to distinguish different versions
	 * of the data model related to the same key. If the data
	 * model of a cached value changes retrieving it can lead to
	 * a deserialization error. Changing the version will lead
	 * to a cache miss and the data will be updated.
	 */
	private final int version;
	
	/**
	 * The name of the singleton element to cache.
	 * This is useful since you may need to add different
	 * singleton values to the same cache region.
	 * Giving a different name to each value will avoid
	 * key conflicts.
	 */
	private final String name;
	
	/**
	 * Serialized form of the key.
	 * This implementation of {@link CacheKey} is intended
	 * to be immutable so the value of the serialized form
	 * can be stored.
	 */
	private transient String serializedForm;
	
	/**
	 * This implementation of {@link CacheKey} is intended
	 * to be immutable so the value of the hash code can
	 * be stored.
	 */
	private transient int hashCode;
	
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param name    the name of the singleton element to cache.
	 * @param version version of the data model related to the key.
	 */
	public SingletonCacheKey( String name, int version )
	{
		
		super();
		
		this.hashCode = 0;
		this.serializedForm = null;
		
		this.name    = Require.nonEmpty( name, "The name of the singleton value is mandatory" );
		this.version = version;
		
	}
	
	
	/* ******************* */
	/*  INTERFACE METHODS  */
	/* ******************* */
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		
		if( hashCode == 0 )
			hashCode = HashCoder.hashCode( 79, version, name );
		
		return hashCode;
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals( Object obj )
	{
		
		if( this == obj ) return true;
		
		final SingletonCacheKey other = EqualsUtils.castIfSameClass( this, obj );
		if( other == null ) return false;		
		
		return EqualsUtils.equalsFields( this, other,
										 key -> key.version,
										 key -> key.name ); 
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String serialize()
	{
	
		if( serializedForm == null )
			serializedForm = name + "-v" + version;
		
		return serializedForm;
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		
		return serialize();
		
	}
	
}