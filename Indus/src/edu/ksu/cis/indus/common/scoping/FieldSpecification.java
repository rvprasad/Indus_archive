
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

import java.util.regex.Pattern;

import soot.SootField;


/**
 * This class represents field-level scope specification.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class FieldSpecification
  extends AbstractSpecification {
	/** 
	 * The pattern of the field's name.
	 */
	private Pattern namePattern;

	/** 
	 * This is the specification of the type of the class that declares the field.
	 */
	private TypeSpecification declaringClassSpec;

	/** 
	 * This is the specification of the type of the field.
	 */
	private TypeSpecification fieldTypeSpec;

	/**
	 * Checks if the given field is in the scope of this specification in the given environment.
	 *
	 * @param field to be checked for scope constraints.
	 * @param system in which the check the constraints.
	 *
	 * @return <code>true</code> if the given field lies within the scope defined by this specification; <code>false</code>,
	 * 		   otherwise.
	 *
	 * @pre field != null and system != null
	 */
	public boolean isInScope(final SootField field, final IEnvironment system) {
		boolean _result = accessConformant(new AccessSpecifierWrapper(field));
		_result &= fieldTypeSpec.conformant(field.getType(), system);
		_result &= declaringClassSpec.conformant(field.getDeclaringClass().getType(), system);
		_result &= namePattern.matcher(field.getName()).matches();
		return _result;
	}

	/**
	 * Sets the specification of the class that declares the field.
	 *
	 * @param spec the specification.
	 *
	 * @pre spec != null
	 */
	void setDeclaringClassSpec(final TypeSpecification spec) {
		declaringClassSpec = spec;
	}

	/**
	 * Retrieves the specification of the class that declares the field.
	 *
	 * @return the specification.
	 */
	TypeSpecification getDeclaringClassSpec() {
		return declaringClassSpec;
	}

	/**
	 * Sets the specification of the field's name.
	 *
	 * @param spec is a regular expression.
	 *
	 * @pre spec != null
	 */
	void setFieldNameSpec(final String spec) {
		namePattern = Pattern.compile(spec);
	}

	/**
	 * Retrieves the specification of the field's name.
	 *
	 * @return the specification.
	 */
	String getFieldNameSpec() {
		return namePattern.pattern();
	}

	/**
	 * Sets the specification of the type of the field.
	 *
	 * @param spec the specification.
	 *
	 * @pre spec != null
	 */
	void setFieldTypeSpec(final TypeSpecification spec) {
		fieldTypeSpec = spec;
	}

	/**
	 * Retrieves the specification of the type of the field.
	 *
	 * @return the specification.
	 */
	TypeSpecification getFieldTypeSpec() {
		return fieldTypeSpec;
	}
}

// End of File
