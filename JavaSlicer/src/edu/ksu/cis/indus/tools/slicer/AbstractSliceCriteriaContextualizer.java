
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.slicer.ISliceCriterion;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import soot.SootMethod;


/**
 * This is an abstract implementation of slice criteria contextualizer.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractSliceCriteriaContextualizer
  implements ISliceCriteriaContextualizer {
	/** 
	 * This is the slicer tool provides the context in which filtering occurs.
	 */
	private SlicerTool slicerTool;

	/**
	 * Sets the value of <code>slicerTool</code>.
	 *
	 * @param slicer the new value of <code>slicerTool</code>.
	 */
	public void setSlicerTool(final SlicerTool slicer) {
		slicerTool = slicer;
	}

	/**
	 * @see ISliceCriteriaContextualizer#processCriteriaBasedOnProgramPoint(Context, Collection)
	 */
	public final void processCriteriaBasedOnProgramPoint(final Context context, final Collection baseCriteria) {
		final Collection _contexts = getCallingContextsForProgramPoint(context);
		contextualize(baseCriteria, _contexts);
	}

	/**
	 * @see ISliceCriteriaContextualizer#processCriteriaBasedOnThis(SootMethod, Collection)
	 */
	public final void processCriteriaBasedOnThis(final SootMethod method, final Collection baseCriteria) {
		final Collection _contexts = getCallingContextsForThis(method);
		contextualize(baseCriteria, _contexts);
	}

	/**
	 * Retrieves the value in <code>slicerTool</code>.
	 *
	 * @return the value in <code>slicerTool</code>.
	 */
	protected final SlicerTool getSlicerTool() {
		return slicerTool;
	}

	/**
	 * Retrieves the calling contexts based on given program point.
	 *
	 * @param context is the program point.
	 *
	 * @return a collection of calling contexts.
	 *
	 * @pre context != null
	 * @post result != null and result.oclIsKindOf(Collection(Stack(CallTriple))
	 */
	protected abstract Collection getCallingContextsForProgramPoint(final Context context);

	/**
	 * Retrieves the calling contexts based on "this" variable of the given method.
	 *
	 * @param method in which "this" occurs.
	 *
	 * @return a collection of calling contexts.
	 *
	 * @pre method != null
	 * @post result != null and result.oclIsKindOf(Collection(Stack(CallTriple))
	 */
	protected abstract Collection getCallingContextsForThis(final SootMethod method);

	/**
	 * Contextualizes the given criteria with given contexts.
	 *
	 * @param baseCriteria is the collection of criteria to be contextualized.
	 * @param contexts to be injected into the criteria.
	 *
	 * @pre
	 */
	private void contextualize(final Collection baseCriteria, final Collection contexts) {
		final Collection _result = new HashSet();
		final SliceCriteriaFactory _criteriaFactory = SliceCriteriaFactory.getFactory();
		final Iterator _j = baseCriteria.iterator();
		final int _jEnd = baseCriteria.size();

		for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
			final ISliceCriterion _criterion = (ISliceCriterion) _j.next();
			final Iterator _i = contexts.iterator();
			final int _iEnd = contexts.size();

			for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
				final Stack _callStack = (Stack) _i.next();
				final ISliceCriterion _temp = _criteriaFactory.clone(_criterion);

				if (_callStack != null) {
					_temp.setCallStack((Stack) _callStack.clone());
				} else {
					_temp.setCallStack(null);
				}
				_result.add(_temp);
			}
			_criterion.returnToPool();
			_j.remove();
		}
		baseCriteria.addAll(_result);
	}
}

// End of File
