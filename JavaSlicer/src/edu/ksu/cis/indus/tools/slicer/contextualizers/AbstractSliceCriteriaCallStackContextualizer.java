
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
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.slicer.ISliceCriterion;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;

import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import soot.SootMethod;


/**
 * This is an abstract implementation of slice criteria contextualizer.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractSliceCriteriaCallStackContextualizer
  implements ISliceCriteriaContextualizer {
	/** 
	 * This is the slicer tool provides the context in which filtering occurs.
	 */
	private SlicerTool<?> slicerTool;

	/**
	 * Sets the value of <code>slicerTool</code>.
	 *
	 * @param slicer the new value of <code>slicerTool</code>.
	 */
	public void setSlicerTool(final SlicerTool<?> slicer) {
		slicerTool = slicer;
	}

	/**
	 * @see ISliceCriteriaContextualizer#processCriteriaBasedOnProgramPoint(Context, Collection)
	 */
	public final void processCriteriaBasedOnProgramPoint(final Context programPoint, final Collection<ISliceCriterion> baseCriteria) {
		final Collection<Stack<CallTriple>> _contexts = getCallingContextsForProgramPoint(programPoint);
		contextualize(baseCriteria, _contexts);
	}

	/**
	 * @see ISliceCriteriaContextualizer#processCriteriaBasedOnThis(SootMethod, Collection)
	 */
	public final void processCriteriaBasedOnThis(final SootMethod method, final Collection<ISliceCriterion> baseCriteria) {
		final Collection<Stack<CallTriple>> _contexts = getCallingContextsForThis(method);
		contextualize(baseCriteria, _contexts);
	}

	/**
	 * Retrieves the value in <code>slicerTool</code>.
	 *
	 * @return the value in <code>slicerTool</code>.
	 */
	protected final SlicerTool<?> getSlicerTool() {
		return slicerTool;
	}

	/**
	 * Retrieves the calling contexts based on given program point.
	 *
	 * @param programPoint obviously.
	 *
	 * @return a collection of calling contexts.  The call triples in the calling contexts correspond to the caller side,
	 * 		   i.e, they contain caller and call-site information.
	 *
	 * @pre programPoint != null
	 * @post result != null
	 */
	protected abstract Collection<Stack<CallTriple>> getCallingContextsForProgramPoint(final Context programPoint);

	/**
	 * Retrieves the calling contexts based on "this" variable of the given method.
	 *
	 * @param method in which "this" occurs.
	 *
	 * @return a collection of calling contexts. The call triples in the calling contexts correspond to the caller side, i.e,
	 * 		   they contain caller and call-site information.
	 *
	 * @pre method != null
	 * @post result != null
	 */
	protected abstract Collection<Stack<CallTriple>> getCallingContextsForThis(final SootMethod method);

	/**
	 * Injects the given criterion with the given call stack and adds it to result.
	 *
	 * @param criterion to be modified.
	 * @param stack to be injected.
	 * @param result into which <code>criterion</code> needs to be added.
	 *
	 * @pre criterion != null and result != null
	 * @post result.containsAll(result$pre)
	 * @post stack.equals(stack$pre)
	 */
	private void addCriteriaWithGivenCallStackToResult(final ISliceCriterion criterion, final Stack<CallTriple> stack,
		final Collection<ISliceCriterion> result) {
		criterion.setCallStack(stack);
		result.add(criterion);
	}

	/**
	 * Contextualizes the given criteria with given contexts.
	 *
	 * @param baseCriteria is the collection of criteria to be contextualized.
	 * @param contexts to be injected into the criteria.  <code>null</code> context indicates an open context.
	 *
	 * @pre contexts->forall(o | not o.isEmpty())
	 * @post baseCriteria->forall(o | not baseCriteria$pre.contains(o))
	 * @post baseCriteria$pre->forall(o | contexts->exists(p | p.peek().equals(o.getOccurringMethod()) and
	 * 		 baseCriteria.contains(o.setCallStack(p))) or baseCriteria->forall(t | not t.setCallStack(null).equals(o)))
	 */
	private void contextualize(final Collection<ISliceCriterion> baseCriteria, final Collection<Stack<CallTriple>> contexts) {
		final Collection<ISliceCriterion> _result = new HashSet<ISliceCriterion>();
		final SliceCriteriaFactory _criteriaFactory = SliceCriteriaFactory.getFactory();
		final Iterator<ISliceCriterion> _j = baseCriteria.iterator();
		final int _jEnd = baseCriteria.size();
		final ICallGraphInfo _cgi = getSlicerTool().getCallGraph();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final ISliceCriterion _criterion = _j.next();
			final Iterator<Stack<CallTriple>> _i = contexts.iterator();
			final int _iEnd = contexts.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Stack<CallTriple> _callStack = _i.next();
				final ISliceCriterion _temp = _criteriaFactory.clone(_criterion);

				if (_callStack == null) {
					addCriteriaWithGivenCallStackToResult(_temp, null, _result);
				} else if (!_callStack.isEmpty() && _cgi.getCallers(_temp.getOccurringMethod()).contains(_callStack.peek())) {
                    addCriteriaWithGivenCallStackToResult(_temp, _callStack.clone(), _result);
                }
			}
			_j.remove();
		}
		baseCriteria.addAll(_result);
	}
}

// End of File
