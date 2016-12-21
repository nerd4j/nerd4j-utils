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

/**
 * A type implementing this interface represent a {@link DependentBean} with
 * support for dependency management (add and remove).
 * 
 * @author Nerd4j Team
 */
public interface ConfigurableDependentBean extends DependentBean
{

	/**
	 * Add a new direct dependency.
	 * 
	 * @param dependency bean from which depend.
	 * @return {@code true} if the dependency has been added; <br>
	 *         {@code false} if the dependency hasn't been added because
	 *         already existing.
	 */
	public boolean addDepenency( DependentBean dependency );
	
	/**
	 * Remove a direct dependency.
	 * 
	 * @param dependency dependency to remove.
	 * @return {@code true} if the dependency has been removed; <br>
	 *         {@code false} if the dependency hasn't been removed because
	 *         not existing.
	 */
	public boolean removeDepenency( DependentBean dependency );
	
}
