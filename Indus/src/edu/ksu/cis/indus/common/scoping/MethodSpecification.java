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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootMethod;
import soot.Type;

/**
 * This class represents method-level scope specification.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class MethodSpecification
		extends AbstractSpecification {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(MethodSpecification.class);

	/**
	 * This is the specification of the type of the class that declares the method.
	 */
	private TypeSpecification declaringClassSpec;

	/**
	 * The pattern of the method's name.
	 */
	private Pattern namePattern;

	/**
	 * This is the specifications of the types of the parameters.
	 */
	private final List<TypeSpecification> parameterTypeSpecs = new ArrayList<TypeSpecification>();

	/**
	 * This is the specification of the return type of the method.
	 */
	private TypeSpecification returnTypeSpec;

	/**
	 * Creates the container for parameter type specifications. This is used by java-xml binding.
	 * 
	 * @return a container.
	 * @post result != null
	 */
	static List createParameterTypeSpecContainer() {
		return new ArrayList();
	}

	/**
	 * Retrieves the specification of the class that declares the method.
	 * 
	 * @return the specification.
	 */
	public TypeSpecification getDeclaringClassSpec() {
		return declaringClassSpec;
	}

	/**
	 * Retrieves the specification of the method's name.
	 * 
	 * @return the specification.
	 */
	public String getMethodNameSpec() {
		return namePattern.pattern();
	}

	/**
	 * Retrieves the specification of the type of the parameters of the method.
	 * 
	 * @return a list of specifications.
	 */
	public List<TypeSpecification> getParameterTypeSpecs() {
		return parameterTypeSpecs;
	}

	/**
	 * Retrieves the specification of the return type of the method.
	 * 
	 * @return the specification.
	 */
	public TypeSpecification getReturnTypeSpec() {
		return returnTypeSpec;
	}

	/**
	 * Checks if the given method is in the scope of this specification in the given environment.
	 * 
	 * @param method to be checked for scope constraints.
	 * @param system in which the check the constraints.
	 * @return <code>true</code> if the given method lies within the scope defined by this specification; <code>false</code>,
	 *         otherwise.
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
				final TypeSpecification _pTypeSpec = parameterTypeSpecs.get(_iIndex);

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
	 * Sets the specification of the class that declares the method.
	 * 
	 * @param spec the specification.
	 * @pre spec != null
	 */
	public void setDeclaringClassSpec(final TypeSpecification spec) {
		declaringClassSpec = spec;
	}

	/**
	 * Sets the specification of the method's name.
	 * 
	 * @param spec is a regular expression.
	 * @pre spec != null
	 */
	public void setMethodNameSpec(final String spec) {
		namePattern = Pattern.compile(spec);
	}

	/**
	 * Sets the specification of the type of the parameters of the method.
	 * 
	 * @param specs the specifications.
	 * @pre spec != null
	 */
	public void setParameterTypeSpecs(final List<TypeSpecification> specs) {
		parameterTypeSpecs.addAll(specs);
	}

	/**
	 * Sets the specification of the return type of the method.
	 * 
	 * @param spec the specification.
	 * @pre spec != null
	 */
	public void setReturnTypeSpec(final TypeSpecification spec) {
		returnTypeSpec = spec;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("namePattern", this.namePattern.pattern())
				.append("returnTypeSpec", this.returnTypeSpec).append("parameterTypeSpecs", this.parameterTypeSpecs).append(
						"declaringClassSpec", this.declaringClassSpec).toString();
	}
}

// End of File
