
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
	 * The successor filter, if any.
	 */
	private IProcessingFilter successor;

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessingFilter#chain(edu.ksu.cis.indus.processing.IProcessingFilter)
	 */
	public final void chain(final IProcessingFilter filter) {
		successor = filter;
	}

	/**
	 * {@inheritDoc}  Default implementation returns the given classes as is.
	 */
	public final Collection filterClasses(final Collection classes) {
		Collection _result = localFilterClasses(classes);

		if (successor != null) {
			_result = successor.filterClasses(_result);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}  Default implementation returns the given fields as is.
	 */
	public final Collection filterFields(final Collection fields) {
		Collection _result = localFilterFields(fields);

		if (successor != null) {
			_result = successor.filterFields(_result);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}  Default implementation returns the given methods as is.
	 */
	public final Collection filterMethods(final Collection methods) {
		Collection _result = localFilterMethods(methods);

		if (successor != null) {
			_result = successor.filterMethods(_result);
		}
		return _result;
	}

	/**
	 * Filter the given classes.
	 *
	 * @param classes to be filtered.
	 *
	 * @return the collection of filterate classes.
	 *
	 * @pre classes != null and classes.oclIsKindOf(Collection(SootClass))
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post classes.containsAll(result)
	 */
	protected Collection localFilterClasses(final Collection classes) {
		return classes;
	}

	/**
	 * Filter the given fields.
	 *
	 * @param fields to be filtered.
	 *
	 * @return the collection of filterate fields.
	 *
	 * @pre fields != null and fields.oclIsKindOf(Collection(SootClass))
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post fields.containsAll(result)
	 */
	protected Collection localFilterFields(final Collection fields) {
		return fields;
	}

	/**
	 * Filter the given methods.
	 *
	 * @param methods to be filtered.
	 *
	 * @return the collection of filterate methods.
	 *
	 * @pre methods != null and methods.oclIsKindOf(Collection(SootClass))
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post methods.containsAll(result)
	 */
	protected Collection localFilterMethods(final Collection methods) {
		return methods;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2004/02/14 23:16:49  venku
   - coding convention.

   Revision 1.8  2004/02/11 09:37:21  venku
   - large refactoring of code based  on testing :-)
   - processing filters can now be chained.
   - ofa xmlizer was implemented.
   - xml-based ofa tester was implemented.

   Revision 1.7  2003/12/14 16:44:11  venku
   - coding convention.
   Revision 1.6  2003/12/14 16:43:44  venku
   - extended ProcessingController to filter fields as well.
   - ripple effect.
   Revision 1.5  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.4  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
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
