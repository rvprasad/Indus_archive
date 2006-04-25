/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */

package edu.ksu.cis.indus.interfaces;

import edu.ksu.cis.indus.annotations.NonNull;

/**
 * This interface helps realize the <i>IPrototype</i> design pattern as defined in the Gang of Four book. It provides the
 * methods via which concrete object can be created from a prototype object. The default implementation for these methods
 * should raise <code>UnsupportedOperationException</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <T> is the type of the prototype.
 */
public interface IPrototype<T> {

	/**
	 * Creates a concrete object from this prototype object. The concrete object can be parameterized by the information in
	 * <code>o</code>.
	 * 
	 * @param o object containing the information to parameterize the concrete object.
	 * @return concrete object based on this prototype object.
	 */
	T getClone(@NonNull Object... o);
}

// End of File
