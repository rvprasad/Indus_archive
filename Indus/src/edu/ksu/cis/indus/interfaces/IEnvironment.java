
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

package edu.ksu.cis.indus.interfaces;

import java.util.Collection;

import soot.SootClass;


/**
 * This interface exposes the information pertaining to the environment in which the analyses function. It provides the
 * non-functional information about the system.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IEnvironment {
	/** 
	 * The id of this interface.
	 */
	Object ID = "The Environment";

	/**
	 * Returns the Jimple representation of the given class.
	 *
	 * @param className is the name of the class whose Jimple representation is to be returned.
	 *
	 * @return the requested class.
	 *
	 * @post result.oclType = SootClass
	 */
	SootClass getClass(final String className);

	/**
	 * Returns the classes that form the system.
	 *
	 * @return the classes the form the system.
	 *
	 * @post result->forall(o | o.oclType = SoptClass)
	 */
	Collection getClasses();

	/**
	 * Retrieves the methods that serve as the entry points or as "roots" of the system being analyzed.
	 *
	 * @return a collection of methods that are the "roots".
	 *
	 * @post result != null and result.oclIsKindOf(Collection(soot.SootMethod))
	 */
	Collection getRoots();

	/**
	 * Removes the given class from the environment.
	 *
	 * @param clazz to be removed from the system.
	 *
	 * @pre clazz != null
	 */
	void removeClass(SootClass clazz);
}

// End of File
