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
package org.nerd4j.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This {@link Filter} has been developed with the purpose to change
 * the default behavior of the Tomcat server when receiving a request
 * with method PUT, PATCH or DELETE.
 * 
 * <p>
 * By default the Tomcat server accepts URL parameters to be sent
 * into the request body only for requests with method POST.
 * 
 * <p>
 * The W3C specifications allow also requests with method
 * PUT, PATCH and DELETE to send parameters into the request
 * body and this option is widely used in ReST environment.
 * 
 * <p>
 * This {@link Filter} intercepts the requests with method PUT,
 * PATCH or DELETE and looks for URL encoded parameters into
 * the request body. If there are any are parsed and added
 * to the {@link ServletRequest#getParameterMap()}.
 * 
 * <p>
 * To carry URL encoded parameters the request body must
 * be of type {@code application/x-www-form-urlencoded},
 * otherwise is ignored.
 * 
 * @author Nerd4j Team
 */
public class HttpRequestBodyParameterTomcatPatchFilter implements Filter
{
    
    /** The internal logging system. */
    private static final Logger log = LoggerFactory.getLogger( HttpRequestBodyParameterTomcatPatchFilter.class );
    
    /** The media type used to describe URL encoded parameters sent in the request body. */
    private static final String APPLICATION_URLENCODED_MEDIA_TYPE = "application/x-www-form-urlencoded";

    
    /**
     * Default constructor.
     * 
     */
    public HttpRequestBodyParameterTomcatPatchFilter()
    {
        
        super();
        
    }
    

	/* ******************* */
	/*  INTERFACE METHODS  */
	/* ******************* */
	
	
    /**
     * {@inheritDoc}
     */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException
	{
	    log.debug( "Filter initialized"  );
	}
	
	/**
     * {@inheritDoc}
     */
	@Override
	public void destroy()
	{
	    log.debug( "Filter destroyed"  );
	}

	/**
	 * Checks if the request is an instance of {@link HttpServletRequest}
	 * and if the {@link HttpServletRequest#getMethod()} is one between
	 * {@code PUT}, {@code PATCH} or {@code DELETE}.
	 * <p>
	 * Checks if the request body content type is {@code application/x-www-form-urlencoded}.
	 * In this case parses the request body looking for URL encoded parameters and
	 * adds them to the {@link ServletRequest#getParameterMap()}.
	 * 
	 * @param request  the request to parse.
	 * @param response the related response.
	 * @param chain    the filter chain to continue with.
	 */
	@Override
	public void doFilter( ServletRequest request, ServletResponse response,	FilterChain chain )
	throws IOException, ServletException
	{
	    	    
	    ServletRequest actualRequest = request;
	    
	    /*
	     * If the request body content type is of the required media type
	     * and the request is of type HTTP the filter can proceed. 
	     */
	    final String mediaType = extractMediaType( request.getContentType() );
	    if( APPLICATION_URLENCODED_MEDIA_TYPE.equalsIgnoreCase(mediaType)
	        && request instanceof HttpServletRequest )
	    {
	    
	        final HttpServletRequest httpRequest = (HttpServletRequest) request;
	        final String httpMethod = httpRequest.getMethod();
	        
	        /*
	         * If the request method is one of the handled methods
	         * the filter can proceed.
	         */
	        if( "PUT".equalsIgnoreCase(httpMethod)    ||
	            "DELETE".equalsIgnoreCase(httpMethod) ||
	            "PATCH".equalsIgnoreCase(httpMethod) )
	        {
	            
	            String charEnc = httpRequest.getCharacterEncoding();
	            if( charEnc == null ) charEnc = "UTF-8";
	            
	            log.debug( "Intercepted request with method " + httpMethod );
	            actualRequest = addRequestBodyParameters( httpRequest, charEnc );
	        }
	        
	    }
		
		/*
		 * IMPORTANT NOTE:
		 * The method chain.doFilter must be called after all the operations
		 * that manipulate the request or the response.
		 * After the chain.doFilter returns, the response is immutable
		 * and no other manipulations are possible. 
		 */
		chain.doFilter( actualRequest, response );
				
	}
	
	
	
	/* ***************** */
    /*  PRIVATE METHODS  */
    /* ***************** */
	
	
	/**
	 * Extracts the media type from the given content type.
	 * 
	 * @param contentType content type to parse.
	 * @return the extracted media type.
	 */
	private String extractMediaType( String contentType )
	{
	    
	    if( contentType == null || contentType.isEmpty() )
	        return null;
	    
	    final String[] split = contentType.split( ";" );
	    return split[0];
	    
	}
	
	
	/**
     * Performs the parsing of the request body looking for URL encoded parameters.
     * 
     * @param requestBody the request body to parse.
     * @param charEnc     the character encoding used to write the request body.
     * @param the request with the additional parameters.
     */
    private ServletRequest addRequestBodyParameters( HttpServletRequest request, String charEnc )
    {
        
        try{
        
            final BufferedReader reader = request.getReader();
            final Map<String,List<String>> parameterMap = new HashMap<String,List<String>>();
            
            String line;
            while( (line = reader.readLine()) != null )
            {
                
                line = line.trim();
                if( line.isEmpty() ) continue;
                
                List<String> paramList;
                String paramName, paramValue;
                final String[] params = line.split( "&" );
                for( String param : params )
                {
                    
                    if( param == null || param.isEmpty() ) continue;
                    
                    log.debug( "Found parameter " + param );
                    final String[] pair = param.split( "=" );
                    
                    paramName = URLDecoder.decode( pair[0], charEnc );
                    if( paramName == null || paramName.isEmpty() ) continue;
                    
                    paramValue = pair.length == 2 ? URLDecoder.decode(pair[1], charEnc) : null;
                    paramList = parameterMap.get( paramName );
                    if( paramList == null )
                    {
                        paramList = new LinkedList<String>();
                        parameterMap.put( paramName, paramList );
                    }
                    
                    paramList.add( paramValue );
                    
                }
                
            }
            
            if( ! parameterMap.isEmpty() )
                return new HttpRequestBodyParameterIntegrationWrapper( request, parameterMap );
            
        
        }catch( Exception ex )
        {
            log.error( "Unable to parse request body", ex );
        };
        
        return request;
        
    }
    
    
    /* *************** */
    /*  INNER CLASSES  */
    /* *************** */
	
	
	/**
	 * Wrapper for the class {@link HttpServletRequest} that allows the integration
	 * of the parameters parsed by the Tomcat server and the parameters URL encoded
	 * into the request body.
	 * 
	 * @author Nerd4j Team
	 */
	private static class HttpRequestBodyParameterIntegrationWrapper extends HttpServletRequestWrapper
	{

	    /** The map that associates each parameter to the related value. */
	    private Map<String,String[]> parameterMap;
	    

	    /**
	     * Constructor with parameters
	     * 
	     * @param request the  request to be wrapped.
	     * @param parameterMap the map with additional parameters. 
	     */
        public HttpRequestBodyParameterIntegrationWrapper( HttpServletRequest request, Map<String,List<String>> parameterMap )
        {

            super( request );
            
            this.initParameterMap( request, parameterMap );
            
        }
        
        
        /* ******************* */
        /*  INTERFACE METHODS  */
        /* ******************* */
	    
	    
	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public Map<String,String[]> getParameterMap()
	    {
	        return Collections.unmodifiableMap( parameterMap );
	    }
	    
	    /**
	     * {@inheritDoc}
	     */
	    @Override
	    public String getParameter( String name )
	    {
	        final String[] values = parameterMap.get( name );
	        return values != null && values.length > 0 ? values[0] : null;
	    }
	    
        /**
         * {@inheritDoc}
         */
        @Override
	    public Enumeration<String> getParameterNames()
        {
            return Collections.enumeration( parameterMap.keySet() );
	    }
	    
        /**
         * {@inheritDoc}
         */
        @Override
	    public String[] getParameterValues( String name )
        {
            return parameterMap.get( name );
	    }
        
        
        /* ***************** */
        /*  PRIVATE METHODS  */
        /* ***************** */
        
        
        /**
         * Initializes the internal parameter map by merging the original request parameter map
         * and the parameter map generated by parsing the request body.
         * 
         * @param request original request.
         * @param requestBodyParamMap request map to merge.
         */
        private void initParameterMap( HttpServletRequest request, Map<String,List<String>> requestBodyParamMap )
        {
            
            String paramName;
            String[] paramValues;
            List<String> paramList;
            
            @SuppressWarnings("unchecked")
            final Map<String,String[]> queryStringParamMap = request.getParameterMap();
            for( Map.Entry<String,String[]> entry : queryStringParamMap.entrySet() )
            {
                
                paramName = entry.getKey();
                paramList = requestBodyParamMap.get( paramName );
                if( paramList == null )
                {
                    paramList = new LinkedList<String>();
                    requestBodyParamMap.put( paramName, paramList );
                }
                
                paramValues = entry.getValue();
                for( String value : paramValues )
                    paramList.add( value );
                        
            }
            
            
            this.parameterMap = new HashMap<String,String[]>( requestBodyParamMap.size() );
            for( Map.Entry<String,List<String>> entry : requestBodyParamMap.entrySet() )            
            {
                
                paramList = entry.getValue();
                paramValues = paramList.toArray( new String[paramList.size()] );
                this.parameterMap.put( entry.getKey(), paramValues );
                
            }
            
        }
        	
	}

}