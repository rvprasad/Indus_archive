
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

package edu.ksu.cis.indus.tools.slicer.processing;

import edu.ksu.cis.indus.annotations.AEmpty;
import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.interfaces.IClassHierarchy;

import edu.ksu.cis.indus.slicer.SliceCollector;

import edu.ksu.cis.indus.staticanalyses.dependency.NonTerminationSensitiveEntryControlDA;
import edu.ksu.cis.indus.staticanalyses.impl.ClassHierarchy;

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

import soot.ArrayType;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.TrapManager;
import soot.Type;
import soot.Value;

import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceOfExpr;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;

import soot.toolkits.graph.UnitGraph;


/**
 * This process a vanilla backward and complete slice into an executable slice.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ExecutableSlicePostProcessor
  implements ISlicePostProcessor {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutableSlicePostProcessor.class);

    /** 
     * The slice collector to be used to add on to the slice.
     */
    protected SliceCollector collector;

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
	private final Collection<SootMethod> processedMethodCache = new HashSet<SootMethod>();

	/** 
	 * This is the workbag of methods to process.
	 *
	 * @invariant methodWorkBag != null and methodWorkBag.getWork().oclIsKindOf(SootMethod)
	 */
	private final IWorkBag<SootMethod> methodWorkBag = new HistoryAwareFIFOWorkBag<SootMethod>(processedMethodCache);

	/** 
	 * This provides entry-based control dependency information required to include exit points.
	 */
	private NonTerminationSensitiveEntryControlDA cd = new NonTerminationSensitiveEntryControlDA();

	/** 
	 * This indicates if any statements of the method were included during post processing.  If so, other statement based
	 * post processings are triggered.
	 */
	private boolean stmtCollected;

	/**
	 * Creates an instance of this class.
	 */
	@AEmpty public ExecutableSlicePostProcessor() {
		// does nothing
	}

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
	public final void process(final Collection<SootMethod> taggedMethods, final BasicBlockGraphMgr basicBlockMgr,
		final SliceCollector theCollector) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Post Processing - " + theCollector.toString());
		}

		collector = theCollector;
		bbgMgr = basicBlockMgr;
		cd.setBasicBlockGraphManager(basicBlockMgr);
		methodWorkBag.addAllWorkNoDuplicates(taggedMethods);

		// process the methods and gather the collected classes
		while (methodWorkBag.hasWork()) {
			final SootMethod _method = methodWorkBag.getWork();

			processMethod(_method);

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
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Could not process method " + _method.getSignature());
				}
			}
		}

		fixupAbstractMethodsInClassHierarchy();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Post Processing - " + theCollector.toString());
		}
	}

	/**
	 * Resets internal data structure.
	 */
	public final void reset() {
		methodWorkBag.clear();
		processedMethodCache.clear();
	}

	/**
	 * Retrieves a class hierarchy containing the given classes.  This implementation will include classes that are not
	 * mentioned in <code>classes</code> but are required to realize the hierarchy.  Hence, the only requirement is that all
	 * provided classes should  be in captured in the returned hierarchy.
	 *
	 * @param classes of interest.
	 *
	 * @return a class hierarchy.
	 *
	 * @post result != null
	 * @post result.getClasses()->union(result.getInterfaces())->includesAll(classes)
	 */
	protected IClassHierarchy getClassHierarchyContainingClasses(final Collection<SootClass> classes) {
		return ClassHierarchy.createClassHierarchyFrom(classes);
	}

	/**
	 * Fix up class hierarchy such that all abstract methods have an implemented counterpart in the slice.
	 */
	private void fixupAbstractMethodsInClassHierarchy() {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("BEGIN: Fixing Class Hierarchy - " + getClass() + " " + collector.getClassesInSlice());
        }
        
		// setup the variables for fixing the class hierarchy
		final IClassHierarchy _ch = getClassHierarchyContainingClasses(collector.getClassesInSlice());
		final Collection<SootClass> _topologicallyOrderedClasses = _ch.getClassesInTopologicalOrder(true);
		final Map<SootClass, Collection<SootMethod>> _class2abstractMethods = new HashMap<SootClass, Collection<SootMethod>>();
        collector.includeInSlice(_ch.getClasses());
        collector.includeInSlice(_ch.getInterfaces());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Topological Sort: " + _topologicallyOrderedClasses);
		}

		// fixup methods with respect the class hierarchy  
		for (final Iterator<SootClass> _i = _topologicallyOrderedClasses.iterator(); _i.hasNext();) {
			final SootClass _currClass = _i.next();
			final Collection<SootMethod> _abstractMethodsAtCurrClass =
				gatherCollectedAbstractMethodsInSuperClasses(_class2abstractMethods, _currClass);
			final List<SootMethod> _methods = _currClass.getMethods();
			final Collection<SootMethod> _collectedMethods =  collector.getCollected(_methods);
			final Collection<SootMethod> _unCollectedMethods = SetUtils.difference(_methods, _collectedMethods);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Fixing up " + _currClass);
			}

			// remove all abstract methods which are overridden by collected methods of this class.
			Util.removeMethodsWithSameSignature(_abstractMethodsAtCurrClass, _collectedMethods);
			// gather uncollected methods of this class which have the same signature as any gathered "super" abstract 
			// methods.
			Util.retainMethodsWithSameSignature(_unCollectedMethods, _abstractMethodsAtCurrClass);
			// remove abstract counterparts of all methods that were included in the slice in the previous step.
			Util.removeMethodsWithSameSignature(_abstractMethodsAtCurrClass, _unCollectedMethods);
			// include the uncollected concrete methods into the slice and process them
			collector.includeInSlice(_unCollectedMethods);

			// gather collected abstract methods in this class/interface
			if (_currClass.isInterface()) {
				_abstractMethodsAtCurrClass.addAll(_collectedMethods);
			} else if (_currClass.isAbstract()) {
				for (final Iterator<SootMethod> _j = _collectedMethods.iterator(); _j.hasNext();) {
					final SootMethod _sm = _j.next();

					if (_sm.isAbstract()) {
						_abstractMethodsAtCurrClass.add(_sm);
					}
				}
			}

			// record the abstract methods
			if (!_abstractMethodsAtCurrClass.isEmpty()) {
				_class2abstractMethods.put(_currClass, new ArrayList<SootMethod>(_abstractMethodsAtCurrClass));
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
	 * @pre class2abstractMethods != null
	 * @pre clazz != null
	 * @post result != null
	 * @post result->forall(o | class2abstractMethods->exists(p | p.values().includes(o)))
	 */
	private Collection<SootMethod> gatherCollectedAbstractMethodsInSuperClasses(final Map<SootClass, Collection<SootMethod>> class2abstractMethods, final SootClass clazz) {
		final Collection<SootMethod> _methods = new HashSet<SootMethod>();

		// gather collected abstract methods from super interfaces and classes.
		for (@SuppressWarnings("unchecked") final Iterator<SootClass> _j = clazz.getInterfaces().iterator(); _j.hasNext();) {
			final SootClass _interface =  _j.next();

			if (collector.hasBeenCollected(_interface)) {
				final Collection<SootMethod> _abstractMethods = class2abstractMethods.get(_interface);

				if (_abstractMethods != null) {
					_methods.addAll(_abstractMethods);
				}
			}
		}

		if (clazz.hasSuperclass()) {
			final SootClass _superClass = clazz.getSuperclass();

			if (collector.hasBeenCollected(_superClass)) {
				final Collection<SootMethod> _abstractMethods = class2abstractMethods.get(_superClass);

				if (_abstractMethods != null) {
					_methods.addAll(_abstractMethods);
				}
			}
		}
		return _methods;
	}

	/**
	 * Picks a return point from the given set of return points.  All other options should be tried and it should be certain
	 * that a  random choice is safe.
	 *
	 * @param returnPoints from which to pick a random one.
	 *
	 * @pre returnPoints != null and returnPoints.oclIsKindOf(Collection(Stmt))
	 */
	private void pickARandomReturnPoint(final Collection<BasicBlock> returnPoints) {
		Stmt _exitStmt = null;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("pickARandomReturnPoint(returnPoints = " + returnPoints + ")");
		}

		for (final Iterator<BasicBlock> _i = returnPoints.iterator(); _i.hasNext();) {
			final BasicBlock _bb = _i.next();
			final Stmt _stmt = _bb.getTrailerStmt();

			if (_stmt instanceof ReturnStmt || _stmt instanceof ReturnVoidStmt) {
				// if there exists a return statement grab it.
				_exitStmt = _stmt;
				break;
			} else if (_stmt instanceof ThrowStmt) {
				// if there have been no return statements, then grab a throw statement and continue to look.
				_exitStmt = _stmt;
			} else if (_exitStmt == null) {
				// if there have been no exit points, then grab the psedu exit and continue to look.
				_exitStmt = _stmt;
			}
		}
		processAndIncludeExitStmt(_exitStmt);
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
		final Collection<BasicBlock> _tails = new HashSet<BasicBlock>();
		_tails.addAll(_bbg.getTails());
		cd.analyze(Collections.singleton(method));

		if (_tails.size() == 1) {
			// If there is only one tail then include the statement
			final BasicBlock _bb = _tails.iterator().next();
			processAndIncludeExitStmt(_bb.getTrailerStmt());
		} else {
			boolean _tailWasNotPicked = true;

			// if there are more than one tail then pick only the ones that are reachable via collected statements
			for (final Iterator<BasicBlock> _j = _tails.iterator(); _j.hasNext();) {
				final BasicBlock _bb = _j.next();
				final Stmt _stmt = _bb.getTrailerStmt();
				final Collection<Stmt> _dependees = cd.getDependees(_stmt, method);

				if (!collector.hasBeenCollected(_stmt)
					  && (_dependees.isEmpty() || !Util.getHostsWithTag(_dependees, _tagName).isEmpty())) {
					processAndIncludeExitStmt(_stmt);
					_tailWasNotPicked = false;

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Picked " + _stmt + " in " + method);
					}
				}
			}

			/*
			 * It might be the case that all tails are control dependent on some dependee and none of these dependees are
			 * in the slice.  In this case, _flag will always be false in the above scenario.  In such cases, we pick the
			 * first return statement.  If none found, then first throw statetement. If none found, some arbitrary tail node.
			 */
			if (_tailWasNotPicked) {
				pickARandomReturnPoint(_tails);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Picking return points in " + method);
		}
	}

	/**
	 * Processes the given exit statement and also includes it into the slice.   It handles <code>throw</code> statements
	 * approriately.  Refer to <code>processThrowStmt</code> for details.
	 *
	 * @param exitStmt to be included.
	 *
	 * @pre exitStmt != null
	 */
	private void processAndIncludeExitStmt(final Stmt exitStmt) {
		if (exitStmt instanceof ThrowStmt) {
			processThrowStmt((ThrowStmt) exitStmt);
		}
		collector.includeInSlice(exitStmt);
	}

	/**
	 * Process assignment statements to include the classes of the types appearing in them.
	 *
	 * @param assignStmt to be processed.
	 *
	 * @pre assignStmt != null
	 */
	private void processAssignmentsForTypes(final Stmt assignStmt) {
		final Value _rightOp = ((AssignStmt) assignStmt).getRightOp();
		Type _type = null;

		if (_rightOp instanceof CastExpr) {
			final CastExpr _v = (CastExpr) _rightOp;
			_type = _v.getCastType();
		} else if (_rightOp instanceof InstanceOfExpr) {
			final InstanceOfExpr _v = (InstanceOfExpr) _rightOp;
			_type = _v.getCheckType();
		}

		if (_type != null) {
			if (_type instanceof ArrayType) {
				_type = ((ArrayType) _type).baseType;
			}

			if (_type instanceof RefType) {
				final SootClass _sootClass = ((RefType) _type).getSootClass();
				collector.includeInSlice(_sootClass);
			}
		}
	}

	/**
	 * Marks the traps to be included in the slice.  The statement is assumed to be in the slice.
	 *
	 * @param method in which the statement occurs.
	 * @param stmt will trigger the traps to include.
	 * @param bbg is the basic block graph of the method.
	 *
	 * @pre method != null and stmt != null and bbg != null and collector.hasBeenCollected(stmt)
	 */
	private void processHandlers(final SootMethod method, final Stmt stmt, final BasicBlockGraph bbg) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Pruning handlers " + stmt + "@" + method);
		}

		/*
		 * Include the first statement of the handler for all traps found to cover atleast one statement included in the
		 * slice.
		 */
		for (final Iterator _i = TrapManager.getTrapsAt(stmt, method.retrieveActiveBody()).iterator(); _i.hasNext();) {
			final Trap _trap = (Trap) _i.next();
			final IdentityStmt _handlerUnit = (IdentityStmt) _trap.getHandlerUnit();

			if (bbg.getEnclosingBlock(_handlerUnit) != null) {
				collector.includeInSlice(_handlerUnit);
				collector.includeInSlice(_handlerUnit.getLeftOpBox());
				collector.includeInSlice(_handlerUnit.getRightOpBox());

				final SootClass _exceptionClass = _trap.getException();
				collector.includeInSlice(Util.getAncestors(_exceptionClass));
				collector.includeInSlice(_exceptionClass);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Pruning handlers " + stmt + "@" + method);
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
	private void processMethod(final SootMethod method) {
		final Collection<Type> _temp = new HashSet<Type>();

		for (final Iterator _i = Util.findMethodInSuperClassesAndInterfaces(method).iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			collector.includeInSlice(_sm.getDeclaringClass());
			collector.includeInSlice(_sm);
			_temp.clear();
			_temp.add(_sm.getReturnType());
			_temp.addAll(_sm.getParameterTypes());

			for (final Iterator<Type> _j = _temp.iterator(); _j.hasNext();) {
				final Type _type = _j.next();

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

		final BasicBlockGraph _bbg = bbgMgr.getBasicBlockGraph(method);
		final UnitGraph _unitGraph = _bbg.getStmtGraph();

		for (final Iterator _i = _unitGraph.iterator(); _i.hasNext();) {
			final Stmt _stmt = (Stmt) _i.next();

			if (_stmt instanceof IdentityStmt) {
				final IdentityStmt _identityStmt = (IdentityStmt) _stmt;
				final Value _rhs = _identityStmt.getRightOp();

				if (_rhs instanceof ThisRef || _rhs instanceof ParameterRef) {
					collector.includeInSlice(_identityStmt.getLeftOpBox());
					collector.includeInSlice(_identityStmt.getRightOpBox());
					collector.includeInSlice(_identityStmt);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Picked " + _identityStmt + " in " + method);
					}
					processHandlers(method, _stmt, _bbg);
					stmtCollected = true;
				}
			} else if (collector.hasBeenCollected(_stmt)) {
				if (_stmt.containsInvokeExpr() && !(_stmt.getInvokeExpr() instanceof StaticInvokeExpr)) {
					/*
					 * If an invoke expression occurs in the slice, the slice will include only the invoked method and not any
					 * incarnations of it in it's ancestral classes.  This will lead to unverifiable system of classes.
					 * This can be fixed by sucking all the method definitions that need to make the system verifiable
					 * and empty bodies will be substituted for such methods.
					 */
					processMethod(_stmt.getInvokeExpr().getMethod());

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Included method invoked at " + _stmt + " in " + method);
					}
				} else if (_stmt instanceof ThrowStmt) {
					processThrowStmt((ThrowStmt) _stmt);
				} else if (_stmt instanceof AssignStmt) {
					processAssignmentsForTypes(_stmt);
				}
				processHandlers(method, _stmt, _bbg);
				stmtCollected = true;
			}
		}
	}

	/**
	 * Processes the throw statement to include any required classes, if necessary.
	 *
	 * @param throwStmt to be processed.
	 *
	 * @pre throwStmt != null
	 */
	private void processThrowStmt(final ThrowStmt throwStmt) {
		if (!collector.hasBeenCollected(throwStmt.getOpBox())) {
			final SootClass _exceptionClass = ((RefType) throwStmt.getOp().getType()).getSootClass();
			collector.includeInSlice(_exceptionClass);

			// the ancestors will be added when we fix up the class hierarchy.
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Included classes of exception thrown at " + throwStmt);
			}
		}
	}
}

// End of File
