
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

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.cfg.CFGAnalysis;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;
import edu.ksu.cis.indus.support.FIFOWorkBag;
import edu.ksu.cis.indus.support.IWorkBag;
import edu.ksu.cis.indus.support.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo#getAllocationSites()
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
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo#getExecutedMethods(NewExpr,Context)
	 */
	public Collection getExecutedMethods(final NewExpr ne, final Context ctxt) {
		Set result = (Set) thread2methods.get(new NewExprTriple(ctxt.getCurrentMethod(), ctxt.getStmt(), ne));

		if (result == null) {
			result = Collections.EMPTY_SET;
		} else {
			result = Collections.unmodifiableSet(result);
		}
		return result;
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
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo#getExecutionThreads(SootMethod)
	 */
	public Collection getExecutionThreads(final SootMethod sm) {
		Set result = (Set) method2threads.get(sm);

		if (result == null) {
			result = Collections.EMPTY_SET;
		} else {
			result = Collections.unmodifiableSet(result);
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo#getMultiThreadAllocSites()
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
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo#getStartSites()
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
		IEnvironment env = analyzer.getEnvironment();
		Value value = vBox.getValue();

		if (value instanceof NewExpr) {
			NewExpr ne = (NewExpr) value;
			SootClass clazz = env.getClass(ne.getBaseType().getClassName());

			// collect the new expressions which create Thread objects.
			if (Util.isDescendentOf(clazz, "java.lang.Thread")) {
				SootClass temp = Util.getDeclaringClass(clazz, "start", Collections.EMPTY_LIST, VoidType.v());

				if (temp != null && temp.getName().equals("java.lang.Thread")) {
					Stmt stmt = context.getStmt();
					SootMethod sm = context.getCurrentMethod();
					NewExprTriple o = new NewExprTriple(sm, stmt, ne);
					newThreadExprs.add(o);

					if (cfgAnalysis.checkForLoopEnclosedNewExpr(stmt, sm)) {
						threadAllocSitesMulti.add(o);
					} else {
						threadAllocSitesSingle.add(o);
					}
				} else {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("How can there be a descendent of java.lang.Thread without access to start() method.");
					}
					throw new RuntimeException("start() method is unavailable via " + clazz
						+ " even though it is a descendent of java.lang.Thread.");
				}
			}
		} else if (value instanceof VirtualInvokeExpr) {
			VirtualInvokeExpr ve = (VirtualInvokeExpr) value;
			RefLikeType rlt = (RefLikeType) ve.getBase().getType();
			SootClass clazz = null;

			if (rlt instanceof RefType) {
				clazz = env.getClass(((RefType) rlt).getClassName());

				SootMethod method = ve.getMethod();

				if (Util.isDescendentOf(clazz, "java.lang.Thread")
					  && method.getName().equals("start")
					  && method.getReturnType() instanceof VoidType
					  && method.getParameterCount() == 0) {
					SootClass temp = Util.getDeclaringClass(clazz, "start", Collections.EMPTY_LIST, VoidType.v());

					if (temp != null && temp.getName().equals("java.lang.Thread")) {
						startSites.add(new CallTriple(context.getCurrentMethod(), context.getStmt(), ve));
					} else {
						if (LOGGER.isWarnEnabled()) {
							LOGGER.warn(
								"How can there be a descendent class of java.lang.Thread without access to start() method.");
						}
						throw new RuntimeException("start() method is unavailable via " + clazz
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

		long start = System.currentTimeMillis();

		// capture the run call-site in Thread.start method
		IEnvironment env = analyzer.getEnvironment();
		SootClass threadClass = env.getClass("java.lang.Thread");

		SootMethod startMethod = threadClass.getMethodByName("start");
		Context ctxt = new Context();
		ctxt.setRootMethod(startMethod);

		Collection values = analyzer.getValuesForThis(ctxt);
		Map class2runCallees = new HashMap();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("New thread expressions are: " + values);
		}

		for (Iterator i = values.iterator(); i.hasNext();) {
			NewExpr value = (NewExpr) i.next();
			SootClass sc = env.getClass(value.getBaseType().getClassName());
			Collection methods;

			if (!class2runCallees.containsKey(sc)) {
				boolean flag = false;

				SootClass scTemp = Util.getDeclaringClass(sc, "run", Collections.EMPTY_LIST, VoidType.v());

				if (scTemp != null) {
					flag = scTemp.getName().equals("java.lang.Thread");
				} else {
					LOGGER.error("How can there be a descendent of java.lang.Thread without access to run() method.");
					throw new RuntimeException("run() method is unavailable via " + sc
						+ " even though it is a descendent of java.lang.Thread.");
				}

				if (flag) {
					// Here we use the knowledge of only one target object is associated with a thread object.
					Collection t = new ArrayList();
					t.add(value);
					methods = new HashSet();

					// It is possible that the same thread allocation site(loop enclosed) be associated with multiple target 
					// object 
					for (Iterator j = analyzer.getValues(threadClass.getFieldByName("target"), t).iterator(); j.hasNext();) {
						Object obj = j.next();

						if (t instanceof NewExpr) {
							NewExpr temp = (NewExpr) obj;
							scTemp = env.getClass((temp.getBaseType()).getClassName());
							methods.addAll(transitiveThreadCallClosure(scTemp.getMethod("run", Collections.EMPTY_LIST,
										VoidType.v())));
						}
					}
				} else {
					methods = transitiveThreadCallClosure(sc.getMethod("run", Collections.EMPTY_LIST, VoidType.v()));
				}

				class2runCallees.put(sc, methods);
			}

			NewExprTriple thread = extractNewExprTripleFor(value);

			if (thread == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("thread cannot be null. This can happen if there are not "
						+ "threads other than the main thread in the system." + value);
				}
				continue;
			}

			methods = (Collection) class2runCallees.get(sc);
			thread2methods.put(thread, methods);

			for (Iterator j = methods.iterator(); j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();
				Collection threads = (Collection) method2threads.get(sm);

				if (threads == null) {
					threads = new HashSet();
					method2threads.put(sm, threads);
				}
				threads.add(thread);
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
		Collection heads = cgi.getHeads();
		int mainThreadCount = 1;
		NewExprTriple classInitThread =
			new NewExprTriple(null, null, Jimple.v().newNewExpr(RefType.v("ClassInitThread:1:_jvm_")));

		for (Iterator i = heads.iterator(); i.hasNext();) {
			SootMethod head = (SootMethod) i.next();
			NewExprTriple thread = null;

			if (head.getName().equals("<clinit>")) {
				thread = classInitThread;
			} else {
				thread =
					new NewExprTriple(null, null,
						Jimple.v().newNewExpr(RefType.v("MainThread:" + mainThreadCount++ + ":" + head.getSignature())));
			}

			newThreadExprs.add(thread);

			Collection methods = transitiveThreadCallClosure(head);
			thread2methods.put(thread, methods);

			for (Iterator j = methods.iterator(); j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();
				Collection threads = (Collection) method2threads.get(sm);

				if (threads == null) {
					threads = new HashSet();
					method2threads.put(sm, threads);
				}
				threads.add(thread);
			}
		}

		// prune the startSites such that it only contains reachable start sites.
		Collection temp = new HashSet();
		Collection reachables = cgi.getReachableMethods();

		for (Iterator i = startSites.iterator(); i.hasNext();) {
			CallTriple ctrp = (CallTriple) i.next();

			if (!reachables.contains(ctrp.getMethod())) {
				temp.add(ctrp);
			}
		}
		startSites.removeAll(temp);

		// Consolidate information pertaining to execution frequency of thread allocation sites.
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("consolidate thread allocation site execution frequency information.");
		}

		Collection tassBak = new HashSet(threadAllocSitesSingle);

		// Mark any thread allocation site that will be executed multiple times via a loop or call graph SCC 
		// as creating multiple threads.  The execution considers entire call chain to the entry method.
		for (Iterator i = tassBak.iterator(); i.hasNext();) {
			NewExprTriple trp = (NewExprTriple) i.next();
			SootMethod encloser = trp.getMethod();

			if (cfgAnalysis.executedMultipleTimes(encloser)) {
				threadAllocSitesSingle.remove(trp);
				threadAllocSitesMulti.add(trp);
			}
		}

		Collection multiExecMethods = new HashSet();

		// Collect methods executed in threads which are created at sites that create more than one thread.  These methods 
		// will be executed multiple times.
		for (Iterator i = threadAllocSitesMulti.iterator(); i.hasNext();) {
			NewExprTriple ntrp = (NewExprTriple) i.next();
			ctxt.setRootMethod(ntrp.getMethod());
			ctxt.setStmt(ntrp.getStmt());
			multiExecMethods.addAll(getExecutedMethods(ntrp.getExpr(), ctxt));
		}
		tassBak.clear();
		tassBak.addAll(threadAllocSitesSingle);

		// filter the thread allocation site sets based on multiExecMethods.
		for (Iterator i = tassBak.iterator(); i.hasNext();) {
			NewExprTriple trp = (NewExprTriple) i.next();
			SootMethod encloser = trp.getMethod();

			if (multiExecMethods.contains(encloser)) {
				threadAllocSitesSingle.remove(trp);
				threadAllocSitesMulti.add(trp);
			}
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: equivalence class based escape analysis consolidation");
		}

		long stop = System.currentTimeMillis();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: thread graph consolidation");
			LOGGER.info("TIMING: thread graph consolidation took " + (stop - start) + "ms.");
		}
	}

	/**
	 * Provides a stringized representation of the thread graph.
	 *
	 * @return stringized representation of the thread graph.
	 */
	public String dumpGraph() {
		StringBuffer result = new StringBuffer();
		List l = new ArrayList();

		result.append("Total number of threads: " + thread2methods.size() + "\n");

		for (Iterator i = thread2methods.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			NewExprTriple net = (NewExprTriple) entry.getKey();

			if (net.getMethod() == null) {
				result.append("\n" + net.getExpr().getType() + "\n");
			} else {
				result.append("\n" + net.getStmt() + "@" + net.getMethod() + "->" + net.getExpr() + "\n");
			}

			l.clear();

			for (Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();
				l.add(sm.getSignature());
			}
			Collections.sort(l);

			for (Iterator j = l.iterator(); j.hasNext();) {
				result.append("\t" + j.next() + "\n");
			}
		}
		return result.toString();
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IProcessor#hookup(ProcessingController)
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
	 * @see edu.ksu.cis.indus.interfaces.IProcessor#unhook(ProcessingController)
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
		NewExprTriple result = null;

		for (Iterator i = newThreadExprs.iterator(); i.hasNext();) {
			NewExprTriple ntrp = (NewExprTriple) i.next();

			if (ntrp.getExpr() == ne) {
				result = ntrp;
				break;
			}
		}
		return result;
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
		Collection result = new HashSet();
		IWorkBag wb = new FIFOWorkBag();
		wb.addWork(starterMethod);
		result.add(starterMethod);

		while (wb.hasWork()) {
			SootMethod sm = (SootMethod) wb.getWork();

			if (sm.getName().equals("start")
				  && sm.getDeclaringClass().getName().equals("java.lang.Thread")
				  && sm.getParameterCount() == 0
				  && sm.getReturnType().equals(VoidType.v())) {
				continue;
			}

			Collection callees = cgi.getCallees(sm);

			for (Iterator i = callees.iterator(); i.hasNext();) {
				CallTriple ctrp = (CallTriple) i.next();
				SootMethod temp = ctrp.getMethod();

				if (!result.contains(temp)) {
					result.add(temp);
					wb.addWorkNoDuplicates(temp);
				}
			}
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
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
