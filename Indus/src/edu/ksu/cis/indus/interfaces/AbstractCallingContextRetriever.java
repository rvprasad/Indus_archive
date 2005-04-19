
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

package edu.ksu.cis.indus.interfaces;

import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.Triple;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;


/**
 * This is an abstract implementation of <code>ICallingContextRetriever</code>.  A concrete implementation of this class asis
 * will return null (open) contexts.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractCallingContextRetriever
  extends AbstractIDBasedInfoManagement
  implements ICallingContextRetriever {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractCallingContextRetriever.class);

	/** 
	 * The call graph to be used.
	 */
	private ICallGraphInfo callGraph;

	/**
	 * Creates an instance of this class.
	 */
	public AbstractCallingContextRetriever() {
		super();
	}

	/**
	 * Sets the value of <code>callGraph</code>.
	 *
	 * @param cgi the new value of <code>callGraph</code>.
	 */
	public final void setCallGraph(final ICallGraphInfo cgi) {
		callGraph = cgi;
	}

	/**
	 * @see ICallingContextRetriever#getCallingContextsForProgramPoint(Context)
	 */
	public final Collection getCallingContextsForProgramPoint(final Context context) {
		final Collection _result;
		final SootMethod _currentMethod = context.getCurrentMethod();

		if (considerProgramPoint(context)) {
			_result = getCallingContexts(getTokenForProgramPoint(context), _currentMethod);
		} else {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * @see ICallingContextRetriever#getCallingContextsForThis(Context)
	 */
	public final Collection getCallingContextsForThis(final Context methodContext) {
		final Collection _result;

		if (considerThis(methodContext)) {
			_result = getCallingContexts(getTokenForThis(methodContext), methodContext.getCurrentMethod());
		} else {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * Retrieves the callGraph used by this object.
	 *
	 * @return the callGraph.
	 */
	protected final ICallGraphInfo getCallGraph() {
		return callGraph;
	}

	/**
	 * Retrieves caller side token at the given call-site corresponding to the given token at the callee side.  If this
	 * method returns <code>null</code> then that will terminate the extension of current call context along this "path".
	 *
	 * @param token on the calle side.
	 * @param callee of course.
	 * @param callsite at which <code>callee</code> is called.
	 *
	 * @return caller side token.
	 *
	 * @throws UnsupportedOperationException when this implementation is invoked.
	 *
	 * @pre token != null and callee != null and callsite != null
	 */
	protected Object getCallerSideToken(final Object token, final SootMethod callee, final CallTriple callsite) {
		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("getCallerSideToken(token = " + token + ", callee = " + callee + ", callsite = " + callsite + ")");
		}

		throw new UnsupportedOperationException("This method is unsupported.");
	}

	/**
	 * Retrieves the token for the program point specified in the given context.
	 *
	 * @param programPointContext of interest.
	 *
	 * @return token corresponding to the program point.
	 *
	 * @throws UnsupportedOperationException when this implementation is invoked.
	 *
	 * @pre programPointContext != null
	 */
	protected Object getTokenForProgramPoint(final Context programPointContext) {
		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("getTokenForProgramPoint(programPointContext = " + programPointContext + ")", null);
		}

		throw new UnsupportedOperationException("This method is unsupported.");
	}

	/**
	 * Retrieves the token for for the method specified in the given context.
	 *
	 * @param methodContext of interest.
	 *
	 * @return token corresponding to the method.
	 *
	 * @throws UnsupportedOperationException when this implementation is invoked.
	 *
	 * @pre methodContext != null
	 */
	protected Object getTokenForThis(final Context methodContext) {
		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("getTokenForThis(methodContext = " + methodContext + ")", null);
		}

		throw new UnsupportedOperationException("This method is unsupported.");
	}

	/**
	 * Checks if the program point specified in the given context (calling context base) should be considered for call
	 * context generation.
	 *
	 * @param programPointContext of interest.
	 *
	 * @return <code>true</code> if it should be considered; <code>false</code>, otherwise.
	 *
	 * @throws UnsupportedOperationException when this implementation is invoked.
	 *
	 * @pre programPointContext != null
	 */
	protected boolean considerProgramPoint(final Context programPointContext) {
		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("considerProgramPoint(programPointContext = " + programPointContext + ")", null);
		}

		throw new UnsupportedOperationException("This method is unsupported.");
	}

	/**
	 * Checks if the "this" variable of the method specified in the given context should be considered for call context
	 * generation.
	 *
	 * @param methodContext of interest.
	 *
	 * @return <code>true</code> if it should be considered; <code>false</code>, otherwise.
	 *
	 * @throws UnsupportedOperationException when this implementation is invoked.
	 *
	 * @pre methodContext != null
	 */
	protected boolean considerThis(final Context methodContext) {
		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("considerThis(methodContext = " + methodContext + ")", null);
		}

		throw new UnsupportedOperationException("This method is unsupported.");
	}

	/**
	 * Checks if the unextensible inverted call stacks with the given callee on top of them should be considered as valid
	 * call stacks.
	 *
	 * @param calleeToken is the token in the callee
	 * @param callee is the top element on the inverted call stack.
	 * @param callSite for which the call stack could not be extended.
	 *
	 * @return <code>true</code> if they should be considered; <code>false</code>, otherwise.
	 *
	 * @throws UnsupportedOperationException when this implementation is invoked.
	 */
	protected boolean shouldConsiderUnextensibleStacksAt(final Object calleeToken, final SootMethod callee,
		final CallTriple callSite) {
		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("shouldConsiderUnextensibleStacksAt(calleeToken = " + calleeToken + ", callee = " + callee
				+ ", callSite = " + callSite + ")", null);
		}

		throw new UnsupportedOperationException("This method is unsupported.");
	}

	/**
	 * Retrieves the contexts based on the given token and method in which it occurs.
	 *
	 * @param token is the seed token
	 * @param method where the calling context should start from.
	 *
	 * @return a collection of calling contexts.
	 *
	 * @pre token != null and method != null
	 * @post result != null and result.oclIsKindOf(Collection(Stack(CallTriple)))
	 */
	private Collection getCallingContexts(final Object token, final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getCallingContexts(Object token = " + token + ", SootMethod method = " + method + ") - BEGIN");
		}

		final Collection _result;

		if (token == null) {
			_result = ICallingContextRetriever.NULL_CONTEXTS;
		} else {
			final IWorkBag _wb = new LIFOWorkBag();
			_wb.addWork(new Triple(method, token, new HashSet()));
			_result = new HashSet();

			while (_wb.hasWork()) {
				final Triple _triple = (Triple) _wb.getWork();
				final SootMethod _callee = (SootMethod) _triple.getFirst();
				final Object _calleeToken = _triple.getSecond();
				final Collection _calleeCallStacks = (Collection) _triple.getThird();
				final Collection _callers = callGraph.getCallers(_callee);

				// if there were no callers then we add all call chains as the border of the call graph was reached.
				if (_callers.isEmpty()) {
					_result.addAll(_calleeCallStacks);
				} else {
					// for each caller 
					final int _jEnd = _callers.size();
					final Iterator _j = _callers.iterator();

					// For collection of call stacks associated to _currToken
					for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final CallTriple _callSite = (CallTriple) _j.next();
						final Object _callerToken = getCallerSideToken(_calleeToken, _callee, _callSite);

						// if there was a corresponding token on the caller side
						if (_callerToken != null) {
							final Collection _stacks = createNewStacks(_calleeCallStacks, _callSite);

							if (!_stacks.isEmpty()) {
								final SootMethod _caller = _callSite.getMethod();
								_wb.addWorkNoDuplicates(new Triple(_caller, _callerToken, _stacks));
							}
						} else if (shouldConsiderUnextensibleStacksAt(_calleeToken, _callee, _callSite)) {
							// if we have reached the property-based "pivotal" point in the call chain then we decide
							// to extend all call chains and add it to the resulting contexts.
							_result.addAll(createNewStacks(_calleeCallStacks, _callSite));
						}
					}
				}
				_calleeCallStacks.clear();
			}

			// Reverse the call stacks as they have been constructed bottom-up.
			final Iterator _j = _result.iterator();
			final int _jEnd = _result.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final Stack _stack = (Stack) _j.next();

				if (_stack != null) {
					Collections.reverse(_stack);
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("getCallingContexts() - END - return value = " + _result);
		}

		return _result;
	}

	/**
	 * Extends the given call stacks to include the given call site.
	 *
	 * @param calleeSideCallStacks is the collection of stacks to be extended.
	 * @param callSite to be included.
	 *
	 * @return a collection of new call stacks.
	 *
	 * @pre calleeSideCallStacks != null and callSite != null
	 * @pre calleeSideCallStacks.oclIsKindOf(Collection(CallTriple))
	 * @post result != null and result.oclIsKindOf(Collection(CallTriple))
	 */
	private Collection createNewStacks(final Collection calleeSideCallStacks, final CallTriple callSite) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getClass().getName() + ".createNewStacks(Collection calleeSideCallStacks = " + calleeSideCallStacks
				+ ", CallTriple callSite = " + callSite + ") - BEGIN");
		}

		final Collection _t = new HashSet(calleeSideCallStacks);

		if (_t.isEmpty()) {
			_t.add(new Stack());
		}

		final Iterator _k = _t.iterator();
		final int _kEnd = _t.size();
		final Collection _stacks = new HashSet();

		for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
			final Stack _callStack = (Stack) _k.next();

			// In case of recursion, we will extend the stack to contain atmost 2 occurrences of a call site 
			if (!_callStack.contains(callSite) || _callStack.indexOf(callSite) == _callStack.lastIndexOf(callSite)) {
				final Stack _stack = (Stack) _callStack.clone();
				_stack.push(callSite);
				_stacks.add(_stack);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("createNewStacks() - END - return value = " + _stacks);
		}
		return _stacks;
	}
}

// End of File
