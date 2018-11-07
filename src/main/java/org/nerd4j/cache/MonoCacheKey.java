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



/**
 * Implementation of the {@link CacheKey} interface
 * representing a mono-dimensional key space.
 * 
 * <p>
 * This is an optimized implementation to use when
 * the cache key is made of a single value.
 * For example an ID. 
 * 
 * @param <Value> type of the value used in the key.
 * 
 * @author Nerd4j Team
 */
public class MonoCacheKey<Value> implements CacheKey
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
	 * The value used to set the key.
	 */
	private final Value value;
	
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
	 * @param value     the value used to set the key.
	 * @param version   version of the data model related to the key.
	 */
	public MonoCacheKey( Value value, int version )
	{
		
		super();
		
		this.hashCode = 0;
		this.serializedForm = null;
		
		this.value   = value;
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
			hashCode = HashCoder.hashCode( 79, value, version );
		
		return hashCode;
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals( Object obj )
	{
		
		if( this == obj ) return true;
		
		final MonoCacheKey<?> other = EqualsUtils.castIfSameClass( this, obj );
		if( other == null ) return false;		
		
		return EqualsUtils.deepEqualsFields( this, other,
										 	 key -> key.value,
										 	 key -> key.version ); 
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String serialize()
	{
	
		if( serializedForm == null )
			serializedForm = value + "-MonoCacheKey-v" + version;
		
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