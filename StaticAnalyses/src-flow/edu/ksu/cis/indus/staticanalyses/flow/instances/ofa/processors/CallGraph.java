
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

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

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;

import org.apache.commons.collections.collection.CompositeCollection;

import org.apache.commons.collections.map.LRUMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;

import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.VirtualInvokeExpr;


/**
 * This class calculates call graphCache information from the given object flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath </a>
 * @author $Author$
 * @version $Revision$
 */
public class CallGraph
  extends AbstractValueAnalyzerBasedProcessor
  implements ICallGraphInfo {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(CallGraph.class);

	/** 
	 * The collection of methods from which the system can be started. Although an instance of a class can be created and a
	 * method can be invoked on it from the environment, this method will not be considered as a <i>head method </i>.
	 * However, our definition of head methods are those methods (excluding those invoked via <code>invokespecial</code>
	 * bytecode) with no caller method that belongs to the system.
	 *
	 * @invariant head != null and heads.oclIsKindOf(Set(SootMethod))
	 */
	private final Collection heads = new HashSet();

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
	 * The collection of methods that are reachble in the system.
	 *
	 * @invariant reachables.oclIsKindOf(Set(SootMethod))
	 */
	private Collection reachables = new HashSet();

	/** 
	 * The FA instance which implements object flow analysis. This instance is used to calculate call graphCache information.
	 *
	 * @invariant analyzer.oclIsKindOf(OFAnalyzer)
	 */
	private IValueAnalyzer analyzer;

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
	 * This maps callees to callers.
	 *
	 * @invariant callee2callers.oclIsKindOf(Map(SootMethod, Set(CallTriple)))
	 */
	private Map callee2callers = new HashMap();

	/** 
	 * This maps callers to callees.
	 *
	 * @invariant caller2callees.oclIsKindOf(Map(SootMethod, Set(CallTriple)))
	 */
	private Map caller2callees = new HashMap();

	/** 
	 * This manages pair objects.
	 */
	private final PairManager pairMgr;

	/** 
	 * This caches a traversable graphCache representation of the call graphCache.
	 */
	private SimpleNodeGraph graphCache;

	/** 
	 * This is used to retrieve nodes from the graph cache.
	 */
	private Transformer nodeRetrievingTransformer;

	/**
	 * Creates a new CallGraph object.
	 *
	 * @param pairManager to be used when creating pair objects.
	 *
	 * @pre pairManager != null
	 */
	public CallGraph(final PairManager pairManager) {
		pairMgr = pairManager;
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
	 * Sets the analyzer to be used to calculate call graph information upon call back.
	 *
	 * @param objFlowAnalyzer that provides the information to create the call graph.
	 *
	 * @pre objFlowAnalyzer != null and objFlowAnalyzer.oclIsKindOf(OFAnalyzer)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer objFlowAnalyzer) {
		this.analyzer = (OFAnalyzer) objFlowAnalyzer;
		heads.clear();
		reachables.clear();
		graphCache = null;
		nodeRetrievingTransformer = null;
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
			_context.setStmt(stmt);
			_context.setRootMethod(caller);

			final Collection _callees = getCallees(_ie, _context);
			_result = CollectionUtils.exists(CollectionUtils.collect(_callees, nodeRetrievingTransformer), _rp);
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
	 * Called by the post process controller when it walks a jimple value AST node.
	 *
	 * @param vBox is the AST node to be processed.
	 * @param context in which value should be processed.
	 *
	 * @pre context != null and vBox != null
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(ValueBox,Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
		final Stmt _stmt = context.getStmt();
		final SootMethod _caller = context.getCurrentMethod();
		SootMethod _callee = null;
		Set _callees;
		Set _callers;
		CallTriple _triple;
		final Value _value = vBox.getValue();

		if (_value instanceof StaticInvokeExpr) {
			final InvokeExpr _invokeExpr = (InvokeExpr) _value;
			_callee = _invokeExpr.getMethod();

			if (caller2callees.containsKey(_caller)) {
				_callees = (Set) caller2callees.get(_caller);
			} else {
				_callees = new HashSet();
				caller2callees.put(_caller, _callees);
			}
			_triple = new CallTriple(_callee, _stmt, _invokeExpr);
			_callees.add(_triple);

			if (callee2callers.containsKey(_callee)) {
				_callers = (Set) callee2callers.get(_callee);
			} else {
				_callers = new HashSet();
				callee2callers.put(_callee, _callers);
			}
			_triple = new CallTriple(_caller, _stmt, _invokeExpr);
			_callers.add(_triple);
		} else if (_value instanceof InterfaceInvokeExpr
			  || _value instanceof VirtualInvokeExpr
			  || _value instanceof SpecialInvokeExpr) {
			callBackOnInstanceInvokeExpr(context, (InstanceInvokeExpr) _value);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		// all method marked by the object flow analyses are reachable.
		reachables.add(method);

		if (method.getName().equals("<clinit>")) {
			heads.add(method);
		}
	}

	/**
	 * This calculates information such as heads, tails, and such.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#consolidate()
	 */
	public void consolidate() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: call graph consolidation");
		}
		heads.addAll(analyzer.getEnvironment().getRoots());

		// populate the caller2callees with head information in cases there are
		// no calls in the system.
		if (caller2callees.isEmpty()) {
			for (final Iterator _i = heads.iterator(); _i.hasNext();) {
				final Object _head = _i.next();
				caller2callees.put(_head, Collections.EMPTY_LIST);
			}
		}

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
		nodeRetrievingTransformer = new IObjectDirectedGraph.NodeRetrievingTransformer(graphCache);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: call graph consolidation");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		unstable();
		ppc.register(VirtualInvokeExpr.class, this);
		ppc.register(InterfaceInvokeExpr.class, this);
		ppc.register(StaticInvokeExpr.class, this);
		ppc.register(SpecialInvokeExpr.class, this);
		ppc.register(this);
	}

	/**
	 * Resets all internal data structure and forgets all info from the previous run.
	 */
	public void reset() {
		unstable();
		caller2callees.clear();
		callee2callers.clear();
		analyzer = null;
		graphCache = null;
		nodeRetrievingTransformer = null;
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
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(VirtualInvokeExpr.class, this);
		ppc.unregister(InterfaceInvokeExpr.class, this);
		ppc.unregister(StaticInvokeExpr.class, this);
		ppc.unregister(SpecialInvokeExpr.class, this);
		ppc.unregister(this);
		stable();
	}

	/**
	 * Testing purposes only.
	 *
	 * @return the cached copy of the call graph.
	 */
	final IDirectedGraph getCallGraph() {
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
	 * Called as a result of callback durign processing the AST when instance invoke expression is encountered.
	 *
	 * @param context in which expression should be processed.
	 * @param expr is the expression.
	 *
	 * @pre context != null and stmt != null and caller != null and expr != null
	 */
	private void callBackOnInstanceInvokeExpr(final Context context, final InstanceInvokeExpr expr) {
		final Stmt _stmt = context.getStmt();
		final SootMethod _caller = context.getCurrentMethod();
		final SootMethod _calleeMethod = expr.getMethod();
		context.setProgramPoint(expr.getBaseBox());

		final Collection _values = analyzer.getValues(expr.getBase(), context);

		if (!_values.isEmpty()) {
			final Set _callees;

			if (caller2callees.containsKey(_caller)) {
				_callees = (Set) caller2callees.get(_caller);
			} else {
				_callees = new HashSet();
				caller2callees.put(_caller, _callees);
			}

			final CallTriple _ctrp = new CallTriple(_caller, _stmt, expr);

			for (final Iterator _i = _values.iterator(); _i.hasNext();) {
				final Object _t = _i.next();

				if (!(_t instanceof NewExpr || _t instanceof StringConstant)) {
					continue;
				}

				SootClass _accessClass = null;

				if (expr instanceof SpecialInvokeExpr && _calleeMethod.getName().equals("<init>")) {
					_accessClass = _calleeMethod.getDeclaringClass();
				} else if (_t instanceof NewExpr) {
					final NewExpr _newExpr = (NewExpr) _t;
					_accessClass = analyzer.getEnvironment().getClass(_newExpr.getBaseType().getClassName());
				} else if (_t instanceof StringConstant) {
					_accessClass = analyzer.getEnvironment().getClass("java.lang.String");
				}

				final String _methodName = _calleeMethod.getName();
				final List _parameterTypes = _calleeMethod.getParameterTypes();
				final Type _returnType = _calleeMethod.getReturnType();
				final SootMethod _callee = findMethodImplementation(_accessClass, _methodName, _parameterTypes, _returnType);
				final CallTriple _triple = new CallTriple(_callee, _stmt, expr);
				_callees.add(_triple);

				final Set _callers;

				if (callee2callers.containsKey(_callee)) {
					_callers = (Set) callee2callers.get(_callee);
				} else {
					_callers = new HashSet();
					callee2callers.put(_callee, _callers);
				}
				_callers.add(_ctrp);
			}
		}
	}

	/**
	 * Finds the implementation of the given method when accessed via <code>accessClass</code>.
	 *
	 * @param accessClass is the class via which the method is invoked.
	 * @param methodName is the name of the method.
	 * @param parameterTypes is the list of parameter types of the method.
	 * @param returnType is the return type of the method.
	 *
	 * @return the implementation of the requested method if present in the class hierarchy; <code>null</code>, otherwise.
	 *
	 * @pre accessClass != null and methodName != null and parameterTypes != null and returnType != null
	 */
	private SootMethod findMethodImplementation(final SootClass accessClass, final String methodName,
		final List parameterTypes, final Type returnType) {
		SootMethod _result = null;

		if (accessClass.declaresMethod(methodName, parameterTypes, returnType)) {
			_result = accessClass.getMethod(methodName, parameterTypes, returnType);
		} else {
			if (accessClass.hasSuperclass()) {
				final SootClass _superClass = accessClass.getSuperclass();
				_result = findMethodImplementation(_superClass, methodName, parameterTypes, returnType);
			} else {
				if (LOGGER.isErrorEnabled()) {
					LOGGER.error(methodName + "(" + parameterTypes + "):" + returnType + " is not accessible from "
						+ accessClass);
				}
			}
		}
		return _result;
	}
}

// End of File
