
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

package edu.ksu.cis.indus.transformations.slicer;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.SootClass;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;

import soot.jimple.toolkits.scalar.NopEliminator;

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
import edu.ksu.cis.indus.transformations.common.ITransformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * This class accepts slice criterions and generates slices of the given system.
 * 
 * <p>
 * The term "immediate slice" in the context of this file implies the slice containing only entities on which the given term
 * depends on, not the transitive closure.
 * </p>
 * 
 * <p>
 * There are 2 flavours of executable slicing: forward and backward.  Backward slicing is inclusion of anything that leads to
 * the slice criterion from the given entry points to the system.  This can provide a executable system which will  simulate
 * the given system along all paths from the entry points leading to the slice criterion independent of the input.   In case
 * the input causes a divergence in this path then the simulation ends there.
 * </p>
 * 
 * <p>
 * However, in case of forward slicing, one would include everything that is affected by the slice criterion.  This  will
 * never lead to an semantically meaningful executable slice as the part of the system that leads to the slice criterion is
 * not captured. Rather a more meaningful notion is that of a complete slice. This includes everything that affects the
 * given slice criterion and  everything affected by the slice criterion.
 * </p>
 * 
 * <p>
 * Due to the above view we only support backward and complete slicing.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SlicingEngine {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SlicingEngine.class);

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
	 * The controller used to access the dependency analysis info during slicing.
	 */
	private AbstractController controller;

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
	 * The ids of the dependencies to be considered for slicing.
	 */
	private final Collection dependencies = new HashSet();

	/**
	 * The dependency analyses to be considered for intra-procedural slicing.
	 */
	private final Collection intraProceduralDependencies = new ArrayList();

	/**
	 * This provides the call graph information in the system being sliced.
	 */
	private ICallGraphInfo cgi;

	/**
	 * This transforms the system based on the slicing decision of this object.
	 */
	private ITransformer transformer;

	/**
	 * The direction of the slice.  It's default value is <code>BACKWARD_SLICE</code>.
	 *
	 * @invariant sliceTypes.contains(sliceType)
	 */
	private Object sliceType = BACKWARD_SLICE;

	/**
	 * Sets the slicing criteria on which the slice should be based on.
	 *
	 * @param sliceCriteria are ofcourse the slicing criteria
	 * @param dependenceInfoController provides dependency information required for slicing.
	 * @param callgraph provides call graph information about the system being sliced.
	 * @param sliceTransformer transforms the system based on the slicing decisions of this object.  The provided
	 * 		  implementation should provide sound and complete information as the engine will use this information to while
	 * 		  slicing.
	 * @param dependenciesToUse is the ids of the dependecies to be considered for slicing.
	 *
	 * @throws IllegalStateException when the given criterion are not of type<code>AbstractSliceCriterion</code>.
	 *
	 * @pre callgraph != null and dependenceInfoController != null and sliceCriteria != null and
	 * 		sliceTransformer != null and dependenciesToUse != null
	 * @pre dependeciesToUse->forall(o | controller.getAnalysis(o) != null)
	 */
	public void setSliceCriteria(final Collection sliceCriteria, final AbstractController dependenceInfoController,
		final ICallGraphInfo callgraph, final ITransformer sliceTransformer, final Collection dependenciesToUse) {
		for (Iterator i = sliceCriteria.iterator(); i.hasNext();) {
			Object o = i.next();

			if (!(o instanceof SliceStmt || o instanceof SliceExpr)) {
				LOGGER.error("The work piece is not a subtype of AbstractSliceCriterion");
				throw new IllegalStateException("The work piece is not a subtype of AbstractSliceCriterion");
			}
		}
		criteria.addAll(sliceCriteria);

		controller = dependenceInfoController;
		cgi = callgraph;
		transformer = sliceTransformer;
		dependencies.addAll(dependenciesToUse);
		intraProceduralDependencies.clear();

		for (Iterator i = dependencies.iterator(); i.hasNext();) {
			Object id = i.next();

			if (id.equals(SimpleController.METHOD_LOCAL_DATA_DA)
				  || id.equals(SimpleController.SYNCHRONIZATION_DA)
				  || id.equals(SimpleController.CONTROL_DA)) {
				intraProceduralDependencies.add(controller.getAnalysis(id));
			}
		}
	}

	/**
	 * Resets internal data structures and removes all references to objects provided at initialization time. For other
	 * operations to be meaningful following a call to this method, the user should call <code>initialize</code> before
	 * calling any other methods.
	 */
	public void reset() {
		cgi = null;
		transformer = null;
		criteria.clear();

		// clear the work bag of slice criterion
		while (workbag.hasWork()) {
			AbstractSliceCriterion work = (AbstractSliceCriterion) workbag.getWork();
			work.sliced();
		}
	}

	/**
	 * Slices the system provided at initialization for the initialized criteria to generate the given type of slice..
	 *
	 * @param theSliceType is the type of slice requested.  This has to be one of<code>XXX_SLICE</code> values defined in
	 * 		  this class.
	 *
	 * @throws IllegalStateException when slice criteria, class manager, or controller is unspecified.
	 * @throws IllegalArgumentException when direction is not one of the <code>XXX_SLICE</code> values.
	 *
	 * @pre theSliceType != null
	 */
	public void slice(final Object theSliceType) {
		if (criteria == null || criteria.size() == 0) {
			LOGGER.warn("Slice criteria is unspecified.");
			throw new IllegalStateException("Slice criteria is unspecified.");
		} else if (controller == null) {
			LOGGER.warn("Class Manager and/or Controller is unspecified.");
			throw new IllegalStateException("Class Manager and/or Controller is unspecified.");
		}

		if (!SLICE_TYPES.contains(theSliceType)) {
			throw new IllegalArgumentException("sliceType is not one of XXX_SLICE values defined in this class.");
		}
		sliceType = theSliceType;

		workbag.addAllWorkNoDuplicates(criteria);

		boolean flag = theSliceType.equals(COMPLETE_SLICE);

		// we are assuming the mapping will capture the past-processed information to prevent processed criteria from 
		// reappearing.  
		while (workbag.hasWork()) {
			AbstractSliceCriterion work = (AbstractSliceCriterion) workbag.getWork();

			if (work instanceof SliceStmt) {
				SliceStmt temp = (SliceStmt) work;
				SootMethod sm = temp.getOccurringMethod();
				sliceStmt((Stmt) temp.getCriterion(), sm, flag || temp.isIncluded());
			} else if (work instanceof SliceExpr) {
				sliceExpr((SliceExpr) work);
			}
			work.sliced();
		}
		fixupMethods();
		transformer.completeTransformation();
	}

	/**
	 * Retrieves the sliced statement corresponding to the given unsliced statement.
	 *
	 * @param unslicedStmt for which the corresponding sliced statement is requested.
	 * @param unslicedMethod in which <code>unslicedStmt</code> occurs.
	 *
	 * @return the sliced statement corresponding to the given unsliced statement
	 *
	 * @pre unslicedStmt != null and unslicedMethod != null
	 * @post result != null
	 */
	private Stmt getSlicedStmt(final Stmt unslicedStmt, final SootMethod unslicedMethod) {
		Stmt result = null;
		SootMethod slicedMethod = transformer.getTransformed(unslicedMethod);
		Iterator j = slicedMethod.getActiveBody().getUnits().iterator();

		for (Iterator i = unslicedMethod.getActiveBody().getUnits().iterator(); i.hasNext();) {
			Stmt stmt = (Stmt) i.next();
			result = (Stmt) j.next();

			if (stmt.equals(unslicedStmt)) {
				break;
			}
		}
		return result;
	}

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
		NopEliminator nopTranformation = NopEliminator.v();

		for (Iterator i = transformer.getTransformedClasses().iterator(); i.hasNext();) {
			SootClass slicedClass = (SootClass) i.next();
			SootClass unslicedClass = transformer.getUntransformed(slicedClass);

			for (Iterator j = slicedClass.getMethods().iterator(); j.hasNext();) {
				SootMethod slicedMethod = (SootMethod) j.next();
				SootMethod unslicedMethod =
					unslicedClass.getMethod(slicedMethod.getName(), slicedMethod.getParameterTypes(),
						slicedMethod.getReturnType());
				Body unslicedBody = unslicedMethod.getActiveBody();
				Body slicedBody = slicedMethod.getActiveBody();

				//fixup traps
				Chain slicedTraps = slicedBody.getTraps();

				for (Iterator k = unslicedBody.getTraps().iterator(); k.hasNext();) {
					Trap unslicedTrap = (Trap) k.next();
					Unit unslicedBeginTrap = unslicedTrap.getBeginUnit();
					Unit slicedBeginTrap = (Unit) transformer.getTransformed((Stmt) unslicedBeginTrap, unslicedMethod);
					Unit unslicedEndTrap = unslicedTrap.getEndUnit();
					Unit slicedEndTrap = (Unit) transformer.getTransformed((Stmt) unslicedEndTrap, unslicedMethod);
					Unit unslicedHandler = unslicedTrap.getHandlerUnit();
					Unit slicedHandler = (Unit) transformer.getTransformed((Stmt) unslicedHandler, unslicedMethod);
					slicedTraps.add(jimple.newTrap(transformer.getTransformed(unslicedTrap.getException()), slicedBeginTrap,
							slicedEndTrap, slicedHandler));
				}

				/*
				 * fixing up the gotos.  We will just copy the control flow from the sliced method and use post slicing
				 * transformation to prune the code.
				 */
				for (Iterator k = unslicedBody.getUnits().iterator(); k.hasNext();) {
					Stmt unslicedStmt = (Stmt) k.next();

					if (unslicedStmt.branches()) {
						if (unslicedStmt instanceof GotoStmt) {
							GotoStmt slicedStmt = (GotoStmt) transformer.getTransformed(unslicedStmt, unslicedMethod);
							slicedStmt.setTarget(getSlicedStmt((Stmt) slicedStmt.getTarget(), unslicedMethod));
						} else if (unslicedStmt instanceof IfStmt) {
							IfStmt slicedStmt = (IfStmt) transformer.getTransformed(unslicedStmt, unslicedMethod);
							slicedStmt.setTarget(getSlicedStmt(slicedStmt.getTarget(), unslicedMethod));
						} else if (unslicedStmt instanceof LookupSwitchStmt) {
							LookupSwitchStmt slicedStmt =
								(LookupSwitchStmt) transformer.getTransformed(unslicedStmt, unslicedMethod);

							for (int index = 0; index < slicedStmt.getTargetCount(); index++) {
								Stmt target = (Stmt) slicedStmt.getTarget(index);
								slicedStmt.setTarget(index, getSlicedStmt(target, unslicedMethod));
							}

							Stmt target = (Stmt) slicedStmt.getDefaultTarget();
							slicedStmt.setDefaultTarget(getSlicedStmt(target, unslicedMethod));
						} else if (unslicedStmt instanceof TableSwitchStmt) {
							TableSwitchStmt slicedStmt =
								(TableSwitchStmt) transformer.getTransformed(unslicedStmt, unslicedMethod);

							for (int index = 0; index < slicedStmt.getHighIndex() - slicedStmt.getLowIndex(); index++) {
								Stmt target = (Stmt) slicedStmt.getTarget(index);
								slicedStmt.setTarget(index, getSlicedStmt(target, unslicedMethod));
							}

							Stmt target = (Stmt) slicedStmt.getDefaultTarget();
							slicedStmt.setDefaultTarget(getSlicedStmt(target, unslicedMethod));
						}
					}
				}

				//fixup exception list
				pruneExceptionsAtMethodInterface(slicedMethod, slicedBody.getUnits());

				//This will remove unnecessary nop statements, hence, fixing the gotos.
				nopTranformation.transform(slicedBody);
			}
		}
	}

	/**
	 * This is a helper method used for generating slice criteria based on interprocedural dependence expressions.
	 *
	 * @param slices is a collection of statement and method pairs.
	 *
	 * @pre slices != null
	 * @pre slices.oclIsKindOf(Collection(Pair(Stmt, SootMethod))
	 */
	private void interproceduralSliceHelper(final Collection slices) {
		for (Iterator i = slices.iterator(); i.hasNext();) {
			Pair pair = (Pair) i.next();
			Stmt unslicedStmt = (Stmt) pair.getFirst();
			SootMethod unslicedMethod = (SootMethod) pair.getSecond();

			if (transformer.getTransformed(unslicedStmt, unslicedMethod) == null) {
				transformer.transform(unslicedStmt, unslicedMethod);

				SliceStmt sliceCriterion = SliceStmt.getSliceStmt();
				sliceCriterion.initialize(unslicedMethod, unslicedStmt, true);
				workbag.addWorkNoDuplicates(sliceCriterion);
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
				thrownInBody.add(transformer.getTransformedSootClass(
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
	 * Generates new slice criteria based on dependence info pertaining to alias expression, both in sequential and
	 * concurrent setting.  By nature of Jimple, only one alias expression(array/field access) can occur in a statement,
	 * hence, the arguments.
	 *
	 * @param stmt in which expression which leads to aliased-data dependence occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @pre stmt != null and method != null
	 * @pre stmt.containsArrayRef() == true or stmt.containsFieldRef()
	 */
	private void sliceBasedOnAliasedDataDependence(final Stmt stmt, final SootMethod method) {
		DependencyAnalysis[] da = new DependencyAnalysis[2];
		da[0] = (DependencyAnalysis) controller.getAnalysis(SimpleController.INTERFERENCE_DA);
		da[1] = (DependencyAnalysis) controller.getAnalysis(SimpleController.CLASS_DATA_DA);

		Collection slices = new HashSet();

		for (int i = da.length - 1; i >= 0; i--) {
			if (sliceType.equals(COMPLETE_SLICE)) {
				slices.addAll(da[i].getDependents(stmt, method));
				slices.addAll(da[i].getDependees(stmt, method));
			} else if (sliceType.equals(BACKWARD_SLICE)) {
				slices.addAll(da[i].getDependees(stmt, method));
			}
		}
		interproceduralSliceHelper(slices);
	}

	/**
	 * Generates new slice criteria based on ready dependence info pertaining given statement.
	 *
	 * @param stmt which may cause to ready dependence.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @pre stmt != null and method != null
	 * @pre stmt.oclIsKindOf(ExitMonitorStmt) or stmt.oclIsKindOf(EnterMonitorStmt) or stmt.oclIsKindOf(InvokeStmt)
	 */
	private void sliceBasedOnReadyDependence(final Stmt stmt, final SootMethod method) {
		if (dependencies.contains(SimpleController.READY_DA)) {
			DependencyAnalysis da = (DependencyAnalysis) controller.getAnalysis(SimpleController.READY_DA);

			Collection slices = new HashSet();

			if (sliceType.equals(COMPLETE_SLICE)) {
				slices.addAll(da.getDependents(stmt, method));
				slices.addAll(da.getDependees(stmt, method));
			} else if (sliceType.equals(BACKWARD_SLICE)) {
				slices.addAll(da.getDependees(stmt, method));
			}

			interproceduralSliceHelper(slices);
		}
	}

	/**
	 * Generates immediate slice for the given expression.
	 *
	 * @param sExpr is the expression-level slice criterion.
	 *
	 * @pre sExpr != null and sExpr.getOccurringStmt() != null and sExpr.getOccurringMethod() != null
	 * @pre sExpr.getCriterion() != null and sExpr.getCriterion().oclIsKindOf(ValueBox)
	 */
	private void sliceExpr(final SliceExpr sExpr) {
		Stmt stmt = sExpr.getOccurringStmt();
		SootMethod method = sExpr.getOccurringMethod();
		ValueBox expr = (ValueBox) sExpr.getCriterion();

		if (sliceType.equals(COMPLETE_SLICE) || sExpr.isIncluded()) {
			sliceStmt(stmt, method, true);
		} else {
			Value value = expr.getValue();

			if (value instanceof FieldRef || value instanceof ArrayRef) {
				sliceBasedOnAliasedDataDependence(stmt, method);
			}

			for (Iterator i = value.getUseBoxes().iterator(); i.hasNext();) {
				ValueBox vBox = (ValueBox) i.next();

				if (vBox.getValue() instanceof Local) {
					sliceLocal(vBox, stmt, method);
				}
			}

			// include the statement to capture control dependency
			sliceStmt(stmt, method, false);
		}
	}

	/**
	 * Generates new slice criteria based on on what affects the given occurrence of the invoke expression.  By nature of
	 * Jimple, only one invoke expression can occur in a statement, hence, the arguments.
	 *
	 * @param stmt in which the field occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @pre stmt != null and method != null
	 * @pre stmt.containsInvokeExpr() == true
	 */
	private void sliceInvokeExpr(final Stmt stmt, final SootMethod method) {
		InvokeExpr expr = stmt.getInvokeExpr();

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
				SliceStmt sliceCriterion = SliceStmt.getSliceStmt();
				sliceCriterion.initialize(callee, bb.getTrailer(), true);
				workbag.addWorkNoDuplicates(sliceCriterion);
			}
		}
		sliceBasedOnReadyDependence(stmt, method);
	}

	/**
	 * Generates new slice criteria based on what affects the given occurrence of the local.
	 *
	 * @param vBox is the occurrence of the local.
	 * @param stmt is the statement in which <code>vBox</code> occurs.
	 * @param method is the method in which <code>stmt</code> occurs.
	 *
	 * @pre vBox != null and stmt != null and method != null
	 */
	private void sliceLocal(final ValueBox vBox, final Stmt stmt, final SootMethod method) {
		DependencyAnalysis da = (DependencyAnalysis) controller.getAnalysis(SimpleController.METHOD_LOCAL_DATA_DA);

		// add the new local
		SootMethod transformedMethod = transformer.getTransformed(method);
		Body body = transformedMethod.getActiveBody();
		Local local = (Local) vBox.getValue();

		if (transformer.getTransformedLocal(local, transformedMethod) == null) {
			body.getLocals().add(jimple.newLocal(local.getName(), local.getType()));
		}

		// slice
		Collection newCriteria = new HashSet();

		if (sliceType.equals(COMPLETE_SLICE)) {
			newCriteria.addAll(da.getDependents(stmt, method));
			newCriteria.addAll(da.getDependees(new Pair(stmt, vBox), method));
		} else if (sliceType.equals(BACKWARD_SLICE)) {
			newCriteria.addAll(da.getDependees(new Pair(stmt, vBox), method));
		}

		// update the workbag
		for (Iterator i = newCriteria.iterator(); i.hasNext();) {
			Stmt unsliced = (Stmt) i.next();
			SliceStmt sliceStmt = SliceStmt.getSliceStmt();
			sliceStmt.initialize(method, unsliced, true);
			workbag.addWorkNoDuplicates(sliceStmt);
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
		if (inclusive) {
			if (stmt.containsInvokeExpr()) {
				sliceInvokeExpr(stmt, method);
			} else if (stmt.containsArrayRef() || stmt.containsFieldRef()) {
				sliceBasedOnAliasedDataDependence(stmt, method);
			} else if (stmt instanceof EnterMonitorStmt || stmt instanceof ExitMonitorStmt) {
				sliceBasedOnReadyDependence(stmt, method);
			}

			for (Iterator i = stmt.getUseAndDefBoxes().iterator(); i.hasNext();) {
				ValueBox vBox = (ValueBox) i.next();
				Value value = vBox.getValue();

				if (value instanceof Local) {
					sliceLocal(vBox, stmt, method);
				}
			}

			//Stmt slice = cloner.cloneASTFragment(stmt, method);
			//writeIntoAt(slice, slicedSL, stmt, unslicedSL);
			//slicemap.addMapping(slice, stmt, method);
			transformer.transform(stmt, method);
		}

		Collection slices = new HashSet();

		// add criteria for an intra-procedural dependency.
		if (sliceType.equals(COMPLETE_SLICE)) {
			for (Iterator i = intraProceduralDependencies.iterator(); i.hasNext();) {
				DependencyAnalysis da = (DependencyAnalysis) i.next();
				slices.addAll(da.getDependents(stmt, method));
				slices.addAll(da.getDependees(stmt, method));
			}
		} else if (sliceType.equals(BACKWARD_SLICE)) {
			for (Iterator i = intraProceduralDependencies.iterator(); i.hasNext();) {
				DependencyAnalysis da = (DependencyAnalysis) i.next();
				slices.addAll(da.getDependees(stmt, method));
			}
		}

		for (Iterator i = slices.iterator(); i.hasNext();) {
			Stmt unsliced = (Stmt) i.next();

			if (transformer.getTransformed(unsliced, method) == null) {
				//Stmt sliced = cloner.cloneASTFragment(unsliced, method);
				//writeIntoAt(sliced, slicedSL, unsliced, unslicedSL);
				//slicemap.addMapping(sliced, unsliced, method);
				transformer.transform(stmt, method);

				SliceStmt sliceStmt = SliceStmt.getSliceStmt();
				sliceStmt.initialize(method, unsliced, true);
				workbag.addWorkNoDuplicates(sliceStmt);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.11  2003/08/19 11:52:25  venku
   The following renaming have occurred ITransformMap to ITransformer, SliceMapImpl to SliceTransformer,
   and  Slicer to SliceEngine.
   Ripple effect of the above.

   Revision 1.10  2003/08/19 11:37:41  venku
   Major changes:
    - Changed ITransformMap extensively such that it now provides
      interface to perform the actual transformation.
    - Extended ITransformMap as AbstractTransformer to provide common
      functionalities.
    - Ripple effect of the above change in SlicerMapImpl.
    - Ripple effect of the above changes in Slicer.
    - The slicer now actually detects what needs to be included in the slice.
      Hence, it is more of an analysis/driver/engine that drives the transformation
      and SliceMapImpl is the engine that does or captures the transformation.
   The immediate following change will be to rename ITransformMap to ITransformer,
    SliceMapImpl to SliceTransformer, and Slicer to SliceEngine.
   Revision 1.9  2003/08/18 12:14:13  venku
   Well, to start with the slicer implementation is complete.
   Although not necessarily bug free, hoping to stabilize it quickly.
   Revision 1.8  2003/08/18 05:01:45  venku
   Committing package name change in source after they were moved.
   Revision 1.7  2003/08/18 04:56:47  venku
   Spruced up Documentation and specification.
   But committing before moving slicer under transformation umbrella of Indus.
   Revision 1.6  2003/05/22 22:23:49  venku
   Changed interface names to start with a "I".
   Formatting.
 */
