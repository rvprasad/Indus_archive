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

package edu.ksu.cis.indus.staticanalyses.flow.processors;

import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.ToStringBasedComparator;
import edu.ksu.cis.indus.common.collections.CollectionUtils;
import edu.ksu.cis.indus.common.collections.IteratorUtils;
import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.common.datastructures.Pair.PairManager;
import edu.ksu.cis.indus.common.soot.ApplicationInitInfoFactory;
import edu.ksu.cis.indus.common.soot.Constants;
import edu.ksu.cis.indus.common.soot.SootPredicatesAndTransformers;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;
import soot.jimple.AssignStmt;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;

/**
 * This class provides information regarding threads that occur in the system. It can provide information such as the possible
 * threads in the system and the methods invoked in these threads.
 * <p>
 * Each thread is associated with a start() call-site which we refer to as <i>thread creation site</i>. A thread creation
 * site is represented as a pair of object allocating statement with the method enclosing the statement. A thread is
 * represented as a triple of the invocation statement, method enclosing the invocation statement, and the class providing the
 * run() method that is executed in the thread.
 * </p>
 * <p>
 * Each thread object is associated with an object allocation site which we refer to as <i>thread allocation site</i>. A
 * thread allocation site is represented as a pair of object allocating statement with the method enclosing the statement.
 * </p>
 * <p>
 * As for the syntactically non-existent threads, we assume an imaginary method creates the non-existent threads. We refer to
 * this as the <i>system creator method</i>. We assume all class initializers are executed by the same dedicated thread. We
 * refer to this as <i>class initializign thread</i>. <code>CLASS_INIT_THREAD_CREATION_SITE</code>,
 * <code>CLASS_INIT_THREAD_ALLOCATION_SITE</code>, and <code>CLASS_INIT_THREAD</code> represent the creation site,
 * allocation site, and the class initializing thread.
 * </p>
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
		extends AbstractValueAnalyzerBasedProcessor<Value>
		implements IThreadGraphInfo {

	/**
	 * The name of the runnable interface in Java.
	 */
	private static final String JAVA_LANG_RUNNABLE = "java.lang.Runnable";

	/**
	 * The name of the thread class in Java.
	 */
	private static final String JAVA_LANG_THREAD = "java.lang.Thread";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadGraph.class);

	/**
	 * The name of the "run" method.
	 */
	private static final String RUN_METHOD_NAME = "run";

	/**
	 * The name of the "start" method.
	 */
	private static final String START_METHOD_NAME = "start";

	/**
	 * This provides inter-procedural control-flow information.
	 */
	final CFGAnalysis cfgAnalysis;

	/**
	 * This provides object flow information required by this analysis.
	 */
	private IValueAnalyzer<Value> analyzer;

	/**
	 * This provides call graph information pertaining to the system.
	 * 
	 * @invariant cgi != null
	 */
	private final ICallGraphInfo cgi;

	/**
	 * This maps methods to thread creation sites which create threads in which the key method is executed.
	 */
	private final Map<SootMethod, Collection<Triple<InvokeStmt, SootMethod, SootClass>>> method2threadCreationSite = new HashMap<SootMethod, Collection<Triple<InvokeStmt, SootMethod, SootClass>>>(
			Constants.getNumOfMethodsInApplication());

	/**
	 * The collection of thread allocation sites.
	 */
	private final Collection<Pair<AssignStmt, SootMethod>> newThreadExprs = new HashSet<Pair<AssignStmt, SootMethod>>();

	/**
	 * This manages pair objects.
	 */
	private PairManager pairMgr;

	/**
	 * The collection of method invocation sites at which <code>java.lang.Thread.start()</code> is invoked.
	 */
	private final Collection<Pair<InvokeStmt, SootMethod>> startSites = new HashSet<Pair<InvokeStmt, SootMethod>>();

	/**
	 * This maps threads creation sites to the methods which are executed in the created threads.
	 */
	private final Map<Triple<InvokeStmt, SootMethod, SootClass>, Collection<SootMethod>> thread2methods = new HashMap<Triple<InvokeStmt, SootMethod, SootClass>, Collection<SootMethod>>();

	/**
	 * A collection of thread allocation sites which are executed from within a loop or a SCC in the call graph. This also
	 * includes any allocation sites reachable from a method executed in a loop.
	 */
	private final Collection<Pair<? extends Object, ? extends Object>> threadCreationSitesMulti;

	/**
	 * A collection of thread allocation sites which are not executed from within a loop or a SCC in the call graph. This also
	 * includes any allocation sites reachable from a method executed in a loop. This is the dual of
	 * <code>threadAllocSitesMulti.</code>
	 */
	private final Collection<Pair<? extends Object, ? extends Object>> threadCreationSitesSingle;

	/**
	 * This is the collection of thread entry point methods.
	 */
	private final Collection<SootMethod> threadEntryPoints = new HashSet<SootMethod>();

	/**
	 * Creates a new ThreadGraph object.
	 * 
	 * @param callGraph provides call graph information.
	 * @param cfa provides control-flow information.
	 * @param pairManager to be used.
	 * @pre callGraph != null and cfa != null and pairManager != null
	 */
	public ThreadGraph(final ICallGraphInfo callGraph, final CFGAnalysis cfa, final PairManager pairManager) {
		cgi = callGraph;
		pairMgr = pairManager;
		threadCreationSitesSingle = new HashSet<Pair<? extends Object, ? extends Object>>();
		threadCreationSitesMulti = new HashSet<Pair<? extends Object, ? extends Object>>();
		cfgAnalysis = cfa;
	}

	/**
	 * Called by the post processing controller on encountering <code>NewExpr</code> and <code>VirtualInvokeExpr</code>
	 * values in the system.
	 * 
	 * @param vBox that was encountered and needs processing.
	 * @param context in which the value was encountered.
	 * @pre value != null and context != null
	 */
	@Override public void callback(final ValueBox vBox, final Context context) {
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
	@Override public void consolidate() {
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

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Thread graph info - " + toString());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: thread graph consolidation");
		}
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IThreadGraphInfo#containsClassInitThread(java.util.Collection)
	 */
	public boolean containsClassInitThread(final Collection<Triple<InvokeStmt, SootMethod, SootClass>> executionThreads) {
		return executionThreads.contains(ApplicationInitInfoFactory.getClassInitExecutingThread());
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IThreadGraphInfo#getAllocationSites()
	 */
	public Collection<Pair<AssignStmt, SootMethod>> getAllocationSites() {
		return Collections.unmodifiableCollection(newThreadExprs);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IThreadGraphInfo#getCreationSites()
	 */
	public Collection<Pair<InvokeStmt, SootMethod>> getCreationSites() {
		return Collections.unmodifiableCollection(startSites);
	}

	/**
	 * @see IThreadGraphInfo#getExecutedMethods(InvokeStmt,SootMethod)
	 */
	public Collection<SootMethod> getExecutedMethods(final InvokeStmt startStmt, final SootMethod method) {
		final Context _ctxt = new Context();
		final VirtualInvokeExpr _virtualInvokeExpr = (VirtualInvokeExpr) startStmt.getInvokeExpr();
		_ctxt.setProgramPoint(_virtualInvokeExpr.getBaseBox());
		_ctxt.setStmt(startStmt);
		_ctxt.setRootMethod(method);
		final IEnvironment _env = analyzer.getEnvironment();
		final Collection<Value> _baseValues = analyzer.getValues(_virtualInvokeExpr.getBase(), _ctxt);
		final Collection<SootMethod> _result = new HashSet<SootMethod>();
		for (final Iterator<Value> _j = IteratorUtils.filteredIterator(_baseValues.iterator(),
				SootPredicatesAndTransformers.NEW_EXPR_PREDICATE); _j.hasNext();) {
			final NewExpr _value = (NewExpr) _j.next();
			final SootClass _sc = _env.getClass(_value.getBaseType().getClassName());
			final Triple<InvokeStmt, SootMethod, SootClass> _thread = new Triple<InvokeStmt, SootMethod, SootClass>(
					startStmt, method, _sc);
			_result.addAll(MapUtils.queryCollection(thread2methods, _thread));
		}
		return Collections.unmodifiableCollection(_result);
	}

	/**
	 * @see IThreadGraphInfo#getExecutionThreads(SootMethod)
	 */
	public Collection<Triple<InvokeStmt, SootMethod, SootClass>> getExecutionThreads(final SootMethod sm) {
		final Collection<Triple<InvokeStmt, SootMethod, SootClass>> _result = MapUtils.queryCollection(
				method2threadCreationSite, sm);
		return Collections.unmodifiableCollection(_result);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public Collection<? extends Comparable<?>> getIds() {
		return Collections.singleton(IThreadGraphInfo.ID);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IThreadGraphInfo#getThreadEntryPoints()
	 */
	public Collection<SootMethod> getThreadEntryPoints() {
		return Collections.unmodifiableCollection(threadEntryPoints);
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
		final Collection<Triple<InvokeStmt, SootMethod, SootClass>> _t1 = getExecutionThreads(methodOne);
		final Collection<Triple<InvokeStmt, SootMethod, SootClass>> _t2 = getExecutionThreads(methodTwo);
		return _t1.isEmpty() || _t2.isEmpty() || !CollectionUtils.containsAny(_t1, _t2);
	}

	/**
	 * @see IThreadGraphInfo#mustOccurInSameThread(SootMethod, SootMethod)
	 */
	public boolean mustOccurInSameThread(final SootMethod methodOne, final SootMethod methodTwo) {
		final Collection<Triple<InvokeStmt, SootMethod, SootClass>> _t1 = getExecutionThreads(methodOne);
		final Collection<Triple<InvokeStmt, SootMethod, SootClass>> _t2 = getExecutionThreads(methodTwo);
		return _t1.size() == 1 && _t1.size() == _t2.size() && _t1.containsAll(_t2)
				&& threadCreationSitesSingle.containsAll(_t1);
	}

	/**
	 * Resets all internal data structure and forgets all info from the previous run.
	 */
	@Override public void reset() {
		thread2methods.clear();
		method2threadCreationSite.clear();
		threadEntryPoints.clear();
		analyzer = null;
		startSites.clear();
		newThreadExprs.clear();
		threadCreationSitesMulti.clear();
		threadCreationSitesSingle.clear();
		unstable();
	}

	/**
	 * Sets the object flow analyzer to be used for analysis.
	 * 
	 * @param objFlowAnalyzer is the object flow analyzer.
	 * @pre objFlowAnalyzer != null
	 */
	@Override public void setAnalyzer(final IValueAnalyzer<Value> objFlowAnalyzer) {
		analyzer = objFlowAnalyzer;
	}

	/**
	 * @see Object#toString()
	 */
	@Override public String toString() {
		final StringBuffer _result = new StringBuffer();
		final List<String> _l = new ArrayList<String>();

		_result.append("Total number of threads: " + thread2methods.size() + "\n");

		final List<Triple<?, ?, ?>> _temp1 = new ArrayList<Triple<?, ?, ?>>();
		_temp1.addAll(thread2methods.keySet());
		Collections.<Triple<?, ?, ?>> sort(_temp1, ToStringBasedComparator.getComparator());

		for (final Iterator<Triple<?, ?, ?>> _i = _temp1.iterator(); _i.hasNext();) {
			final Triple<?, ?, ?> _triple = _i.next();
			_result.append("\n" + _triple.getFirst() + "@" + _triple.getSecond() + "#" + _triple.getThird() + " ["
					+ thread2methods.get(_triple).size() + "]\n");
			_l.clear();

			for (final Iterator<SootMethod> _j = thread2methods.get(_triple).iterator(); _j.hasNext();) {
				final SootMethod _sm = _j.next();
				_l.add(_sm.getSignature());
			}
			Collections.sort(_l);

			for (final Iterator<String> _j = _l.iterator(); _j.hasNext();) {
				_result.append("\t" + _j.next() + "\n");
			}
		}

		_result.append("Method to thread mapping: \n");
		final List<SootMethod> _t = new ArrayList<SootMethod>(method2threadCreationSite.keySet());
		Collections.sort(_t, ToStringBasedComparator.getComparator());

		for (final Iterator<SootMethod> _i = _t.iterator(); _i.hasNext();) {
			final SootMethod _sm = _i.next();
			_result.append("\n" + _sm.toString() + " [" + ((Collection) method2threadCreationSite.get(_sm)).size() + "]\n");
			_l.clear();

			for (final Iterator<Triple<InvokeStmt, SootMethod, SootClass>> _j = method2threadCreationSite.get(_sm).iterator(); _j
					.hasNext();) {
				final Triple<InvokeStmt, SootMethod, SootClass> _s = _j.next();
				_l.add(_s.toString());
			}
			Collections.sort(_l);

			for (final Iterator<String> _j = _l.iterator(); _j.hasNext();) {
				_result.append("\t" + _j.next() + "\n");
			}
		}

		int _count = 1;
		_result.append("\nThread mapping: [" + getAllocationSites().size() + "]\n");

		for (final Iterator<Pair<AssignStmt, SootMethod>> _j = getAllocationSites().iterator(); _j.hasNext();) {
			final Pair<AssignStmt, SootMethod> _element = _j.next();
			final String _tid = "T" + _count++;

			_result.append(_tid + " -> " + _element.getFirst() + "@" + _element.getSecond() + "\n");
		}

		_result.append("\nThread entry points: [" + threadEntryPoints.size() + "]\n");
		_result.append(CollectionUtils.prettyPrint(threadEntryPoints));

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
	 * Retrieves the runnable methods in the given class
	 * 
	 * @param clazz of interest.
	 * @return a collection of runnable methods in the given class.
	 */
	@NonNull @NonNullContainer Collection<SootMethod> getRunnableMethods(@NonNull final SootClass clazz) {
		final Collection<Value> _runnables = getRunnables(clazz);
		final IEnvironment _env = analyzer.getEnvironment();
		final Collection<SootMethod> _result = new HashSet<SootMethod>(_runnables.size());
		for (final Iterator<Value> _j = IteratorUtils.filteredIterator(_runnables.iterator(),
				SootPredicatesAndTransformers.NEW_EXPR_PREDICATE); _j.hasNext();) {
			final NewExpr _temp = (NewExpr) _j.next();
			final SootClass _runnable = _env.getClass((_temp.getBaseType()).getClassName());
			final SootMethod _entryPoint = getRunMethod(_runnable);
			_result.add(_entryPoint);
		}
		return _result;
	}

	/**
	 * Calculates thread call graph.
	 * 
	 * @throws RuntimeException when the system being analyzed is not type-safe.
	 */
	private void calculateThreadCallGraph() throws RuntimeException {
		// capture the run call-site in Thread.start method
		final IEnvironment _env = analyzer.getEnvironment();
		final SootClass _threadClass = _env.getClass(JAVA_LANG_THREAD);
		final SootMethod _startMethod = _threadClass.getMethodByName(START_METHOD_NAME);
		final Context _ctxt = new Context();
		_ctxt.setRootMethod(_startMethod);

		final Collection<Value> _values = analyzer.getValuesForThis(_ctxt);
		final Map<SootClass, Collection<SootMethod>> _class2runCallees = new HashMap<SootClass, Collection<SootMethod>>();
		final Collection<SootMethod> _runnables = getRunnableMethods(_threadClass);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Thread expressions are: " + _values);
			LOGGER.debug("Runnables are: " + _runnables);
		}

		for (final Iterator<Value> _i = IteratorUtils.filteredIterator(_values.iterator(),
				SootPredicatesAndTransformers.NEW_EXPR_PREDICATE); _i.hasNext();) {
			final NewExpr _value = (NewExpr) _i.next();
			final SootClass _sc = _env.getClass(_value.getBaseType().getClassName());
			Collection<SootMethod> _methods;

			if (!_class2runCallees.containsKey(_sc)) {
				boolean _flag = false;

				final SootClass _scTemp = Util.getDeclaringClass(_sc, RUN_METHOD_NAME, Collections.<Type> emptyList(),
						VoidType.v());

				if (_scTemp != null) {
					_flag = _scTemp.getName().equals(JAVA_LANG_THREAD);
				} else {
					LOGGER.error("How can there be a descendent of java.lang.Thread without access to run() method.");
					throw new RuntimeException("run() method is unavailable via " + _sc
							+ " even though it is a descendent of java.lang.Thread.");
				}

				if (_flag) {
					_methods = new HashSet<SootMethod>();

					for (final Iterator<SootMethod> _j = _runnables.iterator(); _j.hasNext();) {
						final SootMethod _entryPoint = _j.next();
						threadEntryPoints.add(_entryPoint);
						_methods.addAll(transitiveThreadCallClosure(_entryPoint));
					}
				} else {
					final SootMethod _entryPoint = getRunMethod(_sc);
					threadEntryPoints.add(_entryPoint);
					_methods = transitiveThreadCallClosure(_entryPoint);
				}

				_class2runCallees.put(_sc, _methods);
			}
		}

		final Collection<CallTriple> _callTriples = cgi.getCallers(_startMethod);
		final Iterator<CallTriple> _i = _callTriples.iterator();
		final int _iEnd = _callTriples.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final CallTriple _ctrp = _i.next();
			final SootMethod _caller = _ctrp.getMethod();
			_ctxt.setRootMethod(_caller);

			final InvokeStmt _callStmt = (InvokeStmt) _ctrp.getStmt();
			_ctxt.setStmt(_callStmt);

			final VirtualInvokeExpr _virtualInvokeExpr = (VirtualInvokeExpr) _callStmt.getInvokeExpr();
			_ctxt.setProgramPoint(_virtualInvokeExpr.getBaseBox());

			final Collection<Value> _baseValues = analyzer.getValues(_virtualInvokeExpr.getBase(), _ctxt);

			for (final Iterator<Value> _j = IteratorUtils.filteredIterator(_baseValues.iterator(),
					SootPredicatesAndTransformers.NEW_EXPR_PREDICATE); _j.hasNext();) {
				final NewExpr _value = (NewExpr) _j.next();
				final SootClass _sc = _env.getClass(_value.getBaseType().getClassName());
				if (_class2runCallees.containsKey(_sc)) {
					final Collection<SootMethod> _methods = _class2runCallees.get(_sc);
					final Triple<InvokeStmt, SootMethod, SootClass> _thread = new Triple<InvokeStmt, SootMethod, SootClass>(
							_callStmt, _caller, _sc);
					MapUtils.putAllIntoCollectionInMap(thread2methods, _thread, _methods);

					for (final Iterator<SootMethod> _k = _methods.iterator(); _k.hasNext();) {
						final SootMethod _sm = _k.next();
						MapUtils.putIntoCollectionInMap(method2threadCreationSite, _sm, _thread);
					}
				}
			}
		}
	}

	/**
	 * Considers the property that a thread allocation site may be executed multiple times.
	 */
	private void considerMultipleExecutions() {
		final Collection<Pair<? extends Object, ? extends Object>> _tassBak = new HashSet<Pair<? extends Object, ? extends Object>>(
				threadCreationSitesSingle);

		// Mark any thread allocation site that will be executed multiple times via a loop or call graph SCC
		// as creating multiple threads. The execution considers entire call chain to the entry method.
		for (final Iterator<Pair<? extends Object, ? extends Object>> _i = _tassBak.iterator(); _i.hasNext();) {
			final Pair<? extends Object, ? extends Object> _pair = _i.next();
			final Object _o = _pair.getSecond();

			if (_o instanceof SootMethod) {
				final SootMethod _encloser = (SootMethod) _o;

				if (cfgAnalysis.executedMultipleTimes(_encloser)) {
					threadCreationSitesSingle.remove(_pair);
					threadCreationSitesMulti.add(_pair);
				}
			}
		}

		final Collection<SootMethod> _multiExecMethods = new HashSet<SootMethod>();

		// Collect methods executed in threads which are created at sites that create more than one thread. These methods
		// will be executed multiple times.
		for (final Iterator<Pair<? extends Object, ? extends Object>> _i = threadCreationSitesMulti.iterator(); _i.hasNext();) {
			final Pair<? extends Object, ? extends Object> _pair = _i.next();
			final Object _o = _pair.getSecond();

			if (_o instanceof SootMethod) {
				_multiExecMethods.addAll(getExecutedMethods((InvokeStmt) _pair.getFirst(), (SootMethod) _o));
			}
		}
		_tassBak.clear();
		_tassBak.addAll(threadCreationSitesSingle);

		// filter the thread allocation site sets based on multiExecMethods.
		for (final Iterator<Pair<? extends Object, ? extends Object>> _i = _tassBak.iterator(); _i.hasNext();) {
			final Pair<? extends Object, ? extends Object> _pair = _i.next();
			final Object _encloser = _pair.getSecond();

			if (_multiExecMethods.contains(_encloser)) {
				threadCreationSitesSingle.remove(_pair);
				threadCreationSitesMulti.add(_pair);
			}
		}
	}

	/**
	 * Retrieves a method with the signature "void run()" in the given class, if it exists.
	 * 
	 * @param threadClass of interest
	 * @return the run method
	 * @pre threadClass != null
	 */
	private SootMethod getRunMethod(final SootClass threadClass) {
		return threadClass.getMethod(RUN_METHOD_NAME, Collections.EMPTY_LIST, VoidType.v());
	}

	/**
	 * Retrieves the runnable implementations that get executed in the <code>run()</code> method of
	 * <code>java.lang.Thread</code>.
	 * 
	 * @param threadClass is the <code>java.lang.Thread</code> class.
	 * @return a collection of object flow values.
	 * @pre threadClass != null
	 * @post result != null
	 */
	private Collection<Value> getRunnables(final SootClass threadClass) {
		Collection<Value> _result = Collections.emptySet();
		final SootMethod _threadRunMethod = getRunMethod(threadClass);

		final Iterator<Stmt> _units = _threadRunMethod.retrieveActiveBody().getUnits().iterator();
		for (final Iterator<Stmt> _iter = IteratorUtils.filteredIterator(_units,
				SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE); _iter.hasNext();) {
			final Stmt _stmt = _iter.next();
			final InvokeExpr _expr = _stmt.getInvokeExpr();
			final SootMethod _invokedMethod = _expr.getMethod();

			if (_expr instanceof InterfaceInvokeExpr && _invokedMethod.getName().equals(RUN_METHOD_NAME)
					&& _invokedMethod.getDeclaringClass().getName().equals(JAVA_LANG_RUNNABLE)) {
				final InterfaceInvokeExpr _iExpr = (InterfaceInvokeExpr) _expr;
				final Context _context = new Context();
				_context.setRootMethod(_threadRunMethod);
				_context.setStmt(_stmt);
				_context.setProgramPoint(_iExpr.getBaseBox());
				_result = analyzer.getValues(_iExpr.getBase(), _context);
				break;
			}
		}
		return _result;
	}

	/**
	 * Injects information for the "syntactically" non-occurring main thread.
	 */
	private void injectMainThread() {
		/*
		 * Note, main threads in the system are non-existent in terms of allocation sites. So, we create a hypothetical expr
		 * with no associated statement and method and use that to create a NewExprTriple. This triple represents the thread
		 * allocation site of main threads. The triple would have null for the method and statement, but a NewExpr whose type
		 * name starts with "MainThread:". Also, class initializers (<clinit> methods) need to be treated differently. They
		 * execute either in the current thread or another thread. However, the interesting property is that class
		 * initializers are executed only once and by atmost one thread (which can be any thread) via locking. Hence, for
		 * thread-sensitive analyses this matters. For other cases, we can assume that there is an arbitrary but same thread
		 * in the VM that just executes these initializers. Another alternative is that each initializer is executed in a new
		 * dedicated thread. Like in the case of main threads we will use similar triples except "ClassInitThread:" is used as
		 * the type name prefix. We adapt the first approach of one dedicated thread executes all intializers in the VM.
		 * Hence, all <clinit> methods are associated with an "ClassInitThread:".
		 */
		for (final Iterator<SootMethod> _i = cgi.getEntryMethods().iterator(); _i.hasNext();) {
			final SootMethod _head = _i.next();
			final Pair<InvokeStmt, SootMethod> _threadCreationSite;
			final Pair<AssignStmt, SootMethod> _threadAllocationSite;
			final Triple<InvokeStmt, SootMethod, SootClass> _thread;

			if (_head.getName().equals("<clinit>")) {
				_threadCreationSite = ApplicationInitInfoFactory.getClassInitExecutingThreadCreationSite();
				_threadAllocationSite = ApplicationInitInfoFactory.getClassInitExecutingThreadAllocationSite();
				_thread = ApplicationInitInfoFactory.getClassInitExecutingThread();
			} else {
				_threadCreationSite = ApplicationInitInfoFactory.getApplicationStartingThreadCreationSite();
				_threadAllocationSite = ApplicationInitInfoFactory.getApplicationStartingThreadAllocationSite();
				_thread = ApplicationInitInfoFactory.getApplicationStartingThread();
			}

			newThreadExprs.add(_threadAllocationSite);
			threadCreationSitesSingle.add(_threadCreationSite);

			final Collection<SootMethod> _methods = transitiveThreadCallClosure(_head);
			MapUtils.putAllIntoCollectionInMap(thread2methods, _thread, _methods);
			threadEntryPoints.add(_head);

			for (final Iterator<SootMethod> _j = _methods.iterator(); _j.hasNext();) {
				final SootMethod _sm = _j.next();
				MapUtils.putIntoCollectionInMap(method2threadCreationSite, _sm, _thread);
			}
		}
	}

	/**
	 * Processes new expressoins.
	 * 
	 * @param context in which the new expression occurs.
	 * @param env to be used.
	 * @param newExpr to be processed.
	 * @throws RuntimeException when there is a glitch in the system being analyzed is not type-safe.
	 * @pre context != null and env != null and newExpr != null
	 * @pre context.getStmt() != null and context.getCurrentMethod() != null
	 * @pre context.getStmt().isOclKindOf(AssignStmt)
	 */
	private void processNewExpr(final Context context, final IEnvironment env, final NewExpr newExpr) throws RuntimeException {
		final SootClass _clazz = env.getClass(newExpr.getBaseType().getClassName());

		// collect the new expressions which create Thread objects.
		if (Util.isDescendentOf(_clazz, JAVA_LANG_THREAD)) {
			final SootClass _temp = Util.getDeclaringClass(_clazz, START_METHOD_NAME, Collections.<Type> emptyList(),
					VoidType.v());

			if (_temp != null && _temp.getName().equals(JAVA_LANG_THREAD)) {
				final AssignStmt _stmt = (AssignStmt) context.getStmt();
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
	 * @pre context != null and env != null and invokeExpr != null
	 * @pre context.getStmt() != null and context.getCurrentMethod() != null
	 */
	private void processVirtualInvokeExpr(final Context context, final VirtualInvokeExpr invokeExpr) {
		final RefLikeType _rlt = (RefLikeType) invokeExpr.getBase().getType();

		if (_rlt instanceof RefType) {
			final SootMethod _method = invokeExpr.getMethod();

			if (Util.isStartMethod(_method)) {
				final SootMethod _caller = context.getCurrentMethod();
				final InvokeStmt _callStmt = (InvokeStmt) context.getStmt();
				final Collection<SootMethod> _callees = cgi.getCallees(invokeExpr, context);
				final Iterator<SootMethod> _i = _callees.iterator();
				final int _iEnd = _callees.size();

				for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
					final SootMethod _callee = _i.next();

					if (Util.isStartMethod(_callee)) {
						final Pair<InvokeStmt, SootMethod> _callPair = pairMgr.getPair(_callStmt, _caller);
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
		final Collection<Pair<InvokeStmt, SootMethod>> _temp = new HashSet<Pair<InvokeStmt, SootMethod>>();
		final Collection<SootMethod> _reachables = cgi.getReachableMethods();

		for (final Iterator<Pair<InvokeStmt, SootMethod>> _i = startSites.iterator(); _i.hasNext();) {
			final Pair<InvokeStmt, SootMethod> _callPair = _i.next();

			if (!_reachables.contains(_callPair.getSecond())) {
				_temp.add(_callPair);
			}
		}
		startSites.removeAll(_temp);
	}

	/**
	 * Calculates the transitive closure of methods called from the given method. The inclusion constraint for the closure is
	 * that the method cannot be an instance of <code>java.lang.Thread.start()</code>.
	 * 
	 * @param starterMethod is the method from where the closure calculation starts.
	 * @return a collection of <code>SootMethod</code>s occurring the call closure.
	 * @pre starterMethod != null
	 * @post result != null and result.isOclKindOf(Collection(SootMethod))
	 */
	private Collection<SootMethod> transitiveThreadCallClosure(final SootMethod starterMethod) {
		final Collection<SootMethod> _result = new HashSet<SootMethod>();
		final IWorkBag<SootMethod> _wb = new HistoryAwareFIFOWorkBag<SootMethod>(_result);
		_wb.addWork(starterMethod);
		_result.add(starterMethod);

		while (_wb.hasWork()) {
			final SootMethod _sm = _wb.getWork();

			if (!Util.isStartMethod(_sm)) {
				final Collection<CallTriple> _callees = cgi.getCallees(_sm);

				for (final Iterator<CallTriple> _i = _callees.iterator(); _i.hasNext();) {
					final CallTriple _ctrp = _i.next();
					final SootMethod _temp = _ctrp.getMethod();

					_wb.addWorkNoDuplicates(_temp);
				}
			}
		}
		return _result;
	}
}

// End of File
