
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

package edu.ksu.cis.indus.staticanalyses.tokens;

import java.util.Collection;


/**
 * This interface can be used to detect value-to-type relationship that may be orthogonal or transparent to the type system
 * being represented.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <V> is the type of the value object (in the representation).
 */
public interface IDynamicTokenTypeRelationDetector<V> {
	/**
	 * Retrieves values conforming to the given type.
	 *
	 * @param values for which new relations need to be discovered.
	 * @param type based on which new relations need to be discovered.
	 *
	 * @return a collection of values that conform to the given type.
	 *
	 * @pre values != null and type != null
	 * @post result != null
	 * @post values.containsAll(result)
	 */
	Collection<V> getValuesConformingTo(Collection<V> values, IType type);

	/**
	 * Reset the token manager.
	 */
	void reset();
}

// End of File
