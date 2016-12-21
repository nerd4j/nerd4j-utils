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

import java.util.Set;

/**
 * A bean implementing this interface declares to depends from other beans for
 * its functionality.
 * 
 * <p>
 * <i>Note:</i> given that a type can depend only from other
 * {@code DependentBean} it needs to declare only direct dependencies. Other
 * dependencies will be inherited from the dependency tree.
 * 
 * @author Nerd4j Team
 */
public interface DependentBean
{
	
	/**
	 * Returns the current dependency set.
	 * 
	 * @return current dependency set
	 */
	public Set<? extends DependentBean> getDependencies();
	
}
