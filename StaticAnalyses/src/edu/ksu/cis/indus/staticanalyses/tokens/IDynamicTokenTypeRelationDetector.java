
/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

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
