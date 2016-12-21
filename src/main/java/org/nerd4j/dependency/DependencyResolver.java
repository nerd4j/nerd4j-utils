/*
 * #%L
 * Nerd4j Utils
 * %%
 * Copyright (C) 2011 - 2016 Nerd4j
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
/*
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
import java.util.List;


/**
 * Resolves dependency order between {@link DependentBean}s
 * 
 * @author Nerd4j Team
 */
public interface DependencyResolver
{

	/**
	 * Resolves dependencies of given {@link DependentBean} returning an
	 * ordered list of dependencies that need to be satisfied.
	 * 
	 * @param bean bean to be evaluated for dependency resolution.
	 * @param <X> actual type of the {@link DependentBean}.
	 * @return ordered dependency list.
	 * @throws CircularDependencyException
	 *             if found a unsolvable dependency due to a cycle in the
	 *             dependency graph.
	 */
	public <X extends DependentBean> List<X> resolve( X bean ) throws CircularDependencyException;
	
	/**
	 * Resolves dependencies of given collection of {@link DependentBean}
	 * returning an ordered list of dependencies that need to be satisfied.
	 * 
	 * @param beans beans to be evaluated for dependency resolution.
	 * @param <X> actual type of the {@link DependentBean}.
	 * @return ordered dependency list.
	 * @throws CircularDependencyException
	 *             if found a unsolvable dependency due to a cycle in the
	 *             dependency graph.
	 */
	public <X extends DependentBean> List<X> resolve( Collection<X> beans )  throws CircularDependencyException;
	
}
