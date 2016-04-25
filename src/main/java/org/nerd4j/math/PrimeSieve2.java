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
package org.nerd4j.math;

import java.util.Arrays;


/**
 * This class permits calculations about prime numbers.
 * 
 * <p>
 *  This class uses an implementation of the Eratosthenes Sieve
 *  to get all the prime numbers less or equal than a given number.
 * </p>
 * <p>
 *  This implementation is an improvement compared to {@link PrimeSieve}
 *  in the sense that it works with {@code long} instead of working
 *  with {@code int}. However it handles only numbers in the interval
 *  {@code [0,2^38-128)} i.e. {@code [0,Integer.MAX_VALUE * Long.SIZE * 2)}.
 * </p> 
 * <p>
 *  This class keeps the bit-array used to compute the Eratosthenes Sieve
 *  so if the requested prime number is very big this class needs and keeps
 *  allocated a lot of memory. To tell if the required N is prime it needs
 *  up to N bits (therefore up to N/8 bytes) of memory.
 * </p>
 * <p>
 *  The limit of {@code 2^38-128} is based on the fact that the JVM does not allow
 *  to allocate an array with a size greater than {@link Integer.MAX_VALUE}, but
 *  the platform-specific limits can be more restrictive.  
 * </p>
 * <p>
 *  However, if the required N is too big you may run into a {@link java.lang.OutOfMemoryError},
 *  for example finding all primes in the interval {@code [0,2^38-128)} requires {@code 16Gb} of memory.
 *  You can use the method {@link PrimeSieve2#getMemoryOccupation(long)} to know the memory occupation
 *  given the interval upper limit and you can use the method {@link PrimeSieve2#getComputableIntervalUpperLimit(long)}
 *  to know the computable interval using the given amount of memory.
 * </p>
 * <p> 
 *  Using the method {@link #clean()} the memory can be freed but all the
 *  computed primes will be lost and need to be computed again.
 * </p>
 * 
 * @author Nerd4j Team
 */
public class PrimeSieve2
{
	
	
	/** The size of a data block. */
	private static final int BLOCK_SIZE = Long.SIZE;

	/**
	 * The number of bits representing the block size.
	 * For example if the block size is 64, this value
	 * is 6 = log_2(64).
	 */
	private static final int BLOCK_SIZE_SHIFT = 6;

	/**
	 * The limit of blocks that can be allocated is given by
	 * the current limits of array size that is {@link Integer.MAX_VALUE}.
	 */
	private static final int BLOCK_LIMIT = Integer.MAX_VALUE;
	
	/**
	 * The greatest value theoretically handled by this class is {@code 2^38-128}
	 * i.e. {@code Integer.MAX_VALUE * Long.SIZE = (2^31-1) * 64 * 2}.
	 * This is an upper limit but the platform-specific limits can be more restrictive.
	 */
	private static final long MAX_VALUE = ((long) BLOCK_LIMIT) << BLOCK_SIZE_SHIFT + 1;
	
	
	/** Singleton instance */
	private static PrimeSieve2 instance = new PrimeSieve2();

	/**
	 * Contains data for Eratosthenes Sieve.
	 * 
	 * <p>
	 *  For each bit in this bit-field:
	 *  <ul>
	 *   <li>0 means that the related position is not prime,</li>
	 *   <li>1 means that the related position is prime.</li>
	 *  </ul>
	 * </p>
	 */
	private long[] primePool;
	
	
	/**
	 * Default constructor.
	 * 
	 */
	private PrimeSieve2()
	{
		
		super();
		
		this.init();
		
	}
	
	
	/* ******************* */
	/*  INTERFACE METHODS  */
	/* ******************* */
	
	
	/**
	 * Tells if the given value is a prime number.
	 * <p>
	 *  The given value must belong to the interval [0,2^38-128),
	 *  but must take into consideration the amount of memory
	 *  available for the computation.
	 * </p>
	 * <p>
	 *  The computation for value N takes up to N/8 bytes of memory.
	 * </p>
	 * 
	 * @param value the value to check.
	 * @return {@code true} if it is a prime number, {@code false} otherwise.
	 * @throws OutOfMemoryError if the required memory exceeds the available one.
	 * @throws IndexOutOfBoundsException if the value doen't belong to the interval.
	 */
	public static boolean isPrime( long value )
	{
		
		return instance._isPrime( value );
		
	}
	
	
	/**
	 * Returns the first prime number greater or equal than the provided value.
	 * <p>
	 *  If there are no prime numbers grater or equal than the given threshold
	 *  within the internal bounds the value {@code -1} will be returned.
	 * </p>
	 * @param  value the threshold value.
	 * @return the prime found if any, {@code -1} otherwise.
	 */
	public static long getFirstPrimeGreaterEqual( long value )
	{
		
		return instance._getFirstPrimeGreaterEqual( value );
		
	}

	
	/**
	 * Returns the first prime number less or equal than the provided value.
	 * <p>
	 *  If there are no prime numbers less or equal than the given threshold
	 *  within the internal bounds the value {@code -1} will be returned.
	 * </p>
	 * @param  value the threshold value.
	 * @return the prime found if any, {@code -1} otherwise.
	 */
	public static long getFirstPrimeLessEqual( long value )
	{
		
		return instance._getFirstPrimeLessEqual( value );
		
	}
	
	
	/**
	 * Returns the maximum amount of memory (in bytes) needed
	 * to compute all primes in the interval {@code [0,intervalUpperLimit)}.
	 * <p>
	 *  In any case the given value must belong to the interval
	 *  {@code [0,2^38-128)}.
	 * </p>
	 *  
	 * @param intervalUpperLimit the upper limit of the interval to compute.
	 * @return the needed amount of memory expressed in bytes.
	 */
	public static long getMemoryOccupation( long intervalUpperLimit )
	{
		
		/* Checks if the limit belongs to the interval [0,2^38-128). */
		checkBounds( intervalUpperLimit );
		
		/* 
		 * We use long words to store the bit-array,
		 * so the memory occupation is a multiple of
		 * 64 bit (8 byte). Each 
		 */
		final long blocks = intervalUpperLimit >> ( BLOCK_SIZE_SHIFT + 1 );
		
		/*
		 * We always use at least one block of 16 bit,
		 * than we convert the memory size in bytes.
		 */	
		return ( blocks << 3 ) + 8;
		
	}

	
	/**
	 * Returns the interval storable in the given amount
	 * of bytes of available memory.
	 * 
	 * @param availableMemory number of bytes of available memory.
	 * @return the upper limit of the interval computable using the given amount of memory.
	 * @throws IllegalArgumentException if the amount of available memory is not positive.
	 */
	public static long getComputableIntervalUpperLimit( long availableMemory )
	{
		
		/* The available memory must be strict positive. */
		if( availableMemory < 8 )
			throw new IllegalArgumentException( "The amount of available memory must be at least 8 byte." );
				
		/* 
		 * We use long words to store the bit-array,
		 * so the memory occupation is a multiple of
		 * 8 byte (64 bit ).
		 */
		final long blocks = availableMemory >> 3;
		
		/*
		 * The upper limit is given by the amount of bit
		 * representable by the storable amount of blocks.
		 */	
		return Math.min( blocks << BLOCK_SIZE_SHIFT, MAX_VALUE );
		
	}
	
	
	/**
	 * Clean current <tt>Singleton</tt> PrimeSieve instance. This can be
	 * useful to clean memory if PrimeSieve will not used any more.
	 * Cleaning the instance will cause all prime data to be lost.
	 */
	public static void clean()
	{
		
		instance = new PrimeSieve2();
		
	}
	
	
	/* ***************** */
	/*  PRIVATE METHODS  */
	/* ***************** */
	
	
	/**
	 * Checks if the given value belongs to the interval
	 * {@code [0,2^38-128)} otherwise throws an {@link IndexOutOfBoundsException}.
	 * 
	 * @param value the value to check.
	 * @throws IndexOutOfBoundsException if the value is not in the interval.
	 */
	private static void checkBounds( long value )
	{
		
		if( value < 0  || value > MAX_VALUE )
			throw new IndexOutOfBoundsException( value + " is not in the interval [0,2^38-128)." );
		
	}
	
	
	/**
	 * Tells if the given value is a prime number.
	 * <p>
	 *  The given value must belong to the interval [0,2^38-128),
	 *  but must take into consideration the amount of memory
	 *  available for the computation.
	 * </p>
	 * <p>
	 *  The computation for value N takes up to N/8 bytes of memory.
	 * </p>
	 * 
	 * @param value the value to check.
	 * @return {@code true} if it is a prime number, {@code false} otherwise.
	 * @throws OutOfMemoryError if the required memory exceeds the available one.
	 * @throws IndexOutOfBoundsException if the value doen't belong to the interval.
	 */
	private boolean _isPrime( long value )
	{
		
		/*
		 * An even number different from 2 can't be prime.
		 * This check is necessary because the internal
		 * structure is optimized to handle only odd values.
		 */
		if( value == 2 ) return true;
		if( value % 2 == 0 ) return false;
		
		ensureBounds( value );
		
		return isPrime( this.primePool, value );
		
	}
	
	
	/**
	 * Returns the first prime number greater or equal than the provided value.
	 * <p>
	 *  If there are no prime numbers grater or equal than the given threshold
	 *  within the internal bounds the value {@value -1} will be returned.
	 * </p>
	 * @param  value the threshold value.
	 * @return the prime found if any, {@value -1} otherwise.
	 */
	private long _getFirstPrimeGreaterEqual( long value )
	{
		
		ensureBounds( value );

		/* If the given value is 0 or 1 we return the first prime that is 2. */
		if( value <= 2 ) return 2;
		
		/* We get the index related to the given value. */
		final long index = value >> 1;
		
		/* We get the position of the block containing the given index. */
		int blockPos = (int)(index >> BLOCK_SIZE_SHIFT);
		
		/* We get the block containing the given index. */
		long block = primePool[blockPos];
		
		/*
		 * Now we check if the searched prime is in this block.
		 * To do so we need to mask all the bits smaller than
		 * the current value. 
		 */
		long mask = -1L << index;
		
		/* If the value is greater than 0 we found the prime. */
		long prime = getSmallestPrimeInBlock( blockPos, block & mask );
		if( prime > 0 ) return prime;
		
		/*
		 * If the searched prime is not in the current block
		 * we look in the others.
		 */
		
		while( ++blockPos < BLOCK_LIMIT )
		{

			if( blockPos >= primePool.length )
				enlargePool( blockPos << BLOCK_SIZE_SHIFT );
			
			block =  primePool[blockPos];
			if( (prime = getSmallestPrimeInBlock(blockPos, block)) > 0 )
				return prime;
			
		}
		
		return -1;
		
	}

	
	/**
	 * Returns the first prime number less or equal than the provided value.
	 * <p>
	 *  If there are no prime numbers less or equal than the given threshold
	 *  within the internal bounds the value {@value -1} will be returned.
	 * </p>
	 * @param  value the threshold value.
	 * @return the prime found if any, {@value -1} otherwise.
	 */
	private long _getFirstPrimeLessEqual( long value )
	{
		
		ensureBounds( value );
		
		/* This checks are needed because the system handles only odd values. */
		if( value < 2 ) return -1;
		if( value == 2 ) return 2;
		
		/*
		 * The system handles only odd values, if we get an even value
		 * we have to use the its predecessor instead.
		 */
		if( value % 2 == 0 ) --value;
				
		/* We get the index related to the given value. */
		final long index = value >> 1;
		
		/* We get the position of the block containing the given index. */
		int blockPos = (int)(index >> BLOCK_SIZE_SHIFT);
		
		/* We get the block containing the given value. */
		long block = primePool[blockPos];
		
		/*
		 * Now we check if the searched prime is in this block.
		 * To do so we need to mask all the bits smaller than
		 * the current value. 
		 */
		long mask = -1L >>> -index-1;
		
		/* If the value is greater than 0 we found the prime. */
		long prime = getGreatestPrimeInBlock( blockPos, block & mask );
		if( prime > 0 ) return prime;
		
		/*
		 * If the searched prime is not in the current block
		 * we look in the others.
		 */
		while( --blockPos >= 0 )
		{
			
			block =  primePool[blockPos];
			
			if( (prime = getGreatestPrimeInBlock(blockPos, block)) > 0 )
				return prime;
			
		}
		
		return -1;
		
	}
	
	
	/**
	 * Ensures that the internal {@link #primePool} is big enough
	 * to contain the given value.
	 * <p>
	 *  If the requested value is not in the interval [0,{@link Integer#MAX_VALUE})
	 *  an {@link IndexOutOfBoundsException} will be thrown.
	 * </p>
	 * 
	 * @param value the value to check.
	 * @throws IndexOutOfBoundsException if the not in the interval [0,{@link Integer#MAX_VALUE}).
	 */
	private void ensureBounds( long value )
	{
		
		/* Checks if the value belongs to the interval [0,2^38-128). */
		checkBounds( value );
		
		final long index = value >> 1;
		if( index >= (primePool.length << BLOCK_SIZE_SHIFT) )
			enlargePool( index );
		
	}
	
	
	/**
	 * Returns {@value true} if the bit in the given position
	 * is set to 1, {@value false} otherwise.
	 * 
	 * @param data     the {@code int} bit-word containing the bit to evaluate.
	 * @param position the position of the bit in range [0,32).
	 *            
	 * @return {@value true} if the bit value is 1;<br />
	 *         {@value false} otherwise.
	 */
	private boolean boolAtPosition( long data, int position )
	{
		
		/* We assume the bit position to be in range [0,64). */
		return ( (data >> position) & 1L ) == 0 ? false : true;
		
	}
	
	
	/**
	 * Returns {@value true} if the given value is prime.
	 * 
	 * @param pool  pool of primes where to check.
	 * @param value value to check.
	 * @return {@value true} if is prime, {@value false} otherwise.
	 */
	private boolean isPrime( long[] pool, long value )
	{
		
		/*
		 * We always ensure the requested value to be odd.
		 * The bits in the bit-array represents only odd
		 * values, so to get the right position in the
		 * bit-array we need to divide the value by 2.
		 */
		
		final long index = value >> 1;
		final int block = (int)(index >> BLOCK_SIZE_SHIFT);
		final int position = (int)(index % BLOCK_SIZE);
		
		return boolAtPosition( pool[block], position );
				
	}
	
	
	/**
	 * Initialize the {@link #primePool} with the
	 * default values.
	 * 
	 */
	private void init()
	{
		
		/*
		 * 1 is not considered to be prime, so we start
		 * setting 1 as not prime.
		 */
		final long[] pool = { -2, -1, -1, -1, -1, -1, -1, -1,
                              -1, -1, -1, -1, -1, -1, -1, -1 };
		
		/* We apply the sieve starting from the first block. */
		performSieve( pool, 0 );
		
		this.primePool = pool;
		
	}
	
	
	/**
	 * Enlarges the {@link #primePool} size to enclose the given value.
	 * 
	 * @param index the index to be enclosed by the {@link #primePool}.
	 */
	private synchronized void enlargePool( long index )
	{
		
		final int currentPoolSize = primePool.length;

		/*
		 * We double the size of the pool until
		 * the requested value belongs to the pool.
		 */		
		long newPoolSize = primePool.length;
		while( index >= (newPoolSize << BLOCK_SIZE_SHIFT) )
			newPoolSize = newPoolSize << 1; /* poolSize = poolSize * 2 */
		
		/* If another thread had already enlarged the pool nothing needs to be done. */
		if( newPoolSize == currentPoolSize ) return;
				
		/*
		 * Otherwise we create a new bit-array with the needed size,
		 * we copy the old bit-array into the new one, we initialize
		 * the remaining bits to 1 and apply the sieve to the new bits.
		 */
		final long[] newPool = Arrays.copyOf( this.primePool, (int) newPoolSize );
		
		for( int block = currentPoolSize; block < newPoolSize; ++block )
			newPool[block] = -1L; /* We set all the bits of the block to 1. */
		
		/* We apply the sieve to the new blocks. */
		performSieve( newPool, currentPoolSize );
		
		/* Finally we update the internal primePool. */
		this.primePool = newPool;
		
	}
	
	
	/**
	 * This method actually performs the Eratosthenes Sieve algorithm.
	 * <p>
	 *  This method can be called with a partially computed bit-array
	 *  therefore it requires the index of the block to start with.
	 * </p>
	 * 
	 * @param data       the data to sift.
	 * @param startBlock the block to start with.
	 */
	private void performSieve( long[] data, int startBlock )
	{
		
		/* This is the superior limit of the data to be sifted. */ 
		final long endIndex = ((long) data.length) << BLOCK_SIZE_SHIFT;
		
		/* The index of the first bit of the start block. */
		final long startBlockIndex = ((long) startBlock) << BLOCK_SIZE_SHIFT;
		
		/* The number represented by the first bit of the start block. */
		final long startBlockValue = startBlockIndex << 1;
		
		/*
		 * We need to sift only the primes in the interval
		 * [2, sqrt(end)] because the other non primes
		 * have already been sifted. 
		 */
		final long lastPrime = Math.round( Math.sqrt(endIndex<<1) );
		
		/*
		 * The bit-array doesn't contain even values
		 * so removing multiples of 2 is not necessary.
		 */
		long startValue, startIndex;
		for( long i = 3; i <= lastPrime; i += 2 )
			if( isPrime(data, i) )
			{
				
				/*
				 * We don't need to start from the beginning
				 * because all the non-primes in the interval
				 * [3, i*i) have already been sifted. Actually
				 * we can start from the greatest value between
				 * startBlockValue and i * i. 
				 */
				startValue = i * i;
				
				/*
				 * We need the start value to be an odd multiple
				 * of i. If the greatest value is i * i we already
				 * have a valid value, otherwise we have a power
				 * of 2, so we need to adjust it to get the needed value.
				 */
				if( startValue <= startBlockValue )
					startValue = adjustValue( startBlockValue, i );
				
				/* The start index is the startValue / 2. */
				startIndex = startValue >> 1;
				
				/* We remove all the multiples of i. */
				siftMultiples( data, startIndex, endIndex, i );
				
			}
			
	}

	
	/**
	 * Takes the given value and the given prime and returns
	 * the greatest odd multiple of prime smaller than value.
	 * 
	 * @param value value to adjust.
	 * @param prime prime value to adjust for.
	 * @return the adjusted value.
	 */
	private long adjustValue( long value, long prime )
	{
		
		/*
		 * We need the value to be an odd multiple of prime.
		 * As first step we bring it to be a multiple of prime
		 * removing the difference between value and the
		 * greatest multiple of prime smaller chat value.
		 */
		value = value - (value % prime);
		
		/*
		 * If value is even, we remove prime to get the
		 * greatest odd multiple of prime smaller than
		 * value. The couples of values given to this
		 * method ensures that the returned value will
		 * never be smaller then 3; 
		 */
		return value % 2 == 0 ? value - prime : value;
		
	}
	
	
	/**
	 * This method sifts the multiples of the given prime
	 * according to the Eratosthenes Sieve algorithm.
	 *  
	 * @param data       the data to sift.
	 * @param startIndex the index to start with.
	 * @param endIndex   the index that represents the end of the interval.
	 * @param prime      the prime which multiples have to be sift out. 
	 */
	private void siftMultiples( long[] data, long startIndex, long endIndex, long prime )
	{
		
		/* 
		 * Setting the bit-word to 1 means that all the
		 * bits in the bit-word are 0 rather than the last one.
		 * 1 = 00000000-00000000-00000000-00000001 (32 bit-word) 
		 *
		 * This operation moves all the bits to the left the given
		 * number of positions and fills the rest of the word with 0.
		 * For example if N = 20 we obtain the following mask:
		 * mask = 00000000-00010000-00000000-00000000
		 * The positions are 0-based so shifting position 20 means that the bit 1 is in the 21th position.
		 * 
		 * This operation is modular that means that shifting 33 positions
		 * is the same as shifting 1 position. We need a mask with all 1 and
		 * only one 0 in the requested position, but using directly such mask
		 * does't work because using
		 *   -2 = 11111111-11111111-11111111-11111110
		 * and shifting it by 20 positions we obtain
		 * mask = 11111111-11100000-00000000-00000000
		 * that is not what we need.
		 */
		
		long mask;
		int block;
		for( long index = startIndex; index < endIndex; index += prime )
		{
			
			/* I didn't find a smartest way to get the current block. */
			block = (int)( index >> BLOCK_SIZE_SHIFT );
			
			/* We create the bit-mask for the given value. */
			mask = 1L << index;
			
			/*
			 * Sets the bit to 0, to do this we need to
			 * use the unary bitwise complement ~.
			 */			
			data[ block ] &= ~mask;
								
		}
			
	}
	
	
	/**
	 * Returns the value of the smallest prime in the given block if any.
	 * 
	 * @param blockPos the position of the block
	 * @param block    the block to analyze.
	 * @return the smallest prime if any, {@code -1} otherwise.
	 */
	private long getSmallestPrimeInBlock( long blockPos, long block )
	{
		
		/* Returns the number of zero-bit preceding the first 1-bit. */
		int zeros = Long.numberOfTrailingZeros( block );
		
		/*
		 * If the number of zeros equals the block size it means that
		 * there is no 1-bits in the block, otherwise we use the
		 * zeros to retrieve the position of the searched prime.
		 */
		if( zeros != BLOCK_SIZE )
		{
			final long index = (blockPos << BLOCK_SIZE_SHIFT) + zeros;
			return (index << 1) + 1;
		}
		else	
			return -1;
		
	}
	
	
	/**
	 * Returns the value of the greatest prime in the given block if any.
	 * 
	 * @param blockPos the position of the block
	 * @param block    the block to analyze.
	 * @return the greatest prime if any, {@code -1} otherwise.
	 */
	private long getGreatestPrimeInBlock( long blockPos, long block )
	{
		
		/* Returns the number of zero-bit following the last 1-bit. */
		int zeros = Long.numberOfLeadingZeros( block );
		
		/*
		 * If the number of zeros equals the block size it means that
		 * there is no 1-bits in the block, otherwise we use the
		 * zeros to retrieve the position of the searched prime.
		 */
		if( zeros != BLOCK_SIZE )
		{
			final long index = (++blockPos << BLOCK_SIZE_SHIFT) - ++zeros;
			return (index << 1) + 1;
		}
		else	
			return -1;
		
	}

}
