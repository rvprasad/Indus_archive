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

package edu.ksu.cis.indus.interfaces;

import java.util.Collection;

import soot.SootClass;
import soot.SootMethod;

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
	Comparable<String> ID = "The Environment";

	/**
	 * Returns the Jimple representation of the given class.
	 *
	 * @param className is the name of the class whose Jimple representation is to be returned.
	 * @return the requested class.
	 * @post result.oclType = SootClass
	 */
	SootClass getClass(final String className);

	/**
	 * Returns the classes that form the system.
	 *
	 * @return the classes the form the system.
	 */
	Collection<SootClass> getClasses();

	/**
	 * Retrieves the methods that serve as the entry points or as "roots" of the system being analyzed.
	 *
	 * @return a collection of methods that are the "roots".
	 * @post result != null
	 */
	Collection<SootMethod> getRoots();

	/**
	 * Checks if a class by the givne name exists in the environment.
	 *
	 * @param scName if the FQN of the class of interest.
	 * @return <code>true</code> if it exists; <code>false</code>, otherwise.
	 */
	boolean hasClass(String scName);

	/**
	 * Removes the given class from the environment.
	 *
	 * @param clazz to be removed from the system.
	 * @pre clazz != null
	 */
	void removeClass(SootClass clazz);
}

// End of File
