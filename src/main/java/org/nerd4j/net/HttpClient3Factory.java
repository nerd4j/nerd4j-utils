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
package org.nerd4j.net;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 * Factory used to build objects of type {@link HttpClient}
 * version {@code 3.1} or earlier.
 * 
 * @author Nerd4j Team
 */
public class HttpClient3Factory
{
	
	/* ******************* */
	/*  INTERFACE METHODS  */
	/* ******************* */
	

	/**
	 * Creates an instance of {@link HttpClient} to be used in a
	 * single thread environment.
	 * 
	 * @param connectionTimeout waiting time for the connection to be opened.
	 * @param socketTimeout     waiting time for the request to get a response.
	 * @return a new instance of {@link HttpClient}.
	 */
	public static HttpClient createSingleThreadInstance( int connectionTimeout, int socketTimeout )
	{
		
		/* Creates a new http client to be used in a single thread environment. */
		return createInstance( new SimpleHttpConnectionManager(), connectionTimeout, socketTimeout );
		
	}
	
	
	/**
	 * Creates an instance of {@link HttpClient} to be used in a
	 * multi thread environment.
	 * 
	 * @param connectionTimeout waiting time for the connection to be opened.
	 * @param socketTimeout     waiting time for the request to get a response.
	 * @return a new instance of {@link HttpClient}.
	 */
	public static HttpClient createMultiThreadInstance( int connectionTimeout, int socketTimeout )
	{
		
		/* Creates a new http client to be used in a multi thread environment. */
		return createInstance( new MultiThreadedHttpConnectionManager(), connectionTimeout, socketTimeout );
		
	}
	
	
	/* ***************** */
	/*  PRIVATE METHODS  */
	/* ***************** */

	
	/**
	 * Creates an instance of {@link HttpClient} using the given {@link HttpConnectionManager}.
	 * 
	 * @param connectionManager the {@link HttpConnectionManager} to use.
	 * @param connectionTimeout waiting time for the connection to be opened.
	 * @param socketTimeout     waiting time for the request to get a response.
	 * @return a new instance of {@link HttpClient}.
	 */
	private static HttpClient createInstance( HttpConnectionManager connectionManager, int connectionTimeout, int socketTimeout )
	{
		
		/* Connection parameters. */
		final HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		
		/* Sets connection and socket timeouts. */
		params.setConnectionTimeout( connectionTimeout );
		params.setSoTimeout( socketTimeout );
		
		/* Configures the connection manager with the given parameters. */
		connectionManager.setParams( params );
		
		/* Creates and configures the http client. */
		return new HttpClient( connectionManager );
		
	}
	
}
