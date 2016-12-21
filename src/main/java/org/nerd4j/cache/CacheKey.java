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
 * Represents the search key used by the {@link AutoLoadingCacheManager}s
 * and the related {@link CacheProvider}s.
 * 
 * <p>
 * All the implementations of this interface need to properly
 * implement the methods {@link #equals(Object)} e {@link #hashCode()}.
 * 
 * <p>
 * Besides all implementations of this interface need to properly
 * implement the {@link #serialize()} method so that the {@code region}
 * appears in the serialized form and {@code a.serialize().equals(b.serialize())}
 * if and only if {@code a.equals(b)}.
 * 
 * @author Nerd4j Team
 */
public interface CacheKey
{
	
	/**
	 * Creates a textual representation of the key such that:
	 * 
	 * if {@code a} and {@code b} are two instances of {@link CacheKey},
	 * {@code a.stringKey().equals(b.stringKey())} if and only if {@code a.equals(b)}.
	 * <p>
	 * All the implementations of {@link #serialize()} must add the
	 * {@code region} value to the serialized form.
	 * 
	 * @return textual representation of the key.
	 */
	public String serialize();
	
	/**
	 * All the implementation of this interface need to properly
	 * implement this method as described in the javaDoc of the
	 * class {@link Object}.
	 * 
	 * @return a properly computed hash code.
	 */
	@Override
	public int hashCode();

	/**
	 * All the implementation of this interface need to properly
	 * implement this method as described in the javaDoc of the
	 * class {@link Object}.
	 * 
	 * @return {@code true} if the current object equals the given one.
	 */
	@Override
	public boolean equals( Object obj );

}