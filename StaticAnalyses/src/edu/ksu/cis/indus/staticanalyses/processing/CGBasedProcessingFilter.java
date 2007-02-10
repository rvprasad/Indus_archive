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

package edu.ksu.cis.indus.staticanalyses.processing;

import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.processing.AbstractProcessingFilter;

import java.util.Collection;

import soot.SootMethod;

/**
 * Call-Graph-based processing filter. This filters out unreachable methods.
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
	 * Creates a new CGBasedProcessingFilter object.
	 * 
	 * @param cgiPrm provides the call graph information to drive the processing.
	 * @pre cgiPrm != null
	 */
	public CGBasedProcessingFilter(final ICallGraphInfo cgiPrm) {
		cgi = cgiPrm;
	}

	/**
	 * Filters out methods that unreachable in the call graph provided at construction.
	 * 
	 * @see AbstractProcessingFilter#localFilterMethods(Collection)
	 */
	@Override protected final Collection<SootMethod> localFilterMethods(final Collection<SootMethod> methods) {
		return SetUtils.intersection(methods, cgi.getReachableMethods());
	}
}

// End of File
