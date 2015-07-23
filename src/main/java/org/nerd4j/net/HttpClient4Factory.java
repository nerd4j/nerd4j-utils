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

import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;


/**
 * Factory used to build objects of type {@link HttpClient}
 * version {@code 4.0} and greater.
 * 
 * @author Nerd4j Team
 */
public class HttpClient4Factory
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
		return createInstance( new BasicHttpClientConnectionManager(), connectionTimeout, socketTimeout );
		
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
		return createInstance( new PoolingHttpClientConnectionManager(), connectionTimeout, socketTimeout );
		
	}

	
	/**
	 * Creates an instance of {@link HttpClient} to be used in a
	 * multi thread environment.
	 * 
	 * @param connectionTimeout waiting time for the connection to be opened.
	 * @param socketTimeout     waiting time for the request to get a response.
	 * @param totalConnections    total number of available connections.
	 * @param perRouteConnections number of available connection for the same route.
	 * @return a new instance of {@link HttpClient}.
	 */
	public static HttpClient createMultiThreadInstance( int connectionTimeout, int socketTimeout,
			                                            int totalConnections, int perRouteConnections)
	{
		
		/* Creates a pooled connection manager with the given connection limits. */
		final PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
		poolingHttpClientConnectionManager.setDefaultMaxPerRoute( perRouteConnections );
		poolingHttpClientConnectionManager.setMaxTotal( totalConnections );
		
		/* Creates a new http client to be used in a multi thread environment. */
		return createInstance( poolingHttpClientConnectionManager, connectionTimeout, socketTimeout );
		
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
	private static HttpClient createInstance( HttpClientConnectionManager connectionManager, int connectionTimeout, int socketTimeout )
	{
		
		/* Default request settings. */
		final RequestConfig requestConfig = RequestConfig.custom()
				.setConnectTimeout( connectionTimeout )
				.setSocketTimeout( socketTimeout )
				.build();
		
		/* Creates and configures the http client. */
		final HttpClient httpClient = HttpClientBuilder.create()
				.setConnectionManager( connectionManager )
				.setDefaultRequestConfig( requestConfig )
				.build();
		
		return httpClient;
		
	}
	
}
