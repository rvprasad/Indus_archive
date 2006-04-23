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

package edu.ksu.cis.indus.common.scoping;

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
	String getName();

	/**
	 * Sets the access specification. If not set, default access specification is attached to this specification.
	 * 
	 * @param accessSpecification for this specification.
	 * @pre accessSpecification != null
	 */
	void setAccessSpec(final AccessSpecification accessSpecification);

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
	 * @pre nameOfTheSpec != null
	 */
	void setName(final String nameOfTheSpec);

}

// End of File
