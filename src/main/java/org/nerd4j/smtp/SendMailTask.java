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

import javax.mail.Message;
import javax.mail.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a task to be given to an {@link Executor}
 * in order to send email messages asynchronously.
 * 
 * @author Nerd4j Team
 */
class SendMailTask implements Runnable
{
	
	/** Internal logging system. */
	private static final Logger log = LoggerFactory.getLogger( SendMailTask.class );
	
	/** The {@link Message} to send. */
	private Message message;
	
	/** The {@code SMTP} session. */
	private Session session;
	
	
	/**
	 * Constructor with parameters.
	 * 
	 * @param message the {@link Message} to send.
	 * @param session the {@code SMTP} session.
	 */
	SendMailTask( Message message, Session session )
	{
		
		if( message == null )
			throw new NullPointerException( "The message to send can't be null" );

		if( session == null )
			throw new NullPointerException( "The SMTP session can't be null" );
		
		this.message = message;
		this.session = session;
		
	}

	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run()
	{
	
		try{
			
			JavaMailUtil.send( message, session);
			
		}catch( Exception ex )
		{
			
			log.warn( "Unable to send message due to ", ex );
			
		}
		
	}
	
}
