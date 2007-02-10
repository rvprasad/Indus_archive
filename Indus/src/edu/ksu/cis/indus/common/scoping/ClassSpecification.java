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

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootClass;

/**
 * This class represents class-level scope specification.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ClassSpecification
		extends AbstractSpecification {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassSpecification.class);

	/**
	 * This is the type specification.
	 */
	@NonNull private TypeSpecification typeSpec;

	/**
	 * Creates an instance of this class.
	 */
	@Empty public ClassSpecification() {
		super();
	}

	/**
	 * Retrieves the value in <code>typeSpec</code>.
	 * 
	 * @return the value in <code>typeSpec</code>.
	 */
	@NonNull @Functional public TypeSpecification getTypeSpec() {
		return typeSpec;
	}

	/**
	 * Checks if the given class is in the scope of this specification in the given environment.
	 * 
	 * @param clazz to be checked for scope constraints.
	 * @param system in which the check the constraints.
	 * @return <code>true</code> if the given class lies within the scope defined by this specification; <code>false</code>,
	 *         otherwise.
	 */
	@Functional public boolean isInScope(@NonNull final SootClass clazz, @NonNull final IEnvironment system) {
		boolean _result = accessConformant(new AccessSpecifierWrapper(clazz));
		_result = _result && typeSpec.conformant(clazz.getType(), system);

		if (!isInclusion()) {
			_result = !_result;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(this + " " + clazz + " " + _result);
		}

		return _result;
	}

	/**
	 * Sets the value of <code>typeSpec</code>.
	 * 
	 * @param theTypeSpec the new value of <code>typeSpec</code>.
	 */
	public void setTypeSpec(@NonNull @Immutable final TypeSpecification theTypeSpec) {
		typeSpec = theTypeSpec;
	}

	/**
	 * {@inheritDoc}
	 */
	@Functional @Override public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("typeSpec", this.typeSpec).toString();
	}
}

// End of File
