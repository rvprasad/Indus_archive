
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.processing;

import java.util.Collection;


/**
 * This is an abstract implementation of <code>IProcessingFilter</code> via which class and method filtering will occur in
 * <code>ProcessingController</code>. Default implementation returns the given methods/classes as is.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractProcessingFilter
  implements IProcessingFilter {
	/**
	 * Filter out classes from the given collection of classes. Default implementation returns the given methods as is.
	 *
	 * @param classes is the collection to be filtered.
	 *
	 * @return a collection of classes that were not filtered.
	 *
	 * @pre classes.oclIsKindOf(Collection(soot.SootClass)
	 * @post result.oclIsKinfOf(Collection(soot.SootClass))
	 * @post result->forall(o | classes.contains(o))
	 */
	public Collection filterClasses(final Collection classes) {
		return classes;
	}

	/**
	 * Filter out methods from the given collection of methods.  Default implementation returns the given methods as is.
	 *
	 * @param methods is the collection to be filtered.
	 *
	 * @return a collection of methods that were not filtered.
	 *
	 * @pre methods.oclIsKindOf(Collection(soot.SootMethod)
	 * @post result.oclIsKinfOf(Collection(soot.SootMethod))
	 * @post result->forall(o | methods.contains(o))
	 */
	public Collection filterMethods(final Collection methods) {
		return methods;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/12/02 01:30:59  venku
   - coding conventions and formatting.
   Revision 1.2  2003/11/30 00:20:41  venku
   - documentation.
   Revision 1.1  2003/11/30 00:10:17  venku
   - Major refactoring:
     ProcessingController is more based on the sort it controls.
     The filtering of class is another concern with it's own
     branch in the inheritance tree.  So, the user can tune the
     controller with a filter independent of the sort of processors.
 */
