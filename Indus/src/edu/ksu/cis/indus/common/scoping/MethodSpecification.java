
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.regex.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.Type;


/**
 * This class represents method-level scope specification.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class MethodSpecification
  extends AbstractSpecification {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(MethodSpecification.class);

	/** 
	 * This is the specifications of the types of the parameters.
	 *
	 * @invariant parameterTypeSpecs.oclIsKindOf(List(TypeSpecification))
	 */
	private final List parameterTypeSpecs = new ArrayList();

	/** 
	 * The pattern of the method's name.
	 */
	private Pattern namePattern;

	/** 
	 * This is the specification of the type of the class that declares the method.
	 */
	private TypeSpecification declaringClassSpec;

	/** 
	 * This is the specification of the return type of the method.
	 */
	private TypeSpecification returnTypeSpec;

	/**
	 * Checks if the given method is in the scope of this specification in the given environment.
	 *
	 * @param method to be checked for scope constraints.
	 * @param system in which the check the constraints.
	 *
	 * @return <code>true</code> if the given method lies within the scope defined by this specification; <code>false</code>,
	 * 		   otherwise.
	 *
	 * @pre method != null and system != null
	 */
	public boolean isInScope(final SootMethod method, final IEnvironment system) {
		boolean _result = namePattern.matcher(method.getName()).matches();
		_result = _result && declaringClassSpec.conformant(method.getDeclaringClass().getType(), system);
		_result = _result && returnTypeSpec.conformant(method.getReturnType(), system);
		_result = _result && accessConformant(new AccessSpecifierWrapper(method));

		if (_result) {
			final List _parameterTypes = method.getParameterTypes();
			final Iterator _i = _parameterTypes.iterator();
			final int _iEnd = _parameterTypes.size();

			for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
				final Type _type = (Type) _i.next();
				final TypeSpecification _pTypeSpec = (TypeSpecification) parameterTypeSpecs.get(_iIndex);

				if (_pTypeSpec != null) {
					_result |= _pTypeSpec.conformant(_type, system);
				}
			}
		}

		if (!isInclusion()) {
			_result = !_result;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " " + method + " " + _result);
		}

		return _result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("namePattern", this.namePattern.pattern())
										  .append("returnTypeSpec", this.returnTypeSpec)
										  .append("parameterTypeSpecs", this.parameterTypeSpecs)
										  .append("declaringClassSpec", this.declaringClassSpec).toString();
	}

	/**
	 * Sets the specification of the class that declares the method.
	 *
	 * @param spec the specification.
	 *
	 * @pre spec != null
	 */
	void setDeclaringClassSpec(final TypeSpecification spec) {
		declaringClassSpec = spec;
	}

	/**
	 * Retrieves the specification of the class that declares the method.
	 *
	 * @return the specification.
	 */
	TypeSpecification getDeclaringClassSpec() {
		return declaringClassSpec;
	}

	/**
	 * Sets the specification of the method's name.
	 *
	 * @param spec is a regular expression.
	 *
	 * @pre spec != null
	 */
	void setMethodNameSpec(final String spec) {
		namePattern = Pattern.compile(spec);
	}

	/**
	 * Retrieves the specification of the method's name.
	 *
	 * @return the specification.
	 */
	String getMethodNameSpec() {
		return namePattern.pattern();
	}

	/**
	 * Sets the specification of the type of the parameters of the method.
	 *
	 * @param specs the specifications.
	 *
	 * @pre spec != null and specs.oclIsKindOf(Sequence(TypeSpecification))
	 */
	void setParameterTypeSpecs(final List specs) {
		parameterTypeSpecs.addAll(specs);
	}

	/**
	 * Retrieves the specification of the type of the parameters of the method.
	 *
	 * @return a list of specifications.
	 *
	 * @post result.oclIsKindOf(Sequence(TypeSpecification))
	 */
	List getParameterTypeSpecs() {
		return parameterTypeSpecs;
	}

	/**
	 * Sets the specification of the return type of the method.
	 *
	 * @param spec the specification.
	 *
	 * @pre spec != null
	 */
	void setReturnTypeSpec(final TypeSpecification spec) {
		returnTypeSpec = spec;
	}

	/**
	 * Retrieves the specification of the return type of the method.
	 *
	 * @return the specification.
	 */
	TypeSpecification getReturnTypeSpec() {
		return returnTypeSpec;
	}

	/**
	 * Creates the container for parameter type specifications. This is used by java-xml binding.
	 *
	 * @return a container.
	 *
	 * @post result != null
	 */
	static List createParameterTypeSpecContainer() {
		return new ArrayList();
	}
}

// End of File
