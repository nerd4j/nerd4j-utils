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
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.sun.mail.smtp.SMTPTransport;

/**
 * Utility class to simplify the access and the use of
 * the <tt>javax.mail</tt> API.
 * 
 * <p>
 *  This class provides static methods for the configuration
 *  of the email {@link Message}s. Provides a simple interface
 *  to the configurations most frequently used.
 * </p>
 *   
 * @author Nerd4j Team
 */
public class JavaMailUtil
{
	
    /**
     * Enumerates the Mime types used in email context.
     * 
     * @author Nerd4j Team
     */
	public static enum MimeType
	{
	    
		TEXT_PLAIN	("text/plain; charset=UTF-8"),
		TEXT_HTML	("text/html; charset=UTF-8");
		
		/** The related mime type code. */
		public final String code;
		
		/**
		 * Constructor with parameters.
		 * 
		 * @param code the mime type code.
		 */
		private MimeType ( String code )
		{
			this.code = code;
		}
		
	}

	private static final String TRUE                   = "true";
	private static final String FALSE                  = "false";
	
	public static final String PROTOCOL                = "smtp";
	
	public static final String SMTP_HOST               = "mail.smtp.host";
	public static final String SMTP_PORT               = "mail.smtp.port";
	public static final String SMTP_AUTH               = "mail.smtp.auth";
	public static final String SMTP_USER               = "mail.smtp.user";
	public static final String SMTP_PASS               = "mail.smtp.password";

	public static final String SMTP_SECURE             = "mail.smtp.secure";
	public static final String SMTP_SOCKET_TIMEOUT     = "mail.smtp.timeout";
	public static final String SMTP_CONNECTION_TIMEOUT = "mail.smtp.connectiontimeout";
	
	public static final String SSL_FACTORY             = "javax.net.ssl.SSLSocketFactory";
	
	
    /* **************** */
    /*  PUBLIC METHODS  */
    /* **************** */
    
    
    /**
     * Adds to the given {@link Properties} the properties
     * needed to send email through the {@code SSL} protocol.
     * 
     * @param prop {@link Properties} to be modified.
     */
    public static void addSecurityProperties( Properties prop )
    {


        final String smtpSecure = prop.getProperty( SMTP_SECURE );
        if( smtpSecure != null && TRUE.equals(smtpSecure) )
        {

            final String mailPort = prop.getProperty( SMTP_PORT );
            
            prop.put( "mail.smtp.starttls.enable", TRUE );
            prop.put( "mail.transport.protocol", PROTOCOL );
            
            prop.put( "mail.smtp.socketFactory.port", mailPort );
            prop.put( "mail.smtp.socketFactory.fallback", FALSE );
            prop.put( "mail.smtp.socketFactory.class", SSL_FACTORY );

        }
        
    }
     
    
    /**
     * Creates a new {@code SMTP} Java Mail {@link Session}
     * with the given properties.
     * <p>
     *  The given {@link Properties} object must contain the
     *  following values:
     *  <ul>
     *   <li><tt>mail.smtp.host<tt>: the {@code SMTP} server host;</li>
     *   <li><tt>mail.smtp.port<tt>: the {@code SMTP} server port;</li>
     *   <li><tt>mail.smtp.auth<tt>. tells if the connection requires authentication;</li>
     *   <li><tt>mail.smtp.user<tt>: the user to be authenticated;</li>
     *   <li><tt>mail.smtp.password<tt>: the authentication password;</li>
     *   <li><tt>mail.smtp.secure<tt>: tells if the connection uses a secure protocol;</li>
     *   <li><tt>mail.smtp.timeout<tt>: the protocol timeout;</li>
     *   <li><tt>mail.smtp.connectiontimeout<tt>: the connection timeout.</li>
     *  </ul>
     * </p>
     *
     * @param prop   the properties to use.
     * @param debug  tells if to run in debug mode.
     * @return a new {@code SMTP} Java Mail {@link Session}.
     */
    public static Session createSession( Properties prop, boolean debug )
    {

    	final String user = prop.getProperty( SMTP_USER );
    	final String pass = prop.getProperty( SMTP_PASS );
    	
    	final Authenticator authenticator = new Authenticator()
	    {
    		
	        @Override
	        protected PasswordAuthentication getPasswordAuthentication()
	        {
	        	return new PasswordAuthentication( user, pass );
	        }
	
	     };
    	
        final Session session = Session.getDefaultInstance( prop, authenticator );
        session.setDebug( debug );
        
        return session;

    }
    
    
    /**
     * Creates a single part email {@link Message}.
     * 
     * @param session the {@code SMTP} session to use.
     * @param subject the subject of the {@link Message}.
     * @param text    the content of the {@link Message}.
     * @return the new created email {@link Message}.
     * @throws MessagingException if the reation fails.
     */
    public static Message createSimpleMessage( Session session, String subject, String text )
    throws MessagingException
    {
    	
    	try{

			/*
			 * Note. There is non difference between creating a single
			 *       part or a multi-part message, so we use the same
			 *       implementation.
			 */
    		
    		return createMultipartMessage( session, subject, text );
    		
    	}catch( Exception ex )
    	{
    		
    		throw new MessagingException( "Error creating simple message", ex );
    		
    	}
    	
    }

    
    /**
     * Creates a multi-part email {@link Message} containing attachments.
     * 
     * @param session     the {@code SMTP} session to use.
     * @param subject     the subject of the {@link Message}.
     * @param text        the content of the {@link Message}.
     * @param attachments the attachments to add.
     * @return the new created email {@link Message}.
     * @throws MessagingException if the reation fails.
     */
    public static Message createMultipartMessage( Session session, String subject, String text, File... attachments )
    throws MessagingException
    {
    	
    	try{
    		
    		final Multipart multipart = new MimeMultipart();
    		
    		multipart.addBodyPart( createBodyPart(text, MimeType.TEXT_PLAIN) );
    		
    		addAttachments( multipart, attachments );
    		
    		return createMessage( session, subject, multipart );
    		
    	}catch( Exception ex )
    	{
    		
    		throw new MessagingException( "Error creating multipart message", ex );
    		
    	}
    	
    }
    

    /**
     * Creates a multi-part email {@link Message} containing attachments
     * using two version of the same text: the plain text version and the
     * HTML version.
     * 
     * @param session     the {@code SMTP} session to use.
     * @param subject     the subject of the {@link Message}.
     * @param plainText   the content of the {@link Message} (plain text version).
     * @param htmlText    the content of the {@link Message} (html version).
     * @param attachments the attachments to add.
     * @return the new created email {@link Message}.
     * @throws MessagingException if the reation fails.
     */
	public static Message createMultipartHTMLMessage( Session session, String subject, String plainText, String htmlText, File... attachments )
	throws MessagingException {

		try {

			
			/*
			 * The following schema represents the structure of an email
			 * message with alternative text and attachments.
			 * 
			 * +-----------------------------------------------+
			 * | multipart/related                             |
			 * | +---------------------------+  +------------+ |
			 * | |multipart/alternative      |  | image/gif  | |
			 * | | +-----------+ +---------+ |  |            | |
			 * | | |text/plain | |text/html| |  |            | |
			 * | | +-----------+ +---------+ |  |            | |
			 * | +---------------------------+  +------------+ |
			 * +-----------------------------------------------+ 
			 */
			
			/*  The type should be "related" but gmail doesn't like it so we use "mixed". */
			final Multipart multipart = new MimeMultipart( "mixed" );
			
			final BodyPart alternative = createAlternativeBodyPart(
					createBodyPart( plainText, MimeType.TEXT_PLAIN ),
					createBodyPart( htmlText, MimeType.TEXT_HTML ) );
			
			multipart.addBodyPart( alternative );
			
			addAttachments( multipart, attachments );

			return createMessage( session, subject, multipart );

		} catch (Exception ex) {

			throw new MessagingException("Error creating multipart message", ex);

		}

	}
	

    /**
     * Sets the sender address.
     * 
     * @param message       the {@link Message} to modify.
     * @param senderAddress the sender email address.
     * @throws MessagingException
     */
    public static void setSender( Message message, String senderAddress )
    throws MessagingException
    {
    	
    	message.setFrom( new InternetAddress(senderAddress) );
    	
    }

    
    /**
     * Sets the sender address.
     * 
     * @param message       the {@link Message} to modify.
     * @param senderAddress the sender email address.
     * @param senderLabel   label to be displayed by the recipient email client.
     * @throws MessagingException
     */
    public static void setSender( Message message, String senderAddress, String senderLabel )
    throws MessagingException
    {
    	
    	try{
    	
    		message.setFrom( new InternetAddress(senderAddress, senderLabel) );
    		
    	}catch( UnsupportedEncodingException ex )
    	{
    		
    		throw new MessagingException( "Error while setting the sender address", ex );
    		
    	}
    	
    }

    
    /**
     * Adds a recipient to the given {@link Message}.
     * 
     * @param message     the {@link Message} to modify.
     * @param recipient   the recipient email address to add.
     * @throws MessagingException
     */
    public static void addRecipient( Message message, String recipient )
    throws MessagingException
    {
    	
    	addRecipient( message, recipient, Message.RecipientType.TO );
    	
    }

    
    /**
     * Adds a CC recipient to the given {@link Message}.
     * 
     * @param message     the {@link Message} to modify.
     * @param recipient   the CC recipient email address to add.
     * @throws MessagingException
     */
    public static void addCCRecipient( Message message, String recipient )
    throws MessagingException
    {
    	
    	addRecipient( message, recipient, Message.RecipientType.CC );
    	
    }
    
    
    /**
     * Adds a BCC recipient to the given {@link Message}.
     * 
     * @param message     the {@link Message} to modify.
     * @param recipient   the BCC recipient email address to add.
     * @throws MessagingException
     */
    public static void addBCCRecipient( Message message, String recipient )
    throws MessagingException
    {
    	
    	addRecipient( message, recipient, Message.RecipientType.BCC );
    	
    }
    
    
    /** 
     * Sends the given {@link Message} using the given {@code SMTP} {@link Session}. 
     * 
     * @param message the {@link Message} to send.
     * @param session the {@link Session} to use.
     * @throws MessagingException if the dispatch fails.
     */
    public static void send( Message message, Session session ) throws MessagingException
    {
    	    	
    	final SMTPTransport transport = (SMTPTransport) session.getTransport( PROTOCOL );
    	
    	transport.connect();

    	transport.sendMessage( message, message.getAllRecipients() );
    	
    	transport.close();
    	
    }
    
    
    /* ***************** */
    /*  PRIVATE METHODS  */
    /* ***************** */
    
    
    /**
     * Creates the root {@link Message} given the {@link Multipart} body.
     * 
     * @param session   the {@code SMTP} session to use.
     * @param subject   the subject of the {@link Message}.
     * @param multipart the multi-part message content.
     * @return the new created email {@link Message}.
     * @throws MessagingException if the reation fails.
     */
    private static Message createMessage( Session session, String subject, Multipart multipart )
    throws MessagingException
    {

        try {

            final Message message = new MimeMessage(session);

            message.setContent( multipart );
            message.setSubject( subject );

            return message;

        } catch (Exception ex) {

            throw new MessagingException("Error creating message", ex);

        }
    }

    
	/**
	 * Creates a {@link BodyPart} containing text (text/plain, text/html).
	 * 
	 * @param data the text to use.
	 * @param type the related mime type.
	 * @return a new created {@link BodyPart}.
	 * @throws MessagingException if the creation fails.
	 */
	private static BodyPart createBodyPart( String data, MimeType type )
	throws MessagingException
	{

		try {

			final BodyPart bodyPart = new MimeBodyPart();

            bodyPart.setHeader( "Content-Transfer-Encoding", "8bit" );
			bodyPart.setContent( data, type.code );

			return bodyPart;

		} catch (Exception ex) {

			throw new MessagingException("Error creating bodypart", ex);

		}

	}
	

	/**
     * Creates a {@link BodyPart} with alternative content.
     * <p>
     *  All the {@link BodyPart} provided to this method should contain
     *  the same content represented in alternative forms.
     * </p>
     * @param bodyParts alternative versions of the same content.
     * @return a {@link BodyPart} representing the aggregation of the given ones.
     * @throws MessagingException if the creation fails.
     */
    private static BodyPart createAlternativeBodyPart( BodyPart... bodyParts )
    throws MessagingException
    {
    	
    	try {
    		
    		final Multipart alternativeMultipart = new MimeMultipart( "alternative" );
    		
    		for ( BodyPart bodyPart : bodyParts )
    			alternativeMultipart.addBodyPart( bodyPart );
            
    		final BodyPart alternativeBody = new MimeBodyPart();
    		alternativeBody.setContent( alternativeMultipart );
    		
            return alternativeBody;
    		
    	} catch( Exception ex )
    	{
    		
    		throw new MessagingException( "Error creating bodypart alternative", ex );
    		
    	}
    	
    }
    
    
    /**
     * Adds the given {@link File}s as attachments to the given {@link Multipart}.
     * 
     * @param multipart the {@link Multipart} message to modify.
     * @param files		the {@link File}s to attach.
     * @throws MessagingException if the attachment fails.
     */
    private static void addAttachments( Multipart multipart, File... files )
    throws MessagingException
    {
    	
    	for ( File file : files )
    		addAttachment( multipart, file );
    	
    }
    
    
    /**
     * Adds the given {@link File} as attachment to the given {@link Multipart}.
     * 
     * @param multipart the {@link Multipart} message to modify.
     * @param file      the {@link File} to attach.
     * @throws MessagingException if the attachment fails.
     */
    private static void addAttachment( Multipart multipart, File file )
    throws MessagingException
    {
    	
    	try{
    		
    		final MimeBodyPart attachment = new MimeBodyPart();    	    
    		final DataSource source = new FileDataSource( file );
    	    
    		attachment.setDataHandler( new DataHandler(source) );
    		attachment.setFileName( file.getName() );
    		
    	    multipart.addBodyPart( attachment );

    	}catch( Exception ex )
    	{
    		
    		throw new MessagingException( "Error adding attachment", ex );
    		
    	}
    	
    }
    
    
    /**
     * Adds to the given {@link Message} a recipient of the given type.
     * 
     * @param message   the {@link Message} to modify.
     * @param recipient the recipient address to add.
     * @param type      the recipient type (may be: TO, CC or BCC ).
     * @throws MessagingException if the operation fails.
     */
    private static void addRecipient( Message message, String recipient, Message.RecipientType type )
    throws MessagingException
    {
    	
    	message.addRecipient( type, new InternetAddress(recipient) );
    	
    }
    
}
