
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
 * <p>
 * The constructor accepts a specification with the following structure.
 * </p>
 * 
 * <ul>
 * <li>
 * <b>CLASS SPECIFICATION:</b>
 * </li>
 * </ul>
 * 
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SpecificationBasedScopeDefinition {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private Collection classSpecs = new ArrayList();

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private Collection fieldSpecs = new ArrayList();

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private Collection methodSpecs = new ArrayList();

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
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Collection createClassLevelSpecContainer() {
		return classSpecs;
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Collection createFieldLevelSpecContainer() {
		return fieldSpecs;
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	Collection createMethodLevelSpecContainer() {
		return methodSpecs;
	}
}

// End of File
