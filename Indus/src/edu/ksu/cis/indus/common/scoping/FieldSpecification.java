
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

import java.util.regex.Pattern;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class FieldSpecification {
	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private AccessSpecification accessSpec;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Pattern namePattern;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private TypeSpecification declaringClassSpec;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private TypeSpecification fieldTypeSpec;

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param spec DOCUMENT ME!
	 */
	void setDeclaringClassSpec(final TypeSpecification spec) {
		declaringClassSpec = spec;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	TypeSpecification getDeclaringClassSpec() {
		return declaringClassSpec;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param spec DOCUMENT ME!
	 */
	void setFieldNameSpec(final String spec) {
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	String getFieldNameSpec() {
		return namePattern.pattern();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param spec DOCUMENT ME!
	 */
	void setFieldTypeSpec(final TypeSpecification spec) {
		fieldTypeSpec = spec;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	TypeSpecification getFieldTypeSpec() {
		return fieldTypeSpec;
	}
}

// End of File
