/*
 * #%L
 * Nerd4j Utils
 * %%
 * Copyright (C) 2011 - 2013 Nerd4j
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

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Utility tool for the i18n.
 * 
 * <p>
 *  Provides a simple system to support multi-language
 *  text messages.  
 * </p>
 * 
 * @author Nerd4j Team
 */
public class Localization
{
	
	/** Name of the resource bundle containing the localized messages. */
	private String baseName;
		
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param baseName the name of the resource bundle containing the localized messages.
	 */
	public Localization( String baseName )
	{
		
		super();
		
		this.baseName = baseName;
		
	}
	
	
	/* ******************* */
	/*  INTERFACE METHODS  */
	/* ******************* */

	
	/**
	 * Returns the message related to the given key and the given {@link Locale}.
	 * The message can be in a templated format as described in the {@link MessageFormat}
	 * documentation. If the message needs other arguments to be built they can be
	 * provided as {@code varargs} in the method signature. 
	 * 
	 * @param key		the key to search the message for.
	 * @param locale	the related {@link Locale}.
	 * @param arguments other possible arguments.
	 * @return the message conveniently translated, or {@code null};
	 */	
	public String getMessage( String key, Locale locale, Object... arguments )
	{
		
		assert key != null && ! key.isEmpty() && locale != null;
		
		final ResourceBundle resourceBundle = ResourceBundle.getBundle( baseName, locale );
		String message = resourceBundle.getString( key );
		
		if( arguments != null && message != null )
			message = MessageFormat.format( message, arguments );
		
		return message;
		
	}
	
}
