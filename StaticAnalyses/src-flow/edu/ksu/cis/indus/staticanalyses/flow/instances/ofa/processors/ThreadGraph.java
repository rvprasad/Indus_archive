
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.common.CollectionsModifier;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;

import soot.jimple.Jimple;
import soot.jimple.NewExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * This class provides information regarding threads that occur in the system.  It can provide information such as the
 * possible threads in the system and the methods invoked in these threads.
 * 
 * <p>
 * Main threads do not have allocation sites.  This is addressed by associating each main thread with a
 * <code>NewExprTriple</code> with <code>null</code> for statement and method, but  a <code>NewExpr</code> which creates a
 * type with the name that starts with "MainThread:".  The rest of the name is a number followed by the signature of the
 * starting method in the thread. For example, "MainThread:2:[signature]" represents the second mainthread with the run
 * method given by signature.  Likewise, we assume that all class initializers are executed by a dedicated thread whose type
 * name would start with "ClassInitThread:".
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
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ThreadGraph.class);

	/**
	 * This indicates if the processor has stabilized.  If so, it is safe to query this object for information. By default,
	 * this field is initialized to indicate that the processor is in a stable state.  The subclasses will need to toggle it
	 * suitably.
	 */
	protected boolean stable = true;

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
	private final Collection threadAllocSitesMulti;

	/**
	 * A collection of thread allocation sites which are not executed from within a loop or a SCC in the call graph.  This
	 * also includes any allocation sites reachable from a method executed in a loop. This is the dual of
	 * <code>threadAllocSitesMulti.</code>
	 *
	 * @invariant threadAllocSitesSingle.oclIsKindOf(Triple(SootMethod, Stmt, NewExpr)))
	 */
	private final Collection threadAllocSitesSingle;

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
	private final Map method2threads = new HashMap();

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
	 * Creates a new ThreadGraph object.
	 *
	 * @param callGraph provides call graph information.
	 * @param cfa provides control-flow information.
	 */
	public ThreadGraph(final ICallGraphInfo callGraph, final CFGAnalysis cfa) {
		this.cgi = callGraph;
		threadAllocSitesSingle = new HashSet();
		threadAllocSitesMulti = new HashSet();
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
	 * @see edu.ksu.cis.indus.interfaces.IThreadGraphInfo#getExecutedMethods(NewExpr,Context)
	 */
	public Collection getExecutedMethods(final NewExpr ne, final Context ctxt) {
		Set _result = (Set) thread2methods.get(new NewExprTriple(ctxt.getCurrentMethod(), ctxt.getStmt(), ne));

		if (_result == null) {
			_result = Collections.EMPTY_SET;
		} else {
			_result = Collections.unmodifiableSet(_result);
		}
		return _result;
	}

	/**
	 * Please refer to class documentation for important information.
	 *
	 * @param sm is the method of interest.
	 *
	 * @return a collection of threads in which <code>sm</code> executes.
	 *
	 * @pre sm != null
	 * @post result->forall(o | o.getExpr().getType().getClassName().indexOf("MainThread") == 0 implies (o.getStmt() = null
	 * 		 and o.getSootMethod() = null))
	 * @post result.oclIsKindOf(Collection(NewExprTriple))
	 *
	 * @see edu.ksu.cis.indus.interfaces.IThreadGraphInfo#getExecutionThreads(SootMethod)
	 */
	public Collection getExecutionThreads(final SootMethod sm) {
		Set _result = (Set) method2threads.get(sm);

		if (_result == null) {
			_result = Collections.EMPTY_SET;
		} else {
			_result = Collections.unmodifiableSet(_result);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IThreadGraphInfo#getMultiThreadAllocSites()
	 */
	public Collection getMultiThreadAllocSites() {
		return Collections.unmodifiableCollection(threadAllocSitesMulti);
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IStatus#isStable()
	 */
	public boolean isStable() {
		return stable;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IThreadGraphInfo#getStartSites()
	 */
	public Collection getStartSites() {
		return Collections.unmodifiableCollection(startSites);
	}

	/**
	 * Called by the post processing controller on encountering <code>NewExpr</code> and <code>VirtualInvokeExpr</code>
	 * values in the system.
	 *
	 * @param vBox that was encountered and needs processing.
	 * @param context in which the value was encountered.
	 *
	 * @throws RuntimeException when there is a glitch in the system being analyzed is not type-safe.
	 *
	 * @pre value != null and context != null
	 */
	public void callback(final ValueBox vBox, final Context context) {
		final IEnvironment _env = analyzer.getEnvironment();
		final Value _value = vBox.getValue();

		if (_value instanceof NewExpr) {
			final NewExpr _ne = (NewExpr) _value;
			final SootClass _clazz = _env.getClass(_ne.getBaseType().getClassName());

			// collect the new expressions which create Thread objects.
			if (Util.isDescendentOf(_clazz, "java.lang.Thread")) {
				final SootClass _temp = Util.getDeclaringClass(_clazz, "start", Collections.EMPTY_LIST, VoidType.v());

				if (_temp != null && _temp.getName().equals("java.lang.Thread")) {
					final Stmt _stmt = context.getStmt();
					final SootMethod _sm = context.getCurrentMethod();
					final NewExprTriple _o = new NewExprTriple(_sm, _stmt, _ne);
					newThreadExprs.add(_o);

					if (cfgAnalysis.checkForLoopEnclosedNewExpr(_stmt, _sm)) {
						threadAllocSitesMulti.add(_o);
					} else {
						threadAllocSitesSingle.add(_o);
					}
				} else {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("How can there be a descendent of java.lang.Thread without access to start() method.");
					}
					throw new RuntimeException("start() method is unavailable via " + _clazz
						+ " even though it is a descendent of java.lang.Thread.");
				}
			}
		} else if (_value instanceof VirtualInvokeExpr) {
			final VirtualInvokeExpr _ve = (VirtualInvokeExpr) _value;
			final RefLikeType _rlt = (RefLikeType) _ve.getBase().getType();
			SootClass _clazz = null;

			if (_rlt instanceof RefType) {
				_clazz = _env.getClass(((RefType) _rlt).getClassName());

				final SootMethod _method = _ve.getMethod();

				if (Util.isDescendentOf(_clazz, "java.lang.Thread")
					  && _method.getName().equals("start")
					  && _method.getReturnType() instanceof VoidType
					  && _method.getParameterCount() == 0) {
					final SootClass _temp = Util.getDeclaringClass(_clazz, "start", Collections.EMPTY_LIST, VoidType.v());

					if (_temp != null && _temp.getName().equals("java.lang.Thread")) {
						startSites.add(new CallTriple(context.getCurrentMethod(), context.getStmt(), _ve));
					} else {
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn(
								"How can there be a descendent class of java.lang.Thread without access to start() method.");
						}
						throw new RuntimeException("start() method is unavailable via " + _clazz
							+ " even though it is a descendent of java.lang.Thread.");
					}
				}
			}
		}
	}

	/**
	 * Consolidates the thread graph information before it is available to the application.
	 *
	 * @throws RuntimeException when there is a glitch in the system being analyzed is not type-safe.
	 */
	public void consolidate() {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: thread graph consolidation");
		}

		final long _start = System.currentTimeMillis();

		// capture the run call-site in Thread.start method
		final IEnvironment _env = analyzer.getEnvironment();
		final SootClass _threadClass = _env.getClass("java.lang.Thread");

		final SootMethod _startMethod = _threadClass.getMethodByName("start");
		final Context _ctxt = new Context();
		_ctxt.setRootMethod(_startMethod);

		final Collection _values = analyzer.getValuesForThis(_ctxt);
		final Map _class2runCallees = new HashMap();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("New thread expressions are: " + _values);
		}

		for (final Iterator _i = _values.iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (!(_o instanceof NewExpr)) {
				continue;
			}

			final NewExpr _value = (NewExpr) _o;
			final SootClass _sc = _env.getClass(_value.getBaseType().getClassName());
			Collection _methods;

			if (!_class2runCallees.containsKey(_sc)) {
				boolean _flag = false;

				SootClass _scTemp = Util.getDeclaringClass(_sc, "run", Collections.EMPTY_LIST, VoidType.v());

				if (_scTemp != null) {
					_flag = _scTemp.getName().equals("java.lang.Thread");
				} else {
					LOGGER.error("How can there be a descendent of java.lang.Thread without access to run() method.");
					throw new RuntimeException("run() method is unavailable via " + _sc
						+ " even though it is a descendent of java.lang.Thread.");
				}

				if (_flag) {
					// Here we use the knowledge of only one target object is associated with a thread object.
					final Collection _t = new ArrayList();
					_t.add(_value);
					_methods = new HashSet();

					// It is possible that the same thread allocation site(loop enclosed) be associated with multiple target 
					// object 
					for (final Iterator _j = analyzer.getValues(_threadClass.getFieldByName("target"), _t).iterator();
						  _j.hasNext();) {
						final Object _obj = _j.next();

						if (_t instanceof NewExpr) {
							NewExpr temp = (NewExpr) _obj;
							_scTemp = _env.getClass((temp.getBaseType()).getClassName());
							_methods.addAll(transitiveThreadCallClosure(_scTemp.getMethod("run", Collections.EMPTY_LIST,
										VoidType.v())));
						}
					}
				} else {
					_methods = transitiveThreadCallClosure(_sc.getMethod("run", Collections.EMPTY_LIST, VoidType.v()));
				}

				_class2runCallees.put(_sc, _methods);
			}

			final NewExprTriple _thread = extractNewExprTripleFor(_value);

			if (_thread == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("thread cannot be null. This can happen if there are not "
						+ "threads other than the main thread in the system. [" + _value + "]");
				}
				continue;
			}

			_methods = (Collection) _class2runCallees.get(_sc);
			thread2methods.put(_thread, _methods);

			for (final Iterator _j = _methods.iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();
				CollectionsModifier.putIntoCollectionInMap(method2threads, _sm, _value, new HashSet());
			}
		}

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
		final Collection _heads = cgi.getHeads();
		int _mainThreadCount = 1;
		final NewExprTriple _classInitThread =
			new NewExprTriple(null, null, Jimple.v().newNewExpr(RefType.v("ClassInitThread:1:_jvm_")));

		for (final Iterator _i = _heads.iterator(); _i.hasNext();) {
			final SootMethod _head = (SootMethod) _i.next();
			NewExprTriple _thread = null;

			if (_head.getName().equals("<clinit>")) {
				_thread = _classInitThread;
			} else {
				_thread =
					new NewExprTriple(null, null,
						Jimple.v().newNewExpr(RefType.v("MainThread:" + _mainThreadCount++ + ":" + _head.getSignature())));
			}

			newThreadExprs.add(_thread);

			Collection methods = transitiveThreadCallClosure(_head);
			thread2methods.put(_thread, methods);

			for (final Iterator _j = methods.iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();
				CollectionsModifier.putIntoCollectionInMap(method2threads, _sm, _thread, new HashSet());
			}
		}

		// prune the startSites such that it only contains reachable start sites.
		final Collection _temp = new HashSet();
		final Collection _reachables = cgi.getReachableMethods();

		for (final Iterator _i = startSites.iterator(); _i.hasNext();) {
			final CallTriple _ctrp = (CallTriple) _i.next();

			if (!_reachables.contains(_ctrp.getMethod())) {
				_temp.add(_ctrp);
			}
		}
		startSites.removeAll(_temp);

		// Consolidate information pertaining to execution frequency of thread allocation sites.
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("consolidate thread allocation site execution frequency information.");
		}

		Collection tassBak = new HashSet(threadAllocSitesSingle);

		// Mark any thread allocation site that will be executed multiple times via a loop or call graph SCC 
		// as creating multiple threads.  The execution considers entire call chain to the entry method.
		for (final Iterator _i = tassBak.iterator(); _i.hasNext();) {
			final NewExprTriple _trp = (NewExprTriple) _i.next();
			final SootMethod _encloser = _trp.getMethod();

			if (cfgAnalysis.executedMultipleTimes(_encloser)) {
				threadAllocSitesSingle.remove(_trp);
				threadAllocSitesMulti.add(_trp);
			}
		}

		final Collection _multiExecMethods = new HashSet();

		// Collect methods executed in threads which are created at sites that create more than one thread.  These methods 
		// will be executed multiple times.
		for (final Iterator _i = threadAllocSitesMulti.iterator(); _i.hasNext();) {
			final NewExprTriple _ntrp = (NewExprTriple) _i.next();
			_ctxt.setRootMethod(_ntrp.getMethod());
			_ctxt.setStmt(_ntrp.getStmt());
			_multiExecMethods.addAll(getExecutedMethods(_ntrp.getExpr(), _ctxt));
		}
		tassBak.clear();
		tassBak.addAll(threadAllocSitesSingle);

		// filter the thread allocation site sets based on multiExecMethods.
		for (final Iterator _i = tassBak.iterator(); _i.hasNext();) {
			final NewExprTriple _trp = (NewExprTriple) _i.next();
			final SootMethod _encloser = _trp.getMethod();

			if (_multiExecMethods.contains(_encloser)) {
				threadAllocSitesSingle.remove(_trp);
				threadAllocSitesMulti.add(_trp);
			}
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: equivalence class based escape analysis consolidation");
		}

		final long _stop = System.currentTimeMillis();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: thread graph consolidation");
			LOGGER.info("TIMING: thread graph consolidation took " + (_stop - _start) + "ms.");
		}
	}

	/**
	 * Provides a stringized representation of the thread graph.
	 *
	 * @return stringized representation of the thread graph.
	 */
	public String dumpGraph() {
		final StringBuffer _result = new StringBuffer();
		final List _l = new ArrayList();

		_result.append("Total number of threads: " + thread2methods.size() + "\n");

		final List _temp1 = new ArrayList();
		_temp1.addAll(thread2methods.keySet());
		Collections.sort(_temp1,
			new Comparator() {
				public int compare(Object o1, Object o2) {
					NewExprTriple _ne = (NewExprTriple) o1;
					final String _s1 = _ne.getStmt() + "@" + _ne.getMethod() + "->" + _ne.getExpr();
					_ne = (NewExprTriple) o2;
					return _s1.compareTo(_ne.getStmt() + "@" + _ne.getMethod() + "->" + _ne.getExpr());
				}
			});

		for (final Iterator _i = _temp1.iterator(); _i.hasNext();) {
			final NewExprTriple _net = (NewExprTriple) _i.next();

			if (_net.getMethod() == null) {
				_result.append("\n" + _net.getExpr().getType() + "\n");
			} else {
				_result.append("\n" + _net.getStmt() + "@" + _net.getMethod() + "->" + _net.getExpr() + "\n");
			}

			_l.clear();

			for (final Iterator _j = ((Collection) thread2methods.get(_net)).iterator(); _j.hasNext();) {
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
			final NewExprTriple _element = (NewExprTriple) _j.next();
			final String _tid = "T" + _count++;

			if (_element.getMethod() == null) {
				_result.append(_tid + " -> " + _element.getExpr().getType() + "\n");
			} else {
				_result.append(_tid + " -> " + _element.getStmt() + "@" + _element.getMethod() + "\n");
			}
		}

		return _result.toString();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		stable = false;
		ppc.register(NewExpr.class, this);
		ppc.register(VirtualInvokeExpr.class, this);
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
		threadAllocSitesMulti.clear();
		threadAllocSitesSingle.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(NewExpr.class, this);
		ppc.unregister(VirtualInvokeExpr.class, this);
		stable = true;
	}

	/**
	 * Given an allocation expression it returns the corresponding <code>NewExprTriple</code> object for it.
	 *
	 * @param ne is the allocation expression.
	 *
	 * @return the triple corresponding to the allocation expression.
	 *
	 * @pre ne != null
	 */
	private NewExprTriple extractNewExprTripleFor(final NewExpr ne) {
		NewExprTriple _result = null;

		for (final Iterator _i = newThreadExprs.iterator(); _i.hasNext();) {
			final NewExprTriple _ntrp = (NewExprTriple) _i.next();

			if (_ntrp.getExpr() == ne) {
				_result = _ntrp;
				break;
			}
		}
		return _result;
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

			if (_sm.getName().equals("start")
				  && _sm.getDeclaringClass().getName().equals("java.lang.Thread")
				  && _sm.getParameterCount() == 0
				  && _sm.getReturnType().equals(VoidType.v())) {
				continue;
			}

			final Collection _callees = cgi.getCallees(_sm);

			for (final Iterator _i = _callees.iterator(); _i.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _i.next();
				final SootMethod _temp = _ctrp.getMethod();

				_wb.addWorkNoDuplicates(_temp);
			}
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.28  2004/03/04 14:06:40  venku
   - nulls should not be considered as new expressions. FIXED.
   Revision 1.27  2004/02/25 00:04:02  venku
   - documenation.
   Revision 1.26  2004/01/25 15:12:03  venku
   - formatting and coding convention.
   Revision 1.25  2004/01/21 13:30:40  venku
   - log formatting.
   Revision 1.24  2004/01/21 01:34:56  venku
   - logging.
   Revision 1.23  2004/01/06 00:17:01  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.22  2003/12/14 20:21:10  venku
   - logging.
   Revision 1.21  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.20  2003/12/09 04:22:10  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.19  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.18  2003/12/08 12:15:59  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.17  2003/12/02 09:42:38  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.16  2003/11/26 02:55:45  venku
   - now handles clinit in a more robust way.
   Revision 1.15  2003/11/17 16:47:50  venku
    - class cast exception due to change from value to valuebox in callbacks.
   Revision 1.14  2003/11/17 15:42:46  venku
   - changed the signature of callback(Value,..) to callback(ValueBox,..)
   Revision 1.13  2003/11/10 03:17:18  venku
   - renamed AbstractProcessor to AbstractValueAnalyzerBasedProcessor.
   - ripple effect.
   Revision 1.12  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.11  2003/11/05 09:32:48  venku
   - ripple effect of splitting Workbag.
   Revision 1.10  2003/09/29 05:52:44  venku
   - added more info to the dump.
   Revision 1.9  2003/09/28 07:33:36  venku
   - simple optimization.
   Revision 1.8  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.7  2003/09/08 02:21:53  venku
   - supports a new method to extract thread allocation sites
     which may be executed multiple times.
   Revision 1.6  2003/09/01 07:50:36  venku
   - getField() required signature and caused the program to crash.
     getFieldByName() just requires the name and fixed the issue. FIXED.
   - In OFA, NullConstant objects can flow into primaries of invocation
     sites.  Hence, iteration on values needs to type check for NewExpr. FIXED.
   Revision 1.5  2003/08/25 09:31:39  venku
   Enabled reset() support for these classes.
   Revision 1.4  2003/08/21 03:43:56  venku
   Ripple effect of adding IStatus.
   Revision 1.3  2003/08/13 08:29:40  venku
   Spruced up documentation and specification.
   Revision 1.2  2003/08/11 04:27:33  venku
   - Ripple effect of changes to Pair
   - Ripple effect of changes to _content in Marker
   - Changes of how thread start sites are tracked in ThreadGraphInfo
   Revision 1.1  2003/08/07 06:40:25  venku
   Major:
    - Moved the package under indus umbrella.
 */
