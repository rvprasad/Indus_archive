
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

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;


/**
 * This class can be used to match classes, methods, and fields based on names and hierarchical relation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SpecificationBasedScopeDefinition {
	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Collection classSpecs;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Collection fieldSpecs;

	/** 
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Collection methodSpecs;

	/**
	 * DOCUMENT ME!
	 *
	 * @param clazz
	 * @param system
	 *
	 * @return
	 */
	public boolean isInScope(final SootClass clazz, final IEnvironment system) {
		// TODO: Auto-generated method stub
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param method
	 * @param system
	 *
	 * @return
	 */
	public boolean isInScope(final SootMethod method, final IEnvironment system) {
		// TODO: Auto-generated method stub
		return false;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param field
	 * @param system
	 *
	 * @return
	 */
	public boolean isInScope(final SootField field, final IEnvironment system) {
		// TODO: Auto-generated method stub
		return false;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public void reset() {
	}

	/**
	 * Sets the value of <code>classSpecs</code>.
	 *
	 * @param classSpecs the new value of <code>classSpecs</code>.
	 */
	void setClassSpecs(Collection classSpecs) {
		this.classSpecs = classSpecs;
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
	 * @param fieldSpecs the new value of <code>fieldSpecs</code>.
	 */
	void setFieldSpecs(Collection fieldSpecs) {
		this.fieldSpecs = fieldSpecs;
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
	 * @param methodSpecs the new value of <code>methodSpecs</code>.
	 */
	void setMethodSpecs(Collection methodSpecs) {
		this.methodSpecs = methodSpecs;
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
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	static Collection createClassSpecContainer() {
		return new ArrayList();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	static Collection createFieldSpecContainer() {
		return new ArrayList();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	static Collection createMethodSpecContainer() {
		return new ArrayList();
	}
}

// End of File
