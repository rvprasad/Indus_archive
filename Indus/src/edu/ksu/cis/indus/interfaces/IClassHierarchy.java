
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
 * This is the interface to class hierarchy information.
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IClassHierarchy {
	/**
	 * Retrieves the classes in the hierarchy.
	 *
	 * @return the classes in the hierarchy.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post result->forall(o | not o.isInterface())
	 */
	Collection getClasses();

	/**
	 * Retrieves the interfaces in the hierarchy.
	 *
	 * @return the interfaces in the hierarchy.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post result->forall(o | o.isInterface())
	 */
	Collection getInterfaces();

	/**
	 * Retrieves the classes that are the proper ancestors of the given class.
	 *
	 * @param clazz of interest.
	 *
	 * @return the proper ancestor classes.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post result->forall(o | not o.isInterface())
	 */
	Collection properAncestorClassesOf(final SootClass clazz);

	/**
	 * Retrieves the interfaces that are the proper ancestors of the given class.
	 *
	 * @param clazz of interest.
	 *
	 * @return the proper ancestor classes.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post result->forall(o | o.isInterface())
	 */
	Collection properAncestorInterfacesOf(final SootClass clazz);

	/**
	 * Retrieves the immediate subclasses of the given class.
	 *
	 * @param clazz of interest.
	 *
	 * @return the immediate subclasses.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post clazz.isInterface() implies result->forall(o | o.getInterfaces().contains(clazz))
	 * @post (not clazz.isInterface()) implies result->forall(o | o.getSuperClass().equals(clazz))
	 */
	Collection properImmediateSubClassesOf(final SootClass clazz);

	/**
	 * Retrieves the subclasses of the given class.
	 *
	 * @param clazz of interest.
	 *
	 * @return the subclasses.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post clazz.isInterface() implies result->forall(o | properAncestorsClasses(o).contains(clazz))
	 * @post (not clazz.isInterface()) implies result->forall(o | properAncestorsInterfaces(o).contains(clazz))
	 */
	Collection properSubclassesOf(final SootClass clazz);
}

// End of File
