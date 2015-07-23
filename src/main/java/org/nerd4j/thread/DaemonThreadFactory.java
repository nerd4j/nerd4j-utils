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
package org.nerd4j.thread;


/**
 * Extension of the {@link NamedThreadFactory} class used to
 * create daemon threads.
 * 
 * @author Nerd4j Team
 * 
 * @see Thread#setDaemon(boolean)
 */
public class DaemonThreadFactory extends NamedThreadFactory 
{

	
	/**
	 * Constructor with parameters.
	 * 
	 * @param name name to be associated to the factory.
	 */
	public DaemonThreadFactory( String name )
	{

		super( name );

	}
	
	
	/* ******************* */
    /*  INTERFACE METHODS  */
    /* ******************* */
	

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Thread newThread( Runnable task )
	{
		
		/* Creates the new thread with the related name. */
		final Thread thread = super.newThread( task );
		
		/* Sets this thread to become a deamon. */
		thread.setDaemon( true );
		
		return thread;

	}
	
}