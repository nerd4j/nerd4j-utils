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

import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import org.nerd4j.lang.ArrayIterator;
import org.nerd4j.util.EqualsUtils;
import org.nerd4j.util.HashCoder;
import org.nerd4j.util.Require;



/**
 * Simple implementation of the {@link CacheKey} interface
 * with a consistent implementation of {@link #hashCode()},
 * {@link #equals(Object)} and {@link #serialize()}.
 * 
 * <p>
 * This implementation requires a name used to distinguish
 * two keys with the same values and stored in the same cache
 * region that point to different entries. And a version
 * used to distinguish different models with the same key values.
 * 
 * <p>
 * This implementation uses an internal array to store the
 * values that compose the key.
 * 
 * <p>
 * For make it easier to build keys from data this class provides
 * a {@link Builder} that can be configured with name and version
 * and creates keys based on the given values.
 * 
 * <p>
 * The builder is thread safe and can be stored as static object.
 *
 * 
 * @author Nerd4j Team
 */
public class SimpleCacheKey implements CacheKey
{
	
	/** Values used to build the key. */
	private final Object[] values;
	
	/**
	 * This value is intended to distinguish different versions
	 * of the data model related to the same key. If the data
	 * model of a cached value changes retrieving it can lead to
	 * a deserialization error. Changing the version will lead
	 * to a cache miss and the data will be updated.
	 */
	private final int version;
		
	/**
	 * The name of the cache key values space.
	 * This is useful since you may need to add key spaces
	 * with the same value ranges to the same cache region.
	 * Giving a different name to each space will avoid
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
	 * @param name      name of the cache key space.
	 * @param version   version of the data model related to the key.
	 */
	public SimpleCacheKey( String name, int version )
	{
		
		this( null, name, version );
		
	}
	
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param values    values used to build the key.
	 * @param name      name of the cache key space.
	 * @param version   version of the data model related to the key.
	 */
	public SimpleCacheKey( Object[] values, String name, int version )
	{
		
		super();
		
		this.hashCode = 0;
		this.serializedForm = null;
		
		this.name       = Require.nonEmpty( name, "The name of the key values space is mandatory" );
		this.version    = version;
		this.values     = values;
		
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
			hashCode = HashCoder.hashCode( 79, version, name, values );
		
		return hashCode;
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals( Object obj )
	{
		
		if( this == obj ) return true;
		
		final SimpleCacheKey other = EqualsUtils.castIfSameClass( this, obj );
		if( other == null ) return false;		
		
		return EqualsUtils.deepEqualsFields( this, other,
											 key -> key.version,
											 key -> key.name,
											 key -> key.values );
		
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
		
		final StringBuilder sb = new StringBuilder( 100 );
		
		addValues( sb );
		addTail( sb );
		
		return sb.toString();		
		
	}

	/**
	 * Adds to the given {@link StringBuffer}
	 * the key properties separated by a colon.
	 * 
	 * @param sb the buffer used to build the serialized form.
	 */
	private void addValues( StringBuilder sb )
	{
		
		if( values == null || values.length == 0 )
			return;
		
		add( ArrayIterator.create(values),
				elem -> add(elem, sb),
				elem -> add(':', elem, sb),
				"", "", sb );
		
	}
	
	/**
	 * Adds the given value to the {@link StringBuilder}.
	 * 
	 * @param value value to add.
	 * @param sb    {@link StringBuilder} to update.
	 */
	private void add( Object value, StringBuilder sb )
	{
		
		if( value == null ) return;
		
		if( value instanceof Iterable )
			add( ((Iterable<?>) value).iterator(),
					elem -> add(elem, sb),
					elem -> add(',', elem, sb),
					"[", "]", sb );
		
		else if( value.getClass().isArray() )
			add( ArrayIterator.create(value),
					elem -> add(elem, sb),
					elem -> add(',', elem, sb),
					"[", "]", sb );
		
		else if( value instanceof Map )
			add( ((Map<?,?>) value).entrySet().iterator(),
					elem -> add(elem, sb),
					elem -> add(',', elem, sb),
					"{", "}", sb );
		
		else if( value instanceof Map.Entry )
			addMapEntry( value, sb );
		
		else
			sb.append( value );
		
	}
	
	/**
	 * Adds the given separator if needed and then
	 * adds the given value to the {@link StringBuilder}.
	 * 
	 * @param separator the separator character to add if needed.
	 * @param value     value to add.
	 * @param sb        {@link StringBuilder} to update.
	 */
	private void add( char separator, Object value, StringBuilder sb )
	{
		
		final int l = sb.length();
		if( l > 0 && value != null )
		{
			
			final char c = sb.charAt( l-1 );
			if( c != '[' && c != '{' )
				sb.append( separator );
			
		}
		
		add( value, sb );
		
	}
	
	/**
	 * Adds the elements in the given iterator to the {@link StringBuilder}.
	 * 
	 * @param values the iterator containing the elements add.
	 * @param first  logic to apply to the first element.
	 * @param others logic to apply to the other elements.
	 * @param prefix string to ad before all elements.
	 * @param suffix string to ad after all elements. 
	 * @param sb     {@link StringBuilder} to update.
	 */
	private void add( Iterator<?> values, Consumer<Object> first, Consumer<Object> others,
			          String prefix, String suffix, StringBuilder sb )
	{
		
		sb.append( prefix );
				
		if( values.hasNext() )
		{
		
			first.accept( values.next() );
			while( values.hasNext() )
				others.accept( values.next() );
			
		}
		
		sb.append( suffix );
		
	}
	
	/**
	 * Adds the given map entry to the {@link StringBuilder}.
	 * 
	 * @param values    the map entry to add.
	 * @param sb        {@link StringBuilder} to update.
	 */
	private void addMapEntry( Object value, StringBuilder sb )
	{
		
		final Map.Entry<?,?> entry = (Map.Entry<?,?>) value;
		
		add( entry.getKey(), sb );
		sb.append( '=' );
		add( entry.getValue(), sb );
		
	}
		
	/**
	 * Adds after the properties the name of the
	 * class and the version of the related data model.
	 * 
	 * @param sb the buffer used to build the serialized form.
	 */
	private void addTail( StringBuilder sb )
	{
		
		if( sb.length() > 0 )
			sb.append( '-' );
		
		sb.append( name )
		  .append( "-v" )
		  .append( version );
		
	}
	
	
	/* ***************** */
	/*  FACTORY METHODS  */
	/* ***************** */
	
	
	/**
	 * Creates a new {@link SimpleCacheKey.Builder}.
	 * 
	 * @param name      name of the cache key space.
	 * @param version   version of the data model related to the key.
	 * @return new {@link SimpleCacheKey.Builder}.
	 */
	public static Builder builder( String name, int version )
	{
		
		return new Builder( name, version );
		
	}
	
	
	/* *************** */
	/*  INNER CLASSES  */
	/* *************** */
	
	
	/**
	 * Utility class to build {@link SimpleCacheKey} instances in a clean way.
	 * 
	 * <p>
	 * This builder is created providing the {@code name} and {@code version}
	 * fields and provides a factory method to build the keys based on the
	 * actual values.
	 * 
	 * 
	 * @author Nerd4j Team
	 */
	public static class Builder
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
		 * The name of the cache key values space.
		 * This is useful since you may need to add key spaces
		 * with the same value ranges to the same cache region.
		 * Giving a different name to each space will avoid
		 * key conflicts.
		 */
		private final String name;

		
		/**
		 * Constructor with parameters.
		 * 
		 * @param name      name of the cache key space.
		 * @param version   version of the data model related to the key.
		 */
		private Builder( String name, int version )
		{
			
			super();
			
			this.name       = Require.nonEmpty( name, "The name of the key values space is mandatory" );
			this.version    = version;
			
		}
		
		
		/* **************** */
		/*  PUBLIC METHODS  */
		/* **************** */
		
		
		/**
		 * Creates a new {@link SimpleCacheKey} with the given values.
		 * 
		 * @param values values used to create the key.
		 * @return a new {@link SimpleCacheKey}
		 */
		public SimpleCacheKey get( Object... values )
		{
			
			return values == null || values.length == 0
					? new SimpleCacheKey( name, version )
					: new SimpleCacheKey( values, name, version );
			
		}
		
	}
	
}