
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

package edu.ksu.cis.indus.tools.slicer.processing;

import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.slicer.SliceCollector;

import edu.ksu.cis.indus.staticanalyses.dependency.EntryControlDA;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.Body;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.TrapManager;
import soot.Type;
import soot.Value;

import soot.jimple.IdentityStmt;
import soot.jimple.ParameterRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;


/**
 * This process a vanilla backward and complete slice into an executable slice.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ExecutableSlicePostProcessor
  implements ISlicePostProcessor {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ExecutableSlicePostProcessor.class);

	/**
	 * The basic block manager.
	 */
	private BasicBlockGraphMgr bbgMgr;

	/**
	 * This tracks the methods processed in <code>process()</code>.
	 *
	 * @invariant processedMethodCache != null
	 * @invariant processedMethodCache.oclIsKindOf(Set(SootMethod))
	 */
	private final Collection processedMethodCache = new HashSet();

	/**
	 * This tracks the methods processed in <code>processStmts()</code>.
	 *
	 * @invariant processedStmtCache != null
	 * @invariant processedStmtCache.oclIsKindOf(Set(Stmt))
	 */
	private final Collection processedStmtCache = new HashSet();

	/**
	 * This is the workbag of methods to process.
	 *
	 * @invariant methodWorkBag != null and methodWorkBag.getWork().oclIsKindOf(SootMethod)
	 */
	private final IWorkBag methodWorkBag = new HistoryAwareFIFOWorkBag(processedMethodCache);

	/**
	 * This is the workbag of statements to process.
	 *
	 * @invariant stmtWorkBag != null and stmtWorkBag.getWork().oclIsKindOf(Stmt)
	 */
	private final IWorkBag stmtWorkBag = new HistoryAwareFIFOWorkBag(processedStmtCache);

	/**
	 * This provides entry-based control dependency information required to include exit points.
	 */
	private EntryControlDA cd = new EntryControlDA();

	/**
	 * The slice collector to be used to add on to the slice.
	 */
	private SliceCollector collector;

	/**
	 * This indicates if any statements of the method were included during post processing.  If so, other statement based
	 * post processings are triggered.
	 */
	private boolean stmtCollected;

	/**
	 * Processes the given methods.
	 *
	 * @param taggedMethods are the methods to process.
	 * @param basicBlockMgr is the basic block manager to be used to retrieve basic blocks while processing methods.
	 * @param theCollector is the slice collector to extend the slice.
	 *
	 * @pre taggedMethods != null and basicBlockMgr != null and theCollector != null
	 * @pre taggedMethods.oclIsKindOf(Collection(SootMethod))
	 */
	public void process(final Collection taggedMethods, final BasicBlockGraphMgr basicBlockMgr,
		final SliceCollector theCollector) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Post Processing.");
			LOGGER.debug("SLICE BEFORE: " + ExecutableSlicePostProcessor.class.getClass() + "\n"
				+ theCollector.toString());
		}

		collector = theCollector;
		bbgMgr = basicBlockMgr;
		cd.setBasicBlockGraphManager(basicBlockMgr);
		methodWorkBag.addAllWorkNoDuplicates(taggedMethods);

		// process the methods and gather the collected classes
		while (methodWorkBag.hasWork()) {
			final SootMethod _method = (SootMethod) methodWorkBag.getWork();

			processMethods(_method);

			if (_method.isConcrete()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Post Processing method " + _method);
				}
				stmtCollected = false;
				processStmts(_method);

				if (stmtCollected) {
					pickReturnPoints(_method);
				}
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Could not process method " + _method.getSignature());
				}
			}
		}

		fixupClassHierarchy();

		if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("SLICE AFTER: " + ExecutableSlicePostProcessor.class.getClass() + "\n"
                    + theCollector.toString());

			LOGGER.debug("END: Post Processing.");
		}
	}

	/**
	 * Resets internal data structure.
	 */
	public void reset() {
		methodWorkBag.clear();
		processedMethodCache.clear();
	}

	/**
	 * Fix up class hierarchy such that all abstract methods have an implemented counterpart in the slice.
	 */
	private void fixupClassHierarchy() {
		final Map _class2abstractMethods = new HashMap();
		final Collection _temp = new HashSet();

		// include the ancestor classes of all the collected classes.
		for (final Iterator _i = collector.getClassesInSlice().iterator(); _i.hasNext();) {
			_temp.addAll(Util.getAncestors((SootClass) _i.next()));
		}
		collector.includeInSlice(_temp);

		// setup the variables for fixing the class hierarchy
		final Collection _classesInSlice = collector.getClassesInSlice();
		final Collection _topologicallyOrderedClasses = Util.getClassesInTopologicallySortedOrder(_classesInSlice, true);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Fixing Class Hierarchy");
			LOGGER.debug("Topological Sort: " + _topologicallyOrderedClasses);
		}

		// fixup methods with respect the class hierarchy  
		for (final Iterator _i = _topologicallyOrderedClasses.iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			final Collection _methods = gatherCollectedAbstractMethodsInSuperClasses(_class2abstractMethods, _sc);

			// remove all abstract methods which are overridden by collected methods of this class.
			Util.removeMethodsWithSameSignature(_methods, collector.getCollected(_sc.getMethods()));
			// gather uncollected methods of this class which have the same signature as any gathered "super" abstract 
			// methods and include them in the slice.
			_temp.clear();
			_temp.addAll(_sc.getMethods());
			Util.retainMethodsWithSameSignature(_temp, _methods);
			collector.includeInSlice(_temp);
			// remove abstract counterparts of all methods that were included in the slice in the previous step.
			Util.removeMethodsWithSameSignature(_methods, _temp);

			// gather collected abstract methods in this class/interface
			if (_sc.isInterface()) {
				_methods.addAll(collector.getCollected(_sc.getMethods()));
			} else if (_sc.isAbstract()) {
				for (final Iterator _j = collector.getCollected(_sc.getMethods()).iterator(); _j.hasNext();) {
					final SootMethod _sm = (SootMethod) _j.next();

					if (_sm.isAbstract()) {
						_methods.add(_sm);
					}
				}
			}

			// record the abstract methods
			if (_methods.isEmpty()) {
				_class2abstractMethods.put(_sc, Collections.EMPTY_SET);
			} else {
				_class2abstractMethods.put(_sc, new ArrayList(_methods));
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Class -> abstract Method mapping:\n" + _class2abstractMethods);
			LOGGER.debug("END: Fixing Class Hierarchy");
		}
	}

	/**
	 * Gathers the collected abstract methods that belong to the super classes/interface of the given class.
	 *
	 * @param class2abstractMethods maps classes to collected abstract methods.
	 * @param clazz for which the collection should be performed.
	 *
	 * @return a collection of collected abstract methods belonging to the super classes.
	 *
	 * @pre class2abstractMethods != null and class2abstractMethods.oclIsKindOf(Map(SootClass, Collection(SootMethod)))
	 * @pre clazz != null
	 * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 * @post result->forall(o | class2abstractMethods->exists(p | p.values().includes(o)))
	 */
	private Collection gatherCollectedAbstractMethodsInSuperClasses(final Map class2abstractMethods, final SootClass clazz) {
		final Collection _methods = new HashSet();

		// gather collected abstract methods from super interfaces and classes.
		for (final Iterator _j = clazz.getInterfaces().iterator(); _j.hasNext();) {
			final SootClass _interface = (SootClass) _j.next();

			if (collector.hasBeenCollected(_interface)) {
				final Collection _abstractMethods = (Collection) class2abstractMethods.get(_interface);
				_methods.addAll(_abstractMethods);
			}
		}

		if (clazz.hasSuperclass()) {
			final SootClass _superClass = clazz.getSuperclass();

			if (collector.hasBeenCollected(_superClass)) {
				final Collection _abstractMethods = ((Collection) class2abstractMethods.get(_superClass));
				_methods.addAll(_abstractMethods);
			}
		}
		return _methods;
	}

	/**
	 * Picks the return points of the method required to make it's slice executable.
	 *
	 * @param method to be processed.
	 *
	 * @pre method != null
	 */
	private void pickReturnPoints(final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Picking return points in " + method);
		}

		// pick all return/throw points in the methods.
		final String _tagName = collector.getTagName();
		final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(method);
		final Collection _tails = new HashSet();
		_tails.addAll(_bbg.getTails());
		_tails.addAll(_bbg.getPseudoTails());
		cd.analyze(Collections.singleton(method));

		if (_tails.size() == 1) {
			// If there is only one tail then include the statement
			final BasicBlock _bb = (BasicBlock) _tails.iterator().next();
			collector.includeInSlice(_bb.getTrailerStmt());
		} else {
			// if there are more than one tail then pick only the ones that are reachable via collected statements
			for (final Iterator _j = _tails.iterator(); _j.hasNext();) {
				final BasicBlock _bb = (BasicBlock) _j.next();
				final Stmt _stmt = _bb.getTrailerStmt();
				final Collection _dependees = cd.getDependees(_stmt, method);
				boolean _flag = _dependees.isEmpty();

				if (!_flag) {
					for (final Iterator _i = _dependees.iterator(); _i.hasNext() && !_flag;) {
						_flag = ((Stmt) _i.next()).hasTag(_tagName);
					}
				}

				if (!_stmt.hasTag(_tagName) && _flag) {
					collector.includeInSlice(_stmt);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Picked " + _stmt + " in " + method);
					}
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Picking return points in " + method);
		}
	}

	/**
	 * Marks the traps to be included in the slice.
	 *
	 * @param method in which the traps are to be marked.
	 * @param stmt will trigger the traps to include.
	 *
	 * @pre method != null and stmt != null
	 */
	private void processHandlers(final SootMethod method, final Stmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Pruning handlers " + stmt + "@" + method);
		}

		final Body _body = method.retrieveActiveBody();
		final Collection _temp = new ArrayList();

		// calculate the relevant traps
		if (collector.hasBeenCollected(stmt)) {
			_temp.addAll(TrapManager.getTrapsAt(stmt, _body));
		}

		/*
		 * Include the first statement of the handler for all traps found to cover atleast one statement included in the
		 * slice.
		 */
		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			final Trap _trap = (Trap) _i.next();
			collector.includeInSlice(Util.getAncestors(_trap.getException()));

			final IdentityStmt _handlerUnit = (IdentityStmt) _trap.getHandlerUnit();
			collector.includeInSlice(_handlerUnit);
			collector.includeInSlice(_handlerUnit.getLeftOpBox());
			collector.includeInSlice(_handlerUnit.getRightOpBox());
			collector.includeInSlice(_trap.getException());
			stmtWorkBag.addWorkNoDuplicates(_handlerUnit);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Pruning handlers " + stmt + "@" + method);
		}
	}

	/**
	 * Includes statement and it's parts that access this variable and parameters (statements that pop them of the stack) in
	 * the slice.
	 *
	 * @param method to process.
	 * @param stmt to be processed.
	 *
	 * @pre method != null and stmt != null
	 */
	private void processIdentityStmt(final SootMethod method, final IdentityStmt stmt) {
		/*
		 * Pick all identity statements in the program that retrieve parameters and this reference upon
		 * entrance into the method. Note that it is required that all elements pushed to the stack should be
		 * popped before returning with only the return value on the stack (if it exists).  This does not
		 * mean that all parameters need to be popped off the stack upon entry.  Hence, we should scan the
		 * entire body for Identity statements with ParameterRef or ThisRef on RHS.
		 */
		final IdentityStmt _id = stmt;
		final Value _rhs = _id.getRightOp();

		if (_rhs instanceof ThisRef || _rhs instanceof ParameterRef) {
			collector.includeInSlice(_id.getLeftOpBox());
			collector.includeInSlice(_id.getRightOpBox());
			collector.includeInSlice(_id);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Picked " + stmt + " in " + method);
			}
		}
	}

	/**
	 * For the given method, this method includes the declarations/definitions of methods with identical signature in the
	 * super classes to make the slice executable.
	 *
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @pre method != null
	 */
	private void processMethods(final SootMethod method) {
		final Collection _temp = new HashSet();

		for (final Iterator _i = Util.findMethodInSuperClassesAndInterfaces(method).iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			collector.includeInSlice(_sm.getDeclaringClass());
			collector.includeInSlice(_sm);
			_temp.clear();
			_temp.add(_sm.getReturnType());
			_temp.addAll(_sm.getParameterTypes());

			for (final Iterator _j = _temp.iterator(); _j.hasNext();) {
				final Type _type = (Type) _j.next();

				if (_type instanceof RefType) {
					collector.includeInSlice(((RefType) _type).getSootClass());
				} else if (_type instanceof ArrayType) {
					final Type _baseType = ((ArrayType) _type).baseType;

					if (_baseType instanceof RefType) {
						collector.includeInSlice(((RefType) _baseType).getSootClass());
					}
				}
			}
			methodWorkBag.addWorkNoDuplicates(_sm);
		}
	}

	/**
	 * Process the statements in the slice body of the given method.
	 *
	 * @param method whose statements need to be processed.
	 *
	 * @pre method != null and method.isConcrete()
	 */
	private void processStmts(final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Picking up identity statements and methods required in" + method);
		}

		final Body _body = method.retrieveActiveBody();
		stmtWorkBag.clear();
		stmtWorkBag.addAllWork(_body.getUnits());
		processedStmtCache.clear();

		while (stmtWorkBag.hasWork()) {
			final Stmt _stmt = (Stmt) stmtWorkBag.getWork();

			//processedStmtCache.add(_stmt);
			if (collector.hasBeenCollected(_stmt)) {
				if (_stmt instanceof IdentityStmt) {
					processIdentityStmt(method, (IdentityStmt) _stmt);
				} else if (_stmt.containsInvokeExpr()
					  && collector.hasBeenCollected(_stmt)
					  && !(_stmt.getInvokeExpr() instanceof StaticInvokeExpr)) {
					/*
					 * If an invoke expression occurs in the slice, the slice will include only the invoked method and not any
					 * incarnations of it in it's ancestral classes.  This will lead to unverifiable system of classes.
					 * This can be fixed by sucking all the method definitions that need to make the system verifiable
					 * and empty bodies will be substituted for such methods.
					 */
					processMethods(_stmt.getInvokeExpr().getMethod());

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Included method invoked at " + _stmt + " in " + method);
					}
				}
				processHandlers(method, _stmt);
				stmtCollected = true;
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.20  2004/03/29 01:55:08  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.19  2004/03/03 10:09:42  venku
   - refactored code in ExecutableSlicePostProcessor and TagBasedSliceResidualizer.
   Revision 1.18  2004/02/06 07:09:02  venku
   - refactoring.
   - types of methods signatures were not sucked in. FIXED.
   Revision 1.17  2004/02/06 00:12:16  venku
   - coding convention.
   Revision 1.16  2004/02/05 18:20:58  venku
   - moved getClassesInTopologicalSortedOrder() into Util.
   - logging.
   - getClassesInTopologicalSortedOrder() was collecting the
     retain methods rather than the methods from which
     to retain. FIXED.
   Revision 1.15  2004/02/04 04:33:15  venku
   - logging.
   Revision 1.14  2004/01/31 01:49:49  venku
   - control dependence information is incorrectly used. FIXED.
   Revision 1.13  2004/01/30 23:59:00  venku
   - uses entry control DA to pick only the required exit
     points while making the slice executable.
   Revision 1.12  2004/01/25 09:06:23  venku
   - coding convention.
   Revision 1.11  2004/01/25 07:50:20  venku
   - changes to accomodate class hierarchy fixup and handling of
     statements which are marked as true but in which none of the
     expressions are marked as true.
   Revision 1.10  2004/01/24 02:03:55  venku
   - added logic to fix up class hierarchy when
     abstract methods are included in the slice.
   Revision 1.9  2004/01/22 12:30:58  venku
   - Considers pseudo tails during processing.
   Revision 1.8  2004/01/20 17:12:57  venku
   - while we include handlers, we do not include types referred
     to in the handlers.  FIXED.
   Revision 1.7  2004/01/19 22:52:49  venku
   - inclusion of method declaration with identical signature in the
     super classes/interfaces is a matter of executability.  Hence,
     this is now deferred to ExecutableSlicePostProcessor.  The
     ramifications are each method is processed in
     ExecutablePostProcessor to include methods in super classes;
     only the called methods, it's declaring class
     are included in the slice in SlicingEngine.
   - renamed generateNewCriteriaForCallToEnclosingMethod() to
     generateNewCriteriaForCallToMethod()
   -
   Revision 1.6  2004/01/19 13:03:53  venku
   - coding convention.
   Revision 1.5  2004/01/19 11:38:06  venku
   - moved findMethodInSuperClasses into Util.
   Revision 1.4  2004/01/17 00:35:35  venku
   - post process of statements was optimized.
   - handler processing was fixed.
   - processing of invoke expression was fixed.
   Revision 1.3  2004/01/15 23:20:37  venku
   - When handler unit was included for a trap, it's exception
     was not included.  FIXED.
   - Locals and traps are now removed from the body if not
     required.
   Revision 1.2  2004/01/14 11:55:45  venku
   - when pruning handlers, we need to include the rhs and lhs of
     the identity statement that occurs at the handler unit.
   Revision 1.1  2004/01/13 10:15:24  venku
   - In terms of post processing we need to do so only when we
     require executable slice and the processing is the same
     independent of the direction of the slice.  Hence, we have just
     one processor instead of 3.  Now we can have specializing
     post processor if we wanted to but I need more application
     information before I decide on this.
   Revision 1.1  2004/01/13 07:53:51  venku
   - as post processing beyond retention of semantics of slice is
     particular to the application or the tool.  Hence, moved the
     post processors into processing package under slicer tool.
   - added a new method to AbstractSliceGotoProcessor to
     process a collection of methods given a basic block graph
     manager.
   Revision 1.2  2004/01/13 04:39:29  venku
   - method and class visibility.
   Revision 1.1  2004/01/13 04:35:08  venku
   - added a new package called "processing" and it will house
     all processing done on the slice to ensure the slice satisfies
     certain properties such as executability.
   - Moved GotoProcessors into processing package.
 */
