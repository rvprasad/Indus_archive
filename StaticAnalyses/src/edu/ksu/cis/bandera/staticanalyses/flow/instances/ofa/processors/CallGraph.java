
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

package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.processors;

import ca.mcgill.sable.soot.ArrayType;
import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.Type;

import ca.mcgill.sable.soot.jimple.InterfaceInvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.NewExpr;
import ca.mcgill.sable.soot.jimple.NonStaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.NullConstant;
import ca.mcgill.sable.soot.jimple.SpecialInvokeExpr;
import ca.mcgill.sable.soot.jimple.StaticInvokeExpr;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.VirtualInvokeExpr;

import ca.mcgill.sable.util.List;

import edu.ksu.cis.bandera.staticanalyses.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.flow.AbstractAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.interfaces.Processor;
import edu.ksu.cis.bandera.staticanalyses.support.Marker;
import edu.ksu.cis.bandera.staticanalyses.support.Node;
import edu.ksu.cis.bandera.staticanalyses.support.SimpleNodeGraph;
import edu.ksu.cis.bandera.staticanalyses.support.SimpleNodeGraph.SimpleNode;
import edu.ksu.cis.bandera.staticanalyses.support.Triple;
import edu.ksu.cis.bandera.staticanalyses.support.WorkBag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;


/**
 * This class calculates class graph information from the associated object flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class CallGraph
  extends AbstractProcessor
  implements CallGraphInfo,
	  Processor {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(CallGraph.class);

	/**
	 * The collection of methods from which the system can be started.  Although an instance of a class can be created and a
	 * method can be invoked on it from the environment, this method will not be considered as a <i>head method</i>.
	 * However, our definition of head methods are those methods(excluding those in invoked via <code>invokespecial</code>
	 * bytecode) with no caller method that belongs to the system.
	 *
	 * @invariant heads->forall(o | o.oclType = SootMethod)
	 */
	private final Collection heads = new HashSet();

	/**
	 * The collection of list of methods that form cycles.
	 *
	 * @invariant cycles->forall(o | o.oclIsKindOf(List(SootMethod)))
	 */
	private Collection cycles = new HashSet();

	/**
	 * The collection of methods that are reachble in the system.
	 *
	 * @invariant reachables->forall(o | o.oclType = SootMethod)
	 */
	private Collection reachables = new HashSet();

	/**
	 * The collection of methods which are the head of a recursion cycle.
	 *
	 * @invariant recursionRoots->forall(o | o.oclType = SootMethod)
	 */
	private Collection recursionRoots = new HashSet();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Collection sccs = new HashSet();

	/**
	 * This maps callees to callers.
	 *
	 * @invariant callee2callers.oclIsKindOf(Map(SootMethod, Bag(SootMethod)))
	 */
	private Map callee2callers = new HashMap();

	/**
	 * This maps callers to callees.
	 *
	 * @invariant caller2callees.oclIsKindOf(Map(SootMethod, Bag(SootMethod)))
	 */
	private Map caller2callees = new HashMap();

	/**
	 * The BFA instance which implements object flow analysis.  This instance is used to calculate call graph information.
	 */
	private OFAnalyzer analyzer;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private SimpleNodeGraph graph;

	/**
	 * Sets the analyzer to be used to calculate call graph information upon call back.
	 *
	 * @param analyzer that provides the information to create the call graph.
	 *
	 * @throws IllegalArgumentException when the analyzer is not an instance of <code>OFAnalyzer</code>.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#setAnalyzer(
	 * 		edu.ksu.cis.bandera.staticanalyses.flow.AbstractAnalyzer)
	 */
	public void setAnalyzer(AbstractAnalyzer analyzer) {
		if(analyzer instanceof OFAnalyzer) {
			this.analyzer = (OFAnalyzer) analyzer;
			heads.clear();
			recursionRoots.clear();
			reachables.clear();
			cycles.clear();
			graph = null;
		} else {
			throw new IllegalArgumentException("analyzer has to be of type OFAnalyzer.");
		}
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo#getCallGraph()
	 */
	public SimpleNodeGraph getCallGraph() {
		return graph;
	}

	/**
	 * Returns a collection of methods called by <code>caller</code>.
	 *
	 * @param caller which calls the returned methods.
	 *
	 * @return a collection of <code>CallTriple</code>s corresponding to the call sites.
	 *
	 * @post result->forall(o | o.isOclKindOf(CallTriple))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo#getCallees(ca.mcgill.sable.soot.SootMethod,
	 * 		edu.ksu.cis.bandera.staticanalyses.flow.Context)
	 */
	public Collection getCallees(SootMethod caller) {
		Collection result = Collections.EMPTY_LIST;
		Collection callees = (Collection) caller2callees.get(caller);

		if(callees != null) {
			result = Collections.unmodifiableCollection(callees);
		}
		return result;
	}

	/**
	 * Returns the set of method implementations that shall be invoked at the given callsite expression in the given method.
	 *
	 * @param invokeExpr the method call site.
	 * @param context in which the call occurs.
	 *
	 * @return a collection of <code>SootMethod</code>s.
	 *
	 * @post result->forall(o | o.oclType = SootMethod)
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo#getCallees(ca.mcgill.sable.soot.jimple.InvokeExpr,
	 * 		edu.ksu.cis.bandera.staticanalyses.flow.Context)
	 */
	public Collection getCallees(InvokeExpr invokeExpr, Context context) {
		Collection result;

		if(invokeExpr instanceof StaticInvokeExpr || invokeExpr instanceof SpecialInvokeExpr) {
			result = Collections.singletonList(invokeExpr.getMethod());
		} else {
			Collection newExprs = analyzer.getValues(((NonStaticInvokeExpr) invokeExpr).getBase(), context);
			result = new HashSet();

			for(Iterator i = newExprs.iterator(); i.hasNext();) {
				Object o = i.next();
				SootClass sc = null;

				if(o instanceof NullConstant) {
					continue;
				} else {
					Type t = ((Value) o).getType();
System.out.println(">>>" + t);
					if(t instanceof RefType) {
						sc = analyzer.getEnvironment().getClass(((RefType) t).className);
					} else if(t instanceof ArrayType) {
						sc = analyzer.getEnvironment().getClass("java.lang.Object");
					}
				}
				result.add(findMethodImplementation(sc, invokeExpr.getMethod()));
			}
		}
		return result;
	}

	/**
	 * Returns the methods that call the given method independent of any context.
	 *
	 * @param callee is the method being called.
	 *
	 * @return a collection of <code>CallTriple</code>s corresponding to the call sites.
	 *
	 * @post result->forall(o | o.isOclKindOf(CallTriple))
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo#getCallers(ca.mcgill.sable.soot.SootMethod)
	 */
	public Collection getCallers(SootMethod callee) {
		Collection result = Collections.EMPTY_LIST;
		Collection callers = (Collection) callee2callers.get(callee);

		if(callers != null) {
			result = Collections.unmodifiableCollection(callers);
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo#getCycles()
	 */
	public Collection getCycles() {
		Collection result = new HashSet();

		for(Iterator i = cycles.iterator(); i.hasNext();) {
			java.util.List cycle = (java.util.List) i.next();
			result.add(Collections.unmodifiableList(cycle));
		}
		return result;
	}

	/**
	 * Returns the methods that are the entry point for the analyzed system.
	 *
	 * @return a collection of methods.
	 *
	 * @post result->forall(o | o.oclType = SootMethod)
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo#getHeads()
	 */
	public Collection getHeads() {
		return Collections.unmodifiableCollection(heads);
	}

	/**
	 * Checks if the given method is reachable in the analyzed system.
	 *
	 * @param method to be checked for reachabiliy.
	 *
	 * @return <code>true</code> if <code>method</code> is reachable; <code>false</code>, otherwise.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo#isReachable(ca.mcgill.sable.soot.SootMethod)
	 */
	public boolean isReachable(SootMethod method) {
		return reachables.contains(method);
	}

	/**
	 * Returns the methods reachable in the analyzed system.
	 *
	 * @return a collection of methods.
	 *
	 * @post result->forall(o | o.oclType = SootMethod)
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo#getReachableMethods()
	 */
	public Collection getReachableMethods() {
		return Collections.unmodifiableCollection(reachables);
	}

	/**
	 * Returns the methods which are the heads of recursion cycles.
	 *
	 * @return a colleciton of methods.
	 *
	 * @post result->forall(o | o.oclType = SootMethod)
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo#getRecursionRoots()
	 */
	public Collection getRecursionRoots() {
		if(recursionRoots == null) {
			recursionRoots = new HashSet();

			Context context = new Context();
			Stack callStack = new Stack();
			WorkBag workbag = new WorkBag(WorkBag.FIFO);

			for(Iterator i = heads.iterator(); i.hasNext();) {
				SootMethod sm = (SootMethod) i.next();
				workbag.addWork(new Triple(sm, null, null));

				while(!workbag.isEmpty()) {
					Object o = workbag.getWork();

					if(o instanceof Marker) {
						Object temp = ((Marker) o)._CONTENT;

						for(Object obj = callStack.pop(); !temp.equals(obj); obj = callStack.pop()) {
							;
						}
					} else {
						Triple triple = (Triple) o;
						SootMethod callee = (SootMethod) triple.getFirst();

						if(callStack.contains(callee)) {
							recursionRoots.add(callee);
						} else {
							context.setRootMethod(callee);

							Collection callees = getCallees(callee);

							if(!callees.isEmpty()) {
								callStack.push(callee);
								workbag.addWork(new Marker(callee));
								workbag.addAllWork(callees);
							}
						}
					}
				}
				context.returnFromCurrentMethod();
			}
		}
		return Collections.unmodifiableCollection(recursionRoots);
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo#getSCCs()
	 */
	public Collection getSCCs() {
		Collection result = new HashSet();

		for(Iterator i = sccs.iterator(); i.hasNext();) {
			java.util.List scc = (java.util.List) i.next();
			result.add(Collections.unmodifiableList(scc));
		}
		return result;
	}

	/**
	 * Called by the post process controller when it walks a jimple value AST node.
	 *
	 * @param value is the AST node to be processed.
	 * @param context in which value should be processed.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#callback(ca.mcgill.sable.soot.jimple.Value,
	 * 		edu.ksu.cis.bandera.staticanalyses.flow.Context)
	 */
	public void callback(Value value, Context context) {
		Stmt stmt = context.getStmt();
		SootMethod caller = context.getCurrentMethod();
		SootMethod callee = null;
		Set callees;
		Set callers;
		CallTriple triple;

		// We treat SpecialInvokeExpr as StaticInvokeExpr as the method implementation to be invoked is known.
		if(value instanceof StaticInvokeExpr || value instanceof SpecialInvokeExpr) {
			InvokeExpr invokeExpr = (InvokeExpr) value;
			callee = invokeExpr.getMethod();

			if(caller2callees.containsKey(caller)) {
				callees = (Set) caller2callees.get(caller);
			} else {
				callees = new HashSet();
				caller2callees.put(caller, callees);
			}
			triple = new CallTriple(callee, stmt, invokeExpr);
			callees.add(triple);

			if(callee2callers.containsKey(callee)) {
				callers = (Set) callee2callers.get(callee);
			} else {
				callers = new HashSet();
				callee2callers.put(callee, callers);
			}
			triple = new CallTriple(caller, stmt, invokeExpr);
			callers.add(triple);
		} else if(value instanceof InterfaceInvokeExpr || value instanceof VirtualInvokeExpr) {
			NonStaticInvokeExpr invokeExpr = (NonStaticInvokeExpr) value;
			SootMethod calleeMethod = invokeExpr.getMethod();

			if(caller2callees.containsKey(caller)) {
				callees = (Set) caller2callees.get(caller);
			} else {
				callees = new HashSet();
				caller2callees.put(caller, callees);
			}

			context.setProgramPoint(invokeExpr.getBaseBox());

			for(Iterator i = analyzer.getValues(invokeExpr.getBase(), context).iterator(); i.hasNext();) {
				Object t = i.next();

				if(!(t instanceof NewExpr)) {
					continue;
				}

				NewExpr newExpr = (NewExpr) t;
				SootClass accessClass = analyzer.getEnvironment().getClass(newExpr.getBaseType().className);
				callee = findMethodImplementation(accessClass, calleeMethod);

				triple = new CallTriple(callee, stmt, invokeExpr);
				callees.add(triple);

				if(callee2callers.containsKey(callee)) {
					callers = (Set) callee2callers.get(callee);
				} else {
					callers = new HashSet();
					callee2callers.put(callee, callers);
				}
				triple = new CallTriple(caller, stmt, invokeExpr);
				callers.add(triple);
			}
		}
	}

	/**
	 * This calculates information such as heads, tails, recursion roots, and such.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#consolidate()
	 */
	public void consolidate() {
		LOGGER.info("BEGIN: call graph consolidation");

		long start = System.currentTimeMillis();
		heads.addAll(analyzer.getRoots());

		LOGGER.debug("Starting the calculation of reachables...");

		// calculate reachables.
		WorkBag wb = new WorkBag(WorkBag.FIFO);
		wb.addAllWork(heads);
		reachables.addAll(heads);

		while(!wb.isEmpty()) {
			SootMethod sm = (SootMethod) wb.getWork();

			for(Iterator i = getCallees(sm).iterator(); i.hasNext();) {
				CallTriple ctrp = (CallTriple) i.next();
				SootMethod callee = ctrp.getMethod();

				if(reachables.contains(callee)) {
					continue;
				}
				wb.addWork(callee);
				reachables.add(callee);
			}
		}

		// calculate heads, recursion roots, and cycle information.
		graph = new SimpleNodeGraph();
		LOGGER.debug("Starting construction of call graph...");

		for(Iterator i = reachables.iterator(); i.hasNext();) {
			SootMethod sm = (SootMethod) i.next();
			Collection temp = (Collection) caller2callees.get(sm);

			if(temp != null) {
				Node callerNode = graph.getNode(sm);

				for(Iterator j = temp.iterator(); j.hasNext();) {
					CallTriple ctrp = (CallTriple) j.next();
					SootMethod method = ctrp.getMethod();
					Node calleeNode = graph.getNode(method);

					graph.addEdgeFromTo(callerNode, calleeNode);
				}
			}
		}

		/*
		   LOGGER.debug("Starting cycle calculation...");
		   Collection temp = graph.getCycles();
		   for(Iterator i = temp.iterator(); i.hasNext();) {
		       Collection cycle = (Collection) i.next();
		       java.util.List l = new ArrayList();
		       for(Iterator j = cycle.iterator(); j.hasNext();) {
		           l.add(((SimpleNode) j.next())._OBJECT);
		       }
		       cycles.add(l);
		   }
		 */
		LOGGER.debug("Starting strongly connected component calculation...");

		Collection temp = graph.getSCCs(true);

		for(Iterator i = temp.iterator(); i.hasNext();) {
			Collection scc = (Collection) i.next();
			java.util.List l = new ArrayList();

			for(Iterator j = scc.iterator(); j.hasNext();) {
				l.add(((SimpleNode) j.next())._OBJECT);
			}
			sccs.add(l);
		}

		LOGGER.debug("Fixing the recursion roots...");

		for(Iterator i = cycles.iterator(); i.hasNext();) {
			java.util.List cycle = (java.util.List) i.next();
			recursionRoots.add(cycle.get(0));
		}

		long stop = System.currentTimeMillis();
		LOGGER.info("END: call graph consolidation");
		LOGGER.info("TIMING: call graph consolidation took " + (stop - start) + "ms.");
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public String dumpGraph() {
		StringBuffer result = new StringBuffer();

		result.append("top-down\n");

		for(Iterator i = caller2callees.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod caller = (SootMethod) entry.getKey();
			result.append("\n" + caller.getSignature() + "\n");

			Collection callees = (Collection) entry.getValue();

			for(Iterator j = callees.iterator(); j.hasNext();) {
				CallTriple ctrp = (CallTriple) j.next();
				result.append("\t" + ctrp.getMethod().getSignature() + "\n");
			}
		}

		result.append("bottom-up\n");

		for(Iterator i = callee2callers.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			SootMethod callee = (SootMethod) entry.getKey();
			result.append("\n" + callee.getSignature() + "\n");

			Collection callers = (Collection) entry.getValue();

			for(Iterator j = callers.iterator(); j.hasNext();) {
				CallTriple ctrp = (CallTriple) j.next();
				result.append("\t" + ctrp.getMethod().getSignature() + "\n");
			}
		}

		return result.toString();
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#hookup(
	 * 		edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController)
	 */
	public void hookup(ProcessingController ppc) {
		ppc.register(VirtualInvokeExpr.class, this);
		ppc.register(InterfaceInvokeExpr.class, this);
		ppc.register(StaticInvokeExpr.class, this);
		ppc.register(SpecialInvokeExpr.class, this);
	}

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.interfaces.Processor#unhook(
	 * 		edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController)
	 */
	public void unhook(ProcessingController ppc) {
		ppc.unregister(VirtualInvokeExpr.class, this);
		ppc.unregister(InterfaceInvokeExpr.class, this);
		ppc.unregister(StaticInvokeExpr.class, this);
		ppc.unregister(SpecialInvokeExpr.class, this);
	}

	/**
	 * Finds the implementation of <code>method</code> when accessed via <code>accessClass</code>.
	 *
	 * @param accessClass is the class via which <code>method</code> is accesed.
	 * @param method being accessed/invoked.
	 *
	 * @return the implementation of <code>method</code> if present in the class hierarchy; <code>null</code>, otherwise.
	 */
	private SootMethod findMethodImplementation(SootClass accessClass, SootMethod method) {
		String methodName = method.getName();
		List parameterTypes = method.getParameterTypes();
		Type returnType = method.getReturnType();
		return findMethodImplementation(accessClass, methodName, parameterTypes, returnType);
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
	 */
	private SootMethod findMethodImplementation(SootClass accessClass, String methodName, List parameterTypes, Type returnType) {
		SootMethod result = null;

		if(accessClass.declaresMethod(methodName, parameterTypes, returnType)) {
			result = accessClass.getMethod(methodName, parameterTypes, returnType);
		} else {
			if(accessClass.hasSuperClass()) {
				SootClass superClass = accessClass.getSuperClass();
				result = findMethodImplementation(superClass, methodName, parameterTypes, returnType);
			} else {
				LOGGER.error(methodName + "(" + parameterTypes + "):" + returnType + " is not accessible from " + accessClass);
			}
		}
		return result;
	}
}

/*****
 ChangeLog:

$Log$

*****/
