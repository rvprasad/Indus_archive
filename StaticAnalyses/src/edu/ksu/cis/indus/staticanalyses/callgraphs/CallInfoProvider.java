
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * This is a data class that stores/provides data pertaining to call information.  There is no processing element in this 
 * class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class CallInfoProvider
implements CallGraphInfo.ICallInfoProvider {
	/** 
	 * The collection of methods from which the system can be started. Although an instance of a class can be created and a
	 * method can be invoked on it from the environment, this method will not be considered as a <i>head method </i>.
	 * However, our definition of head methods are those methods (excluding those invoked via <code>invokespecial</code>
	 * bytecode) with no caller method that belongs to the system.
	 *
	 * @invariant head != null and heads.oclIsKindOf(Set(SootMethod))
	 */
	final Collection heads = new HashSet();

	/** 
	 * The collection of methods that are reachble in the system.
	 *
	 * @invariant reachables.oclIsKindOf(Set(SootMethod))
	 */
	final Collection reachables = new HashSet();

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
	 * Resets internal data structures.
	 */
	public void reset() {
		callee2callers.clear();
		caller2callees.clear();
		heads.clear();
		reachables.clear();
	}
    
    /**
     * @see CallGraphInfo.ICallInfoProvider#getCallee2CallersMap()
     */
    public Map getCallee2CallersMap() {
        return Collections.unmodifiableMap(callee2callers);
    }

    /**
     * @see CallGraphInfo.ICallInfoProvider#getCaller2CalleesMap()
     */
    public Map getCaller2CalleesMap() {
        return Collections.unmodifiableMap(caller2callees);
    }

    /**
     * @see CallGraphInfo.ICallInfoProvider#getHeads()
     */
    public Collection getHeads() {
        return Collections.unmodifiableCollection(heads);
    }

    /**
     * @see CallGraphInfo.ICallInfoProvider#getReachableMethods()
     */
    public Collection getReachableMethods() {
        return Collections.unmodifiableCollection(reachables);
    }
}

// End of File
