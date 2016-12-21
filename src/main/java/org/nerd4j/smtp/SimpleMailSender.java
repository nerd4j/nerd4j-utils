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
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;

import org.nerd4j.thread.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class to create and send email messages.
 *
 * @author Nerd4j Team
 */
public class SimpleMailSender
{
	
	/** Internal logging system. */
    protected Logger   logger = LoggerFactory.getLogger( SimpleMailSender.class );
    
    /** Default sender address to use if no specific address is defined. */
    protected String   defaultSenderAddress;
    
    /** Default sender label to use if no specific label is defined. */
    protected String   defaultSenderLabel;
    
    /** Tells if the debug information should be logged. */
    protected boolean  logDebugInfo;
    
    /** Tells if an {@link Executor} should be used in order to send the messages. */
    protected boolean  sendAsynchronously;
    
    /** The {@link Executor} to use in case {@code sendAsynchronously} is {@code true}. */
	protected Executor executor;
    
	/** The properties to use to configure the {@code SMTP}. */
	private Properties smtpProperties;
	
    /** The {@code SMTP} session to use. */
    private Session session;
    
    
    /**
     * Default constructor.
     * 
     */
    public SimpleMailSender()
    {
        
        super();
        
        this.session              = null;
        this.executor             = null;
        this.smtpProperties       = null;
        
        this.defaultSenderLabel   = null;
        this.defaultSenderAddress = null;
        
        this.logDebugInfo         = false;
        this.sendAsynchronously   = false;
        
    }
    
    
    /**
     * Initialization method used to configure the {@code SMTP} session properties.
     * <p>
     * This method must be called before any other in this class.
     * 
     * @throws MessagingException if the initialization fails.
     */
    public void init() throws MessagingException
    {
 
        JavaMailUtil.addSecurityProperties( smtpProperties );
        session = JavaMailUtil.createSession( smtpProperties, logDebugInfo );
        
        if( sendAsynchronously )
        	executor = Executors.newSingleThreadExecutor( new NamedThreadFactory("SEND-MAIL-THREAD") );
        
    }
    

    /**
     * Creates a new email {@link Message} with the given data.
     * 
     * @param subject     the subject of the {@link Message}.
     * @param body        the text of the {@link Message}.
     * @param attachments any possible attachments.
     * @return a new email {@link Message}.
     * @throws MessagingException if the creation fails.
     */
    public Message create( String subject, String body, File... attachments )
    throws MessagingException
    {
    	
    	try{
	    	
    		return attachments != null && attachments.length > 0 ?
    		JavaMailUtil.createMultipartMessage( session, subject, body, attachments ) :
    		JavaMailUtil.createSimpleMessage( session, subject,  body );
	    
    	}catch( MessagingException ex )
    	{
    		
    		logger.warn( "Unable to create mail message", ex );
    		throw ex;
    		
    	}catch( Exception ex )
    	{
    		
    		logger.warn( "Unable to create mail message", ex );
    		throw new MessagingException( ex.getMessage(), ex );
    		
    	}
        
    }
    
    /**
     * 
     * @param plain      il testo del messaggio in formato plain.
     * @param html       il testo del messaggio in formato html.
     * @param attachments eventuali file da aggiungere in allegato.
     * @return il nuovo messaggio di posta.
     * @throws MessagingException se qualcosa non funziona.
     */
    /**
     * Creates a new email {@link Message} with the given data.
     * 
     * @param subject     the subject of the {@link Message}.
     * @param plain       the {@link Message} content (plain text version)
     * @param html        the {@link Message} content (html version)
     * @param attachments any possible attachments.
     * @return a new email {@link Message}.
     * @throws MessagingException if the creation fails.
     */
    public Message create( String subject, String plain, String html, File... attachments )
    throws MessagingException
    {
    	
    	try{
	    	
    		return JavaMailUtil.createMultipartHTMLMessage( session, subject, plain, html, attachments );
	    
    	}catch( MessagingException ex )
    	{
    		
    		logger.warn( "Unable to create mail message", ex );
    		throw ex;
    		
    	}catch( Exception ex )
    	{
    		
    		logger.warn( "Unable to create mail message", ex );
    		throw new MessagingException( ex.getMessage(), ex );
    		
    	}
        
    }

    
    /**
     * Sends the {@link Message} to all the given recipients.
     * 
     * @param message    {@link Message} to send.
     * @param recipients list of recipients (at least one must be given).
     * @throws MessagingException if the operation fails.
     */
    public void send( Message message, String... recipients ) throws MessagingException
    {
    	
    	if( message == null )
    		throw new MessagingException( "Unable to send a message of type null" );
    	
    	if( recipients == null || recipients.length < 1 )
    		throw new MessagingException( "The mail message must have at least one recipient" );

    	try{
    	
    		if( message.getFrom() == null || message.getFrom().length == 0 )
    			JavaMailUtil.setSender( message, defaultSenderAddress, defaultSenderLabel );
	    	
	    	for( String recipient : recipients )
	    		JavaMailUtil.addRecipient( message, recipient );
    	
	    	send( message );
	    
    	}catch( MessagingException ex )
    	{
    	
    		logger.warn( "Unable to send message", ex );
    		throw ex;
    	
    	}catch( Exception ex )
    	{
    		
    		logger.warn( "Unable to send message", ex );
    		throw new MessagingException( ex.getMessage(), ex );
    		
    	}
    	
    }

    
    /* ***************** */
    /*  PRIVATE METHODS  */
    /* ***************** */

    
    /**
     * Performs the dispatch of the {@link Message} 
     * in the way defined by the value of the
     * {@value sendAsynchronously} flag.
     *  
     * @param message the {@link Message} to send.
     * @throws MessagingException if the dispatch fails.
     */
    private void send( Message message ) throws MessagingException
    {
    	
    	if( sendAsynchronously )
    		executor.execute( new SendMailTask(message, session) );
    	else
	    	JavaMailUtil.send( message, session );
    	
    }
    
    
    /* ******************* */
    /*  GETTERS & SETTERS  */
    /* ******************* */
    
    
    public void setSenderLabel( String senderLabel )
    {
        this.defaultSenderLabel = senderLabel;
    }
    
    public void setSenderAddress( String senderAddress )
    {
    	this.defaultSenderAddress = senderAddress;
    }

	public void setLogDebugInfo( boolean logDebugInfo )
	{
		this.logDebugInfo = logDebugInfo;
	}

	public void setSendAsynchronously( boolean sendAsynchronously )
	{
		this.sendAsynchronously = sendAsynchronously;
	}

	public void setSmtpProperties( Properties smtpProperties )
	{
		this.smtpProperties = smtpProperties;
	}

}
