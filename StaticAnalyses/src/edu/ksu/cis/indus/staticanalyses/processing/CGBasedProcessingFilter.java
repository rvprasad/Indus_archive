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
