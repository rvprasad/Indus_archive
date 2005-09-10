
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

import org.apache.commons.lang.builder.ToStringBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public final class TypeSpecification {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(TypeSpecification.class);

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	private static final Object EXCLUSIVE_ANCESTORS = "EXCLUSIVE_ANCESTORS";

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	private static final Object EXCLUSIVE_DESCENDANTS = "EXCLUSIVE_DESCENDANTS";

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	private static final Object INCLUSIVE_ANCESTORS = "INCLUSIVE_ANCESTORS";

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	private static final Object INCLUSIVE_DESCENDANTS = "INCLUSIVE_DESCENDANTS";

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	private static final Object IDENTITY = "IDENTITY";

	/** 
	 * This correspond to the enumeration constants used in java-xml binding under <code>scopeExtension</code> element.
	 */
	private static final Object PRIMITIVE = "PRIMITIVE";

	/** 
	 * This contains the regex pattern in case of IDENTITY type specification.
	 */
	private Pattern nameRegex;

	/** 
	 * This is the specification for the name of the type.
	 */
	private String namePattern;

	/** 
	 * One of EXCLUSIVE_ANCESTORS, EXCLUSIVE_DESCENDANTS, INCLUSIVE_ANCESTORS, INCLUSIVE_DESCENDANTS, IDENTITY, and
	 * PRIMITIVE.
	 */
	private String scopeExtension;

	/**
	 * Sets the value of <code>namePattern</code>.
	 *
	 * @param spec the new value of <code>namePattern</code>.
	 *
	 * @pre spec != null
	 */
	public void setNamePattern(final String spec) {
		this.namePattern = spec;

		if (scopeExtension != null && scopeExtension.equals(IDENTITY)) {
			nameRegex = Pattern.compile(spec);
		}
	}

	/**
	 * Retrieves the value in <code>namePattern</code>.
	 *
	 * @return the value in <code>namePattern</code>.
	 */
	public String getNamePattern() {
		return namePattern;
	}

	/**
	 * Sets the value of <code>scopeExtension</code>.
	 *
	 * @param theScopeExtension the new value of <code>scopeExtension</code>.
	 */
	public void setScopeExtension(final String theScopeExtension) {
		this.scopeExtension = theScopeExtension;

		if (scopeExtension.equals(IDENTITY) && namePattern != null) {
			nameRegex = Pattern.compile(namePattern);
		}
	}

	/**
	 * Retrieves the value in <code>scopeExtension</code>.
	 *
	 * @return the value in <code>scopeExtension</code>.
	 */
	public String getScopeExtension() {
		return scopeExtension;
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

		if (scopeExtension.equals(IDENTITY)) {
			_result = nameRegex.matcher(_name).matches();
		} else if (scopeExtension.equals(PRIMITIVE)) {
			_result = namePattern.equals(_name);
		} else {
			final SootClass _sc = ((RefType) type).getSootClass();
			final SootClass _basisClass = system.getClass(namePattern);

			if (scopeExtension.equals(EXCLUSIVE_ANCESTORS)) {
				_result = Util.isDescendentOf(_basisClass, _sc) && !_sc.equals(_basisClass);
			} else if (scopeExtension.equals(EXCLUSIVE_DESCENDANTS)) {
				_result = Util.isDescendentOf(_sc, _basisClass) && !_sc.equals(_basisClass);
			} else if (scopeExtension.equals(INCLUSIVE_ANCESTORS)) {
				_result = Util.isDescendentOf(_basisClass, _sc);
			} else if (scopeExtension.equals(INCLUSIVE_DESCENDANTS)) {
				_result = Util.isDescendentOf(_sc, _basisClass);
			} else {
				final String _msg = "Invalid scope extension [" + scopeExtension + "] for reference type " + _name;
				LOGGER.error(_msg);
				throw new IllegalStateException(_msg);
			}
		}

		return _result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("namePattern", this.namePattern)
										  .append("nameRegex", this.nameRegex).append("scopeExtension", this.scopeExtension)
										  .toString();
	}
}

// End of File
