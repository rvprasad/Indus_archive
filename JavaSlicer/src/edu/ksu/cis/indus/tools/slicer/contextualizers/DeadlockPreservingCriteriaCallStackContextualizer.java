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
