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

import org.nerd4j.format.AnnotatedFormattedBean;
import org.nerd4j.format.Formatted;
import org.nerd4j.util.DataConsistency;
import org.nerd4j.util.EqualsUtils;
import org.nerd4j.util.HashCoder;


/**
 * Represents an entry in the caching system related to a given key.
 * <p>
 *  The entry provides also an expiration time after which data
 *  should be updated.
 * </p>
 * 
 * @param <V> type of data in the cache entry.
 * 
 * @author Nerd4j Team
 */
public class CacheEntry<V> extends AnnotatedFormattedBean
{
	
	/** Serial Version UID. */
	private static final long serialVersionUID = 1L;
	
	
	/** Unix Timestamp (in seconds) when the entry expires. */
	@Formatted
	private int expiration;
	
	/** Value of the current cache entry. */
	@Formatted
	private V value;
	
	
	/**
	 * Default constructor.
	 * <p>
	 *  This constructor is intended to be used by reflection
	 *  during deserialization. Do not remove or some implementations
	 *  could break!
	 * </p>
	 * 
	 */
	protected CacheEntry()
	{
		
		super();
		
		this.value = null;
		this.expiration = 0;
		
	}
	
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param value    value to be cached.
	 * @param duration duration in seconds before expiration.
	 */
	public CacheEntry( V value, int duration )
	{
		
		super();
		
		DataConsistency.checkIfTrue( "duration >= 0", duration >= 0 );
		
		this.value = value;
		
		final int now = (int) System.currentTimeMillis() / 1000;
		this.expiration = now + duration;
		
	}
	
	
	/* ******************* */
	/*  GETTERS & SETTERS  */
	/* ******************* */
	
	
	public V getValue()
	{
		return value;
	}
	
	public int getExpiration()
	{
		return expiration;
	}
	
	public boolean hasExpired()
	{
		final int now = (int) System.currentTimeMillis() / 1000;
		return expiration < now;
	}
	
	/**
	 * This setter is intended to be used by reflection during deserialization.
	 * 
	 * @param expiration Unix Timestamp (in seconds) when the entry expires.
	 */
	protected void setExpiration( int expiration )
	{
		this.expiration = expiration;
	}
	
	/**
	 * This setter is intended to be used by reflection during deserialization.
	 * 
	 * @param value the value of the current cache entry.
	 */
	protected void setValue( V value )
	{
		this.value = value;
	}
	
	
	/* ******************** */
	/*  COMPARISON METHODS  */
	/* ******************** */
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		
		return HashCoder.hashCode( 79, expiration, value );
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals( Object obj )
	{
		
		if( this == obj ) return true;
		final CacheEntry<?> other = EqualsUtils.castIfSameClass( obj, CacheEntry.class );
		if( other == null ) return false;
		
		return EqualsUtils.deepEqualsFields( this.expiration, other.expiration,
				                             this.value, other.value );
		
	}	
	
}
