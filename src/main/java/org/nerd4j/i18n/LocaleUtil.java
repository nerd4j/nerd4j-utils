/*
 * #%L
 * Nerd4j Utils
 * %%
 * Copyright (C) 2011 - 2015 Nerd4j
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
package org.nerd4j.i18n;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Utility methods to handle {@link Locale} instances.
 * 
 * @author Nerd4j Team
 */
public class LocaleUtil
{

	/** Generated locale cache */
	private static final Map<String,Locale> cache = new ConcurrentHashMap<String,Locale>();
	
	/** Locale split pattern from string as "en_UK_" */
	private static final Pattern LOCALE_SPLIT = Pattern.compile("_");
	
	/**
	 * Find the locale with given standard name.
	 * <p>
	 * Locale name must be in form "ll_CC_variant" with:
	 * <ul>
	 * <li><tt>ll</tt>: lowercase two-letter language ISO-639 code</li>
	 * <li><tt>CC</tt>: uppercase two-letter country ISO-3166 code</li>
	 * <li><tt>ll_CC_variant</tt>: language variant</li>
	 * </ul>
	 * </p>
	 * <p>
	 * A similar method was added in Java 7 {@code Locale.forLanguageTag(String)}.
	 * </p>
	 * 
	 * @param name locale name
	 * 
	 * @return desired locale.
	 * 
	 * @throws NullPointerException if given locale name is null
	 */
	public static final Locale getLocale( final String name ) throws NullPointerException
	{
		
		if ( name == null )
			throw new NullPointerException( "Locale name cannot be null" );
		
		Locale locale = cache.get( name );
		
		if ( locale == null )
		{
			
			final String[] splits = LOCALE_SPLIT.split( name, 3 );
			
			switch ( splits.length )
			{
				case 1:
					locale = new Locale(splits[0]);
					break;
				case 2:
					locale = new Locale(splits[0], splits[1]);
					break;
				case 3:
					locale = new Locale(splits[0], splits[1], splits[2]);
					break;
			}
			
			cache.put( name, locale );
			
		}
		
		return locale;
		
	}
	
}
