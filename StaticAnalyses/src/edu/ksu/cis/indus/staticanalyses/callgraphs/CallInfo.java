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

package edu.ksu.cis.indus.staticanalyses.callgraphs;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import soot.SootMethod;

/**
 * This is a data class that stores/provides data pertaining to call information. There is no processing element in this
 * class.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class CallInfo
		implements CallGraphInfo.ICallInfo {

	/**
	 * This maps callees to callers.
	 * 
	 * @invariant callee2callers.oclIsKindOf(Map(SootMethod, Set(CallTriple)))
	 */
	final Map callee2callers = new HashMap();

	/**
	 * This maps callers to callees.
	 * 
	 * @invariant caller2callees.oclIsKindOf(Map(SootMethod, Set(CallTriple)))
	 */
	final Map caller2callees = new HashMap();

	/**
	 * The collection of methods that are reachble in the system.
	 * 
	 * @invariant reachables.oclIsKindOf(Set(SootMethod))
	 */
	private final Set reachables = new HashSet();

	/**
	 * Records the given method as reachable.
	 * 
	 * @param method
	 *            to be recorded.
	 * @pre method != null
	 */
	public void addReachable(final SootMethod method) {
		reachables.add(method);
	}

	/**
	 * @see CallGraphInfo.ICallInfo#getCallee2CallersMap()
	 */
	public Map getCallee2CallersMap() {
		return Collections.unmodifiableMap(callee2callers);
	}

	/**
	 * @see CallGraphInfo.ICallInfo#getCaller2CalleesMap()
	 */
	public Map getCaller2CalleesMap() {
		return Collections.unmodifiableMap(caller2callees);
	}

	/**
	 * @see CallGraphInfo.ICallInfo#getReachableMethods()
	 */
	public Collection getReachableMethods() {
		final Collection _r = new HashSet();
		_r.addAll(reachables);
		_r.addAll(callee2callers.keySet());
		_r.addAll(caller2callees.keySet());
		return _r;
	}

	/**
	 * Resets internal data structures.
	 */
	public void reset() {
		callee2callers.clear();
		caller2callees.clear();
		reachables.clear();
	}

	/**
	 * Injects empty sets for caller and callee information of methods with no callees and callers.
	 */
	void fixupMethodsHavingZeroCallersAndCallees() {
		final Iterator _i = reachables.iterator();
		final int _iEnd = reachables.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Object _o = _i.next();

			if (callee2callers.get(_o) == null) {
				callee2callers.put(_o, Collections.EMPTY_SET);
			}

			if (caller2callees.get(_o) == null) {
				caller2callees.put(_o, Collections.EMPTY_SET);
			}
		}

		assert validate();
	}

	/**
	 * Validates the information. This is designed to be used inside an assertion.
	 * 
	 * @return <code>true</code> if the info is valid. An assertion violation is raised otherwise.
	 */
	private boolean validate() {
		final Collection _k1 = caller2callees.keySet();
		for (final Iterator _i = callee2callers.values().iterator(); _i.hasNext();) {
			final Collection _c = (Collection) _i.next();
			for (final Iterator _j = _c.iterator(); _j.hasNext();) {
				final ICallGraphInfo.CallTriple _ctrp = (ICallGraphInfo.CallTriple) _j.next();
				assert _k1.contains(_ctrp.getMethod());
			}
		}

		final Collection _k2 = callee2callers.keySet();
		for (final Iterator _i = caller2callees.values().iterator(); _i.hasNext();) {
			final Collection _c = (Collection) _i.next();
			for (final Iterator _j = _c.iterator(); _j.hasNext();) {
				final ICallGraphInfo.CallTriple _ctrp = (ICallGraphInfo.CallTriple) _j.next();
				assert _k2.contains(_ctrp.getMethod());
			}
		}

		assert _k1.containsAll(reachables);
		assert _k2.containsAll(reachables);

		return true;
	}

}

// End of File
