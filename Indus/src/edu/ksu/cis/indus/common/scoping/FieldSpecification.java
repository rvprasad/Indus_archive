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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.regex.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootField;

/**
 * This class represents field-level scope specification.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class FieldSpecification
		extends AbstractSpecification {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(FieldSpecification.class);

	/**
	 * This is the specification of the type of the class that declares the field.
	 */
	@NonNull private TypeSpecification declaringClassSpec;

	/**
	 * This is the specification of the type of the field.
	 */
	@NonNull private TypeSpecification fieldTypeSpec;

	/**
	 * The pattern of the field's name.
	 */
	@NonNull private Pattern namePattern;

	/**
	 * Creates an instance of this class.
	 */
	@Empty public FieldSpecification() {
		super();
	}

	/**
	 * Retrieves the specification of the class that declares the field.
	 * 
	 * @return the specification.
	 */
	@NonNull @Functional public TypeSpecification getDeclaringClassSpec() {
		return declaringClassSpec;
	}

	/**
	 * Retrieves the specification of the field's name.
	 * 
	 * @return the specification.
	 */
	@NonNull @Functional public String getFieldNameSpec() {
		return namePattern.pattern();
	}

	/**
	 * Retrieves the specification of the type of the field.
	 * 
	 * @return the specification.
	 */
	@NonNull @Functional public TypeSpecification getFieldTypeSpec() {
		return fieldTypeSpec;
	}

	/**
	 * Checks if the given field is in the scope of this specification in the given environment.
	 * 
	 * @param field to be checked for scope constraints.
	 * @param system in which the check the constraints.
	 * @return <code>true</code> if the given field lies within the scope defined by this specification; <code>false</code>,
	 *         otherwise.
	 */
	@Functional public boolean isInScope(@NonNull final SootField field, @NonNull final IEnvironment system) {
		boolean _result = accessConformant(new AccessSpecifierWrapper(field));
		_result = _result && fieldTypeSpec.conformant(field.getType(), system);
		_result = _result && declaringClassSpec.conformant(field.getDeclaringClass().getType(), system);
		_result = _result && namePattern.matcher(field.getName()).matches();

		if (!isInclusion()) {
			_result = !_result;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " " + field + " " + _result);
		}

		return _result;
	}

	/**
	 * Sets the specification of the class that declares the field.
	 * 
	 * @param spec the specification.
	 */
	public void setDeclaringClassSpec(@NonNull @Immutable final TypeSpecification spec) {
		declaringClassSpec = spec;
	}

	/**
	 * Sets the specification of the field's name.
	 * 
	 * @param spec is a regular expression.
	 */
	public void setFieldNameSpec(@NonNull @Immutable final String spec) {
		namePattern = Pattern.compile(spec);
	}

	/**
	 * Sets the specification of the type of the field.
	 * 
	 * @param spec the specification.
	 */
	public void setFieldTypeSpec(@NonNull @Immutable final TypeSpecification spec) {
		fieldTypeSpec = spec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("namePattern", this.namePattern.pattern())
				.append("fieldTypeSpec", this.fieldTypeSpec).append("declaringClassSpec", this.declaringClassSpec).toString();
	}
}

// End of File
