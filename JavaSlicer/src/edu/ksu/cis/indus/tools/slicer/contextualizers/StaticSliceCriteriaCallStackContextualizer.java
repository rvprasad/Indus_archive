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

package edu.ksu.cis.indus.tools.slicer.contextualizers;

import edu.ksu.cis.indus.common.collections.Stack;
import edu.ksu.cis.indus.processing.Context;

import static edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import java.util.ArrayList;
import java.util.Collection;

import soot.SootMethod;

/**
 * This implementation can be used to combine the given set of contexts with the criteria. This implementation will always
 * provide the same set of given contexts. These contexts will be same for both <code>getCallingContextsForProgramPoint</code>
 * and <code>getCallingContextsForThis</code>.
 * <p>
 * This implementation is intended to be used with <code>StaticSliceCriteriaGenerator</code>.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class StaticSliceCriteriaCallStackContextualizer
		extends AbstractSliceCriteriaCallStackContextualizer {

	/**
	 * The calling contexts to be returned.
	 */
	private final Collection<Stack<CallTriple>> contexts;

	/**
	 * Creates a new StaticSliceCriteriaCallStackContextualizer object.
	 * 
	 * @param callingContexts to be used. The call triples in the calling contexts correspond to the caller side, i.e, they
	 *            contain caller and call-site information.
	 * @pre callingContexts != null
	 */
	public StaticSliceCriteriaCallStackContextualizer(final Collection<Stack<CallTriple>> callingContexts) {
		contexts = new ArrayList<Stack<CallTriple>>(callingContexts);
	}

	/**
	 * @see AbstractSliceCriteriaCallStackContextualizer#getCallingContextsForProgramPoint(Context)
	 */
	@Override protected Collection<Stack<CallTriple>> getCallingContextsForProgramPoint(
			@SuppressWarnings("unused") final Context programPoint) {
		return contexts;
	}

	/**
	 * @see AbstractSliceCriteriaCallStackContextualizer#getCallingContextsForThis(SootMethod)
	 */
	@Override protected Collection<Stack<CallTriple>> getCallingContextsForThis(
			@SuppressWarnings("unused") final SootMethod method) {
		return contexts;
	}
}

// End of File
