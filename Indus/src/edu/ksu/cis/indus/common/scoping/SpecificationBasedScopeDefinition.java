
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
import java.util.Collection;
import java.util.Iterator;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;


/**
 * This class represents scope definition. It can be used to filter classes, methods, and fields based on names  and
 * hierarchical relation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SpecificationBasedScopeDefinition {
	/** 
	 * The collection of class-level specification.
	 *
	 * @invariant classSpecs.oclIsKindOf(Collection(ClassSpecification))
	 */
	private Collection classSpecs;

	/** 
	 * The collection of field-level specification.
	 *
	 * @invariant classSpecs.oclIsKindOf(Collection(FieldSpecification))
	 */
	private Collection fieldSpecs;

	/** 
	 * The collection of method-level specification.
	 *
	 * @invariant classSpecs.oclIsKindOf(Collection(MethodSpecification))
	 */
	private Collection methodSpecs;

	/**
	 * Checks if the given class is in the scope in the given system.
	 *
	 * @param clazz to be checked.
	 * @param system in which to check.
	 *
	 * @return <code>true</code> if the given class is in the scope in the given system; <code>false</code>, otherwise.
	 *
	 * @pre clazz != null and system != null
	 */
	public boolean isInScope(final SootClass clazz, final IEnvironment system) {
		final Iterator _i = classSpecs.iterator();
		final int _iEnd = classSpecs.size();
		boolean _result = false;

		for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
			final ClassSpecification _cs = (ClassSpecification) _i.next();
			_result |= _cs.isInScope(clazz, system);
		}
		return _result;
	}

	/**
	 * Checks if the given method is in the scope in the given system.
	 *
	 * @param method to be checked.
	 * @param system in which to check.
	 *
	 * @return <code>true</code> if the given method is in the scope in the given system; <code>false</code>, otherwise.
	 *
	 * @pre method != null and system != null
	 */
	public boolean isInScope(final SootMethod method, final IEnvironment system) {
		final Iterator _i = methodSpecs.iterator();
		final int _iEnd = methodSpecs.size();
		boolean _result = false;

		for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
			final MethodSpecification _ms = (MethodSpecification) _i.next();
			_result |= _ms.isInScope(method, system);
		}
		return _result;
	}

	/**
	 * Checks if the given field is in the scope in the given system.
	 *
	 * @param field to be checked.
	 * @param system in which to check.
	 *
	 * @return <code>true</code> if the given field is in the scope in the given system; <code>false</code>, otherwise.
	 *
	 * @pre field != null and system != null
	 */
	public boolean isInScope(final SootField field, final IEnvironment system) {
		final Iterator _i = fieldSpecs.iterator();
		final int _iEnd = fieldSpecs.size();
		boolean _result = false;

		for (int _iIndex = 0; _iIndex < _iEnd && !_result; _iIndex++) {
			final FieldSpecification _fs = (FieldSpecification) _i.next();
			_result |= _fs.isInScope(field, system);
		}
		return _result;
	}

	/**
	 * Sets the value of <code>classSpecs</code>.
	 *
	 * @param theClassSpecs the new value of <code>classSpecs</code>.
	 */
	void setClassSpecs(final Collection theClassSpecs) {
		this.classSpecs = theClassSpecs;
	}

	/**
	 * Retrieves the value in <code>classSpecs</code>.
	 *
	 * @return the value in <code>classSpecs</code>.
	 */
	Collection getClassSpecs() {
		return classSpecs;
	}

	/**
	 * Sets the value of <code>fieldSpecs</code>.
	 *
	 * @param theFieldSpecs the new value of <code>fieldSpecs</code>.
	 */
	void setFieldSpecs(final Collection theFieldSpecs) {
		this.fieldSpecs = theFieldSpecs;
	}

	/**
	 * Retrieves the value in <code>fieldSpecs</code>.
	 *
	 * @return the value in <code>fieldSpecs</code>.
	 */
	Collection getFieldSpecs() {
		return fieldSpecs;
	}

	/**
	 * Sets the value of <code>methodSpecs</code>.
	 *
	 * @param theMethodSpecs the new value of <code>methodSpecs</code>.
	 */
	void setMethodSpecs(final Collection theMethodSpecs) {
		this.methodSpecs = theMethodSpecs;
	}

	/**
	 * Retrieves the value in <code>methodSpecs</code>.
	 *
	 * @return the value in <code>methodSpecs</code>.
	 */
	Collection getMethodSpecs() {
		return methodSpecs;
	}

	/**
	 * Creates the container for specifications. This is used by java-xml binding.
	 *
	 * @return a container.
	 *
	 * @post result != null
	 */
	static Collection createSpecContainer() {
		return new ArrayList();
	}

	/**
	 * Resets internal data structures.
	 */
	void reset() {
		classSpecs.clear();
		methodSpecs.clear();
		fieldSpecs.clear();
	}
}

// End of File
