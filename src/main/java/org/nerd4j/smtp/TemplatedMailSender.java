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

import java.io.File;
import java.util.Locale;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.nerd4j.util.DataConsistency;

import freemarker.template.Configuration;

/**
 * Utility class to send templated email using {@code Freemarker}
 * as template resolver. 
 *
 * @author Nerd4j Team
 */
public class TemplatedMailSender extends SimpleMailSender
{

	/** Path to the directory that contains the email templates in the file system. */
	private File templatePath;
	
	/**
	 * Path to the directory that contains the email templates in the classpath.
	 * The provided classpath must be absolute, the first character is expected
	 * to be a '/'. 
	 */
	private String templateClasspath;
	
	/** Wrapper used to handle the data model. */
	private FreemarkerUtil.Wrapper wrapper;
	
	/** The {@code Freemarker} framework configuration.  */
	private Configuration configuration;
	
	
	/**
     * Default constructor.
     * 
     */
    public TemplatedMailSender()
    {
        
        super();
        
        this.wrapper           = null;
        this.templatePath      = null;
        this.configuration     = null;
        this.templateClasspath = null;
        
    }
    
	
	/**
     * Initialization method used to configure the {@code SMTP} session properties.
     * <p>
     * This method must be called before any other in this class.
     * 
     * @throws MessagingException if the initialization fails.
     */
	@Override
    public void init() throws MessagingException
    {
 
		try{
		
			super.init();
			if( templateClasspath != null )
				configuration = FreemarkerUtil.createConfiguration( getClass(), templateClasspath, wrapper );
			
			else if( templatePath != null )
				configuration = FreemarkerUtil.createConfiguration( templatePath, wrapper );
			
			else
				throw new NullPointerException( "At least one among templatePath or templateClasspath must be defined" );
			
		}catch( MessagingException ex )
		{
			
			logger.error( "Error during initialization", ex );
			throw ex;
			
		}
        
    }
	
	
	/* ******************* */
    /*  INTERFACE METHODS  */
    /* ******************* */
    
    
	/**
     * Creates a new email {@link Message} using the given
     * template and the given data model.
     * 
     * @param subject      the subject of the {@link Message}.
     * @param templateName the name of the template to use.
     * @param model        the related data model.
     * @param locale       the current {@link Locale}.
     * @param attachments  the attached files.
     * @return a new email {@link Message}.
     * @throws MessagingException if the creation fails.
     */
    public Message create( String subject, String templateName,
    		               Map<String,?> model, Locale locale,
    		               File... attachments ) throws MessagingException
    {

    	String body;
		try{
			
			DataConsistency.checkIfNotNull( "model", model );
			DataConsistency.checkIfNotNull( "locale", locale );
			DataConsistency.checkIfValued( "template name", templateName );
			
			body = FreemarkerUtil.renderTemplate( configuration, templateName, model, locale );
			return create( subject, body, attachments );
						
		}catch( MessagingException ex )
    	{
    		
			logger.warn( "Unable to create templated mail message", ex );
			throw ex;
    	
		} catch( Exception ex )
		{
			
			logger.warn( "Unable to create templated mail message", ex );
			throw new MessagingException( ex.getMessage(), ex );
			
		}
    	
    }
    
    /**
     * Creates a new email {@link Message} using the given
     * template and the given data model.
     * 
     * @param subject           the subject of the {@link Message}.
     * @param plainTemplateName the name of the template to use (plain text version).
     * @param htmlTemplateName  the name of the template to use (HTML version).
     * @param model             the related data model.
     * @param locale            the current {@link Locale}.
     * @param attachments  the attached files.
     * @return a new email {@link Message}.
     * @throws MessagingException if the creation fails.
     */
    public Message create( String subject, String plainTemplateName,
    		               String htmlTemplateName, Map<String,?> model,
    		               Locale locale, File... attachments ) throws MessagingException
    {

		try{
			
			String plain, html;
			
			DataConsistency.checkIfNotNull( "model", model );
			DataConsistency.checkIfNotNull( "locale", locale );
			DataConsistency.checkIfValued( "plain template name", plainTemplateName );
			DataConsistency.checkIfValued( "html template name", htmlTemplateName );
			
			plain = FreemarkerUtil.renderTemplate( configuration, plainTemplateName, model, locale );
			html = FreemarkerUtil.renderTemplate( configuration, htmlTemplateName, model, locale );
			
			return create( subject, plain, html, attachments );
						
		}catch( MessagingException ex )
    	{
    		
			logger.warn( "Unable to create templated mail message", ex );
			throw ex;
    	
		} catch( Exception ex )
		{
			
			logger.warn( "Unable to create templated mail message", ex );
			throw new MessagingException( ex.getMessage(), ex );
			
		}
    	
    }


    /* ******************* */
    /*  GETTERS & SETTERS  */
    /* ******************* */
    
    
    public void setTemplatePath( String templatePath )
    {
    	this.templatePath = new File( templatePath );
    	this.templateClasspath = null;
    }

    public void setTemplatePath( File templatePath )
    {
        this.templatePath = templatePath;
        this.templateClasspath = null;
    }
    
    public void setTemplateClasspath( String templateClasspath )
    {
    	if( templateClasspath != null && ! templateClasspath.startsWith("/") )
    		this.templateClasspath = "/" + templateClasspath;
    	else
    		this.templateClasspath = templateClasspath;
    	
    	this.templatePath = null;
    }

    public void setWrapper( FreemarkerUtil.Wrapper wrapper )
    {
    	this.wrapper = wrapper;
    }
    
}
