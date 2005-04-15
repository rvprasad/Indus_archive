
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.LIFOWorkBag;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.apache.commons.collections.map.LazyMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;


/**
 * This is an abstract implementation of <code>ICallingContextRetriever</code>.  A concrete implementation of this class
 * asis will return null (open) contexts.
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
	 * @see ICallingContextRetriever#getCallingContextsForThis(SootMethod)
	 */
	public final Collection getCallingContextsForThis(final SootMethod method) {
		final Collection _result;

		if (considerThis(method)) {
			_result = getCallingContexts(getTokenForThis(method), method);
		} else {
			_result = Collections.EMPTY_SET;
		}
		return _result;
	}

	/**
	 * Retrieves the value in <code>callGraph</code>.
	 *
	 * @return the value in <code>callGraph</code>.
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
	 * @return caller side token.  This implementation returns <code>null</code>.
	 *
	 * @pre token != null and callee != null and callsite != null
	 */
	protected Object getCallerSideToken(final Object token, final SootMethod callee, final CallTriple callsite) {
		return null;
	}

	/**
	 * Retrieves the token for the given program point. If this  method returns <code>null</code> then that will terminate
	 * the extension of current call context along this "path".
	 *
	 * @param programPoint of interest.
	 *
	 * @return token corresponding to the program point. This implementation returns <code>null</code>.
	 *
	 * @pre programPoint != null
	 */
	protected Object getTokenForProgramPoint(final Context programPoint) {
		return null;
	}

	/**
	 * Retrieves the token for the given method.
	 *
	 * @param method of interest.
	 *
	 * @return token corresponding to the method. This implementation returns <code>null</code>.
	 *
	 * @pre method != null
	 */
	protected Object getTokenForThis(final SootMethod method) {
		return null;
	}

	/**
	 * Checks if the given program point (calling context base) should be considered for call context generation.
	 *
	 * @param programPoint of interest.
	 *
	 * @return <code>true</code> if it should be considered; <code>false</code>, otherwise.  This implementation returns
	 * 		   <code>true</code>.
	 *
	 * @pre programPoint != null
	 */
	protected boolean considerProgramPoint(final Context programPoint) {
		return true;
	}

	/**
	 * Checks if the "this" variable of the method should be considered for call context generation.
	 *
	 * @param method of interest.
	 *
	 * @return <code>true</code> if it should be considered; <code>false</code>, otherwise.  This implementation returns
	 * 		   <code>true</code>.
	 *
	 * @pre method != null
	 */
	protected boolean considerThis(final SootMethod method) {
		return true;
	}

	/**
	 * Checks if a call stack that is not extended while discoveing calling contexts should be considered as valid calling
	 * context.
	 *
	 * @return <code>true</code> if it should be considered; <code>false</code>, otherwise.  This implementation returns
	 * 		   <code>true</code>.
	 */
	protected boolean shouldUnextendedStacksBeConsidered() {
		return true;
	}

	/**
	 * Retrieves the contexts based on the given token and method in which it occurs.
	 *
	 * @param token is the seed token
	 * @param method where the calling context should end.
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
			_result = NULL_CONTEXTS;
		} else {
			final Map _method2map = LazyMap.decorate(new HashMap(), CollectionsUtilities.HASH_MAP_FACTORY);
			initializeCallStacks(_method2map, token, method);
			_result = new HashSet();

			final Collection _stacks = new HashSet();
			final IWorkBag _wb = new LIFOWorkBag();
			_wb.addAllWork(_method2map.keySet());

			while (_wb.hasWork()) {
				Map _map1 = null;

				final SootMethod _callee = (SootMethod) _wb.getWork();
				final Map _calleeSideRef2callstacks = (Map) _method2map.get(_callee);
				final Iterator _i = _calleeSideRef2callstacks.keySet().iterator();
				final int _iEnd = _calleeSideRef2callstacks.keySet().size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					boolean _zeroStacksWereExtended = true;
					final Object _currToken = _i.next();
					final Collection _calleeSideCallStacks = (Collection) _calleeSideRef2callstacks.get(_currToken);
					final Collection _callers = callGraph.getCallers(_callee);
					final Iterator _j = _callers.iterator();
					final int _jEnd = _callers.size();

                    for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
						final CallTriple _callSite = (CallTriple) _j.next();
						final Object _callerSideReference = getCallerSideToken(_currToken, _callee, _callSite);

						if (_callerSideReference != null) {
							final SootMethod _caller = _callSite.getMethod();
							final Map _callerSideRef2callstacks;

							// we need this to handle concurrent modification to caller-side stacks
							if (_caller.equals(_callee)) {
								_map1 = new HashMap();
								_callerSideRef2callstacks = _map1;
							} else {
								_callerSideRef2callstacks = (Map) _method2map.get(_caller);
							}

							if (!_callerSideRef2callstacks.containsKey(_callerSideReference)) {
								final Collection _temp = new HashSet();
								_callerSideRef2callstacks.put(_callerSideReference, _temp);
							}

							boolean _addCallerToStack = false;
							final Collection _callerSideCallStacks =
								(Collection) _callerSideRef2callstacks.get(_callerSideReference);
							final Iterator _k = _calleeSideCallStacks.iterator();
							final int _kEnd = _calleeSideCallStacks.size();
							_stacks.clear();

							for (int _kIndex = 0; _kIndex < _kEnd; _kIndex++) {
								final Stack _callStack = (Stack) _k.next();

								// HACK: This rejects recursive call chains.  Instead, they should be massaged suitably. 
								if (!_callStack.contains(_callSite)) {
									final Stack _stack = (Stack) _callStack.clone();
									_stack.push(_callSite);
									_stacks.add(_stack);
									_addCallerToStack = true;
								}
							}

							if (_addCallerToStack) {
								_callerSideCallStacks.addAll(_stacks);
								_wb.addWorkNoDuplicates(_caller);
							}
							_zeroStacksWereExtended = false;
						}
					}

					// Collect the contexts when we have reached the top of the (inverted) call chain or the call chains 
					// cannot be exetended based on the property.
					if (_zeroStacksWereExtended) {
                        // if we are dealing with an entry method  
						if (_jEnd == 0) {
							_result.addAll(NULL_CONTEXTS);
						} else if (shouldUnextendedStacksBeConsidered()){
							_result.addAll(_calleeSideCallStacks);
						}
					}
					_calleeSideCallStacks.clear();
				}

				// we need this to handle concurrent modification to caller-side stacks due to recursion.
				if (_map1 != null) {
					_method2map.put(_callee, _map1);
				}
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
	 * Initializes the call stacks.
	 *
	 * @param method2map maps methods to calling contexts.
	 * @param token for which the contexts are stored.
	 * @param callee in which <code>token</code> occurs.
	 *
	 * @pre method2map != null and method2map.oclIsKindOf(Map(SootMethod, Map(Object, Collection(Stack(CallTriple)))))
	 * @pre callee != null
	 */
	private void initializeCallStacks(final Map method2map, final Object token, final SootMethod callee) {
		final Map _ref2callstacks = (Map) method2map.get(callee);

		if (!_ref2callstacks.containsKey(token)) {
			final Collection _callstacks = new HashSet();
			_ref2callstacks.put(token, _callstacks);
		}
	}
}

// End of File
