
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.postprocessors;

import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.VoidType;

import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.NewExpr;
import ca.mcgill.sable.soot.jimple.Value;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.PostProcessingController;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.CallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.CallGraphInfo.CallTriple;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.Environment;
import edu.ksu.cis.bandera.staticanalyses.flow.interfaces.ThreadGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.support.Util;
import edu.ksu.cis.bandera.staticanalyses.support.WorkBag;

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
  extends AbstractPostProcessor
  implements ThreadGraphInfo {
	/**
	 * This provides call graph information pertaining to the system.
	 */
	private final CallGraphInfo cgi;

	/**
	 * The collection of thread allocation sites.
	 *
	 * @invariant newThreadExprs.oclType = Collection(NewExprTriple)
	 */
	private final Collection newThreadExprs = new HashSet();

	/**
	 * This maps methods to thread allocation sites which create threads in which the key method is executed.
	 *
	 * @invariant method2threads.oclType = Map(SootMethod, Collection(NewExprTriple))
	 */
	private final Map method2threads = new HashMap();

	/**
	 * This maps threads allocation sites to the methods which are executed in the created threads.
	 *
	 * @invariant thread2methods.oclType = Map(NewExprTriple, Collection(SootMethod))
	 */
	private final Map thread2methods = new HashMap();

	/**
	 * The object flow analyzer used to calculate thread graph information.
	 */
	private OFAnalyzer ofa;

	/**
	 * Creates a new ThreadGraph object.
	 *
	 * @param cgi provides call graph information.
	 */
	public ThreadGraph(CallGraphInfo cgi) {
		this.cgi = cgi;
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.ThreadGraphInfo#getAllocationSites()
	 */
	public Collection getAllocationSites() {
		return Collections.unmodifiableCollection(newThreadExprs);
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.PostProcessor#setAnalyzer(AbstractAnalyzer)
	 */
	public void setAnalyzer(AbstractAnalyzer ofa) {
		this.ofa = (OFAnalyzer) ofa;
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.ThreadGraphInfo#getExecutedMethods(ca.mcgill.sable.soot.jimple.NewExpr,
	 * 		edu.ksu.cis.bandera.staticanalyses.flow.Context)
	 */
	public Collection getExecutedMethods(NewExpr ne, Context ctxt) {
		Set result = (Set) thread2methods.get(new NewExprTriple(ctxt.getCurrentMethod(), ctxt.getStmt(), ne));

		if(result == null) {
			result = Collections.EMPTY_SET;
		} else {
			result = Collections.unmodifiableSet(result);
		}
		return result;
	}

	/**
	 * <p>
	 * Please refer to class documentation for important information.
	 * </p>
	 *
	 * @post result->forall(o | o.getExpr().getType().className.indexOf("MainThread") == 0 implies (o.getStmt() = null and
	 * 		 o.getSootMethod() = null))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.ThreadGraphInfo#getExecutionThreads(ca.mcgill.sable.soot.SootMethod)
	 */
	public Collection getExecutionThreads(SootMethod sm) {
		Set result = (Set) method2threads.get(sm);

		if(result == null) {
			result = Collections.EMPTY_SET;
		} else {
			result = Collections.unmodifiableSet(result);
		}
		return result;
	}

	/**
	 * Called by the post processing controller on encountering values in the system.
	 *
	 * @param value that was encountered and needs processing.
	 * @param context in which the value was encountered.
	 */
	public void callback(Value value, Context context) {
		Environment env = ofa.getEnvironment();

		if(value instanceof NewExpr) {
			NewExpr ne = (NewExpr) value;
			SootClass clazz = env.getClass(ne.getBaseType().className);

			// collect the new expressions which create Thread objects.
			if(Util.isDescendentOf(clazz, "java.lang.Thread")
					&& Util.getDeclaringClass(clazz, "start", Util.EMPTY_PARAM_LIST, VoidType.v()).getName().equals("java.lang.Thread")) {
				newThreadExprs.add(new NewExprTriple(context.getCurrentMethod(), context.getStmt(), ne));
			}
		}
	}

	/**
	 * Consolidates the thread graph information before it is available to the application.
	 */
	public void consolidate() {
		// capture the run call-site in Thread.start method
		Environment env = ofa.getEnvironment();
		SootClass threadClass = env.getClass("java.lang.Thread");
		threadClass.resolveIfNecessary();

		SootMethod startMethod = threadClass.getMethod("start");
		Context ctxt = new Context();
		ctxt.setRootMethod(startMethod);

		Collection values = ofa.getValuesForThis(ctxt);
		Map class2runCallees = new HashMap();

		for(Iterator i = values.iterator(); i.hasNext();) {
			NewExpr value = (NewExpr) i.next();
			SootClass sc = env.getClass(((RefType) value.getBaseType()).className);
			Collection methods;

			if(!class2runCallees.containsKey(sc)) {
				if(Util.getDeclaringClass(sc, "run", Util.EMPTY_PARAM_LIST, VoidType.v()).getName().equals("java.lang.Thread")) {
					// Here we use the knowledge of only one target object is associated with a thread object.
					Collection t = new ArrayList();
					t.add(value);
					methods = new HashSet();

					// It is possible that the same thread allocation site(loop enclosed) be associated with multiple target object 
					for(Iterator j = ofa.getValues(threadClass.getField("target"), t).iterator(); j.hasNext();) {
						NewExpr temp = (NewExpr) j.next();
						SootClass scTemp = env.getClass(((RefType) temp.getBaseType()).className);
						methods.addAll(transitiveThreadCallClosure(scTemp.getMethod("run", Util.EMPTY_PARAM_LIST, VoidType.v())));
					}
				} else {
					methods = transitiveThreadCallClosure(sc.getMethod("run", Util.EMPTY_PARAM_LIST, VoidType.v()));
				}
				class2runCallees.put(sc, methods);
			}
			methods = (Collection) class2runCallees.get(sc);
			thread2methods.put(extractNewExprTripleFor(value), methods);

			NewExprTriple thread = extractNewExprTripleFor(value);

			for(Iterator j = methods.iterator(); j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();
				Collection threads = (Collection) method2threads.get(sm);

				if(threads == null) {
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

		for(Iterator i = heads.iterator(); i.hasNext(); k++) {
			SootMethod head = (SootMethod) i.next();
			NewExprTriple thread =
				new NewExprTriple(null, null, Jimple.v().newNewExpr(RefType.v("MainThread:" + k + ":" + head.getSignature())));
			newThreadExprs.add(thread);

			Collection methods = transitiveThreadCallClosure(head);
			thread2methods.put(thread, methods);

			for(Iterator j = methods.iterator(); j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();
				Collection threads = (Collection) method2threads.get(sm);

				if(threads == null) {
					threads = new HashSet();
					method2threads.put(sm, threads);
				}
				threads.add(thread);
			}
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

		for(Iterator i = thread2methods.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			NewExprTriple net = (NewExprTriple) entry.getKey();

			if(net.getMethod() == null) {
				result.append("\n" + net.getExpr().getType() + "\n");
			} else {
				result.append("\n" + net.getStmt() + "@" + net.getMethod() + "->" + net.getExpr() + "\n");
			}

			l.clear();

			for(Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();
				l.add(sm.getSignature());
			}
			Collections.sort(l);

			for(Iterator j = l.iterator(); j.hasNext();) {
				result.append("\t" + j.next() + "\n");
			}
		}
		return result.toString();
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.interfaces.PostProcessor#hookup(edu.ksu.cis.bandera.staticanalyses.flow.PostProcessingController)
	 */
	public void hookup(PostProcessingController ppc) {
		ppc.register(NewExpr.class, this);
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

		for(Iterator i = newThreadExprs.iterator(); i.hasNext();) {
			NewExprTriple ntrp = (NewExprTriple) i.next();

			if(ntrp.getExpr().equals(ne) && ntrp.getExpr() == ne) {
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
	 * @post result.oclType = Collection(SootMethod)
	 */
	private Collection transitiveThreadCallClosure(SootMethod starterMethod) {
		Collection result = new HashSet();
		WorkBag wb = new WorkBag(WorkBag.FIFO);
		wb.addWork(starterMethod);
		result.add(starterMethod);

		while(!wb.isEmpty()) {
			SootMethod sm = (SootMethod) wb.getWork();

			if(sm.getName().equals("start")
					&& sm.getDeclaringClass().getName().equals("java.lang.Thread")
					&& sm.getParameterCount() == 0
					&& sm.getReturnType().equals(VoidType.v())) {
				continue;
			}

			Collection callees = cgi.getCallees(sm);

			for(Iterator i = callees.iterator(); i.hasNext();) {
				CallTriple ctrp = (CallTriple) i.next();
				SootMethod temp = ctrp.getMethod();

				if(!result.contains(temp)) {
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
