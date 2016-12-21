/*
 * #%L
 * Nerd4j Utils
 * %%
 * Copyright (C) 2011 - 2014 Nerd4j
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
package org.nerd4j.io;

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods to handle {@link Closeable} and {@link AutoCloseable} instances.
 * 
 * @author Nerd4j Team
 */
public final class CloseableUtil
{

	/** Static class logger. */
	private static final Logger logger = LoggerFactory.getLogger( CloseableUtil.class );
	
	/** Private constructor, no new instances */
	private CloseableUtil() { super(); }
	
	
	/**
	 * Close a {@link AutoCloseable} instance soaking every exception tha may appear.
	 * <p>
	 * If an exception is throw while closing it will be logged at <tt>warn</tt> level.
	 * 
	 * @param closeable instance to close.
	 * @return {@code true} if give instance has been closed without errors.
	 */
	public static boolean closeAndSoak( AutoCloseable closeable )
	{
		
		return closeAndSoak( closeable, "Soaked an exception while closing {}.", closeable );
		
	}
	
	/**
	 * Close a {@link AutoCloseable} instance soaking every exception tha may appear.
	 * <p>
	 * If an exception is throw while closing it will be logged at <tt>warn</tt> level.
	 * 
	 * @param closeable    instance to close.
	 * @param errorMessage error message
	 * @return {@code true} if give instance has been closed without errors.
	 */
	public static boolean closeAndSoak( AutoCloseable closeable, String errorMessage )
	{
		
		return closeAndSoak( closeable, errorMessage, (Object[]) null );
		
	}
	
	/**
	 * Close a {@link AutoCloseable} instance soaking every exception tha may appear.
	 * <p>
	 * If an exception is throw while closing it will be logged at <tt>warn</tt> level.
	 * 
	 * @param closeable    instance to close.
	 * @param errorMessage slf4j formatted error message
	 * @param errorData    data for error message
	 * @return {@code true} if give instance has been closed without errors.
	 */
	public static boolean closeAndSoak( AutoCloseable closeable, String errorMessage, Object... errorData )
	{
		
		/* Try instance close */
		try
		{
			
			closeable.close();
			
			return true;
			
		} catch ( Exception soaked )
		{
			
			/* If in error log it. */
			
			if ( errorData == null )
				logger.warn( errorMessage, soaked );
			else
			{
				
				/* Add exception to error data. */
				Object[] errorDataException = new Object[ errorData.length + 1 ];
				System.arraycopy( errorData, 0, errorDataException, 0, errorData.length);
				errorDataException[ errorData.length ] = soaked;
				
				logger.warn( errorMessage, errorDataException );
			}
			
			return false;
			
		}
		
	}
	
	
	/* *************************************** */
	/*  LEGACY METHODS FOR BACK COMPATIBILITY  */
	/* *************************************** */
	
	
	/**
	 * Close a {@link Closeable} instance soaking every exception tha may appear.
	 * <p>
	 * If an exception is throw while closing it will be logged at <tt>warn</tt> level.
	 * 
	 * @param closeable instance to close.
	 * @return {@code true} if give instance has been closed without errors.
	 * @deprecated use {@link CloseableUtil#closeAndSoak(AutoCloseable)} instead.
	 */
	@Deprecated
	public static boolean closeAndSoak( Closeable closeable )
	{
		
		return closeAndSoak( (AutoCloseable) closeable, "Soaked an exception while closing {}.", closeable );
		
	}
	
	/**
	 * Close a {@link Closeable} instance soaking every exception tha may appear.
	 * <p>
	 * If an exception is throw while closing it will be logged at <tt>warn</tt> level.
	 * 
	 * @param closeable    instance to close.
	 * @param errorMessage error message
	 * @return {@code true} if give instance has been closed without errors.
	 * @deprecated use {@link CloseableUtil#closeAndSoak(AutoCloseable,String)} instead.
	 */
	@Deprecated
	public static boolean closeAndSoak( Closeable closeable, String errorMessage )
	{
		
		return closeAndSoak( (AutoCloseable) closeable, errorMessage, (Object[]) null );
		
	}
	
	/**
	 * Close a {@link Closeable} instance soaking every exception tha may appear.
	 * <p>
	 * If an exception is throw while closing it will be logged at <tt>warn</tt> level.
	 * 
	 * @param closeable    instance to close.
	 * @param errorMessage slf4j formatted error message
	 * @param errorData    data for error message
	 * @return {@code true} if give instance has been closed without errors.
	 * @deprecated use {@link CloseableUtil#closeAndSoak(AutoCloseable, String, Object...)} instead.
	 */
	@Deprecated
	public static boolean closeAndSoak( Closeable closeable, String errorMessage, Object... errorData )
	{
		
		return closeAndSoak( (AutoCloseable) closeable, errorMessage, errorData );
		
	}
	
}
