
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

import edu.ksu.cis.indus.interfaces.IEnvironment;

import soot.Type;


/**
 * This class represents the specification of types in the realm of scope definition.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class TypeSpecification {
	/** 
	 * One of EXCLUSIVE_ANCESTORS, EXCLUSIVE_DESCENDANTS, INCLUSIVE_ANCESTORS, INCLUSIVE_DESCENDANTS, and IDENTITY.
	 */
	private String hierarchySpec;

	/** 
	 * This is the specification for the name of the type. It should be a regular expression.
	 */
	private String namePattern;

	/** 
	 * This indicates if the specification should be interpreted as inclusive or exclusive.
	 */
	private boolean inclusion;

	/**
	 * Creates a new TypeSpecification object. This is used by xml-java binding.
	 */
	TypeSpecification() {
	}

	/**
	 * Creates a new TypeSpecification object.  This is used by xml-java binding.
	 *
	 * @param nameSpec is the name specification.
	 */
	TypeSpecification(final String nameSpec) {
		namePattern = nameSpec;
	}

	/**
	 * Checks if the given type confirms to this specification in the given system.
	 *
	 * @param type to be checked.
	 * @param system in which to check.
	 *
	 * @return <code>true</code> if the type confirms; <code>false</code>, otherwise.
	 *
	 * @pre type != null and system !=null
	 */
	public boolean conformant(final Type type, final IEnvironment system) {
		// TODO:        
		return false;
	}

	/**
	 * Sets the value of <code>hierarchySpec</code>.
	 *
	 * @param theHierarchySpec the new value of <code>hierarchySpec</code>.
	 */
	void setHierarchySpec(String theHierarchySpec) {
		this.hierarchySpec = theHierarchySpec;
	}

	/**
	 * Retrieves the value in <code>hierarchySpec</code>.
	 *
	 * @return the value in <code>hierarchySpec</code>.
	 */
	String getHierarchySpec() {
		return hierarchySpec;
	}

	/**
	 * Sets the value of <code>inclusion</code>.
	 *
	 * @param value the new value of <code>inclusion</code>.
	 */
	void setInclusion(boolean value) {
		this.inclusion = value;
	}

	/**
	 * Retrieves the value in <code>inclusion</code>.
	 *
	 * @return the value in <code>inclusion</code>.
	 */
	boolean isInclusion() {
		return inclusion;
	}

	/**
	 * Sets the value of <code>namePattern</code>.
	 *
	 * @param spec the new value of <code>namePattern</code>.
	 */
	void setNamePattern(final String spec) {
		this.namePattern = spec;
	}

	/**
	 * Retrieves the value in <code>namePattern</code>.
	 *
	 * @return the value in <code>namePattern</code>.
	 */
	String getNamePattern() {
		return namePattern;
	}
}

// End of File
