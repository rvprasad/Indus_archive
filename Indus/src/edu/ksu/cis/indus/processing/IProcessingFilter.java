
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
 * This is the interface via which class and method filtering will occur in <code>ProcessingController</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IProcessingFilter {
	/**
	 * Filter out classes from the given collection of classes.
	 *
	 * @param classes is the collection to be filtered.
	 *
	 * @return a collection of classes that were not filtered.
	 *
	 * @pre classes.oclIsKindOf(Collection(soot.SootClass)
	 * @post result.oclIsKinfOf(Collection(soot.SootClass))
	 * @post result->forall(o | classes.contains(o))
	 */
	Collection filterClasses(Collection classes);

	/**
	 * Filter out fields from the given collection of methods.
	 *
	 * @param fields is the collection to be filtered.
	 *
	 * @return a collection of fields that were not filtered.
	 *
	 * @pre methods.oclIsKindOf(Collection(soot.SootField)
	 * @post result.oclIsKinfOf(Collection(soot.SootField))
	 * @post result->forall(o | fields.contains(o))
	 */
	Collection filterFields(Collection fields);

	/**
	 * Filter out methods from the given collection of methods.
	 *
	 * @param methods is the collection to be filtered.
	 *
	 * @return a collection of methods that were not filtered.
	 *
	 * @pre methods.oclIsKindOf(Collection(soot.SootMethod)
	 * @post result.oclIsKinfOf(Collection(soot.SootMethod))
	 * @post result->forall(o | methods.contains(o))
	 */
	Collection filterMethods(Collection methods);
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2003/12/14 16:43:45  venku
   - extended ProcessingController to filter fields as well.
   - ripple effect.
   Revision 1.4  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.3  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.2  2003/11/30 00:20:41  venku
   - documentation.
   Revision 1.1  2003/11/30 00:10:17  venku
   - Major refactoring:
     ProcessingController is more based on the sort it controls.
     The filtering of class is another concern with it's own
     branch in the inheritance tree.  So, the user can tune the
     controller with a filter independent of the sort of processors.
 */
