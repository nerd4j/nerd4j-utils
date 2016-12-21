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

import org.nerd4j.util.CommandIterator;
import org.nerd4j.util.EqualsUtils;
import org.nerd4j.util.HashCoder;



/**
 * Abstract implementation of the {@link CacheKey} interface
 * with a consistent implementation of {@link #hashCode()},
 * {@link #equals(Object)} and {@link #serialize()}.
 * 
 * <p>
 * A class that extends this abstract implementation
 * only needs to provide an {@code enum} that contains
 * the properties involved.
 * 
 * @param <E> {@code enum} that contains the properties involved.
 * 
 * @author Nerd4j Team
 */
public abstract class AbstractCacheKey<E extends Enum<E>> implements CacheKey
{
	
	/** Properties used to build the key. */
	private final Object[] properties;
	
	/**
	 * This value is intended to distinguish different versions
	 * of the data model related to the same key. If the data
	 * model of a cached value changes retrieving it can lead to
	 * a deserialization error. Changing the version will lead
	 * to a cache miss and the data will be updated.
	 */
	private final int version;
	
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
	 * @param enumClass class that enumerates the key properties.
	 * @param version   version of the data model related to the key.
	 */
	public AbstractCacheKey( Class<E> enumClass, int version )
	{
		
		super();
		
		this.hashCode = 0;
		this.serializedForm = null;
		
		this.version = version;
		this.properties = new Object[enumClass.getEnumConstants().length];
		
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
			hashCode = HashCoder.hashCode( 79, version, properties );
		
		return hashCode;
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals( Object obj )
	{
		
		if( this == obj ) return true;
		
		final AbstractCacheKey<?> other = EqualsUtils.castIfSameClass( obj, AbstractCacheKey.class );
		if( other == null ) return false;		
		
		return EqualsUtils.deepEqualsFields( this.version,    other.version,
			                                 this.properties, other.properties ); 
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String serialize()
	{
	
		if( serializedForm == null )
			serializedForm = buildStringKey();
		
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
	

	/* ***************** */
	/*  EXTENSION HOOKS  */
	/* ***************** */
	
	
	/**
	 * Returns the property identified by the given {@code enum}.
	 * 
	 * @param e identifier of the property.
	 * @param <V> type of the value. 
	 * @return value of the property.
	 */
	@SuppressWarnings("unchecked")
	protected <V> V get( E e )
	{
		
		return (V) properties[e.ordinal()];
		
	}
	

	/**
	 * Sets the value of the property identified
	 * by the given {@code enum}.
	 * 
	 * @param e identifier of the property.
	 * @param v the value to set.
	 * @param <V> type of the value. 
	 */
	protected <V> void set( E e, V v )
	{
		
		properties[e.ordinal()] = v;
		
	}
	
	
	/* ***************** */
	/*  PRIVATE METHODS  */
	/* ***************** */
	
	
	/**
	 * Builds the serialized form of the key.
	 * <p>
	 * This is done by putting beside each property
	 * separated by a colon (:) and followed by the
	 * name of the class and the model version.
	 *  
	 * @return a serialized form of the key.
	 */
	private String buildStringKey()
	{
		
		final StringBuilder sb = new StringBuilder();
		
		if( properties != null && properties.length > 0 )
			addProperties( sb );
		
		addTail( sb );
		
		return sb.toString();		
		
	}

	/**
	 * Adds to the given {@link StringBuffer}
	 * the key properties separated by a colon.
	 * 
	 * @param sb the buffer used to build the serialized form.
	 */
	private void addProperties( final StringBuilder sb )
	{
		
		for( Object property : properties )
		{
				
			if( property == null ) continue;
				
			if( property instanceof Iterable || property.getClass().isArray() )
			{
					
				sb.append( '[' );
				CommandIterator.apply( new CommandIterator.Command()
				{
						
					@Override
					public void executeOn( Object value )
					{
						sb.append( value ).append( ',' );
					}
					
				}, property );
					
				if( sb.charAt(sb.length()-1) == ',' )
					sb.setLength( sb.length() - 1 );
					
				sb.append( ']' );
					
			}
			else
				sb.append( property );
				
			sb.append( ':' );
				
		}
		
	}
	
	
	/**
	 * Adds after the properties the name of the
	 * class and the version of the related data model.
	 * 
	 * @param sb the buffer used to build the serialized form.
	 */
	private void addTail( StringBuilder sb )
	{
		
		final String className = getClass().getSimpleName();
		sb.append( className )
		  .append( "-v" ).append( version );
		
	}
	
}