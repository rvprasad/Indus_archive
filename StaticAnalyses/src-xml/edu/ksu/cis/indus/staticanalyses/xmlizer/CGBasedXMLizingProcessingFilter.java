
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

package edu.ksu.cis.indus.staticanalyses.xmlizer;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;

import edu.ksu.cis.indus.xmlizer.XMLizingProcessingFilter;

import java.util.Collection;


/**
 * This is a call-graph based xmlizing controller.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CGBasedXMLizingProcessingFilter
  extends CGBasedProcessingFilter {
	/**
	 * The instance of xmlizing controller that is wrapped.
	 *
	 * @invariant xmlizingController != null
	 */
	private XMLizingProcessingFilter xmlizingFilter;

	/**
	 * Creates an instance of this class.
	 *
	 * @param cgiPrm provides the call graph information required during controlling.
	 *
	 * @pre cgiPrm != null
	 * @post xmlizingController != null
	 */
	public CGBasedXMLizingProcessingFilter(final ICallGraphInfo cgiPrm) {
		super(cgiPrm);
		xmlizingFilter = new XMLizingProcessingFilter();
	}

	/**
	 * {@inheritDoc} This implementation uses an XMLizingProcessingFilter to filter the classes after the classes  have been
	 * filtered based on call graph.
	 */
	public Collection filterClasses(final Collection classes) {
		return xmlizingFilter.filterClasses(super.filterClasses(classes));
	}

	/**
	 * This implementation uses an XMLizingProcessingFilter to filter the methods after the methods  have been filtered based
	 * on call graph.
	 *
	 * @see edu.ksu.cis.indus.processing.ProcessingController#filterMethods(java.util.Collection)
	 */
	public Collection filterMethods(final Collection methods) {
		return xmlizingFilter.filterMethods(super.filterMethods(methods));
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/12/02 09:42:39  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.2  2003/11/30 09:03:23  venku
   - changed filed name to something more appropriate.
   Revision 1.1  2003/11/30 01:17:15  venku
   - renamed CGBasedXMLizingFilter to CGBasedXMLizingProcessingFilter.
   - renamed XMLizingController to XMLizingProcessingFilter.
   - ripple effect.
   Revision 1.1  2003/11/30 00:10:24  venku
   - Major refactoring:
     ProcessingController is more based on the sort it controls.
     The filtering of class is another concern with it's own
     branch in the inheritance tree.  So, the user can tune the
     controller with a filter independent of the sort of processors.
   Revision 1.3  2003/11/17 16:58:15  venku
   - populateDAs() needs to be called from outside the constructor.
   - filterClasses() was called in CGBasedXMLizingController instead of filterMethods. FIXED.
   Revision 1.2  2003/11/17 02:23:56  venku
   - documentation.
   - xmlizers require streams/writers to be provided to them
     rather than they constructing them.
   Revision 1.1  2003/11/12 05:18:54  venku
   - moved xmlizing classes to a different class.
   Revision 1.1  2003/11/12 05:05:45  venku
   - Renamed SootDependentTest to SootBasedDriver.
   - Switched the contents of DependencyXMLizer and DependencyTest.
   - Corrected errors which emitting xml tags.
   - added a scrapbook.
 */
