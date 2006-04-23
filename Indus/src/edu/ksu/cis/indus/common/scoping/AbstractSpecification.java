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

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This class captures attributes common all scope specifications.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
abstract class AbstractSpecification
		implements ISpecification {

	/**
	 * This is the access control specification.
	 */
	private AccessSpecification accessSpec = new AccessSpecification();

	/**
	 * This indicates if the specification should be interpreted as inclusive or exclusive.
	 */
	private boolean inclusion = true;

	/**
	 * The name of this specification.
	 */
	private String name;

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.common.scoping.ISpecification#getName()
	 */
	public final String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.common.scoping.ISpecification#setAccessSpec(edu.ksu.cis.indus.common.scoping.AccessSpecification)
	 */
	public final void setAccessSpec(final AccessSpecification accessSpecification) {
		this.accessSpec = accessSpecification;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.common.scoping.ISpecification#setInclusion(boolean)
	 */
	public final void setInclusion(final boolean value) {
		this.inclusion = value;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.common.scoping.ISpecification#setName(java.lang.String)
	 */
	public final void setName(final String nameOfTheSpec) {
		this.name = nameOfTheSpec;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		return new ToStringBuilder(this).append("inclusion", this.inclusion).append("accessSpec", this.accessSpec).append(
				"name", this.name).toString();
	}

	/**
	 * Checks if the given access specifier conformant.
	 * 
	 * @param accessSpecifier to be tested.
	 * @return <code>true</code> if it is conformant; <code>false</code>, otherwise.
	 * @pre accessSpecifier != null
	 */
	protected final boolean accessConformant(final AccessSpecifierWrapper accessSpecifier) {
		return accessSpec.conformant(accessSpecifier);
	}

	/**
	 * Retrieves the value in <code>inclusion</code>.
	 * 
	 * @return the value in <code>inclusion</code>.
	 */
	boolean isInclusion() {
		return inclusion;
	}
}

// End of File
