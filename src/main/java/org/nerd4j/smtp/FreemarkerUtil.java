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
package org.nerd4j.smtp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;

/**
 * Utility class, used to interface with the {@code Freemarker} framework
 * to handle the email templates.
 * 
 * @author Nerd4j Team
 */
public class FreemarkerUtil
{
	
	/**
	 * Enumerates the wrapper types supported by {@code Freemarker}.
	 *  
	 * @author Nerd4j Team
	 */
	public static enum Wrapper
	{
		
		DEFAULT,
		SIMPLE,
		BEANS;
		
	}

	/**
	 * Creates the configuration needed by {@code Freemarker}
	 * to load and handle the templates from a directory in 
	 * the file system.
	 * 
	 * @param templatePath path where to find the templates.
	 * @param wrapper      wrapper type to use to handle data.
	 * @return the required configuration.
	 * @throws MessagingException if the path is not reachable.
	 */
	public static Configuration createConfiguration( File templatePath, Wrapper wrapper )
	throws MessagingException
	{

		try{
			
			final Configuration configuration = new Configuration();

			switch( wrapper )
			{
			
			case BEANS: configuration.setObjectWrapper( ObjectWrapper.BEANS_WRAPPER );
			break;
			
			case SIMPLE: configuration.setObjectWrapper( ObjectWrapper.SIMPLE_WRAPPER );
			break;
			
			default: configuration.setObjectWrapper( ObjectWrapper.DEFAULT_WRAPPER );
			
			}
			
			configuration.setDirectoryForTemplateLoading( templatePath );
			return configuration;
		
		}catch( Exception ex )
		{
			
			throw new MessagingException( "Error in creating Freemarker configuration", ex );
			
		}

	}
	
	/**
	 * Creates the configuration needed by {@code Freemarker}
	 * to load and handle the templates from a directory in
	 * the classpath.
	 * 
	 * @param rootClasspath class to use for the {@code Class#getResource(String)}.
	 * @param templatePath path where to find the templates relative to che given class.
	 * @param wrapper      wrapper type to use to handle data.
	 * @return the required configuration.
	 * @throws MessagingException if the path is not reachable.
	 */
	public static Configuration createConfiguration( Class<?> rootClasspath, String templatePath, Wrapper wrapper )
			throws MessagingException
			{
		
		try{
			
			final Configuration configuration = new Configuration();
			
			switch( wrapper )
			{
			
			case BEANS: configuration.setObjectWrapper( ObjectWrapper.BEANS_WRAPPER );
			break;
			
			case SIMPLE: configuration.setObjectWrapper( ObjectWrapper.SIMPLE_WRAPPER );
			break;
			
			default: configuration.setObjectWrapper( ObjectWrapper.DEFAULT_WRAPPER );
			
			}
			
			configuration.setClassForTemplateLoading( rootClasspath, templatePath );
			return configuration;
			
		}catch( Exception ex )
		{
			
			throw new MessagingException( "Error in creating Freemarker configuration", ex );
			
		}
		
			}
	
	/**
	 * Performs the render of the given template using the given data model.
	 * Returns the {@link String} containing the final text. 
	 * 
	 * @param configuration freemarker configuration
	 * @param templateName  name of thetemplate to use.
	 * @param model         data model to use to fill the template.
	 * @param locale        the locale to use.
	 * @return the resulting text.
	 * @throws MessagingException
	 */
	public static String renderTemplate( Configuration configuration, String templateName,
			                             Map<String,?> model, Locale locale )
	throws MessagingException
	{
		
		try{

			final Template template = configuration.getTemplate( templateName, locale );
			final ByteArrayOutputStream stream = new ByteArrayOutputStream();
			final OutputStreamWriter writer = new OutputStreamWriter( stream );
	
			template.process( model, writer );
			writer.flush();
			writer.close();
	
			return stream.toString();

		}catch( Exception ex )
		{

			throw new MessagingException( "Error in rendering templated message", ex );

		}

	}

}
