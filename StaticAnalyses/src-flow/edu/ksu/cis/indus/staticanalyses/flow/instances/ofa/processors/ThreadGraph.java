
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import soot.ArrayType;
import soot.RefLikeType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Value;
import soot.VoidType;

import soot.jimple.Jimple;
import soot.jimple.NewExpr;
import soot.jimple.VirtualInvokeExpr;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.interfaces.IEnvironment;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractProcessor;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.Util;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * This class provides information regarding the threads that occur in the system.  It can provide information such as the
 * possible threads in the system and the methods invoked in these threads.
 * 
 * <p>
 * Main threads do not have allocation sites.  This is addressed by associating each main thread with a
 * <code>NewExprTriple</code> with <code>null</code> for statement and method, but  a <code>NewExpr</code> which creates a
 * type with the name that starts with "MainThread:".  The rest of the name is a number followed by the signature of the
 * starting method in the thread. For example, "MainThread:2:signature" represents the second mainthread with the run method
 * given by signature.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ThreadGraph
  extends AbstractProcessor
  implements IThreadGraphInfo {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ThreadGraph.class);

	/**
	 * The collection of thread allocation sites.
	 *
	 * @invariant newThreadExprs.isOclKindOf(Collection(NewExprTriple))
	 */
	private final Collection newThreadExprs = new HashSet();

	/**
	 * The collection of method invocation sites at which <code>java.lang.Thread.start()</code> is invoked.
	 */
	private final Collection startSites = new HashSet();

	/**
	 * This provides call graph information pertaining to the system.
	 */
	private final ICallGraphInfo cgi;

	/**
	 * This maps methods to thread allocation sites which create threads in which the key method is executed.
	 *
	 * @invariant method2threads.isOclKindOf(Map(SootMethod, Collection(NewExprTriple)))
	 */
	private final Map method2threads = new HashMap();

	/**
	 * This maps threads allocation sites to the methods which are executed in the created threads.
	 *
	 * @invariant thread2methods.isOclKindOf(Map(NewExprTriple, Collection(SootMethod)))
	 */
	private final Map thread2methods = new HashMap();

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private OFAnalyzer analyzer;

	/**
	 * Creates a new ThreadGraph object.
	 *
	 * @param cgi provides call graph information.
	 */
	public ThreadGraph(ICallGraphInfo cgi) {
		this.cgi = cgi;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo#getAllocationSites()
	 */
	public Collection getAllocationSites() {
		return Collections.unmodifiableCollection(newThreadExprs);
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param valueAnalyzer DOCUMENT ME!
	 */
	public void setAnalyzer(IValueAnalyzer valueAnalyzer) {
		analyzer = (OFAnalyzer)valueAnalyzer;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo#getExecutedMethods( soot.jimple.NewExpr,
	 * 		edu.ksu.cis.indus.staticanalyses.flow.Context)
	 */
	public Collection getExecutedMethods(NewExpr ne, Context ctxt) {
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
	 * @param sm DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 *
	 * @post result->forall(o | o.getExpr().getType().getClassName().indexOf("MainThread") == 0 implies (o.getStmt() = null
	 * 		 and o.getSootMethod() = null))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo#getExecutionThreads(soot.SootMethod)
	 */
	public Collection getExecutionThreads(SootMethod sm) {
		Set result = (Set) method2threads.get(sm);

		if (result == null) {
			result = Collections.EMPTY_SET;
		} else {
			result = Collections.unmodifiableSet(result);
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo#getStartSites()
	 */
	public Collection getStartSites() {
		return Collections.unmodifiableCollection(startSites);
	}

	/**
	 * Called by the post processing controller on encountering values in the system.
	 *
	 * @param value that was encountered and needs processing.
	 * @param context in which the value was encountered.
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	public void callback(Value value, Context context) {
		IEnvironment env = analyzer.getEnvironment();

		if (value instanceof NewExpr) {
			NewExpr ne = (NewExpr) value;
			SootClass clazz = env.getClass(ne.getBaseType().getClassName());

			try {
				// collect the new expressions which create Thread objects.
				if (Util.isDescendentOf(clazz, "java.lang.Thread")
					  && Util.getDeclaringClass(clazz, "start", Collections.EMPTY_LIST, VoidType.v()).getName().equals("java.lang.Thread")) {
					newThreadExprs.add(new NewExprTriple(context.getCurrentMethod(), context.getStmt(), ne));
				}
			} catch (NoSuchMethodException e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("How can there be a child class of java.lang.Thread without access to start() method.");
				}
				throw new RuntimeException(e);
			}
		} else if (value instanceof VirtualInvokeExpr) {
			VirtualInvokeExpr ve = (VirtualInvokeExpr) value;
			RefLikeType rlt = (RefLikeType) ve.getBase().getType();
			SootClass clazz = null;

			if (rlt instanceof RefType) {
				clazz = env.getClass(((RefType) rlt).getClassName());
			} else if (rlt instanceof ArrayType) {
				clazz = env.getClass("java.lang.Object");
			} else {
				throw new RuntimeException("Illegal type " + rlt + " arrived at call-site.");
			}

			SootMethod method = ve.getMethod();

			try {
				if (Util.isDescendentOf(clazz, "java.lang.Thread")
					  && method.getName().equals("start")
					  && method.getReturnType() instanceof VoidType
					  && method.getParameterCount() == 0
					  && Util.getDeclaringClass(clazz, "start", Collections.EMPTY_LIST, VoidType.v()).getName().equals("java.lang.Thread")) {
					startSites.add(new CallTriple(context.getCurrentMethod(), context.getStmt(), ve));
				}
			} catch (NoSuchMethodException e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("How can there be a child class of java.lang.Thread without access to start() method.");
				}
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Consolidates the thread graph information before it is available to the application.
	 *
	 * @throws RuntimeException DOCUMENT ME!
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

		for (Iterator i = values.iterator(); i.hasNext();) {
			NewExpr value = (NewExpr) i.next();
			SootClass sc = env.getClass(value.getBaseType().getClassName());
			Collection methods;

			if (!class2runCallees.containsKey(sc)) {
				boolean flag = false;

				try {
					SootClass scTemp = Util.getDeclaringClass(sc, "run", Collections.EMPTY_LIST, VoidType.v());
					flag = scTemp.getName().equals("java.lang.Thread");
				} catch (NoSuchMethodException e) {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("How can there be a child class of java.lang.Thread without access to start() method.");
					}
					throw new RuntimeException(e);
				}

				if (flag) {
					// Here we use the knowledge of only one target object is associated with a thread object.
					Collection t = new ArrayList();
					t.add(value);
					methods = new HashSet();

					// It is possible that the same thread allocation site(loop enclosed) be associated with multiple target 
					// object 
					for (Iterator j = analyzer.getValues(threadClass.getField("target"), t).iterator(); j.hasNext();) {
						NewExpr temp = (NewExpr) j.next();
						SootClass scTemp = env.getClass((temp.getBaseType()).getClassName());
						methods.addAll(transitiveThreadCallClosure(scTemp.getMethod("run", Collections.EMPTY_LIST,
									VoidType.v())));
					}
				} else {
					methods = transitiveThreadCallClosure(sc.getMethod("run", Collections.EMPTY_LIST, VoidType.v()));
				}

				class2runCallees.put(sc, methods);
			}
			methods = (Collection) class2runCallees.get(sc);
			thread2methods.put(extractNewExprTripleFor(value), methods);

			NewExprTriple thread = extractNewExprTripleFor(value);

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
		 */
		Collection heads = cgi.getHeads();
		int k = 1;

		for (Iterator i = heads.iterator(); i.hasNext(); k++) {
			SootMethod head = (SootMethod) i.next();
			NewExpr mainThreadNE = Jimple.v().newNewExpr(RefType.v("MainThread:" + k + ":" + head.getSignature()));
			NewExprTriple thread = new NewExprTriple(null, null, mainThreadNE);
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
            if (!reachables.contains(ctrp.getMethod()))
                temp.add(ctrp);
        }
        startSites.removeAll(temp);

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
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#hookup(
	 * 		edu.ksu.cis.indus.staticanalyses.flow.ProcessingController)
	 */
	public void hookup(ProcessingController ppc) {
		ppc.register(NewExpr.class, this);
		ppc.register(VirtualInvokeExpr.class, this);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#unhook(
	 * 		edu.ksu.cis.indus.staticanalyses.flow.ProcessingController)
	 */
	public void unhook(ProcessingController ppc) {
		ppc.unregister(NewExpr.class, this);
	}

	/**
	 * Given an allocation expression it returns the corresponding <code>NewExprTriple</code> object for it.
	 *
	 * @param ne is the allocation expression.
	 *
	 * @return the triple corresponding to the allocation expression.
	 */
	private NewExprTriple extractNewExprTripleFor(NewExpr ne) {
		NewExprTriple result = null;

		for (Iterator i = newThreadExprs.iterator(); i.hasNext();) {
			NewExprTriple ntrp = (NewExprTriple) i.next();

			if (ntrp.getExpr().equals(ne) && ntrp.getExpr() == ne) {
				result = ntrp;
				break;
			}
		}
		return result;
	}

	/**
	 * Calculates the tranitive closure of methods called from the given method.  The inclusion constraint for the  closure
	 * is that the method cannot be an instance of <code>java.lang.Thread.start()</code>.
	 *
	 * @param starterMethod is the method from where the closure calculation starts.
	 *
	 * @return a collection of <code>SootMethod</code>s occurring the call closure.
	 *
	 * @post result.isOclKindOf(Collection(SootMethod))
	 */
	private Collection transitiveThreadCallClosure(SootMethod starterMethod) {
		Collection result = new HashSet();
		WorkBag wb = new WorkBag(WorkBag.FIFO);
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

/*****
 ChangeLog:

$Log$

*****/
