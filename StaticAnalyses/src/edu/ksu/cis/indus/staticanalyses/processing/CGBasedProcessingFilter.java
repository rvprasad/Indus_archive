
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

package edu.ksu.cis.indus.staticanalyses.processing;

import edu.ksu.cis.indus.processing.AbstractProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;

import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;


/**
 * Call-Graph-based processing filter.  This filters out unreachable methods.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class CGBasedProcessingFilter
  extends AbstractProcessingFilter {
	/**
	 * Provides the call graph information to drive the processing.
	 */
	private ICallGraphInfo cgi;

	/**
	 * Creates a new CGBasedProcessingController object.
	 *
	 * @param cgiPrm provides the call graph information to drive the processing.
	 *
	 * @pre cgiPrm != null
	 */
	public CGBasedProcessingFilter(final ICallGraphInfo cgiPrm) {
		cgi = cgiPrm;
	}

	/**
	 * Filters out methods that unreachable in the call graph provided at construction.
	 *
	 * @see ValueAnalyzerBasedProcessingController#filterMethods(Collection)
	 */
	public Collection filterMethods(final Collection methods) {
		return CollectionUtils.intersection(methods, cgi.getReachableMethods());
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/30 00:10:24  venku
   - Major refactoring:
     ProcessingController is more based on the sort it controls.
     The filtering of class is another concern with it's own
     branch in the inheritance tree.  So, the user can tune the
     controller with a filter independent of the sort of processors.
   Revision 1.8  2003/11/12 03:52:46  venku
   - it just inherits from ProcessingController.  This is incorrect.
   Revision 1.6  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.5  2003/10/21 08:41:04  venku
   - Changed the methods/classes get filtered.
   Revision 1.4  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.2  2003/08/11 06:38:25  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in IWorkBag.
 */
