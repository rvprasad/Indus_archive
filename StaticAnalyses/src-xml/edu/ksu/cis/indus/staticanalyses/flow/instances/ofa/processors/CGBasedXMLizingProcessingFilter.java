
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;

import edu.ksu.cis.indus.xmlizer.XMLizingProcessingFilter;


/**
 * This is a call-graph based xmlizing controller.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * 
 * @deprecated
 */
public final class CGBasedXMLizingProcessingFilter
  extends CGBasedProcessingFilter {
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
		chain(new XMLizingProcessingFilter());
	}
}

// End of File
