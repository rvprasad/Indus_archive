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
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.ThreadEscapeInfoBasedCallingContextRetriever;

import java.util.Collection;
import java.util.Collections;

import soot.SootMethod;

/**
 * This class injects contexts into slice criteria based on escape information.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DeadlockPreservingCriteriaCallStackContextualizer
		extends AbstractSliceCriteriaCallStackContextualizer {

	/**
	 * The context retriever to be used during contextualization.
	 */
	private final ThreadEscapeInfoBasedCallingContextRetriever ecr;

	/**
	 * @param retriever to be used.
	 * @pre retriever != null
	 */
	public DeadlockPreservingCriteriaCallStackContextualizer(final ThreadEscapeInfoBasedCallingContextRetriever retriever) {
		ecr = retriever;
	}

	/**
	 * @see AbstractSliceCriteriaCallStackContextualizer#getCallingContextsForProgramPoint(Context)
	 */
	@Override protected Collection<Stack<CallTriple>> getCallingContextsForProgramPoint(final Context context) {
		final EquivalenceClassBasedEscapeAnalysis _ecba = getSlicerTool().getECBA();
		final Collection<Stack<CallTriple>> _result;

		if (_ecba != null) {
			initialize(_ecba);

			_result = ecr.getCallingContextsForProgramPoint(context);
		} else {
			_result = Collections.emptySet();
		}

		return _result;
	}

	/**
	 * @see AbstractSliceCriteriaCallStackContextualizer#getCallingContextsForThis(SootMethod)
	 */
	@Override protected Collection<Stack<CallTriple>> getCallingContextsForThis(final SootMethod method) {
		final EquivalenceClassBasedEscapeAnalysis _ecba = getSlicerTool().getECBA();
		final Collection<Stack<CallTriple>> _result;

		if (_ecba != null) {
			initialize(_ecba);

			final Context _context = new Context();
			_context.setRootMethod(method);
			_result = ecr.getCallingContextsForThis(_context);
		} else {
			_result = Collections.emptySet();
		}
		return _result;
	}

	/**
	 * Initialize the contextualizer.
	 * 
	 * @param ecba the escape analysis that will provide information during context generation.
	 * @pre ecba != null
	 */
	private void initialize(final EquivalenceClassBasedEscapeAnalysis ecba) {
		ecr.setECBA(ecba);
		ecr.setEscapeInfo(ecba.getEscapeInfo());
		ecr.setCallGraph(getSlicerTool().getCallGraph());
	}
}

// End of File
