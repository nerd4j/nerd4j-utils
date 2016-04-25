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
package org.nerd4j.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.nerd4j.io.CloseableUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility methods to handle {@link Properties} instances.
 * 
 * @author Nerd4j Team
 */
public final class PropertiesUtil
{

	/** Static class logger. */
	private static final Logger logger = LoggerFactory.getLogger( PropertiesUtil.class );
	
	/** Private constructor, no new instances */
	private PropertiesUtil() { super(); }
	
	/**
	 * Returns a string representation of given {@link Properties}..
	 * 
	 * @param properties properties container to convert
	 * @return string representation
	 */
	public static String toString( Properties properties )
	{
		
		StringBuilder builder = new StringBuilder();
		
		builder.append( "[" );
		
		for ( String name : properties.stringPropertyNames() )
			builder
				.append( name )
				.append( "=" )
				.append( properties.getProperty( name ) )
				.append( "," );
		
		final int length = builder.length();
		
		if ( length > 1 )
			builder.deleteCharAt( length - 1 );
		
		builder.append( "]" );
		
		return builder.toString();
		
	}
	
	/**
	 * Create a new {@link Properties} instance from file.
	 * 
	 * @param path properties file path
	 * @return new properties instance
	 * 
	 * @throws IOException if properties cannot be loaded
	 * @throws FileNotFoundException if properties file cannot be found
	 */
	public static Properties load( String path ) throws IOException, FileNotFoundException
	{
		
		return load( path, null );
		
	}
	
	/**
	 * Create a new {@link Properties} instance from file.
	 * <p>
	 * Given <i>parent</i> {@link Properties} will be used as default values.
	 * <p>
	 * 
	 * @param path properties file path
	 * @param parent default properties
	 * @return new properties instance
	 * 
	 * @throws IOException if properties cannot be loaded
	 * @throws FileNotFoundException if properties file cannot be found
	 */
	public static Properties load( String path, Properties parent ) throws IOException, FileNotFoundException
	{
		
		final File file = new File( path );
		InputStream fis = null;
		
		try
		{
			
			fis = new FileInputStream( file );
			
			final Properties properties = new Properties( parent );
			
			properties.load( fis );
			
			return properties;
			
		/* Contains FileNotFoundException too */
		} catch ( IOException e )
		{
			
			logger.error( "Cannot load file {}", path );
			
			throw e;
			
		} catch ( Exception e )
		{
			
			logger.error( "Cannot load file {}", path );
			
			throw new IOException( "Cannot load file " + path, e );
			
		} finally
		{
			
			if ( fis != null )
<<<<<<< HEAD
				CloseableUtil.closeAndSoak( (AutoCloseable) fis, "Cannot properly close file {}", path );
=======
				CloseableUtil.closeAndSoak( fis, "Cannot properly close file {}", path );
>>>>>>> 0ad3c2f22cd7171279fa1eb7b396833e3a098c3f
			
		}
		
	}
	
}
