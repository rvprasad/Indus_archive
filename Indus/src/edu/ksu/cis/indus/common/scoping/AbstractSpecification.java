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
import edu.ksu.cis.indus.annotations.NonNull;

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
	@NonNull private String name;

	/**
	 * {@inheritDoc}
	 */
	@Functional public final String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setAccessSpec(final AccessSpecification accessSpecification) {
		this.accessSpec = accessSpecification;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setInclusion(final boolean value) {
		this.inclusion = value;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void setName(final String nameOfTheSpec) {
		this.name = nameOfTheSpec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @NonNull @Override public String toString() {
		return new ToStringBuilder(this).append("inclusion", this.inclusion).append("accessSpec", this.accessSpec).append(
				"name", this.name).toString();
	}

	/**
	 * Checks if the given access specifier conformant.
	 * 
	 * @param accessSpecifier to be tested.
	 * @return <code>true</code> if it is conformant; <code>false</code>, otherwise.
	 */
	protected final boolean accessConformant(@NonNull final AccessSpecifierWrapper accessSpecifier) {
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
