
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

package edu.ksu.cis.indus.staticanalyses.dependency.xmlizer;

import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingController;
import edu.ksu.cis.indus.xmlizer.XMLizingController;

import java.util.Collection;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CGBasedXMLizingController
  extends CGBasedProcessingController {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private XMLizingController xmlizingController;

	/**
	 * DOCUMENT ME!
	 *
	 * @param cgiPrm DOCUMENT ME!
	 */
	public CGBasedXMLizingController(final ICallGraphInfo cgiPrm) {
		super(cgiPrm);
		xmlizingController = new XMLizingController();
	}

	/**
	 * {@inheritDoc} This implementation uses an XMLizingController to filter the classes after the classes  have been
	 * filtered based on call graph.
	 */
	protected Collection filterClasses(final Collection classes) {
		return xmlizingController.filterClasses(super.filterClasses(classes));
	}

	/**
	 * This implementation uses an XMLizingController to filter the methods after the methods  have been filtered based on
	 * call graph.
	 *
	 * @see edu.ksu.cis.indus.processing.ProcessingController#filterMethods(java.util.Collection)
	 */
	protected Collection filterMethods(final Collection methods) {
		return xmlizingController.filterClasses(super.filterMethods(methods));
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/12 05:05:45  venku
   - Renamed SootDependentTest to SootBasedDriver.
   - Switched the contents of DependencyXMLizer and DependencyTest.
   - Corrected errors which emitting xml tags.
   - added a scrapbook.

 */
