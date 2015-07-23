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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An implementation of the {@link ThreadFactory} interface that allows
 * to configure the name to associate to the created {@link Thread}s.
 * <p>
 *  The resulting {@link Thread} name will be <i>given-name</i>-pool-<i>n</i>-thread-<i>k</i> where:
 *  <ul>
 *   <li><i>given-name</i> is the name given to the factory in creation;</li>
 *   <li><i>n</i> the number of {@link NamedThreadFactory} created;</li>
 *   <li><i>k</i> the number of {@link Thread}s created by the current factory.</li>
 *  </ul>
 * </p>
 * 
 * @author Nerd4j Team
 */
public class NamedThreadFactory implements ThreadFactory 
{

	/** Counts the number of {@link NamedThreadFactory} instances. */
	private static final AtomicInteger poolCount = new AtomicInteger(0);
	
	/** Counts the number of {@link Thread}s created by this factory. */
	private AtomicInteger count = new AtomicInteger(0);

	/**
	 * Prefix of the names to be given to the {@link Thread}s generated
	 * by this {@link ThreadFactory}.
	 */
	private final String threadPrefix;

	
	/**
	 * Constructor with parameters.
	 * 
	 * @param name name to be associated to the factory.
	 */
	public NamedThreadFactory( String name )
	{

		if (name == null || (name = name.trim()).isEmpty())
			name = "Unnamed";

		final int pool = poolCount.getAndIncrement();

		threadPrefix = name + "-pool-" + pool + "-thread-";

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
		
		return new Thread(task, threadPrefix + count.getAndIncrement());

	}
	
}
