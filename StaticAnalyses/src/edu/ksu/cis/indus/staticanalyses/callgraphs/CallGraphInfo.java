
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

import edu.ksu.cis.indus.Constants;

import edu.ksu.cis.indus.common.ToStringBasedComparator;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.graph.GraphReachabilityPredicate;
import edu.ksu.cis.indus.common.graph.IDirectedGraph;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.graph.IObjectDirectedGraph;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph.SimpleNode;

import edu.ksu.cis.indus.interfaces.AbstractStatus;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.processing.Context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import org.apache.commons.collections.collection.CompositeCollection;

import org.apache.commons.collections.map.LRUMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;


/**
 * This class calculates call graphCache information from the given object flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath </a>
 * @author $Author$
 * @version $Revision$
 */
public final class CallGraphInfo
  extends AbstractStatus
  implements ICallGraphInfo {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(CallGraphInfo.class);

	/** 
	 * The collection of methods that don't have callers in the system.  These typically include root methods and class 
     * initializers.
	 *
	 * @invariant head != null and heads.oclIsKindOf(Set(SootMethod))
	 */
	private final Collection heads = new HashSet();

	/** 
	 * The collection of methods that are reachble in the system.
	 *
	 * @invariant reachables.oclIsKindOf(Set(SootMethod))
	 */
	private final Collection reachables = new HashSet();

	/** 
	 * This maps callees to callers.
	 *
	 * @invariant callee2callers.oclIsKindOf(Map(SootMethod, Set(CallTriple)))
	 */
	private final Map callee2callers = new HashMap();

	/** 
	 * This maps callers to callees.
	 *
	 * @invariant caller2callees.oclIsKindOf(Map(SootMethod, Set(CallTriple)))
	 */
	private final Map caller2callees = new HashMap();

	/** 
	 * A cache of mappings from an invocation site to methods reachable from that site via call chain.
	 *
	 * @invariant invocationsite2reachableMethods.oclIsKindOf(Map(Pair(Stmt, SootMethod), Collection(SootMethod)))
	 */
	private final Map invocationsite2reachableMethods = new LRUMap(Constants.getNumOfMethodsInApplication(), true);

	/** 
	 * A cache of mappings from a method to methods that can via a call chain reach the key of the mapping.
	 *
	 * @invariant method2reachableMethods.oclIsKindOf(Map(SootMethod, Collection(SootMethod)))
	 */
	private final Map method2backwardReachableMethods = new LRUMap(Constants.getNumOfMethodsInApplication(), true);

	/** 
	 * A cache of mappings from a method to methods reachable from that site via call chain.
	 *
	 * @invariant method2reachableMethods.oclIsKindOf(Map(SootMethod, Collection(SootMethod)))
	 */
	private final Map method2forwardReachableMethods = new LRUMap(Constants.getNumOfMethodsInApplication(), true);

	/** 
	 * The collection of SCCs in this call graph in bottom-up direction.
	 *
	 * @invariant bottomUpSCC.oclIsKindOf(Sequence(Collection(SootMethod)))
	 */
	private List bottomUpSCC;

	/** 
	 * The collection of SCCs in this call graph in top-down direction.
	 *
	 * @invariant topDownSCC.oclIsKindOf(Sequence(Collection(SootMethod)))
	 */
	private List topDownSCC;

	/** 
	 * This manages pair objects.
	 */
	private final PairManager pairMgr;

	/** 
	 * This caches a traversable graphCache representation of the call graphCache.
	 */
	private SimpleNodeGraph graphCache;

	/**
	 * Creates a new CallGraphInfo object.
	 *
	 * @param pairManager to be used when creating pair objects.
	 *
	 * @pre pairManager != null
	 */
	public CallGraphInfo(final PairManager pairManager) {
		pairMgr = pairManager;
	}

	/**
	 * This is the interface is used to extract call info for a system.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath </a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public static interface ICallInfoProvider {
		/**
		 * Retrieves the callee to callers map.
		 *
		 * @return a map for callee to callers.
		 *
		 * @post result != null and result.oclIsKindOf(Map(CallTriple, Collection(CallTriple)))
		 */
		Map getCallee2CallersMap();

		/**
		 * Retrieves the caller to callees map.
		 *
		 * @return a map for caller to callees.
		 *
		 * @post result != null and result.oclIsKindOf(Map(CallTriple, Collection(CallTriple)))
		 */
		Map getCaller2CalleesMap();
        /**
         * Retrieves the head methods (methods with to caller).
         *
         * @return a collection of head methods.
         *
         * @post result != null and result.oclIsKindOf(Collection(SootMethod))
         */
        Collection getHeads();
        
		/**
		 * Retrieves the methods reachable in the system.
		 *
		 * @return a collection of reachable methods.
		 *
		 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
		 */
		Collection getReachableMethods();
	}

	/**
	 * A comparator to compare call triples based on <code>toString()</code> value of the method being called.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath </a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static class CallTripleMethodToStringBasedComparator
	  implements Comparator {
		/**
		 * @see Comparator#compare(Object,Object)
		 */
		public int compare(final Object o1, final Object o2) {
			return ((CallTriple) o1).getMethod().getSignature().compareTo(((CallTriple) o2).getMethod().getSignature());
		}
	}

	/**
	 * @see ICallGraphInfo#isCalleeReachableFromCallSite(soot.SootMethod, Stmt, soot.SootMethod)
	 */
	public boolean isCalleeReachableFromCallSite(final SootMethod callee, final Stmt stmt, final SootMethod caller) {
		final boolean _result;
		final GraphReachabilityPredicate _rp = new GraphReachabilityPredicate(callee, true, graphCache);

		if (_rp.evaluate(caller)) {
			final InvokeExpr _ie = stmt.getInvokeExpr();
			final Context _context = new Context();
			_context.setRootMethod(caller);

			final Collection _methodsThatMayCallCallee = getCallees(_ie, _context);
			_result = _methodsThatMayCallCallee.contains(callee) || CollectionUtils.exists(_methodsThatMayCallCallee, _rp);
		} else {
			_result = false;
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#isCalleeReachableFromCaller(soot.SootMethod, soot.SootMethod)
	 */
	public boolean isCalleeReachableFromCaller(final SootMethod callee, final SootMethod caller) {
		final INode _calleeNode = graphCache.queryNode(callee);
		final INode _callerNode = graphCache.queryNode(caller);
		return _calleeNode != null && _callerNode != null && graphCache.isReachable(_callerNode, _calleeNode, true);
	}

	/**
	 * Returns a collection of methods called by <code>caller</code>.
	 *
	 * @param caller which calls the returned methods.
	 *
	 * @return a collection of call sites along with callees at those sites.
	 *
	 * @pre caller != null
	 * @post result != null and result.oclIsKindOf(Collection(CallTriple))
	 *
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#getCallees(SootMethod)
	 */
	public Collection getCallees(final SootMethod caller) {
		Collection _result = Collections.EMPTY_LIST;
		final Collection _callees = (Collection) caller2callees.get(caller);

		if (_callees != null) {
			_result = Collections.unmodifiableCollection(_callees);
		}
		return _result;
	}

	/**
	 * Returns the set of method implementations that shall be invoked at the given callsite expression in the given method.
	 *
	 * @param invokeExpr the method call site.
	 * @param context in which the call occurs.
	 *
	 * @return a collection of methods.
	 *
	 * @pre invokeExpr != null and context != null
	 * @pre context.getCurrentMethod() != null
	 * @pre contet.getStmt() != null
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 *
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#getCallees(InvokeExpr,Context)
	 */
	public Collection getCallees(final InvokeExpr invokeExpr, final Context context) {
		final Collection _result;

		final Collection _temp = (Collection) caller2callees.get(context.getCurrentMethod());

		if (_temp != null && !_temp.isEmpty()) {
			_result = new ArrayList();

			for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _i.next();

				if (_ctrp.getExpr().equals(invokeExpr)) {
					_result.add(_ctrp.getMethod());
				}
			}
		} else {
			_result = Collections.EMPTY_LIST;
		}

		return _result;
	}

	/**
	 * Returns the methods that call the given method independent of any context.
	 *
	 * @param callee is the method being called.
	 *
	 * @return a collection of call-sites at which <code>callee</code> is called.
	 *
	 * @pre callee != null
	 * @post result != null and result->forall(o | o.oclIsKindOf(CallTriple))
	 *
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#getCallers(soot.SootMethod)
	 */
	public Collection getCallers(final SootMethod callee) {
		Collection _result = Collections.EMPTY_LIST;
		final Collection _callers = (Collection) callee2callers.get(callee);

		if (_callers != null) {
			_result = Collections.unmodifiableCollection(_callers);
		}
		return _result;
	}

	/**
	 * @see ICallGraphInfo#getCommonMethodsReachableFrom(soot.SootMethod, boolean, soot.SootMethod, boolean)
	 */
	public Collection getCommonMethodsReachableFrom(final SootMethod method1, final boolean forward1,
		final SootMethod method2, final boolean forward2) {
		final Collection _result =
			graphCache.getCommonReachablesFrom(graphCache.queryNode(method1), forward1, graphCache.queryNode(method2),
				forward2);
		CollectionUtils.transform(_result, IObjectDirectedGraph.OBJECT_EXTRACTOR);
		return _result;
	}

	/**
	 * Returns the methods that are the entry point for the analyzed system.
	 *
	 * @return a collection of methods.
	 *
	 * @post result != null and result->forall(o | o.oclType = SootMethod)
	 *
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#getHeads()
	 */
	public Collection getHeads() {
		return Collections.unmodifiableCollection(heads);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(ICallGraphInfo.ID);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#getMethodsInTopologicalOrder(boolean)
	 */
	public List getMethodsInTopologicalOrder(final boolean topdown) {
		final List _topologicalSorted = graphCache.performTopologicalSort(topdown);
		CollectionUtils.transform(_topologicalSorted, IObjectDirectedGraph.OBJECT_EXTRACTOR);
		return _topologicalSorted;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#getMethodsReachableFrom(soot.jimple.Stmt,soot.SootMethod)
	 */
	public Collection getMethodsReachableFrom(final Stmt stmt, final SootMethod root) {
		final Pair _pair = pairMgr.getPair(stmt, root);
		Collection _result = (Collection) invocationsite2reachableMethods.get(_pair);

		if (_result == null) {
			final InvokeExpr _ie = stmt.getInvokeExpr();
			final Context _context = new Context();
			_context.setRootMethod(root);

			final Collection _callees = getCallees(_ie, _context);
			final CompositeCollection _methods = new CompositeCollection();
			_methods.addComposited(_callees);

			for (final Iterator _i = _callees.iterator(); _i.hasNext();) {
				_methods.addComposited(getMethodsReachableFromHelper((SootMethod) _i.next(), true));
			}
			invocationsite2reachableMethods.put(_pair, _methods);
			_result = _methods;
		}
		return Collections.unmodifiableCollection(_result);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#getMethodsReachableFrom(soot.SootMethod,boolean)
	 */
	public Collection getMethodsReachableFrom(final SootMethod root, final boolean forward) {
		return Collections.unmodifiableCollection(getMethodsReachableFromHelper(root, forward));
	}

	/**
	 * Checks if the given method is reachable in the analyzed system.
	 *
	 * @param method to be checked for reachabiliy.
	 *
	 * @return <code>true</code> if <code>method</code> is reachable; <code>false</code>, otherwise.
	 *
	 * @pre method != null
	 *
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#isReachable(soot.SootMethod)
	 */
	public boolean isReachable(final SootMethod method) {
		return reachables.contains(method);
	}

	/**
	 * Returns the methods reachable in the analyzed system.
	 *
	 * @return a collection of methods.
	 *
	 * @post result != null and result->forall(o | o.oclType = SootMethod)
	 *
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#getReachableMethods()
	 */
	public Collection getReachableMethods() {
		return Collections.unmodifiableCollection(reachables);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#getSCCs(boolean)
	 */
	public List getSCCs(final boolean topDown) {
		if (topDownSCC == null) {
			topDownSCC = new ArrayList();

			final List _temp = graphCache.getSCCs(true);

			for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
				final Collection _scc = (Collection) _i.next();
				final List _l = new ArrayList();

				for (final Iterator _j = _scc.iterator(); _j.hasNext();) {
					_l.add(((SimpleNode) _j.next()).getObject());
				}
				topDownSCC.add(Collections.unmodifiableList(_l));
			}
			topDownSCC = Collections.unmodifiableList(topDownSCC);
			bottomUpSCC = new ArrayList(topDownSCC);
			Collections.reverse(bottomUpSCC);
			bottomUpSCC = Collections.unmodifiableList(bottomUpSCC);
		}
		return topDown ? topDownSCC
					   : bottomUpSCC;
	}

	/**
	 * @see ICallGraphInfo#areAnyMethodsReachableFrom(java.util.Collection, soot.jimple.Stmt, soot.SootMethod)
	 */
	public boolean areAnyMethodsReachableFrom(final Collection methods, final Stmt stmt, final SootMethod caller) {
		final Predicate _pred =
			new Predicate() {
				public boolean evaluate(final Object object) {
					return isCalleeReachableFromCallSite((SootMethod) object, stmt, caller);
				}
			};
		return CollectionUtils.exists(methods, _pred);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.ICallGraphInfo#areAnyMethodsReachableFrom(java.util.Collection, soot.SootMethod)
	 */
	public boolean areAnyMethodsReachableFrom(final Collection methods, final SootMethod caller) {
		final Predicate _pred =
			new Predicate() {
				public boolean evaluate(final Object object) {
					return isCalleeReachableFromCaller((SootMethod) object, caller);
				}
			};
		return CollectionUtils.exists(methods, _pred);
	}

	/**
	 * Constructs call graph from the information provided by the given provider.
	 *
	 * @param provider provides call information to be consolidated.
	 */
	public void createCallGraphInfo(final ICallInfoProvider provider) {
		callee2callers.putAll(provider.getCallee2CallersMap());
		caller2callees.putAll(provider.getCaller2CalleesMap());
		reachables.addAll(provider.getReachableMethods());
        heads.addAll(provider.getHeads());
		createGraph();
        stable();
	}

	/**
	 * Resets all internal data structure and forgets all info from the previous run.
	 */
	public void reset() {
		unstable();
		caller2callees.clear();
		callee2callers.clear();
		graphCache = null;
		topDownSCC = null;
		bottomUpSCC = null;
		reachables.clear();
		heads.clear();
		method2forwardReachableMethods.clear();
		invocationsite2reachableMethods.clear();
	}

	/**
	 * Provides a stringized representation of this call graphCache.
	 *
	 * @return stringized representation of the this call graphCache.
	 */
	public String toString() {
		final StringBuffer _result = new StringBuffer();

		_result.append("Root of the system: ");

		for (final Iterator _i = getHeads().iterator(); _i.hasNext();) {
			_result.append("\t" + ((SootMethod) _i.next()).getSignature());
		}
		_result.append("\nReachable methods in the system: " + getReachableMethods().size() + "\n");
		_result.append("Strongly Connected components in the system: " + getSCCs(true).size() + "\n");
		_result.append("top-down\n");

		final List _temp1 = new ArrayList();
		final List _temp2 = new ArrayList();
		_temp1.addAll(caller2callees.keySet());
		Collections.sort(_temp1, ToStringBasedComparator.SINGLETON);

		for (final Iterator _i = _temp1.iterator(); _i.hasNext();) {
			final SootMethod _caller = (SootMethod) _i.next();
			_result.append("\n" + _caller.getSignature() + "\n");
			_temp2.clear();
			_temp2.addAll((Collection) caller2callees.get(_caller));
			Collections.sort(_temp2, new CallTripleMethodToStringBasedComparator());

			for (final Iterator _j = _temp2.iterator(); _j.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _j.next();
				_result.append("\t" + _ctrp + "\n");
			}
		}

		_result.append("bottom-up\n");
		_temp1.clear();
		_temp1.addAll(callee2callers.keySet());
		Collections.sort(_temp1, ToStringBasedComparator.SINGLETON);

		for (final Iterator _i = _temp1.iterator(); _i.hasNext();) {
			final SootMethod _callee = (SootMethod) _i.next();
			_result.append("\n" + _callee.getSignature() + "\n");
			_temp2.clear();
			_temp2.addAll((Collection) callee2callers.get(_callee));
			Collections.sort(_temp2, new CallTripleMethodToStringBasedComparator());

			for (final Iterator _j = _temp2.iterator(); _j.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _j.next();
				_result.append("\t" + _ctrp.getMethod().getSignature() + "\n");
			}
		}

		return _result.toString();
	}

	/**
	 * Testing purposes only.
	 *
	 * @return the cached copy of the call graph.
	 */
	IDirectedGraph getCallGraph() {
		return graphCache;
	}

	/**
	 * Retrieves the reachables in the given direction.  The returned value exposes private data. Hence, callers should
	 * address the issue of keeping this data private.
	 *
	 * @param root see IDirectedGraph.getReachableFrom(INode, boolean)
	 * @param forward see IDirectedGraph.getReachableFrom(INode, boolean)
	 *
	 * @return see IDirectedGraph.getReachableFrom(INode, boolean)
	 *
	 * @pre root != null
	 *
	 * @see IDirectedGraph#getReachablesFrom(INode, boolean)
	 */
	private Collection getMethodsReachableFromHelper(final SootMethod root, final boolean forward) {
		final Map _map;

		if (forward) {
			_map = method2forwardReachableMethods;
		} else {
			_map = method2backwardReachableMethods;
		}

		Collection _result = (Collection) _map.get(root);

		if (_result == null) {
			_result = graphCache.getReachablesFrom(graphCache.queryNode(root), forward);
			CollectionUtils.transform(_result, IObjectDirectedGraph.OBJECT_EXTRACTOR);
			_map.put(root, _result);
		}
		return _result;
	}

	/**
	 * Creates a call graph.
	 */
	private void createGraph() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Starting construction of call graph...");
		}

		// construct call graph
		graphCache = new SimpleNodeGraph();

		for (final Iterator _i = reachables.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			final Collection _temp = (Collection) caller2callees.get(_sm);
			final INode _callerNode = graphCache.getNode(_sm);

			if (_temp != null) {
				for (final Iterator _j = _temp.iterator(); _j.hasNext();) {
					final CallTriple _ctrp = (CallTriple) _j.next();
					final SootMethod _method = _ctrp.getMethod();

					graphCache.addEdgeFromTo(_callerNode, graphCache.getNode(_method));
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: call graph consolidation");
		}
	}
}

// End of File
