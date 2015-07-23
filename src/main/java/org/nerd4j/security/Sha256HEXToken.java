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

package org.nerd4j.security;

import java.util.Arrays;
import java.util.Date;


/**
 * Static class for the generation of identification
 * tokens based on HASH code.
 * <p>
 *  The generated token has the following form:
 *  <blockquote>
 *   base64( public-data-separated-by ":" + expiration-timestamp + ":" +
 *   sha256( public-data-separated-by ":" + expiration-timestamp + ":" +
 *   private-data-separated-by ":" + {@link Sha256HEXToken#PRIVATE_KEY}) )
 *   <dl>
 *    <dt>public-data:<dt><dd>Identification data that can be read by everyone.</dd>
 *    <dt>private-data:</dt><dd>Private data used to perform the identification check.</dd>
 *    <dt>expiration-timestamp:</dt><dd>The time of expiration of the token validity.</dd> 
 *    <dt>sha256-key:</dt><dd>Private key used to generate the SHA_256 hash.</dd>
 *   </dl>
 *   </blockquote>
 * </p>
 * <p>
 *  A common use case of this type of token is the following:
 *  <blockquote>
 *   <ul>
 *    <li>public-data = email</li>
 *    <li>private-data = password</li>
 *    <li>expiration-timestamp = timestamp in milliseconds</li> 
 *    <li>sha256-key = private key used to generate the SHA_256 hash.</li>
 *   </dl>
 *   
 *   base64( email + ":" + expiration-timestamp + ":" + sha256( email ":" + expiration + ":" + password + ":" + {@link Sha256HEXToken#PRIVATE_KEY}) )
 *   </blockquote>
 * </p>
 * 
 * @author Nerd4j Team
 * @version 0.0.1, 15-09-2014
 */
public class Sha256HEXToken
{
	
	/** Key added to the message to generate noise. */
	private static final String PRIVATE_KEY = "5DA31BF129CA6ABE1F427BF669A5D";
	

	/* ******************* */
	/*  INTERFACE METHODS  */
	/* ******************* */
	
	
	/**
	 * Creates a new token using the provided data.
	 * 
	 * @param publicData  public data to be concatenated through ':'.
	 * @param privateData private data to be concatenated through ':'.
	 * @param expiration  token expiration date.
	 * @param privateKey  private key used to generate the SHA_256 hash.
	 * @return the hashed token converted into Base64.
	 */
	public static String create( String[] publicData, String[] privateData, Date expiration, String privateKey )
	{
		
		final StringBuilder sb = new StringBuilder();
		
		addColonSeparated( publicData, sb );
		
		final long expirationTS = expiration != null ? expiration.getTime() : System.currentTimeMillis();
		addColonSeparated( String.valueOf(expirationTS), sb );		
		
		/* public_data_1:public_data_2: ... :public_data_n:expiration */
		final String strPublicData = sb.toString();
		
		addColonSeparated( privateData, sb );
		sb.append( PRIVATE_KEY );
		
		/* public_data_1:public_data_2: ... :public_data_n:expiration:private_data_1:private_data_2: ... :private_data_m:PRIVATE_KEY */
		final String strPrivateData = sb.toString();
		final byte[] hashPrivateData = HASH.hmacSha256( strPrivateData.getBytes(), privateKey.getBytes() );
		final byte[] bytePrivateData = HASH.toHEX( hashPrivateData ).getBytes();
		
		final byte[] bytePublicData = strPublicData.getBytes();

		final byte[] byteToken = new byte[bytePublicData.length + bytePrivateData.length];
		System.arraycopy( bytePublicData, 0, byteToken, 0, bytePublicData.length );
		System.arraycopy( bytePrivateData, 0, byteToken, bytePublicData.length, bytePrivateData.length );
		
		return HASH.toHEX( byteToken );
		
	}
	
	/**
	 * Reads the data in the provided token.
	 * <p>
	 *  The token is supposed to be a valid Base64 text.
	 * </p>
	 * <p>
	 *  This method returns an array of {@link String}s in the form
	 *  <blockquote>
	 *    [ public_data_1, public_data_2, ..., public_data_n, expiration, private_data_hash ]
	 *  </blockquote>
	 * </p>
	 * @param base64Token the token to be read.
	 * @return the data in the token.
	 */
	public static String[] read( String base64Token )
	{
		
		final byte[] byteToken = HASH.fromHEX( base64Token );
		final String strValues = new String( byteToken );
		
		return strValues.split( ":" );
		
	}
	
	/**
	 * Performs a validation of the data in the provided token
	 * using the provided private data and the private key.
	 * <p>
	 *  Computes the hash using the provided data and verifies
	 *  that the new hash is equals to the provided one.
	 * </p>
	 * 
	 * @param tokenValues values read from the token usind the method {@link Sha256HEXToken#read(String)}.
	 * @param privateData the private data that are expected to be found in the hashed field.
	 * @param privateKey  private key used to generate the hash.
	 * @return {@code true} if the new hash is equals to the provided one.
	 */
	public static boolean validate( String[] tokenValues, String[] privateData, String privateKey )
	{
		
		final StringBuilder sb = new StringBuilder();
		
		
		if( tokenValues != null && tokenValues.length > 1 )
		{
			for( int i = 0; i < tokenValues.length - 1; ++i )
				sb.append( tokenValues[i] ).append( ':' );
		}
		
		addColonSeparated( privateData, sb );
		sb.append( PRIVATE_KEY );
		
		/* public_data_1:public_data_2: ... :public_data_n:expiration:private_data_1:private_data_2: ... :private_data_m:PRIVATE_KEY */
		final String strPrivateData = sb.toString();
		final byte[] hashPrivateData = HASH.hmacSha256( strPrivateData.getBytes(), privateKey.getBytes() );
		
		final String strTokenPrivateData = tokenValues[tokenValues.length-1];
		final byte[] hashTokenPrivateData = HASH.fromHEX( strTokenPrivateData );
		
		return Arrays.equals( hashPrivateData, hashTokenPrivateData );
		
	}
	
	/**
	 * Checks the timestamp in the expiration field and returns
	 * {@code true} if it represents an instant in the past. 
	 *  
	 * @param tokenValues values read from the token usind the method {@link Sha256HEXToken#read(String)}.
	 * @return {@code true} if the expiration field represents an instant in the past. 
	 */
	public static boolean isExpired( String[] tokenValues )
	{
		
		final String expiration = tokenValues[tokenValues.length-2];
		final long expirationTS = Long.parseLong( expiration );
		final long currentTS = System.currentTimeMillis();

		return expirationTS < currentTS;
		
	}
	
	/**
	 * Returns the date in the expiration field.
	 *  
	 * @param tokenValues values read from the token usind the method {@link Sha256HEXToken#read(String)}.
	 * @return the date in the expiration field.
	 */
	public static Date getExpiration( String[] tokenValues )
	{
		
		final String expiration = tokenValues[tokenValues.length-2];
		final long expirationTS = Long.parseLong( expiration );
		
		return new Date( expirationTS );
		
	}
	
	/**
	 * Utility method used to concatenate the results of the
	 * methods {@link Sha256HEXToken#read(String)} and
	 * {@link Sha256HEXToken#validate(String, String[], String)}.
	 * 
	 * @param base64Token the token to read.
	 * @param privateData private data in the hashed field.
	 * @param privateKey  private key used to generate the hash.
	 * @return {@code true} if the generated hash is equals to the provided one.
	 */
	public static boolean validate( String base64Token, String[] privateData, String privateKey )
	{
		
		final String[] tokenValues = read( base64Token );
		return validate( tokenValues, privateData, privateKey );
		
	}
	
	/**
	 * Utility method used to concatenate the results of the
	 * methods {@link Sha256HEXToken#read(String)} and
	 * {@link Sha256HEXToken#isExpired(String[])}.
	 *  
	 * @param base64Token the token to read.
	 * @return {@code true} if the expiration field represents an instant in the past. 
	 */
	public static boolean isExpired( String base64Token )
	{
		
		final String[] tokenValues = read( base64Token );
		return isExpired( tokenValues );
		
	}
	
	/**
	 * Utility method used to concatenate the results of the
	 * methods {@link Sha256HEXToken#read(String)} and
	 * {@link Sha256HEXToken#getExpiration(String[])}.
	 *  
	 * @param base64Token the token to read.
	 * @return the date in the expiration field.
	 */
	public static Date getExpiration( String base64Token )
	{
		
		final String[] tokenValues = read( base64Token );
		return getExpiration( tokenValues );
		
	}
	
	
	/* ***************** */
	/*  PRIVATE METHODS  */
	/* ***************** */

	
	/**
	 * Adds to the given {@link StringBuilder} the given
	 * value followed by ':'.
	 * 
	 * @param value the value to be added.
	 * @param sb    {@link StringBuilder} to be written.
	 */
	private static void addColonSeparated( String value, StringBuilder sb )
	{
		
		if( value != null && ! value.isEmpty() )
			sb.append( value ).append( ':' );
		
	}

	/**
	 * Adds all values in the array separated by ':'.
	 * 
	 * @param data values to be added.
	 * @param sb   {@link StringBuilder} to be written.
	 */
	private static void addColonSeparated( String[] data, StringBuilder sb )
	{
		
		if( data != null && data.length > 0 )
			for( String value : data )
				addColonSeparated( value, sb );
		
	}
	
}
