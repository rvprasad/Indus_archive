
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.soot.SootPredicatesAndTransformers;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.MapUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;

import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * This class provides information regarding threads that occur in the system.  It can provide information such as the
 * possible threads in the system and the methods invoked in these threads.
 * 
 * <p>
 * Each thread is associated with a start() call-site which we refer to as <i>thread creation site</i>.  A thread  creation
 * site is represented as a pair of object allocating statement with the method enclosing the statement. A thread  is
 * represented as a triple of the invocation statement, method enclosing the invocation statement, and the class  providing
 * the run() method that is executed in the thread.
 * </p>
 * 
 * <p>
 * Each thread object is associated with an object allocation site which we refer to as <i>thread allocation site</i>. A
 * thread  allocation site is represented as a pair of object allocating statement with the method enclosing the statement.
 * </p>
 * 
 * <p>
 * As for the syntactically non-existent threads, we assume an imaginary method creates the non-existent threads.  We refer
 * to this as the <i>system creator method</i>.  We assume all class initializers are executed by the same dedicated thread.
 * We refer to this as <i>class initializign thread</i>.  <code>CLASS_INIT_THREAD_CREATION_SITE</code>,
 * <code>CLASS_INIT_THREAD_ALLOCATION_SITE</code>, and <code>CLASS_INIT_THREAD</code> represent the creation site,
 * allocation site, and the class initializing thread.
 * </p>
 * 
 * <p>
 * For main/system threads, we use the main classes as the classes providing the implementation along with the system creator
 * method as the creation method along with an imaginary object indicating the invocation site.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ThreadGraph
  extends AbstractValueAnalyzerBasedProcessor
  implements IThreadGraphInfo {
	/** 
	 * This represents the site at which the "class initializing" thread is created.  We assume that all classes are
	 * initialized by a dedicated thread.  This conforms to the JVM spec.
	 */
	public static final Pair CLASS_INIT_THREAD_CREATION_SITE =
		new Pair("CLASS_INIT_THREAD_CREATION_STMT", "CLASS_INIT_THREAD_CREATTION_METHOD");

	/** 
	 * This represents the site at which the "class initializing" thread is allocated.  We assume that all classes are
	 * initialized by a dedicated thread.
	 */
	public static final Pair CLASS_INIT_THREAD_ALLOCATION_SITE =
		new Pair("CLASS_INIT_THREAD_ALLOCATION_STMT", "CLASS_INIT_THREAD_ALLOCATION_METHOD");

	/** 
	 * This represents the method in which the system threads are created.
	 */
	public static final Object SYSTEM_CREATOR_METHOD = "SYSTEM_CREATOR_METHOD";

	/** 
	 * This represents the "class initializing" thread.
	 */
	public static final Triple CLASS_INIT_THREAD =
		new Triple("CLASS_INIT_THREAD_CREATION_STMT", "CLASS_INIT_THREAD_CREATTION_METHOD", "CLASS_INIT_THREAD_CLASS");

	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ThreadGraph.class);

	/** 
	 * This provides inter-procedural control-flow information.
	 */
	final CFGAnalysis cfgAnalysis;

	/** 
	 * The collection of thread allocation sites.
	 *
	 * @invariant newThreadExprs != null and newThreadExprs.oclIsKindOf(Collection(NewExprTriple))
	 */
	private final Collection newThreadExprs = new HashSet();

	/** 
	 * The collection of method invocation sites at which <code>java.lang.Thread.start()</code> is invoked.
	 *
	 * @invariant startSites.oclIsKindOf(Collection(CallTriple)) and startSites != null
	 */
	private final Collection startSites = new HashSet();

	/** 
	 * A collection of thread allocation sites which are executed from within a loop or a SCC in the call graph.  This  also
	 * includes any allocation sites reachable from a method executed in a loop.
	 *
	 * @invariant threadAllocSitesSingle.oclIsKindOf(Triple(SootMethod, Stmt, NewExpr)))
	 */
	private final Collection threadCreationSitesMulti;

	/** 
	 * A collection of thread allocation sites which are not executed from within a loop or a SCC in the call graph.  This
	 * also includes any allocation sites reachable from a method executed in a loop. This is the dual of
	 * <code>threadAllocSitesMulti.</code>
	 *
	 * @invariant threadAllocSitesSingle.oclIsKindOf(Triple(SootMethod, Stmt, NewExpr)))
	 */
	private final Collection threadCreationSitesSingle;

	/** 
	 * This provides call graph information pertaining to the system.
	 *
	 * @invariant cgi != null
	 */
	private final ICallGraphInfo cgi;

	/** 
	 * This maps methods to thread allocation sites which create threads in which the key method is executed.
	 *
	 * @invariant method2threads != null and method2threads.oclIsKindOf(Map(SootMethod, Collection(NewExprTriple)))
	 */
	private final Map method2threads = new HashMap(Constants.getNumOfMethodsInApplication());

	/** 
	 * This maps threads allocation sites to the methods which are executed in the created threads.
	 *
	 * @invariant thread2methods != null and thread2methods.isOclKindOf(Map(NewExprTriple, Collection(SootMethod)))
	 */
	private final Map thread2methods = new HashMap();

	/** 
	 * This provides object flow information required by this analysis.
	 */
	private OFAnalyzer analyzer;

	/** 
	 * This manages pair objects.
	 */
	private PairManager pairMgr;

	/**
	 * Creates a new ThreadGraph object.
	 *
	 * @param callGraph provides call graph information.
	 * @param cfa provides control-flow information.
	 * @param pairManager to be used.
	 *
	 * @pre callGraph != null and cfa != null and pairManager != null
	 */
	public ThreadGraph(final ICallGraphInfo callGraph, final CFGAnalysis cfa, final PairManager pairManager) {
		cgi = callGraph;
		pairMgr = pairManager;
		threadCreationSitesSingle = new HashSet();
		threadCreationSitesMulti = new HashSet();
		cfgAnalysis = cfa;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IThreadGraphInfo#getAllocationSites()
	 */
	public Collection getAllocationSites() {
		return Collections.unmodifiableCollection(newThreadExprs);
	}

	/**
	 * Sets the object flow analyzer to be used for analysis.
	 *
	 * @param objFlowAnalyzer is the object flow analyzer.
	 *
	 * @pre objFlowAnalyzer != null and objFlowAnalyzer.oclIsKindOf(OFAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer objFlowAnalyzer) {
		analyzer = (OFAnalyzer) objFlowAnalyzer;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IThreadGraphInfo#getCreationSites()
	 */
	public Collection getCreationSites() {
		return Collections.unmodifiableCollection(startSites);
	}

	/**
	 * @see IThreadGraphInfo#getExecutedMethods(InvokeStmt,Context)
	 */
	public Collection getExecutedMethods(final InvokeStmt startStmt, final Context ctxt) {
		final Collection _result =
			(Collection) MapUtils.getObject(thread2methods, pairMgr.getPair(startStmt, ctxt.getCurrentMethod()),
				Collections.EMPTY_SET);
		return Collections.unmodifiableCollection(_result);
	}

	/**
	 * @see IThreadGraphInfo#getExecutionThreads(SootMethod)
	 */
	public Collection getExecutionThreads(final SootMethod sm) {
		final Collection _result = (Collection) MapUtils.getObject(method2threads, sm, Collections.EMPTY_SET);
		return Collections.unmodifiableCollection(_result);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(IThreadGraphInfo.ID);
	}

	/**
	 * Called by the post processing controller on encountering <code>NewExpr</code> and <code>VirtualInvokeExpr</code>
	 * values in the system.
	 *
	 * @param vBox that was encountered and needs processing.
	 * @param context in which the value was encountered.
	 *
	 * @pre value != null and context != null
	 */
	public void callback(final ValueBox vBox, final Context context) {
		final IEnvironment _env = analyzer.getEnvironment();
		final Value _value = vBox.getValue();

		if (_value instanceof NewExpr) {
			final NewExpr _ne = (NewExpr) _value;
			processNewExpr(context, _env, _ne);
		} else if (_value instanceof VirtualInvokeExpr) {
			final VirtualInvokeExpr _ve = (VirtualInvokeExpr) _value;
			processVirtualInvokeExpr(context, _ve);
		}
	}

	/**
	 * Consolidates the thread graph information before it is available to the application.
	 */
	public void consolidate() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: thread graph consolidation");
		}

		calculateThreadCallGraph();

		injectMainThread();

		pruneUnreachableStartSites();

		// Consolidate information pertaining to execution frequency of thread allocation sites.
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("consolidate thread allocation site execution frequency information.");
		}

		considerMultipleExecutions();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: thread graph consolidation");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IThreadGraphInfo#containsClassInitThread(java.util.Collection)
	 */
	public boolean containsClassInitThread(final Collection executionThreads) {
		return executionThreads.contains(CLASS_INIT_THREAD);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		unstable();
		ppc.register(NewExpr.class, this);
		ppc.register(VirtualInvokeExpr.class, this);
	}

	/**
	 * @see IThreadGraphInfo#mustOccurInDifferentThread(SootMethod, SootMethod)
	 */
	public boolean mustOccurInDifferentThread(final SootMethod methodOne, final SootMethod methodTwo) {
		final Collection _t1 = getExecutionThreads(methodOne);
		final Collection _t2 = getExecutionThreads(methodTwo);
		return _t1.isEmpty() || _t2.isEmpty() || !CollectionUtils.containsAny(_t1, _t2);
	}

	/**
	 * @see IThreadGraphInfo#mustOccurInSameThread(SootMethod, SootMethod)
	 */
	public boolean mustOccurInSameThread(final SootMethod methodOne, final SootMethod methodTwo) {
		final Collection _t1 = getExecutionThreads(methodOne);
		final Collection _t2 = getExecutionThreads(methodTwo);
		return _t1.size() == 1 && _t1.size() == _t2.size() && _t1.containsAll(_t2)
		  && threadCreationSitesSingle.containsAll(_t1);
	}

	/**
	 * Resets all internal data structure and forgets all info from the previous run.
	 */
	public void reset() {
		thread2methods.clear();
		method2threads.clear();
		analyzer = null;
		startSites.clear();
		newThreadExprs.clear();
		threadCreationSitesMulti.clear();
		threadCreationSitesSingle.clear();
		unstable();
	}

	/**
	 * @see Object#toString()
	 */
	public String toString() {
		final StringBuffer _result = new StringBuffer();
		final List _l = new ArrayList();

		_result.append("Total number of threads: " + thread2methods.size() + "\n");

		final List _temp1 = new ArrayList();
		_temp1.addAll(thread2methods.keySet());
		Collections.sort(_temp1, new Triple.TripleComparator());

		for (final Iterator _i = _temp1.iterator(); _i.hasNext();) {
			final Triple _triple = (Triple) _i.next();
			_result.append("\n" + _triple.getFirst() + "@" + _triple.getSecond() + "#" + _triple.getThird() + "\n");
			_l.clear();

			for (final Iterator _j = ((Collection) thread2methods.get(_triple)).iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();
				_l.add(_sm.getSignature());
			}
			Collections.sort(_l);

			for (final Iterator _j = _l.iterator(); _j.hasNext();) {
				_result.append("\t" + _j.next() + "\n");
			}
		}

		int _count = 1;
		_result.append("\nThread mapping:\n");

		for (final Iterator _j = getAllocationSites().iterator(); _j.hasNext();) {
			final Pair _element = (Pair) _j.next();
			final String _tid = "T" + _count++;

			_result.append(_tid + " -> " + _element.getFirst() + "@" + _element.getSecond() + "\n");
		}

		return _result.toString();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(NewExpr.class, this);
		ppc.unregister(VirtualInvokeExpr.class, this);
		stable();
	}

	/**
	 * Retrieves the runnable implementations that get executed in the <code>run()</code> method of
	 * <code>java.lang.Thread</code>.
	 *
	 * @param threadClass is the <code>java.lang.Thread</code> class.
	 *
	 * @return a collection of object flow values.
	 *
	 * @pre threadClass != null
	 * @post result != null
	 */
	private Collection getRunnables(final SootClass threadClass) {
		Collection _result = Collections.EMPTY_SET;
		final SootMethod _threadRunMethod = threadClass.getMethod("run", Collections.EMPTY_LIST, VoidType.v());

		for (final Iterator _iter =
				IteratorUtils.filteredIterator(_threadRunMethod.getActiveBody().getUnits().iterator(),
					SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE); _iter.hasNext();) {
			final Stmt _stmt = (Stmt) _iter.next();
			final InvokeExpr _expr = _stmt.getInvokeExpr();
			final SootMethod _invokedMethod = _expr.getMethod();

			if (_expr instanceof VirtualInvokeExpr
				  && _invokedMethod.getName().equals("run")
				  && _invokedMethod.getDeclaringClass().getName().equals("java.lang.Runnable")) {
				final Context _context = new Context();
				_context.setRootMethod(_threadRunMethod);
				_context.setStmt(_stmt);
				_context.setProgramPoint(_stmt.getInvokeExprBox());
				_result = analyzer.getValues(((VirtualInvokeExpr) _expr).getBase(), _context);
				break;
			}
		}
		return _result;
	}

	/**
	 * Calculates thread call graph.
	 *
	 * @throws RuntimeException when the system being analyzed is not type-safe.
	 */
	private void calculateThreadCallGraph()
	  throws RuntimeException {
		// capture the run call-site in Thread.start method
		final IEnvironment _env = analyzer.getEnvironment();
		final SootClass _threadClass = _env.getClass("java.lang.Thread");
		final SootMethod _startMethod = _threadClass.getMethodByName("start");
		final Context _ctxt = new Context();
		_ctxt.setRootMethod(_startMethod);

		final Collection _values = analyzer.getValuesForThis(_ctxt);
		final Map _class2runCallees = new HashMap();
		final Collection _runnables = getRunnables(_threadClass);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("New thread expressions are: " + _values);
		}

		for (final Iterator _i =
				IteratorUtils.filteredIterator(_values.iterator(), SootPredicatesAndTransformers.NEW_EXPR_PREDICATE);
			  _i.hasNext();) {
			final NewExpr _value = (NewExpr) _i.next();
			final SootClass _sc = _env.getClass(_value.getBaseType().getClassName());
			Collection _methods;

			if (!_class2runCallees.containsKey(_sc)) {
				boolean _flag = false;

				final SootClass _scTemp = Util.getDeclaringClass(_sc, "run", Collections.EMPTY_LIST, VoidType.v());

				if (_scTemp != null) {
					_flag = _scTemp.getName().equals("java.lang.Thread");
				} else {
					LOGGER.error("How can there be a descendent of java.lang.Thread without access to run() method.");
					throw new RuntimeException("run() method is unavailable via " + _sc
						+ " even though it is a descendent of java.lang.Thread.");
				}

				if (_flag) {
					// Here we use the knowledge that all threads are dispatched via a "target" field of Thread class.
					_methods = new HashSet();

					for (final Iterator _j =
							IteratorUtils.filteredIterator(_runnables.iterator(),
								SootPredicatesAndTransformers.NEW_EXPR_PREDICATE); _j.hasNext();) {
						final NewExpr _temp = (NewExpr) _j.next();
						final SootClass _runnable = _env.getClass((_temp.getBaseType()).getClassName());
						_methods.addAll(transitiveThreadCallClosure(_runnable.getMethod("run", Collections.EMPTY_LIST,
									VoidType.v())));
					}
				} else {
					_methods = transitiveThreadCallClosure(_sc.getMethod("run", Collections.EMPTY_LIST, VoidType.v()));
				}

				_class2runCallees.put(_sc, _methods);
			}
		}

		final Collection _callTriples = cgi.getCallers(_startMethod);
		final Iterator _i = _callTriples.iterator();
		final int _iEnd = _callTriples.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final CallTriple _ctrp = (CallTriple) _i.next();
			final SootMethod _caller = _ctrp.getMethod();
			_ctxt.setRootMethod(_caller);

			final Stmt _callStmt = _ctrp.getStmt();
			_ctxt.setStmt(_callStmt);

			final VirtualInvokeExpr _virtualInvokeExpr = (VirtualInvokeExpr) _callStmt.getInvokeExpr();
			_ctxt.setProgramPoint(_virtualInvokeExpr.getBaseBox());

			final Collection _baseValues = analyzer.getValues(_virtualInvokeExpr.getBase(), _ctxt);

			for (final Iterator _j =
					IteratorUtils.filteredIterator(_baseValues.iterator(), SootPredicatesAndTransformers.NEW_EXPR_PREDICATE);
				  _j.hasNext();) {
				final NewExpr _value = (NewExpr) _j.next();
				final SootClass _sc = _env.getClass(_value.getBaseType().getClassName());

				final Collection _methods = (Collection) _class2runCallees.get(_sc);
				final Triple _thread = new Triple(_callStmt, _caller, _sc);
				thread2methods.put(_thread, _methods);

				for (final Iterator _k = _methods.iterator(); _k.hasNext();) {
					final SootMethod _sm = (SootMethod) _k.next();
					CollectionsUtilities.putIntoSetInMap(method2threads, _sm, _thread);
				}
			}
		}
	}

	/**
	 * Considers the property that a thread allocation site may be executed multiple times.
	 */
	private void considerMultipleExecutions() {
		final Collection _tassBak = new HashSet(threadCreationSitesSingle);
		final Context _ctxt = new Context();

		// Mark any thread allocation site that will be executed multiple times via a loop or call graph SCC 
		// as creating multiple threads.  The execution considers entire call chain to the entry method.
		for (final Iterator _i = _tassBak.iterator(); _i.hasNext();) {
			final Pair _pair = (Pair) _i.next();
			final Object _o = _pair.getSecond();

			if (_o instanceof SootMethod) {
				final SootMethod _encloser = (SootMethod) _o;

				if (cfgAnalysis.executedMultipleTimes(_encloser)) {
					threadCreationSitesSingle.remove(_pair);
					threadCreationSitesMulti.add(_pair);
				}
			}
		}

		final Collection _multiExecMethods = new HashSet();

		// Collect methods executed in threads which are created at sites that create more than one thread.  These methods 
		// will be executed multiple times.
		for (final Iterator _i = threadCreationSitesMulti.iterator(); _i.hasNext();) {
			final Pair _pair = (Pair) _i.next();
			final Object _o = _pair.getSecond();

			if (_o instanceof SootMethod) {
				_ctxt.setRootMethod((SootMethod) _o);
				_multiExecMethods.addAll(getExecutedMethods((InvokeStmt) _pair.getFirst(), _ctxt));
			}
		}
		_tassBak.clear();
		_tassBak.addAll(threadCreationSitesSingle);

		// filter the thread allocation site sets based on multiExecMethods.
		for (final Iterator _i = _tassBak.iterator(); _i.hasNext();) {
			final Pair _pair = (Pair) _i.next();
			final Object _encloser = _pair.getSecond();

			if (_multiExecMethods.contains(_encloser)) {
				threadCreationSitesSingle.remove(_pair);
				threadCreationSitesMulti.add(_pair);
			}
		}
	}

	/**
	 * Injects information for the "syntactically" non-occurring main thread.
	 */
	private void injectMainThread() {
		/* Note, main threads in the system are non-existent in terms of allocation sites.  So, we create a hypothetical expr
		 * with no associated statement and method and use that to create a NewExprTriple.  This triple represents the thread
		 * allocation site of main threads.  The triple would have null for the method and statement, but a NewExpr whose
		 * type name starts with "MainThread:".
		 *
		 * Also, class initializers (<clinit> methods) need to be treated differently.  They execute either in the current
		 * thread or another thread.  However, the interesting property is that class initializers are executed only once and
		 * by atmost one thread (which can be any thread) via locking.  Hence, for thread-sensitive analyses this matters.
		 * For other cases, we can assume that there is an arbitrary but same thread in the VM that just executes these
		 * initializers.  Another alternative is that each initializer is executed in a new dedicated thread. Like in the case
		 * of main threads we will use similar triples except "ClassInitThread:" is used as the type name prefix. We adapt
		 * the first approach of one dedicated thread executes all intializers in the VM.  Hence, all <clinit> methods are
		 * associated with an "ClassInitThread:".
		 */
		for (final Iterator _i = cgi.getHeads().iterator(); _i.hasNext();) {
			final SootMethod _head = (SootMethod) _i.next();
			final Pair _threadCreationSite;
			final Pair _threadAllocationSite;
			final Triple _thread;

			if (_head.getName().equals("<clinit>")) {
				_threadCreationSite = CLASS_INIT_THREAD_CREATION_SITE;
				_threadAllocationSite = CLASS_INIT_THREAD_ALLOCATION_SITE;
				_thread = CLASS_INIT_THREAD;
			} else {
				final SootClass _mainClass = _head.getDeclaringClass();
				_threadCreationSite = new Pair("MAIN_THREAD_CREATION_STMT:" + _mainClass, SYSTEM_CREATOR_METHOD);
				_threadAllocationSite = new Pair("MAIN_THREAD_ALLOCATION_STMT:" + _mainClass, SYSTEM_CREATOR_METHOD);
				_thread = new Triple("MAIN_THREAD_CREATION_STMT:" + _mainClass, SYSTEM_CREATOR_METHOD, _mainClass);
			}

			newThreadExprs.add(_threadAllocationSite);
			threadCreationSitesSingle.add(_threadCreationSite);

			final Collection _methods = transitiveThreadCallClosure(_head);
			thread2methods.put(_thread, _methods);

			for (final Iterator _j = _methods.iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();
				CollectionsUtilities.putIntoCollectionInMap(method2threads, _sm, _thread,
					CollectionsUtilities.HASH_SET_FACTORY);
			}
		}
	}

	/**
	 * Processes new expressoins.
	 *
	 * @param context in which the new expression occurs.
	 * @param env to be used.
	 * @param newExpr to be processed.
	 *
	 * @throws RuntimeException when there is a glitch in the system being analyzed is not type-safe.
	 *
	 * @pre context != null and env != null and newExpr != null
	 * @pre context.getStmt() != null and context.getCurrentMethod() != null
	 */
	private void processNewExpr(final Context context, final IEnvironment env, final NewExpr newExpr)
	  throws RuntimeException {
		final SootClass _clazz = env.getClass(newExpr.getBaseType().getClassName());

		// collect the new expressions which create Thread objects.
		if (Util.isDescendentOf(_clazz, "java.lang.Thread")) {
			final SootClass _temp = Util.getDeclaringClass(_clazz, "start", Collections.EMPTY_LIST, VoidType.v());

			if (_temp != null && _temp.getName().equals("java.lang.Thread")) {
				final Stmt _stmt = context.getStmt();
				final SootMethod _sm = context.getCurrentMethod();
				newThreadExprs.add(pairMgr.getPair(_stmt, _sm));
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("How can there be a descendent of java.lang.Thread without access to start() method.");
				}
				throw new RuntimeException("start() method is unavailable via " + _clazz
					+ " even though it is a descendent of java.lang.Thread.");
			}
		}
	}

	/**
	 * Processes invocation expressoins.
	 *
	 * @param context in which the new expression occurs.
	 * @param invokeExpr to be processed.
	 *
	 * @pre context != null and env != null and invokeExpr != null
	 * @pre context.getStmt() != null and context.getCurrentMethod() != null
	 */
	private void processVirtualInvokeExpr(final Context context, final VirtualInvokeExpr invokeExpr) {
		final RefLikeType _rlt = (RefLikeType) invokeExpr.getBase().getType();

		if (_rlt instanceof RefType) {
			final SootMethod _method = invokeExpr.getMethod();

			if (Util.isStartMethod(_method)) {
				final SootMethod _caller = context.getCurrentMethod();
				final Stmt _callStmt = context.getStmt();
				final Collection _callees = cgi.getCallees(invokeExpr, context);
				final Iterator _i = _callees.iterator();
				final int _iEnd = _callees.size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final SootMethod _callee = (SootMethod) _i.next();

					if (Util.isStartMethod(_callee)) {
						final Pair _callPair = pairMgr.getPair(_callStmt, _caller);
						startSites.add(_callPair);

						if (cfgAnalysis.checkForLoopEnclosedNewExpr(_callStmt, _caller)) {
							threadCreationSitesMulti.add(_callPair);
						} else {
							threadCreationSitesSingle.add(_callPair);
						}
						break;
					}
				}
			}
		}
	}

	/**
	 * Removes any thread creation site that is not reachable in the system.
	 */
	private void pruneUnreachableStartSites() {
		// prune the startSites such that it only contains reachable start sites.
		final Collection _temp = new HashSet();
		final Collection _reachables = cgi.getReachableMethods();

		for (final Iterator _i = startSites.iterator(); _i.hasNext();) {
			final Pair _callPair = (Pair) _i.next();

			if (!_reachables.contains(_callPair.getSecond())) {
				_temp.add(_callPair);
			}
		}
		startSites.removeAll(_temp);
	}

	/**
	 * Calculates the transitive closure of methods called from the given method.  The inclusion constraint for the closure
	 * is that the method cannot be an instance of <code>java.lang.Thread.start()</code>.
	 *
	 * @param starterMethod is the method from where the closure calculation starts.
	 *
	 * @return a collection of <code>SootMethod</code>s occurring the call closure.
	 *
	 * @pre starterMethod != null
	 * @post result != null and result.isOclKindOf(Collection(SootMethod))
	 */
	private Collection transitiveThreadCallClosure(final SootMethod starterMethod) {
		final Collection _result = new HashSet();
		final IWorkBag _wb = new HistoryAwareFIFOWorkBag(_result);
		_wb.addWork(starterMethod);
		_result.add(starterMethod);

		while (_wb.hasWork()) {
			final SootMethod _sm = (SootMethod) _wb.getWork();

			if (!Util.isStartMethod(_sm)) {
				final Collection _callees = cgi.getCallees(_sm);

				for (final Iterator _i = _callees.iterator(); _i.hasNext();) {
					final CallTriple _ctrp = (CallTriple) _i.next();
					final SootMethod _temp = _ctrp.getMethod();

					_wb.addWorkNoDuplicates(_temp);
				}
			}
		}
		return _result;
	}
}

// End of File
