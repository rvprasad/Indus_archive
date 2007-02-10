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

package edu.ksu.cis.indus.common.scoping;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

/**
 * This is the interface to a scope specification.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
interface ISpecification {

	/**
	 * Retrieves the value in <code>name</code>.
	 * 
	 * @return the value in <code>name</code>.
	 */
	@Functional String getName();

	/**
	 * Sets the access specification. If not set, default access specification is attached to this specification.
	 * 
	 * @param accessSpecification for this specification.
	 */
	void setAccessSpec(@NonNull @Immutable final AccessSpecification accessSpecification);

	/**
	 * Sets the value of <code>inclusion</code>.
	 * 
	 * @param value the new value of <code>inclusion</code>.
	 */
	void setInclusion(final boolean value);

	/**
	 * Sets the value of <code>name</code>.
	 * 
	 * @param nameOfTheSpec the new value of <code>name</code>.
	 */
	void setName(@NonNull @Immutable final String nameOfTheSpec);

}

// End of File
