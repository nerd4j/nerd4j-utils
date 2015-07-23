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
package org.nerd4j.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Utility static class to handle common text hashing.
 * 
 * <p>
 *  Uses the native {@code java} {@link MessageDigest}
 *  framework but provides a simplier and ready to use
 *  interface. The available digest algorithms are:
 *  <ul>
 *   <li>MD5</li>
 *   <li>HMAC-MD5</li>
 *   <li>HMAC-SHA1</li>
 *   <li>HMAC-SHA256</li>
 *  </ul>
 * </p>
 * 
 * @author Nerd4j Team
 */
public final class HASH
{
	
	/** The code related to the HMAC-MD5 algorithm. */
	public static final String HMAC_MD5 = "HMACMD5";
	
	/** The code related to the HMAC-SHA-1 algorithm. */
	public static final String HMAC_SHA_1 = "HMACSHA1";

	/** The code related to the HMAC-SHA-256 algorithm. */
	public static final String HMAC_SHA_256 = "HMACSHA256";
	
	/** Message Authentication Code MD5 */
	private static Mac hmacMd5;
	
	/** Message Authentication Code SHA-1 */
	private static Mac hmacSha1;

	/** Message Authentication Code SHA-265 */
	private static Mac hmacSha256;
	
	
	static
	{

		try{
			
			/* MD5 engine to perform the related hashing algorithm. */ 
			hmacMd5 = Mac.getInstance( HMAC_MD5 );
			
			/* SHA-1 engine to perform the related hashing algorithm. */ 
			hmacSha1 = Mac.getInstance( HMAC_SHA_1 );

			/* SHA-256 engine to perform the related hashing algorithm. */ 
			hmacSha256 = Mac.getInstance( HMAC_SHA_256 );
			
		}catch( Exception ex ) {}
		
	}
	
	
	/* ******************* */
	/*  INTERFACE METHODS  */
	/* ******************* */

	
	/**
	 * Returns the {@code MD5} encryption of the provided text.
	 * 
	 * @param text text to encrypt.
	 * @return encryption in {@code MD5}.
	 */
	public static final synchronized byte[] md5( byte[] text )
	{
		
		try{
						
			MessageDigest algorithm = MessageDigest.getInstance( "MD5" );
			
			algorithm.reset();
			algorithm.update( text );			
			
			return algorithm.digest();
			
		}catch( NoSuchAlgorithmException ex )
		{
		    return null;        
		}
		
	}
	
	/**
	 * Returns the {@code HMAC-MD5} encryption of the provided text
	 * using the provided key.
	 * 
	 * @param text text to encrypt.
	 * @param key  secret key to use for encryption.
	 * @return encryption in {@code HMAC-MD5}.
	 */
	public static final synchronized byte[] hmacMd5( byte[] text, byte[] key )
	{
		
		try{
			
		    final SecretKeySpec secret = new SecretKeySpec( key, HMAC_MD5 );
		    
		    hmacMd5.reset();
		    hmacMd5.init( secret );
		    
		    return hmacMd5.doFinal( text );
		    
		}catch( Exception ex )
		{
		    return null;
		}
		
	}
	
	/**
	 * Returns the {@code HMAC-SHA1} encryption of the provided text
	 * using the provided key.
	 * 
	 * @param text text to encrypt.
	 * @param key  secret key to use for encryption.
	 * @return encryption in {@code HMAC-SHA1}.
	 */
	public static final synchronized byte[] hmacSha1( byte[] text, byte[] key )
	{
		
		try{
			
			final SecretKeySpec secret = new SecretKeySpec( key, HMAC_SHA_1 );
			
			hmacSha1.reset();
			hmacSha1.init( secret );
			
			return hmacSha1.doFinal( text );
			
			
		}catch( Exception ex )
		{
			return null;
		}
		
	}
	
	/**
	 * Returns the {@code HMAC-SHA256} encryption of the provided text
	 * using the provided key.
	 * 
	 * @param text text to encrypt.
	 * @param key  secret key to use for encryption.
	 * @return encryption in {@code HMAC-SHA256}.
	 */
	public static final synchronized byte[] hmacSha256( byte[] text, byte[] key )
	{
		
		try{
			
			final SecretKeySpec secret = new SecretKeySpec( key, HMAC_SHA_256 );
			
			hmacSha256.reset();
			hmacSha256.init( secret );
			
			return hmacSha256.doFinal( text );
			
			
		}catch( Exception ex )
		{
			return null;
		}
		
	}
	
	/**
	 * Converts the provided byte array, supposed to
	 * contain an encrypted message digest, into
	 * a {@link String} containing only hexadecimal
	 * characters.
	 * 
	 * @param messageDigest the encrypted message.
	 * @return a {@link String} containing only hexadecimal characters.
	 */
	public static final String toHEX( byte[] messageDigest )
	{
		
		/*
		 * The aim of this method is to convert each byte of the
		 * provided array in a couple of hexadecimal characters.
		 * It uses a StringBuilder with a capacity twice the
		 * length of the provided byte array.
		 */
		String hex;
		final StringBuilder hash = new StringBuilder( messageDigest.length * 2 );
		for( byte b : messageDigest )
		{
			
			/*
			 * Each byte in the array is converted into a couple
			 * of hexadecimal characters.
			 */
			hex = Integer.toHexString( 0xFF & b );
			
			/*
			 * If the conversion method produces only one character
			 * a "0" is added before.
			 */
			hash.append( hex.length() == 1 ? "0" + hex : hex );
				
		}
			
		return hash.toString();
		
	}
	
	/**
	 * Converts the provided {@link String}, supposed to
	 * contain only hexadecimal characters, into a byte
	 * array representing an encrypted message digest.
	 * 
	 * 
	 * @param hash the hexadecimal text.
	 * @return a byte array representing an encrypted message digest.
	 */
	public static final byte[] fromHEX( String hash )
	{
		
		/*
		 * This method assumes the provided text to be the
		 * hexadecimal hash of a byte array. If that's not 
		 * the case the result is unpredictable.
		 * Due to the fact that each byte is represented by
		 * a couple of characters, the length of the string
		 * must be even.
		 */
		if( hash.length() % 2 == 1 )
			throw new IllegalArgumentException( "The hash string must have an even number of characters" );
		
		/*
		 * Due to the fact that each byte is represented by
		 * a couple of characters, the length of the resulting
		 * byte array is half the length of the text (that must
		 * have an even number of characters).
		 */
		final byte[] digest = new byte[hash.length() / 2];
		
		/*
		 * From each couple of hexadecimal characters
		 * we obtain the related byte.
		 */
		for( int i = 0; i < hash.length() - 1; i += 2 )
			digest[i/2] = (byte) Integer.parseInt( hash.substring(i,i+2), 16 );
		
		return digest;
		
	}
	
	
//	public static void main( String[] args )
//	{
//		
//		System.out.println( "MD5 = " + toHEX( md5( "password".getBytes())) );
//		System.out.println( "HAMC-MD5 = " + toHEX( hmacMd5( "password".getBytes(), "password".getBytes())) );
//		System.out.println( "HAMC-SHA1 = " + toHEX( hmacSha1( "password".getBytes(), "password".getBytes())) );
//		System.out.println( "HMAC-SHA256 = " + toHEX( hmacSha256( "password".getBytes(), "password".getBytes())) );
//		
//	}
	
}
