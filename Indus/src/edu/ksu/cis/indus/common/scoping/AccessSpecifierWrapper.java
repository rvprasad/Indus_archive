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
import edu.ksu.cis.indus.annotations.NonNull;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

/**
 * This is an interim class used for the purpose of wrapping Soot entities with access control information in a manner
 * conformant with Espina's philosophy.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class AccessSpecifierWrapper
		implements IAccessSpecifiers {

	/**
	 * The class being wrapped by this instance.
	 */
	private SootClass clazz;

	/**
	 * The field being wrapped by this instance.
	 */
	private SootField field;

	/**
	 * The method being wrapped by this instance.
	 */
	private SootMethod method;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param theClazz being wrapped.
	 */
	public AccessSpecifierWrapper(@NonNull final SootClass theClazz) {
		clazz = theClazz;
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param theField being wrapped.
	 */
	public AccessSpecifierWrapper(@NonNull final SootField theField) {
		field = theField;
	}

	/**
	 * Creates an instance of this class.
	 * 
	 * @param theMethod being wrapped.
	 */
	public AccessSpecifierWrapper(@NonNull final SootMethod theMethod) {
		method = theMethod;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isDefaultAccess() {
		boolean _result = false;

		if (clazz != null) {
			_result = !clazz.isPrivate() && !clazz.isProtected() && !clazz.isPublic();
		} else if (method != null) {
			_result = !method.isPrivate() && !method.isProtected() && !method.isPublic();
		} else if (field != null) {
			_result = !field.isPrivate() && !field.isProtected() && !field.isPublic();
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPrivateAccess() {
		boolean _result = false;

		if (clazz != null) {
			_result = clazz.isPrivate();
		} else if (method != null) {
			_result = method.isPrivate();
		} else if (field != null) {
			_result = field.isPrivate();
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isProtectedAccess() {
		boolean _result = false;

		if (clazz != null) {
			_result = clazz.isProtected();
		} else if (method != null) {
			_result = method.isProtected();
		} else if (field != null) {
			_result = field.isProtected();
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isPublicAccess() {
		boolean _result = false;

		if (clazz != null) {
			_result = clazz.isPublic();
		} else if (method != null) {
			_result = method.isPublic();
		} else if (field != null) {
			_result = field.isPublic();
		}
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Empty public void setDefaultAccess(@SuppressWarnings("unused") final boolean value) {
		// Does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Empty public void setPrivateAccess(@SuppressWarnings("unused") final boolean value) {
		// Does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Empty public void setProtectedAccess(@SuppressWarnings("unused") final boolean value) {
		// Does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Empty public void setPublicAccess(@SuppressWarnings("unused") final boolean value) {
		// Does nothing
	}
}

// End of File
