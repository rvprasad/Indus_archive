
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

import edu.ksu.cis.indus.common.datastructures.FIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph;
import edu.ksu.cis.indus.common.graph.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.slicer.SliceCollector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.Trap;
import soot.TrapManager;
import soot.Value;
import soot.ValueBox;

import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
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
	 * This is the traps of a method that need to be retained.
	 *
	 * @invariant trapsToRetain != null
	 * @invariant trapsToRetain.oclIsKindOf(Set(Trap))
	 */
	private final Collection trapsToRetain = new HashSet();

	/**
	 * This is the workbag of methods to process.
	 *
	 * @invariant methodWorkBag != null and methodWorkBag.getWork().oclIsKindOf(SootMethod)
	 */
	private final IWorkBag methodWorkBag = new FIFOWorkBag();

	/**
	 * This is the workbag of statements to process.
	 *
	 * @invariant stmtWorkBag != null and stmtWorkBag.getWork().oclIsKindOf(Stmt)
	 */
	private final IWorkBag stmtWorkBag = new FIFOWorkBag();

	/**
	 * The slice collector to be used to add on to the slice.
	 */
	private SliceCollector collector;

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
		collector = theCollector;
		bbgMgr = basicBlockMgr;
		methodWorkBag.addAllWorkNoDuplicates(taggedMethods);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Post Processing.");
		}

		while (methodWorkBag.hasWork()) {
			final SootMethod _method = (SootMethod) methodWorkBag.getWork();
			processedMethodCache.add(_method);
			processMethods(_method);

			if (_method.isConcrete()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Post Processing method " + _method);
				}
				processStmts(_method);
				pickReturnPoints(_method);
				pruneHandlers(_method);
				pruneLocals(_method);
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Could not get body for method " + _method.getSignature());
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
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
	 * Adds the given method to <code>methodWorkBag</code> if it was not processed earlier.
	 *
	 * @param method to be added.
	 *
	 * @pre method != null
	 */
	private void addToMethodWorkBag(final SootMethod method) {
		if (!processedMethodCache.contains(method)) {
			methodWorkBag.addWorkNoDuplicates(method);
		}
	}

	/**
	 * Adds the given statement to <code>stmtWorkBag</code>.
	 *
	 * @param stmt to be added.
	 *
	 * @pre stmt != null
	 */
	private void addToStmtWorkBag(final Stmt stmt) {
		if (!processedStmtCache.contains(stmt)) {
			stmtWorkBag.addWorkNoDuplicates(stmt);
		}
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
			LOGGER.debug("Picking up return points in " + method);
		}

		// TODO: There may be methods without tails.  We should detect psuedo-tails and include them.
		// pick all return/throw points in the methods.
		final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(method);
		final Collection _tails = _bbg.getTails();

		for (final Iterator _j = _tails.iterator(); _j.hasNext();) {
			final BasicBlock _bb = (BasicBlock) _j.next();
			final Stmt _stmt = _bb.getTrailerStmt();

			if (_stmt.getTag(collector.getTagName()) == null) {
				collector.includeInSlice(_stmt);
				collector.includeInSlice(method);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Picked " + _stmt + " in " + method);
				}
			}
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
			LOGGER.debug("Pruning handlers in " + method);
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
			final IdentityStmt _handlerUnit = (IdentityStmt) _trap.getHandlerUnit();
			collector.includeInSlice(_handlerUnit);
			collector.includeInSlice(_handlerUnit.getLeftOpBox());
			collector.includeInSlice(_handlerUnit.getRightOpBox());
			collector.includeInSlice(_trap.getException());
			addToStmtWorkBag(_handlerUnit);
		}
		trapsToRetain.addAll(_temp);
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
		for (final Iterator _i = Util.findMethodInSuperClassesAndInterfaces(method).iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			collector.includeInSlice(_sm.getDeclaringClass());
			collector.includeInSlice(_sm);
			addToMethodWorkBag(_sm);
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
			processedStmtCache.add(_stmt);

			if (collector.hasBeenCollected(_stmt)) {
				if (_stmt instanceof IdentityStmt) {
					processIdentityStmt(method, (IdentityStmt) _stmt);
				}

				if (_stmt.containsInvokeExpr() && collector.hasBeenCollected(_stmt)) {
					/*
					 * If an invoke expression occurs in the slice, the slice will include only the invoked method and not any
					 * incarnations of it in it's ancestral classes.  This will lead to unverifiable system of classes.
					 * This can be fixed by sucking all the method definitions that need to make the system verifiable
					 * and empty bodies will be substituted for such methods.
					 */
					final InvokeExpr _expr = _stmt.getInvokeExpr();

					if (!(_expr instanceof StaticInvokeExpr)) {
						processMethods(_expr.getMethod());

						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Included method invoked at " + _stmt + " in " + method);
						}
					}
				}
				processHandlers(method, _stmt);
			}
		}
	}

	/**
	 * Prunes the exception handlers in the given method's slice body.
	 *
	 * @param method in which the exception handlers need to be pruned.
	 *
	 * @pre method != null and method.isConcrete() and method.hasActiveBody()
	 */
	private void pruneHandlers(final SootMethod method) {
		final Body _body = method.getActiveBody();
		_body.getTraps().retainAll(trapsToRetain);
	}

	/**
	 * Prunes the locals in the given method's slice body.
	 *
	 * @param method in which to process the locals.
	 *
	 * @pre method != null and method.isConcrete() and method.hasActiveBody()
	 */
	private void pruneLocals(final SootMethod method) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Pruning locals in " + method);
		}

		final Body _body = method.getActiveBody();
		final Collection _localsToKeep = new ArrayList();

		for (final Iterator _j = _body.getUnits().iterator(); _j.hasNext();) {
			final Stmt _stmt = (Stmt) _j.next();

			if (collector.hasBeenCollected(_stmt)) {
				for (final Iterator _k = _stmt.getUseAndDefBoxes().iterator(); _k.hasNext();) {
					final ValueBox _vBox = (ValueBox) _k.next();
					final Value _value = _vBox.getValue();

					if (collector.hasBeenCollected(_vBox) && _value instanceof Local) {
						_localsToKeep.add(_value);
					}
				}
			}
		}

		_body.getLocals().retainAll(_localsToKeep);
	}
}

/*
   ChangeLog:
   $Log$
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
