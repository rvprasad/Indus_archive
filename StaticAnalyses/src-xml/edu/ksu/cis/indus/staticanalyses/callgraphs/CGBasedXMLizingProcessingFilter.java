
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

package edu.ksu.cis.indus.staticanalyses.callgraphs;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;

import edu.ksu.cis.indus.xmlizer.XMLizingProcessingFilter;


/**
 * This is a call-graph based xmlizing controller.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
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
