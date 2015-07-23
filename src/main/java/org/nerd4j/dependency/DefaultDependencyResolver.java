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
package org.nerd4j.dependency;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default {@link DependencyResolver} implementation.
 * 
 * @author Nerd4j Team
 */
public class DefaultDependencyResolver implements DependencyResolver
{

	/** Static class logger. */
	private static final Logger logger = LoggerFactory.getLogger( DefaultDependencyResolver.class );
	
	/** Static Singleton instance. */
	private static final DependencyResolver INSTANCE = new DefaultDependencyResolver();
	
	/**
	 * Returns the Singleton instance.
	 */
	public static DependencyResolver getInstance()
	{
		return INSTANCE;
	}
	
	/**
	 * Create a new {@link DefaultDependencyResolver}.
	 */
	private DefaultDependencyResolver() {}
	
	/**
	 * Resolves the dependencies of a collection of {@link DependentBean}
	 * returning the dependencies in the order they need to be resolved.
	 * 
	 * @param depenentBeans collection of dependencies to be evaluated.
	 * @return ordered list of dependencies.
	 */
	@Override
	public <X extends DependentBean> List<X> resolve( Collection<X> depenentBeans )
	{
		
		logger.debug( "Building virtual dependency root node." );
		
		/* Creates a new root for the dependency tree. */
		@SuppressWarnings("unchecked")
		final DependentBean root = new VirtualRoot( ( Collection<DependentBean> ) depenentBeans );
		
		/* Resolves the dependencies from starting from the new created root. */
		final List<DependentBean> resolved = dependencyResolve( root );
		
		/* Removes the virtual root */
		resolved.remove( root );
		
		/* The list of DependentBean can now be casted to the actual implementation. */
		@SuppressWarnings("unchecked")
		final List<X> solution = (List<X>) resolved;
		
		return solution;
		
	}
	
	/**
	 * Resolves the dependencies of the given {@link DependentBean}
	 * returning the dependencies in the order they need to be resolved.
	 * 
	 * @param root the root of the dependency subtree.
	 * @return ordered list of dependencies.
	 */
	@Override
	public <X extends DependentBean> List<X> resolve( X root )
	{
		
		@SuppressWarnings("unchecked")
		final List<X> solution = (List<X>) dependencyResolve( root );
		
		return solution;
		
	}
	
	/**
	 * Resolves the order of the dependencies.
	 * 
	 * @param root the root of the dependency subtree.
	 * @return ordered list of dependencies.
	 */
	private List<DependentBean> dependencyResolve( DependentBean root )
	{
		
		final List<DependentBean> resolved = new LinkedList<DependentBean>();
		
		dependencyResolve( root, resolved, new HashSet<DependentBean>(), 0 );
		
		return resolved;
		
	}
	
	/**
	 * Recursively resolves the order of the dependencies.
	 * <p>
	 *  Once the given root has been resolved the root dependency
	 *  will be appended to the list of resolved dependencies.
	 *  Every dependency handled during the process will be added
	 *  to the set of visited dependencies.
	 * </p>
	 * 
	 * @param root     the root of the dependency subtree.
	 * @param resolved list of resolved dependencies.
	 * @param seen     list of already seen dependencies.
	 * @param indents  tree depth (for logging purposes).
	 */
	private void dependencyResolve( DependentBean root, List<DependentBean> resolved, Set<DependentBean> seen, int indents )
	{
		
		++indents;
		prettyLogDebug( indents, "Analyzing DependentBean {}.", root );
		++indents;
		
		/*
		 * We add the current bean to the set of already seen beans.
		 * If we find it again we know that there is a circular dependency.
		 */
		seen.add( root );
		
		/*
		 * To consider a dependency ad resolved we need to resolve
		 * all its sub dependencies.
		 */
		for ( DependentBean dependency : root.getDependencies() )
		{
			
			prettyLogDebug( indents, "Analyzing dependency {}.", dependency );
			
			/* 
			 * We check if the dependency has bean already resolved.
			/* TODO: this check is very inefficient. */
			if ( !resolved.contains( dependency ) )
			{
				
				/*
				 * We check if the dependency has been seen before
				 * but is still not resolved.
				 */
				if ( !seen.contains( dependency ) )
				{
					
					/*
					 * The dependency was never found before so we try
					 * to resolve it.
					 */
					dependencyResolve( dependency, resolved, seen, indents );
					prettyLogDebug( indents, "Resolved dependency {}.", dependency );
					
				}
				else
				{
				
					/*
					 * The dependency has been found before and never
					 * resolved so we must argue that dependency depends
					 * on itself.
					 */
					throw new CircularDependencyException( "Circular dependency for bean " + dependency );
					
				}
				
			} else
			{
				
				/* The dependency has been already resolved. */
				prettyLogDebug( indents, "Already resolved dependency {}.", dependency );
				
			}
		}
		
		/*
		 * At this point all the dependency of the subtree root has been resolved.
		 * Se add the root itself to the list of resolved dependencies.
		 */
		resolved.add( root );
		
		--indents;
		prettyLogDebug( indents, "Resolved DependentBean {}.", root );
		--indents;
		
	}

	/**
	 * Log to debug.
	 * 
	 * @param indents log indent number
	 * @param log     slf4j log string
	 * @param args    slf4j log arguments
	 */
	private void prettyLogDebug( int indents, String log, Object... args )
	{
		
		if ( logger.isDebugEnabled() )
			logger.debug( prettyString(log, indents), args );
		
	}
	
	/**
	 * Prepare a <i>pretty</i> string for logging with a given number of indends.
	 * 
	 * @param string  string to alter
	 * @param indents intents to be added
	 * @return indented string
	 */
	private String prettyString( String string, int indents )
	{
		
		final int ind = indents;
		
		final StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < ind - 1; ++i)
			builder.append("  |  ");
		
		builder.append("  +--");
		
		builder.append(" ");
		
		return builder.append( string ).toString();
		
	}
	
	/**
	 * Virtual dependency tree root. Used when a dependency collection
	 * resolution is requested (essentially create a new tree joining multiple
	 * dependency trees under a unique root).
	 * 
	 * @author Nerd4j Team
	 */
	private class VirtualRoot implements ConfigurableDependentBean
	{

		private Set<DependentBean> dependencies;
		
		public VirtualRoot( Collection<DependentBean> dependencies )
		{
			
			this.dependencies = new HashSet<DependentBean>( dependencies );
			
		}
		
		@Override
		public boolean addDepenency(DependentBean dependency)
		{
			
			return dependencies.add( dependency );
			
		}

		@Override
		public boolean removeDepenency(DependentBean dependency)
		{
			
			return dependencies.remove( dependency );
			
		}

		@Override
		public Set<DependentBean> getDependencies()
		{
			
			return dependencies;
			
		}
		
	}
	
}
