
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
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

package edu.ksu.cis.indus.slicer;

import soot.Body;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.jimple.ThrowStmt;

import soot.toolkits.graph.UnitGraph;

import soot.util.Chain;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.controller.SimpleController;
import edu.ksu.cis.indus.staticanalyses.interfaces.AbstractController;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.indus.staticanalyses.support.Pair;
import edu.ksu.cis.indus.staticanalyses.support.Util;
import edu.ksu.cis.indus.staticanalyses.support.WorkBag;
import edu.ksu.cis.indus.transformations.common.Cloner;
import edu.ksu.cis.indus.transformations.common.ITransformMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * This class accepts slice criterions and generates slices of the given system.
 * 
 * <p>
 * The term "immediate slice" in the context of this file implies the slice containing only entities on which the given term
 * depends on, not the transitive closure.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class Slicer {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(Slicer.class);

	/**
	 * Backward slice request.
	 */
	public static final Object BACKWARD_SLICE = "BACKWARD_SLICE";

	/**
	 * Complete slice request.
	 */
	public static final Object COMPLETE_SLICE = "COMPLETE_SLICE";

	/**
	 * This just a convenience collection of the types of slices supported by this class.
	 *
	 * @invariant sliceTypes.contains(FORWARD_SLICE) and sliceTypes.contains(BACKWARD_SLICE) and sliceTypes.contains
	 * 			  (COMPLETE_SLICE)
	 */
	private static final Collection SLICE_TYPES = new HashSet();

	static {
		SLICE_TYPES.add(BACKWARD_SLICE);
		SLICE_TYPES.add(COMPLETE_SLICE);
	}

	/**
	 * This provides the cloning functionality required while creating the sliced system.
	 */
	private final Cloner cloner = new Cloner();

	/**
	 * The controller used to access the dependency analysis info during slicing.
	 *
	 * @invariant controller != null
	 */
	private AbstractController controller;

	/**
	 * The collection of slice criteria.
	 *
	 * @invariant criteria != null and criteria->forall(o | o.oclIsKindOf(AbstractSliceCriterion))
	 */
	private Collection criteria = new HashSet();

	/**
	 * This is a reference to the jimple body representation.
	 *
	 * @invariant jimple != null
	 */
	private final Jimple jimple = Jimple.v();

	/**
	 * The work bag used during slicing.
	 *
	 * @invariant workbag != null and workbag.oclIsKindOf(Bag)
	 * @invariant workbag->forall(o | o.oclIsKindOf(AbstractSliceCriterion))
	 */
	private final WorkBag workbag = new WorkBag(WorkBag.FIFO);

	/**
	 * This is the basic block graph manager which manages the BB graphs corresponding to the system being sliced/cloned.
	 *
	 * @invariant clonedGraphMgr != null
	 */
	private BasicBlockGraphMgr clonedGraphMgr = new BasicBlockGraphMgr();

	/**
	 * This provides the call graph information in the system being sliced.
	 */
	private ICallGraphInfo cgi;

	/**
	 * This maps unsliced entities to sliced entities and vice versa.
	 */
	private ITransformMap slicemap;

	/**
	 * The direction of the slice.  It's default value is <code>BACKWARD_SLICE</code>.
	 *
	 * @invariant sliceTypes.contains(sliceType)
	 */
	private Object sliceType = BACKWARD_SLICE;

	/**
	 * The class manager which manages unsliced classes.
	 */
	private Scene clazzManager;

	/**
	 * The class manager which manages sliced classes.
	 */
	private Scene slicedClazzManager;

	/**
	 * Sets the slicing criteria on which the slice should be based on.
	 *
	 * @param sliceCriteria are ofcourse the slicing criteria
	 * @param theSystem that is to be sliced.
	 * @param slicedSystem is an out parameter that will contain the sliced system after slicing.
	 * @param dependenceInfoController provides dependency information required for slicing.
	 * @param callgraph provides call graph information about the system being sliced.
	 * @param theSlicemap maps the sliced statements to the unsliced statement and vice versa. This is used by the
	 * 		  transformation may to record this mapping.  The application that uses the slicer should provide an
	 * 		  implementation of this interface to record this mapping in a sound manner as the slicer uses these mappings
	 * 		  too.
	 *
	 * @throws IllegalStateException when the given criterion are not of type<code>AbstractSliceCriterion</code>.
	 *
	 * @pre theSystem != null and callgraph != null and dependenceInfoController != null and sliceCriteria != null and
	 * 		slicedSystem != null and theSlicemap != null
	 */
	public void setSliceCriteria(final Collection sliceCriteria, final Scene theSystem, final Scene slicedSystem,
		final AbstractController dependenceInfoController, final ICallGraphInfo callgraph, final ITransformMap theSlicemap) {
		for (Iterator i = sliceCriteria.iterator(); i.hasNext();) {
			Object o = i.next();

			if (!(o instanceof SliceStmt || o instanceof SliceExpr)) {
				LOGGER.error("The work piece is not a subtype of AbstractSliceCriterion");
				throw new IllegalStateException("The work piece is not a subtype of AbstractSliceCriterion");
			}
		}
		criteria.addAll(sliceCriteria);

		clazzManager = theSystem;
		controller = dependenceInfoController;
		cgi = callgraph;
		slicedClazzManager = slicedSystem;
		slicemap = theSlicemap;
		cloner.initialize(clazzManager, slicedClazzManager, dependenceInfoController);
	}

	/**
	 * Resets internal data structures.
	 */
	public void reset() {
		clazzManager = null;
		slicedClazzManager = null;
		cloner.reset();
		criteria.clear();
		workbag.clear();
	}

	/**
	 * Slices the given system for the given criteria in the given <code>direction</code>.
	 *
	 * @param slice is the type of slice requested.  This has to be one of<code>XXX_SLICE</code> values defined in this
	 * 		  class.
	 *
	 * @throws IllegalStateException when slice criteria, class manager, or controller is unspecified.
	 * @throws IllegalArgumentException when direction is not one of the <code>XXX_SLICE</code> values.
	 *
	 * @pre slice != null
	 */
	public void slice(final Object slice) {
		if (criteria == null || criteria.size() == 0) {
			LOGGER.warn("Slice criteria is unspecified.");
			throw new IllegalStateException("Slice criteria is unspecified.");
		} else if (clazzManager == null || controller == null) {
			LOGGER.warn("Class Manager and/or Controller is unspecified.");
			throw new IllegalStateException("Class Manager and/or Controller is unspecified.");
		}

		if (!SLICE_TYPES.contains(slice)) {
			throw new IllegalArgumentException("sliceType is not one of XXX_SLICE values defined in this class.");
		}
		this.sliceType = slice;

		workbag.addAllWorkNoDuplicates(criteria);

		boolean flag = slice.equals(COMPLETE_SLICE);
		Collection processed = new HashSet();

		while (workbag.hasWork()) {
			AbstractSliceCriterion work = (AbstractSliceCriterion) workbag.getWork();

			if (processed.contains(work)) {
				continue;
			}
			processed.add(work);

			if (work instanceof SliceStmt) {
				SliceStmt temp = (SliceStmt) work;
				SootMethod sm = temp.getOccurringMethod();
				sliceStmt((Stmt) temp.getCriterion(), sm, flag || temp.isIncluded());
			} else if (work instanceof SliceExpr) {
				sliceExpr((SliceExpr) work);
			}
		}
		fixupMethods();
		slicemap.fixupMappings();
	}

	/**
	 * Retrieves the statement list for the slice version of given sliced method.
	 *
	 * @param method is the sliced method.
	 *
	 * @return the statement list for the slice version of the given method.
	 *
	 * @pre method != null
	 * @post result != null
	 */
	private PatchingChain getSliceStmtListFor(final SootMethod method) {
		SootMethod sliceMethod = cloner.getCloneOf(method);
		Body body = sliceMethod.getActiveBody();
		PatchingChain result = body.getUnits();
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */

	/*
	   private void fixupGoto(StmtList clonedSl, StmtList cloneSl, SootMethod clonedMethod, SootMethod cloneMethod) {
	       BasicBlockGraph clonedGraph = clonedGraphMgr.getBasicBlockGraph(controller.getStmtGraph(clonedMethod));
	       Collection gotos = new ArrayList();
	       for(util.Iterator k = clonedSl.iterator(); k.hasNext();) {
	           Stmt stmt = (Stmt) k.next();
	           int index = clonedSl.indexOf(stmt);
	           Stmt cloneStmt = (Stmt) cloneSl.get(index);
	           if(stmt instanceof GotoStmt) {
	               if(cloneStmt == null) {
	                   cloneStmt = cloner.cloneASTFragment(stmt, cloneMethod);
	                   cloneSl.add(index, cloneStmt);
	               }
	               gotos.add(new Pair(stmt, cloneStmt));
	           }
	       }
	       WorkBag wb = new WorkBag(WorkBag.LIFO);
	       Collection processed = new HashSet();
	       for(Iterator i = gotos.iterator(); i.hasNext();) {
	           Pair pair = (Pair) i.next();
	           GotoStmt cloned = (GotoStmt) pair.getFirst();
	           GotoStmt clone = (GotoStmt) pair.getSecond();
	           processed.clear();
	           wb.clear();
	           wb.addWork(cloned);
	   main_loop:
	               while(!wb.isEmpty()) {
	                   cloned = (GotoStmt) wb.getWork();
	                   BasicBlock bb = clonedGraph.getEnclosingBlock(cloned);
	                   processed.add(bb);
	                   List stmts = bb.getStmtsOf();
	                   int size = stmts.size();
	                   for(int j = stmts.indexOf(cloned); j < size; j++) {
	                       Stmt target = (Stmt) cloneSl.get(clonedSl.indexOf(stmts.get(j)));
	                       if(target != null) {
	                           clone.setTarget(target);
	                           break main_loop;
	                       }
	                   }
	                   for(Iterator k = bb.getSuccsOf().iterator(); k.hasNext();) {
	                       BasicBlock temp = (BasicBlock) k.next();
	                       if(!processed.contains(temp)) {
	                           wb.addWork(clonedSl.get(temp.leader));
	                       }
	                   }
	               }
	           }
	       }
	 */

	/**
	 * Fixes up the sliced methods.  This includes adjusting the targets of gotos and traps.  This also involves pruning the
	 * exception list at the method interfaces and removing any unwanted or unnecessary statements such as <code>nop</code>.
	 */
	private void fixupMethods() {
		for (Iterator i = slicedClazzManager.getClasses().iterator(); i.hasNext();) {
			SootClass slicedClass = (SootClass) i.next();
			SootClass unslicedClass = clazzManager.getSootClass(slicedClass.getName());

			for (Iterator j = slicedClass.getMethods().iterator(); j.hasNext();) {
				SootMethod slicedMethod = (SootMethod) j.next();
				SootMethod unslicedMethod =
					unslicedClass.getMethod(slicedMethod.getName(), slicedMethod.getParameterTypes(),
						slicedMethod.getReturnType());
				Body unslicedBody = unslicedMethod.getActiveBody();
				List unslicedStmtList = Arrays.asList(unslicedBody.getUnits().toArray());
				Body slicedBody = slicedMethod.getActiveBody();
				List slicedStmtList = Arrays.asList(slicedBody.getUnits().toArray());

				//fixup traps
				Chain slicedTraps = slicedBody.getTraps();

				for (Iterator k = unslicedBody.getTraps().iterator(); k.hasNext();) {
					Trap unslicedTrap = (Trap) k.next();
					Unit unslicedBeginTrap = unslicedTrap.getBeginUnit();
					Unit slicedBeginTrap = (Unit) slicemap.getTransformedStmt((Stmt) unslicedBeginTrap, unslicedMethod);
					Unit unslicedEndTrap = unslicedTrap.getEndUnit();
					Unit slicedEndTrap = (Unit) slicemap.getTransformedStmt((Stmt) unslicedEndTrap, unslicedMethod);
					Unit unslicedHandler = unslicedTrap.getHandlerUnit();
					Unit slicedHandler = (Unit) slicemap.getTransformedStmt((Stmt) unslicedHandler, unslicedMethod);
					slicedTraps.add(jimple.newTrap(cloner.getCloneOf(unslicedTrap.getException()), slicedBeginTrap,
							slicedEndTrap, slicedHandler));
				}

				/*
				 * fixing up the gotos.  We will just copy the control flow from the cloned method and use post slicing
				 * transformation to prune the code.
				 */
				for (Iterator k = unslicedStmtList.iterator(); k.hasNext();) {
					Stmt unslicedStmt = (Stmt) k.next();

					if (unslicedStmt instanceof GotoStmt) {
						int index = unslicedStmtList.indexOf(unslicedStmt);
						slicedStmtList.remove(index);

						Stmt slicedStmt = cloner.cloneASTFragment(unslicedStmt, slicedMethod);
						slicedStmtList.add(index, slicedStmt);
						slicemap.addMapping(slicedStmt, unslicedStmt, unslicedMethod);
					}
				}

				//fixup exception list
				pruneExceptionsAtMethodInterface(slicedMethod, slicedBody.getUnits());

				//prune the code to remove unwanted statements.  This will include unnecessary goto and nop statements.
				//Transformations.cleanupCode(cloneBody);
			}
		}
	}

	/**
	 * Prunes the list of exception declared as being thrown at the interface of the given method based on the statements in
	 * the method.
	 *
	 * @param sm is the method whose interface should be pruned.
	 * @param sl is the statement list of the method.
	 *
	 * @pre sm != null and sl != null
	 */
	private void pruneExceptionsAtMethodInterface(final SootMethod sm, final Chain sl) {
		Set thrownInBody = new HashSet();
		Collection thrownAtInterface = sm.getExceptions();

		for (Iterator i = sl.iterator(); i.hasNext();) {
			Stmt stmt = (Stmt) i.next();

			if (stmt instanceof ThrowStmt) {
				thrownInBody.add(slicedClazzManager.getSootClass(
						((RefType) ((ThrowStmt) stmt).getOp().getType()).getClassName()));
			} else {
				if (stmt instanceof InvokeStmt) {
					thrownInBody.addAll(stmt.getInvokeExpr().getMethod().getExceptions());
				} else if (stmt instanceof ThrowStmt) {
					SootClass exception = ((RefType) ((ThrowStmt) stmt).getOp().getType()).getSootClass();

					if (!Util.isDescendentOf(exception, "java.lang.RuntimException")) {
						thrownInBody.add(exception);
					}
				}
			}
		}

		for (Iterator i = thrownAtInterface.iterator(); i.hasNext();) {
			SootClass exception = (SootClass) i.next();

			if (!thrownInBody.contains(exception)) {
				sm.removeException(exception);
			}
		}
	}

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 *
	 * @param vBox DOCUMENT ME!
	 * @param stmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 */
	private void sliceArray(final ValueBox vBox, final Stmt stmt, final SootMethod method) {
		ArrayRef value = (ArrayRef) vBox.getValue();
		sliceLocal(value.getBaseBox(), stmt, method);

		if (value.getIndex() instanceof Local) {
			sliceLocal(value.getIndexBox(), stmt, method);
		}

		DependencyAnalysis da = (DependencyAnalysis) controller.getAnalysis(SimpleController.INTERFERENCE_DA);
		Collection slices = new HashSet();

		if (sliceType.equals(COMPLETE_SLICE)) {
			slices.addAll(da.getDependents(stmt, method));
			slices.addAll(da.getDependees(stmt, method));
		} else if (sliceType.equals(BACKWARD_SLICE)) {
			slices.addAll(da.getDependees(stmt, method));
		}
		sliceHelper(slices, method);
	}

	/**
	 * Generates immediate slice for the given expression.
	 *
	 * @param sExpr is the expression-level slice criterion.
	 */
	private void sliceExpr(final SliceExpr sExpr) {
		Stmt stmt = sExpr.getOccurringStmt();
		SootMethod method = sExpr.getOccurringMethod();
		ValueBox expr = (ValueBox) sExpr.getCriterion();

		if (sliceType.equals(COMPLETE_SLICE) || sExpr.isIncluded()) {
			sliceStmt(stmt, method, true);
		} else {
			Value value = expr.getValue();

			if (value instanceof FieldRef) {
				sliceField(expr, stmt, method);
			} else if (value instanceof ArrayRef) {
				sliceArray(expr, stmt, method);
			} else {
				for (Iterator i = value.getUseBoxes().iterator(); i.hasNext();) {
					ValueBox vBox = (ValueBox) i.next();

					if (vBox.getValue() instanceof Local) {
						sliceLocal(vBox, stmt, method);
					}
				}
			}
		}
	}

	/**
	 * Generates immediate slice for the field occurring in the given statement and method.  By nature of Jimple, only one
	 * field can be referred in a statement, hence, the arguments.
	 *
	 * @param vBox DOCUMENT ME!
	 * @param stmt in which the field occurs.
	 * @param method in which <code>stmt</code> occurs.
	 */
	private void sliceField(final ValueBox vBox, final Stmt stmt, final SootMethod method) {
		FieldRef value = (FieldRef) vBox.getValue();

		if (value instanceof InstanceFieldRef) {
			sliceLocal(((InstanceFieldRef) value).getBaseBox(), stmt, method);
		}

		DependencyAnalysis da = (DependencyAnalysis) controller.getAnalysis(SimpleController.INTERFERENCE_DA);
		Collection slices = new HashSet();

		if (sliceType.equals(COMPLETE_SLICE)) {
			slices.addAll(da.getDependents(stmt, method));
			slices.addAll(da.getDependees(stmt, method));
		} else if (sliceType.equals(BACKWARD_SLICE)) {
			slices.addAll(da.getDependees(stmt, method));
		}
		sliceHelper(slices, method);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param slices DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 */
	private void sliceHelper(final Collection slices, final SootMethod method) {
		PatchingChain slicedSL = method.getActiveBody().getUnits();
		PatchingChain sliceSL = getSliceStmtListFor(method);

		for (Iterator i = slices.iterator(); i.hasNext();) {
			Pair pair = (Pair) i.next();
			Stmt sliced = (Stmt) pair.getFirst();

			if (slicemap.getTransformedStmt(sliced, method) == null) {
				Stmt slice = cloner.cloneASTFragment(sliced, method);

				//sliceSL.add(slicedSL.indexOf(unsliced), slice);
				writeIntoAt(slice, sliceSL, sliced, slicedSL);
				slicemap.addMapping(slice, sliced, method);
				workbag.addWorkNoDuplicates(new SliceStmt((SootMethod) pair.getSecond(), sliced, true));
			}
		}
	}

	/**
	 * Generates immediate slice for the local occurrence in the given statement and method.
	 *
	 * @param vBox is the occurrence of the local.
	 * @param stmt is the statement in which <code>vBox</code> occurs.
	 * @param method is the method in which <code>stmt</code> occurs.
	 */
	private void sliceLocal(final ValueBox vBox, final Stmt stmt, final SootMethod method) {
		DependencyAnalysis da = (DependencyAnalysis) controller.getAnalysis(SimpleController.METHOD_LOCAL_DATA_DA);
		Collection slices = new HashSet();

		// add the new local
		SootMethod sliceMethod = cloner.getCloneOf(method);
		Body body = sliceMethod.getActiveBody();
		Local local = (Local) vBox.getValue();
		String lName = local.getName();

		if (cloner.getLocal(lName, sliceMethod) == null) {
			body.getLocals().add(jimple.newLocal(lName, local.getType()));
		}

		// slice
		if (sliceType.equals(COMPLETE_SLICE)) {
			slices.addAll(da.getDependents(stmt, method));
			slices.addAll(da.getDependees(new Pair(stmt, vBox), method));
		} else if (sliceType.equals(BACKWARD_SLICE)) {
			slices.addAll(da.getDependees(new Pair(stmt, vBox), method));
		}

		// update the workbag
		for (Iterator i = slices.iterator(); i.hasNext();) {
			Stmt unsliced = (Stmt) i.next();
			workbag.addWorkNoDuplicates(new SliceStmt(method, unsliced, true));
		}
	}

	/**
	 * Generates immediate slice for the given statement and method.
	 *
	 * @param stmt is the statement-level slice criterion.
	 * @param method is the method in which <code>stmt</code> occurs.
	 * @param inclusive <code>true</code> if all entities in <code>stmt</code> should be included in the slice;
	 * 		  <code>false</code>, otherwise.
	 */
	private void sliceStmt(final Stmt stmt, final SootMethod method, final boolean inclusive) {
		PatchingChain slicedSL = method.getActiveBody().getUnits();
		PatchingChain sliceSL = getSliceStmtListFor(method);

		if (inclusive) {
			InvokeExpr expr = null;

			if (stmt instanceof InvokeStmt) {
				expr = ((InvokeStmt) stmt).getInvokeExpr();
			} else if (stmt instanceof AssignStmt && ((AssignStmt) stmt).getRightOp() instanceof InvokeExpr) {
				expr = (InvokeExpr) ((AssignStmt) stmt).getRightOp();
			}

			if (expr != null) {
				// add exit points of callees as the slice criteria
				Context context = new Context();
				context.setRootMethod(method);
				context.setStmt(stmt);

				Collection callees = cgi.getCallees(expr, context);

				for (Iterator i = callees.iterator(); i.hasNext();) {
					CallTriple ctrp = (CallTriple) i.next();
					SootMethod callee = ctrp.getMethod();
					UnitGraph stmtGraph = controller.getStmtGraph(callee);
					BasicBlockGraph bbg = clonedGraphMgr.getBasicBlockGraph(stmtGraph);

					for (Iterator j = bbg.getTails().iterator(); j.hasNext();) {
						BasicBlock bb = (BasicBlock) j.next();
						workbag.addWorkNoDuplicates(new SliceStmt(callee, bb.getTrailer(), true));
					}
				}
			}

			for (Iterator i = stmt.getUseAndDefBoxes().iterator(); i.hasNext();) {
				ValueBox vBox = (ValueBox) i.next();
				Value value = vBox.getValue();

				if (value instanceof FieldRef) {
					sliceField(vBox, stmt, method);
				} else if (value instanceof ArrayRef) {
					sliceArray(vBox, stmt, method);
				} else if (value instanceof Local) {
					sliceLocal(vBox, stmt, method);
				}
			}

			Stmt slice = cloner.cloneASTFragment(stmt, method);

			//int index = slicedSL.indexOf(stmt);
			//sliceSL.remove(index);
			//sliceSL.add(index, slice);
			writeIntoAt(slice, sliceSL, stmt, slicedSL);
			slicemap.addMapping(slice, stmt, method);
		}

		Collection slices = new HashSet();

		DependencyAnalysis[] da = new DependencyAnalysis[3];
		da[0] = (DependencyAnalysis) controller.getAnalysis(SimpleController.CONTROL_DA);
		da[1] = (DependencyAnalysis) controller.getAnalysis(SimpleController.SYNCHRONIZATION_DA);
		da[2] = (DependencyAnalysis) controller.getAnalysis(SimpleController.DIVERGENCE_DA);

		//da[3] = controller.getDAnalysis(Controller.READY_DA);
		if (sliceType.equals(COMPLETE_SLICE)) {
			for (int i = da.length - 1; i >= 0; i--) {
				slices.addAll(da[i].getDependents(stmt, method));
				slices.addAll(da[i].getDependees(stmt, method));
			}
		} else if (sliceType.equals(BACKWARD_SLICE)) {
			for (int i = da.length - 1; i >= 0; i--) {
				slices.addAll(da[i].getDependees(stmt, method));
			}
		}

		for (Iterator i = slices.iterator(); i.hasNext();) {
			Stmt sliced = (Stmt) i.next();

			if (slicemap.getTransformedStmt(sliced, method) == null) {
				Stmt slice = cloner.cloneASTFragment(sliced, method);

				//sliceSL.add(slicedSL.indexOf(sliced), slice);
				writeIntoAt(slice, sliceSL, sliced, slicedSL);
				slicemap.addMapping(slice, sliced, method);
				workbag.addWorkNoDuplicates(new SliceStmt(method, sliced, true));
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param writeData DOCUMENT ME!
	 * @param writeChain DOCUMENT ME!
	 * @param posData DOCUMENT ME!
	 * @param posChain DOCUMENT ME!
	 */
	private void writeIntoAt(final Object writeData, final PatchingChain writeChain, final Object posData,
		final PatchingChain posChain) {
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/05/22 22:23:49  venku
   Changed interface names to start with a "I".
   Formatting.
 */
