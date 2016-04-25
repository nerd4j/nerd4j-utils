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
 *  This implementation is an improvement compared to {@link PrimeSieve2}
 *  in the sense that it occupies 1/3 less memory than the previous one.
 *  It handles numbers in the interval {@code [0,3*2^37-192)}
 *  i.e. {@code [0,Integer.MAX_VALUE * Long.SIZE * 3)}.
 * </p> 
 * <p>
 *  This class keeps the bit-array used to compute the Eratosthenes Sieve
 *  so if the requested prime number is very big this class needs and keeps
 *  allocated a lot of memory. To tell if the required N is prime it needs
 *  up to 2*N/3 bits (therefore up to N/12 bytes) of memory.
 * </p>
 * <p>
 *  The limit of {@code 3*2^37-192} is based on the fact that the JVM does
 *  not allow to allocate an array with a size greater than {@link Integer.MAX_VALUE},
 *  but the platform-specific limits can be more restrictive.  
 * </p>
 * <p>
 *  However, if the required N is too big you may run into a {@link java.lang.OutOfMemoryError},
 *  for example finding all primes in the interval {@code [0,3*2^37-192)}, besides being
 *  relatively time consuming, it requires {@code 16Gb} of memory.
 *  You can use the method {@link PrimeSieve3#getMemoryOccupation(long)} to know the memory occupation
 *  given the interval upper limit and you can use the method {@link PrimeSieve3#getComputableIntervalUpperLimit(long)}
 *  to know the computable interval using the given amount of memory.
 * </p>
 * <p> 
 *  Using the method {@link #clean()} the memory can be freed but all the
 *  computed primes will be lost and need to be computed again.
 * </p>
 * 
 * @author Nerd4j Team
 */
public class PrimeSieve3
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
	 * The greatest value theoretically handled by this class is {@code 3*2^37-192}
	 * i.e. {@code Integer.MAX_VALUE * Long.SIZE * 3 = (2^31-1) * 64 * 3}.
	 * This is an upper limit but the platform-specific limits can be more restrictive.
	 */
	private static final long MAX_VALUE = ((long) BLOCK_LIMIT) * ((long) BLOCK_SIZE) * 3L;
	
	
	/** Singleton instance */
	private static PrimeSieve3 instance = new PrimeSieve3();

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
	private PrimeSieve3()
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
	 *  The given value must belong to the interval [0,3*2^37-192),
	 *  but must take into consideration the amount of memory
	 *  available for the computation.
	 * </p>
	 * <p>
	 *  The computation for value N takes up to N/12 bytes of memory.
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
	 *  {@code [0,3*2^37-192)}.
	 * </p>
	 *  
	 * @param intervalUpperLimit the upper limit of the interval to compute.
	 * @return the needed amount of memory expressed in bytes.
	 */
	public static long getMemoryOccupation( long intervalUpperLimit )
	{
		
		/* Checks if the limit belongs to the interval [0,3*2^37-192). */
		checkBounds( intervalUpperLimit );
		
		/* 
		 * We use long words to store the bit-array,
		 * so the memory occupation is a multiple of
		 * 64 bit (8 byte). Each 
		 */
		final long blocks = intervalUpperLimit / ( BLOCK_SIZE * 3 );
		
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
		if( availableMemory <= 0 )
			throw new IllegalArgumentException( "The amount of available memory must be greater than 0." );
				
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
		return Math.min( blocks * BLOCK_SIZE * 3, MAX_VALUE );
		
	}
	
	
	/**
	 * Clean current <tt>Singleton</tt> PrimeSieve instance. This can be
	 * useful to clean memory if PrimeSieve will not used any more.
	 * Cleaning the instance will cause all prime data to be lost.
	 */
	public static void clean()
	{
		
		instance = new PrimeSieve3();
		
	}
	
	
	/* ***************** */
	/*  PRIVATE METHODS  */
	/* ***************** */
	
	
	/**
	 * Checks if the given value belongs to the interval
	 * {@code [0,3*2^37-192)} otherwise throws an {@link IndexOutOfBoundsException}.
	 * 
	 * @param value the value to check.
	 * @throws IndexOutOfBoundsException if the value is not in the interval.
	 */
	private static void checkBounds( long value )
	{
		
		if( value < 0  || value > MAX_VALUE )
			throw new IndexOutOfBoundsException( value + " is not in the interval [0,3*2^37-192)." );
		
	}
	
	
	/**
	 * Tells if the given value is a prime number.
	 * <p>
	 *  The given value must belong to the interval [0,3*2^37-192),
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
		 * A multipre of 2 or 3 can't be prime.
		 * An even number different from 2 can't be prime.
		 * This check is necessary because the internal
		 * structure is optimized to handle only odd values
		 * that are not multiple of 3.
		 */
		if( value == 2 || value == 3 ) return true;
		if( value % 2 == 0 || value % 3 == 0 ) return false;
		
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
		
		/* Checks if the value belongs to the interval [0,3*2^37-192). */
		checkBounds( value );
		
		final long index = value / 3;
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
		 * We always ensure the requested value to be odd
		 * and to be not a multiple of 3.
		 * The bits in the bit-array represents only odd
		 * values not multiple of 3, so to get the right
		 * position in the bit-array we need to divide
		 * the value by 3.
		 */
		
		final long index = value / 3;
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
		final long endValue = 3L * data.length * BLOCK_SIZE;
		
		/* The number represented by the first bit of the start block. */
		final long startBlockValue = 3L * startBlock * BLOCK_SIZE;
		
		/*
		 * We need to sift only the primes in the interval
		 * [5, sqrt(end)] because the other non primes
		 * have already been sifted. 
		 */
		final long lastPrime = Math.round( Math.sqrt(endValue) );
		
		/*
		 * The bit-array doesn't contain multiples of 2 or 3
		 * so removing them is not necessary, but also they
		 * are not represented so must be skipped.
		 * The boost value is needed to skip multiples of 3
		 * that are not also multiples of 2 i.e. the odd
		 * multiples of 3.
		 * Starting from the list of all numbers:
		 * 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 ...
		 * we remove all the even numbers getting:
		 * 1 3 5 7 9 11 13 15 17 19 21 23 25 27 29 31 33 ...
		 * n n y y n  y  y  n  y  y  n  y  y  n  y  y  n ...
		 * The numbers represented in the bit-array are all
		 * those marked with 'y'. So in the following loop
		 * we need to skip one position each 2, for this reason
		 * we use the boost. 
		 */
		long startValue;
		int boost = 1;
		for( long i = 5; i <= lastPrime; i += 2 << boost )
		{
			
			/*
			 * boost is initialized to 1 so during this first
			 * loop boost becomes 0 and the next value of i 
			 * is 7. Then boost becomes 1 so 9 will be skipped
			 * and the following value of i will be 11. 
			 */
			boost = 1 - boost;
			
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
				 * of i that is not divisible by 3. If the greatest
				 * value is i * i we already have a valid value,
				 * otherwise we have a power of 2, so we need to
				 * adjust it to get the needed value.
				 */
				if( startValue <= startBlockValue )
					startValue = adjustValue( startBlockValue, i );
				
				/* The start index is the startValue / 3. */
				
				/* We remove all the multiples of i. */
				siftMultiples( data, startValue, endValue, i );
				
			}
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
		 * Ex. value = 64, prime = 7 => 64 - (64 % 7) = 64 - 1 = 63;
		 */
		value = value - (value % prime);
		
		/*
		 * If value is even, we remove prime to get the
		 * greatest odd multiple of prime smaller than
		 * value. The couples of values given to this
		 * method ensures that the returned value will
		 * never be smaller then 5; 
		 * Ex. 63 is odd, the value remains unchanged.
		 */
		value = value % 2 == 0 ? value - prime : value;
		
		/*
		 * At this point we need to ensure that the value
		 * is not divisible by 3, otherwise we add 2 * prime
		 * getting the smallest multiple of prime not divisible
		 * by 3 and not sifted yet.
		 * Ex. 63 is a multiple or 3 => 63 + (7 << 1) = 63 + 14 = 77.
		 */
		return value % 3 == 0 ? value + (prime << 1) : value;
		
	}
	
	
	/**
	 * This method sifts the multiples of the given prime
	 * according to the Eratosthenes Sieve algorithm.
	 *  
	 * @param data       the data to sift.
	 * @param startValue the value to start with.
	 * @param endValue   the value that represents the end of the interval.
	 * @param prime      the prime which multiples have to be sift out. 
	 */
	private void siftMultiples( long[] data, long startValue, long endValue, long prime )
	{
		
		/*
		 * The principle of skipping the odd multiples of 3 exposed in the method
		 * "performSieve" holds also in this method. Here we need to remove all
		 * the multiples of prime that are not divisible by 2 or 3 i.e. we need to
		 * skip all odd values x such that x % 3 = 0.
		 * Assuming prime % 3 = 1 we have:
		 * prime = 3x + 1 => 2 * prime = 3 * 2x + 2 => 2 * prime % 3 = 2.
		 * So we need to skip all the positions p in the form p * i + 2.
		 * Otherwise id prime % 3 = 2 we have:
		 * prime = 3x + 2 => 2 * prime = 3 * (2x+1) + 1 => 2 * prime % 3 = 1.
		 * So we need to skip all the positions p in the form p * i + 1.
		 * For that reason we initialize the boost as prime % 3.
		 */
		int boost = (int)(prime % 3);
		
		/*
		 * The startValue is granted not to be a multiple of 3
		 * so startValue % 3 can be 1 or 2. In the following loop
		 * we start by startValue and we step forward by 2 * prime
		 * or 4 * prime depending on the boost. So we need to
		 * adjust the boost so that we not catch a multiple of 3. 
		 * To do that we need to properly combine the values of
		 * startValue % 3 and prime % 3.
		 * The following operation is given by the table:
		 * 
		 * sartValue % 3	prime % 3	result
		 * 		1				1			2
		 * 		1				2			1
		 * 		2				1			1
		 * 		2 	 			2			2
		 * 
		 * But considering the fact that the boost will be
		 * inverted during the loop we need to invert the
		 * operation to get the right starting value.
		 */
		boost = startValue % 3 == 2 ? 3 - boost : boost; 
		
		
		/* 
		 * NOTES REGARDING THE FOLLOWING BINARY OPERATIONS:
		 * 
		 * Setting the bit-word to 1 means that all the
		 * bits in the bit-word are 0 rather than the last one.
		 * 1 = 00000000-00000000-00000000-00000001 (32 bit-word) 
		 *
		 * This operation moves all the bits to the left by the given
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
		
		int block;
		long mask, index;
		for( long value = startValue; value < endValue; value += prime << boost )
		{
			
			/*
			 * For the same reason exposed in the method "performSieve"
			 * the boost value must be alternated between 1 and 2.
			 * We ensure boost to be 1 or 2 , and with this operation
			 * we set boost = 2 if it was 1 and vice versa. 
			 */
			boost = 3 - boost;
			
			/* The bit-array index is given by the value / 3. */
			index = value / 3;
			
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
