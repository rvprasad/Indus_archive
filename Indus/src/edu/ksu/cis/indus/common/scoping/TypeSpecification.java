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
	 * This is the specification for the name of the type.
	 */
	@NonNull private String namePattern;

	/**
	 * This contains the regex pattern in case of IDENTITY type specification.
	 */
	private Pattern nameRegex;

	/**
	 * This indicates the extension on the scope.
	 */
	private ScopeExtensionEnum scopeExtension;

	/**
	 * Creates an instance of this class.
	 */
	@Empty public TypeSpecification() {
		super();
	}

	/**
	 * Checks if the given type confirms to this specification in the given system.
	 * 
	 * @param type to be checked.
	 * @param system in which to check.
	 * @return <code>true</code> if the type confirms; <code>false</code>, otherwise.
	 * @throws IllegalStateException when the hierarchy specification is incorrect.
	 */
	@Functional public boolean conformant(@NonNull final Type type, @NonNull final IEnvironment system) {
		final String _name = type.toString();
		boolean _result;

		if (scopeExtension.equals(ScopeExtensionEnum.IDENTITY)) {
			_result = nameRegex.matcher(_name).matches();
		} else if (scopeExtension.equals(ScopeExtensionEnum.PRIMITIVE)) {
			_result = namePattern.equals(_name);
		} else {
			final SootClass _sc = ((RefType) type).getSootClass();
			final SootClass _basisClass = system.getClass(namePattern);

			if (scopeExtension.equals(ScopeExtensionEnum.EXCLUSIVE_ANCESTORS)) {
				_result = Util.isDescendentOf(_basisClass, _sc) && !_sc.equals(_basisClass);
			} else if (scopeExtension.equals(ScopeExtensionEnum.EXCLUSIVE_DESCENDANTS)) {
				_result = Util.isDescendentOf(_sc, _basisClass) && !_sc.equals(_basisClass);
			} else if (scopeExtension.equals(ScopeExtensionEnum.INCLUSIVE_ANCESTORS)) {
				_result = Util.isDescendentOf(_basisClass, _sc);
			} else if (scopeExtension.equals(ScopeExtensionEnum.INCLUSIVE_DESCENDANTS)) {
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
	 * Retrieves the value in <code>namePattern</code>.
	 * 
	 * @return the value in <code>namePattern</code>.
	 */
	@NonNull @Functional public String getNamePattern() {
		return namePattern;
	}

	/**
	 * Retrieves the value in <code>scopeExtension</code>.
	 * 
	 * @return the value in <code>scopeExtension</code>.
	 */
	@NonNull @Functional public ScopeExtensionEnum getScopeExtension() {
		return scopeExtension;
	}

	/**
	 * Sets the value of <code>namePattern</code>.
	 * 
	 * @param spec the new value of <code>namePattern</code>.
	 */
	public void setNamePattern(@NonNull @Immutable final String spec) {
		this.namePattern = spec;

		if (scopeExtension != null && scopeExtension.equals(ScopeExtensionEnum.IDENTITY)) {
			nameRegex = Pattern.compile(spec);
		}
	}

	/**
	 * Sets the value of <code>scopeExtension</code>.
	 * 
	 * @param theScopeExtension the new value of <code>scopeExtension</code>.
	 */
	public void setScopeExtension(@NonNull @Immutable final ScopeExtensionEnum theScopeExtension) {
		this.scopeExtension = theScopeExtension;

		if (scopeExtension.equals(ScopeExtensionEnum.IDENTITY) && namePattern != null) {
			nameRegex = Pattern.compile(namePattern);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override @Functional public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("namePattern", this.namePattern).append(
				"nameRegex", this.nameRegex).append("scopeExtension", this.scopeExtension).toString();
	}
}

// End of File
