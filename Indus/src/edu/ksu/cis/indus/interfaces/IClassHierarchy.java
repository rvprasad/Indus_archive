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
import java.util.List;

import soot.SootClass;

/**
 * This is the interface to class hierarchy information.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IClassHierarchy
		extends IIdentification {

	/**
	 * This is the unique identifier that can be used to identify an instance of this class.
	 */
	Comparable<String> ID = "Class Hierarchy Analysis";

	/**
	 * Retrieves the classes in the hierarchy.
	 *
	 * @return the classes in the hierarchy.
	 * @post result != null
	 * @post result->forall(o | not o.isInterface())
	 */
	Collection<SootClass> getClasses();

	/**
	 * Retrieves the classes in this hierarchy in topological order.
	 *
	 * @param topDown indicates if the sorting needs to be done top down(<code>true</code>)or bottom up(<code>false</code>).
	 * @return the classes in topological order
	 * @post result != null
	 * @post result->forall(o | getClasses().contains(o) or getInterfaces().contains(o))
	 */
	List<SootClass> getClassesInTopologicalOrder(boolean topDown);

	/**
	 * Retrieves the interfaces in the hierarchy.
	 *
	 * @return the interfaces in the hierarchy.
	 * @post result != null
	 * @post result->forall(o | o.isInterface())
	 */
	Collection<SootClass> getInterfaces();

	/**
	 * Retrieves the classes that are the proper ancestors of the given class.
	 *
	 * @param clazz of interest.
	 * @return the proper ancestor classes.
	 * @post result != null
	 * @post result->forall(o | not o.isInterface())
	 */
	Collection<SootClass> getProperAncestorClassesOf(final SootClass clazz);

	/**
	 * Retrieves the interfaces that are the proper ancestors of the given class.
	 *
	 * @param clazz of interest.
	 * @return the proper ancestor classes.
	 * @post result != null
	 * @post result->forall(o | o.isInterface())
	 */
	Collection<SootClass> getProperAncestorInterfacesOf(final SootClass clazz);

	/**
	 * Retrieves the immediate subclasses of the given class.
	 *
	 * @param clazz of interest.
	 * @return the immediate subclasses.
	 * @post result != null
	 * @post clazz.isInterface() implies result->forall(o | o.getInterfaces().contains(clazz))
	 * @post (not clazz.isInterface()) implies result->forall(o | o.getSuperClass().equals(clazz))
	 */
	Collection<SootClass> getProperImmediateSubClassesOf(final SootClass clazz);

	/**
	 * Retrieves the proper parent class of the given class.
	 *
	 * @param clazz of interest.
	 * @return the proper ancestor class.
	 * @post not result.isInterface()
	 */
	SootClass getProperParentClassOf(final SootClass clazz);

	/**
	 * Retrieves the interfaces that are the proper parents of the given class.
	 *
	 * @param clazz of interest.
	 * @return the proper parent interfaces.
	 * @post result != null
	 * @post result->forall(o | o.isInterface())
	 */
	Collection<SootClass> getProperParentInterfacesOf(final SootClass clazz);

	/**
	 * Retrieves the subclasses of the given class.
	 *
	 * @param clazz of interest.
	 * @return the subclasses.
	 * @post result != null
	 * @post clazz.isInterface() implies result->forall(o | properAncestorsClasses(o).contains(clazz))
	 * @post (not clazz.isInterface()) implies result->forall(o | properAncestorsInterfaces(o).contains(clazz))
	 */
	Collection<SootClass> getProperSubclassesOf(final SootClass clazz);
}

// End of File
