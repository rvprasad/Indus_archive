
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

import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.RefType;
import soot.SootClass;
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
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(TypeSpecification.class);

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>hierarchySpec</code> element.
	 */
	private static final Object EXCLUSIVE_ANCESTORS = "EXCLUSIVE_ANCESTORS";

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>hierarchySpec</code> element.
	 */
	private static final Object EXCLUSIVE_DESCENDANTS = "EXCLUSIVE_DESCENDANTS";

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>hierarchySpec</code> element.
	 */
	private static final Object INCLUSIVE_ANCESTORS = "INCLUSIVE_ANCESTORS";

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>hierarchySpec</code> element.
	 */
	private static final Object INCLUSIVE_DESCENDANTS = "INCLUSIVE_DESCENDANTS";

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>hierarchySpec</code> element.
	 */
	private static final Object IDENTITY = "IDENTITY";

	/** 
	 * This is the specification for the name of the type. It should be a regular expression.
	 */
	private Pattern namePattern;

	/** 
	 * One of EXCLUSIVE_ANCESTORS, EXCLUSIVE_DESCENDANTS, INCLUSIVE_ANCESTORS, INCLUSIVE_DESCENDANTS, and IDENTITY.
	 */
	private String hierarchySpec;

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
	 *
	 * @pre nameSpec != null
	 */
	TypeSpecification(final String nameSpec) {
		namePattern = Pattern.compile(nameSpec);
	}

	/**
	 * Checks if the given type confirms to this specification in the given system.
	 *
	 * @param type to be checked.
	 * @param system in which to check.
	 *
	 * @return <code>true</code> if the type confirms; <code>false</code>, otherwise.
	 *
	 * @throws IllegalStateException when the hierarchy specification is incorrect.
	 *
	 * @pre type != null and system !=null
	 */
	public boolean conformant(final Type type, final IEnvironment system) {
		final String _name = type.toString();
		boolean _result;

		if (type instanceof RefType) {
			final SootClass _sc = ((RefType) type).getSootClass();
			final SootClass _basisClass = system.getClass(namePattern.pattern());

			if (hierarchySpec.equals(EXCLUSIVE_ANCESTORS)) {
				_result = Util.isDescendentOf(_basisClass, _sc) && !_sc.equals(_basisClass);
			} else if (hierarchySpec.equals(EXCLUSIVE_DESCENDANTS)) {
				_result = Util.isDescendentOf(_sc, _basisClass) && !_sc.equals(_basisClass);
			} else if (hierarchySpec.equals(INCLUSIVE_ANCESTORS)) {
				_result = Util.isDescendentOf(_basisClass, _sc);
			} else if (hierarchySpec.equals(INCLUSIVE_DESCENDANTS)) {
				_result = Util.isDescendentOf(_sc, _basisClass);
			} else if (hierarchySpec.equals(IDENTITY)) {
				_result = namePattern.matcher(_name).matches();
			} else {
				LOGGER.error("Invalid hierarchy spec [" + hierarchySpec + "] for reference type " + _name);
				throw new IllegalStateException("Invalid hierarchy spec [" + hierarchySpec + "] for reference type " + _name);
			}
		} else {
			_result = namePattern.matcher(_name).matches();
		}

		if (!inclusion) {
			_result = !_result;
		}
		return false;
	}

	/**
	 * Sets the value of <code>hierarchySpec</code>.
	 *
	 * @param theHierarchySpec the new value of <code>hierarchySpec</code>.
	 */
	void setHierarchySpec(final String theHierarchySpec) {
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
	void setInclusion(final boolean value) {
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
	 *
	 * @pre spec != null
	 */
	void setNamePattern(final String spec) {
		this.namePattern = Pattern.compile(spec);
	}

	/**
	 * Retrieves the value in <code>namePattern</code>.
	 *
	 * @return the value in <code>namePattern</code>.
	 */
	String getNamePattern() {
		return namePattern.pattern();
	}
}

// End of File
