
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

package edu.ksu.cis.bandera.slicer;

import ca.mcgill.sable.soot.RefType;
import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.Type;

import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.AssignStmt;
import ca.mcgill.sable.soot.jimple.FieldRef;
import ca.mcgill.sable.soot.jimple.GotoStmt;
import ca.mcgill.sable.soot.jimple.InstanceFieldRef;
import ca.mcgill.sable.soot.jimple.InvokeExpr;
import ca.mcgill.sable.soot.jimple.InvokeStmt;
import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.NopStmt;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtBody;
import ca.mcgill.sable.soot.jimple.StmtGraph;
import ca.mcgill.sable.soot.jimple.StmtList;
import ca.mcgill.sable.soot.jimple.ThrowStmt;
import ca.mcgill.sable.soot.jimple.Transformations;
import ca.mcgill.sable.soot.jimple.Trap;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;

import edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.bandera.staticanalyses.dependency.controller.Controller;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.interfaces.ICallGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.bandera.staticanalyses.support.BasicBlockGraphMgr;
import edu.ksu.cis.bandera.staticanalyses.support.Pair;
import edu.ksu.cis.bandera.staticanalyses.support.Util;
import edu.ksu.cis.bandera.staticanalyses.support.WorkBag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * This class accepts slice criterions and generates slices of the given system.
 *
 * <p>
 * The term "immediate slice" in the context of this file implies the slice containing only entities on which the given  term
 * depends on, not the transitive closure.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class Slicer
  implements ASTCloner.IASTClonerHelper {
	/**
	 * <p>
	 * The logger used by instances of this class to log messages.
	 * </p>
	 */
	private static final Log LOGGER = LogFactory.getLog(Slicer.class);

	/**
	 * Backward slice request.
	 */
	public static final String BACKWARD_SLICE = "BACKWARD_SLICE";

	/**
	 * Complete slice request.
	 */
	public static final String COMPLETE_SLICE = "COMPLETE_SLICE";

	/**
	 * This just a convenience collection of the types of slices supported by this class.
	 *
	 * @invariant sliceTypes.contains(FORWARD_SLICE) and sliceTypes.contains(BACKWARD_SLICE) and sliceTypes.contains
	 *               (COMPLETE_SLICE)
	 */
	private static final Collection SLICE_TYPES = new HashSet();

	static {
		SLICE_TYPES.add(BACKWARD_SLICE);
		SLICE_TYPES.add(COMPLETE_SLICE);
	}

	/**
	 * This instance is used to clone Jimple AST chunks.
	 */
	private final ASTCloner cloner = new ASTCloner(this);

	/**
	 * The collection of slice criteria.
	 *
	 * @invariant criteria->forall(o | o.oclIsKindOf(SliceCriterion))
	 */
	private Collection criteria = new HashSet();

	/**
	 * The controller used to access the dependency analysis info during slicing.
	 */
	private Controller controller;

	/**
	 * This is a reference to the jimple body representation.
	 */
	private final Jimple jimple = Jimple.v();

	/**
	 * The work bag used during slicing.
	 *
	 * @invariant workbag.oclIsKindOf(Bag)
	 * @invariant workbag->forall(o | o.oclIsKindOf(SliceCriterion))
	 */
	private final WorkBag workbag = new WorkBag(WorkBag.FIFO);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private BasicBlockGraphMgr clonedGraphMgr = new BasicBlockGraphMgr();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final ICallGraphInfo cgi;

	/**
	 * This maps sliced entities to slice entities and vice versa.
	 */
	private SliceMap slicemap = new SliceMap(this);

	/**
	 * The class manager which manages sliced classes.
	 */
	private SootClassManager clazzManager;

	/**
	 * The class manager which manages slice classes.
	 */
	private SootClassManager sliceClazzManager;

	/**
	 * The direction of the slice.  It's default value is <code>FORWARD_SLICE</code>.
	 *
	 * @invariant sliceTypes.contains(sliceType)
	 */
	private String sliceType = BACKWARD_SLICE;

	/**
	 * Creates a new Slicer object.
	 *
	 * @param cgi DOCUMENT ME!
	 */
	public Slicer(ICallGraphInfo cgi) {
		this.cgi = cgi;
	}

	/**
	 * Clones of the given class.
	 *
	 * @param clazz to be cloned.
	 *
	 * @return the clone of <code>clazz</code>.
	 */
	public SootClass getCloneOf(SootClass clazz) {
		SootClass result = sliceClazzManager.getClass(clazz.getName());

		if (result == null) {
			result = clone(clazz);
		}
		return result;
	}

	/**
	 * Clones of the given field.
	 *
	 * @param field to be cloned.
	 *
	 * @return the cone of <code>field</code>.
	 */
	public SootField getCloneOf(SootField field) {
		SootClass clazz = getCloneOf(field.getDeclaringClass());
		String name = field.getName();
		Type type = field.getType();

		if (!clazz.declaresField(name, type)) {
			clazz.addField(new SootField(name, type, field.getModifiers()));
		}
		return clazz.getField(name, type);
	}

	/**
	 * Clones the given method.  The statement list of the method body of the clone is equal in length to that of the given
	 * method but it only contains <code>NopStmt</code>s.
	 *
	 * @param slicedMethod is the method to be cloned.
	 *
	 * @return the clone of <code>slicedMethod</code>.
	 */
	public SootMethod getCloneOf(SootMethod slicedMethod) {
		SootClass sc = getCloneOf(slicedMethod.getDeclaringClass());
		SootMethod result =
			sc.getMethod(slicedMethod.getName(), slicedMethod.getParameterTypes(), slicedMethod.getReturnType());

		if (result == null) {
			result =
				new SootMethod(slicedMethod.getName(), slicedMethod.getParameterTypes(), slicedMethod.getReturnType(),
					slicedMethod.getModifiers());

			for (ca.mcgill.sable.util.Iterator i = slicedMethod.getExceptions().iterator(); i.hasNext();) {
				SootClass exception = (SootClass) i.next();
				result.addException(exception);
			}

			JimpleBody jb = (JimpleBody) jimple.newBody(result);
			StmtList sl = jb.getStmtList();
			Stmt nop = jimple.newNopStmt();

			for (int i = controller.getStmtGraph(slicedMethod).getBody().getStmtList().size() - 1; i >= 0; i--) {
				sl.add(nop);
			}
			result.storeBody(jimple, jb);
		}
		return result;
	}

	/**
	 * Returns the clone of the class named by <code>clazz</code>.  This requires that a class by the name <code>clazz</code>
	 * exist in the given system.
	 *
	 * @param clazz is the name of the class whose clone is requested.
	 *
	 * @return the clone of the requested class.
	 *
	 * @throws IllegalStateException when a class named <code>clazz</code> does not exist in the system.
	 */
	public SootClass getCloneOf(String clazz)
	  throws IllegalStateException {
		SootClass sc = clazzManager.getClass(clazz);

		if (sc == null) {
			LOGGER.error(clazz + " does not exist in the system.");
			throw new IllegalStateException(clazz + " does not in the system.");
		}
		return getCloneOf(sc);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * <p></p>
	 *
	 * @param name DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Local getLocal(String name, SootMethod method) {
		SootMethod sliceMethod = getCloneOf(method);
		JimpleBody body = (JimpleBody) sliceMethod.getBody(jimple);
		return body.getLocal(name);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * <p></p>
	 *
	 * @param criteria DOCUMENT ME!
	 * @param clazzManager DOCUMENT ME!
	 * @param controller DOCUMENT ME!
	 *
	 * @throws IllegalStateException when the given criterion are not of type <code>SliceCriterion</code>.
	 */
	public void setSliceCriteria(Collection criteria, SootClassManager clazzManager, Controller controller)
	  throws IllegalStateException {
		for (Iterator i = criteria.iterator(); i.hasNext();) {
			Object o = i.next();

			if (!(o instanceof SliceStmt || o instanceof SliceExpr)) {
				LOGGER.error("The work piece is not a subtype of SliceCriterion");
				throw new IllegalStateException("The work piece is not a subtype of SliceCriterion");
			}
		}
		this.criteria.addAll(criteria);

		if (clazzManager != null) {
			this.clazzManager = clazzManager;
		}

		if (controller != null) {
			this.controller = controller;
		}
	}

	/**
	 * Resets internal data structures.
	 */
	public void reset() {
		clazzManager = null;
		sliceClazzManager = null;
		criteria.clear();
	}

	/**
	 * Slices the given system for the given criteria in the given <code>direction</code>.
	 *
	 * @param sliceType is the type of slice requested.  This has to be one of <code>XXX_SLICE</code> values defined in this
	 *           class.
	 *
	 * @throws IllegalStateException when slice criteria, class manager, or controller is unspecified.
	 * @throws IllegalArgumentException when direction is not one of the <code>XXX_SLICE</code> values.
	 */
	public void slice(String sliceType)
	  throws IllegalArgumentException, IllegalStateException {
		if (criteria == null || criteria.size() == 0) {
			LOGGER.warn("Slice criteria is unspecified.");
			throw new IllegalStateException("Slice criteria is unspecified.");
		} else if (clazzManager == null || controller == null) {
			LOGGER.warn("Class Manager and/or Controller is unspecified.");
			throw new IllegalStateException("Class Manager and/or Controller is unspecified.");
		}

		if (!SLICE_TYPES.contains(sliceType)) {
			throw new IllegalArgumentException("sliceType is not one of XXX_SLICE values defined in this class.");
		}
		this.sliceType = sliceType;
		workbag.clear();
		workbag.addAllWorkNoDuplicates(criteria);

		boolean flag = sliceType.equals(COMPLETE_SLICE);
		Collection processed = new HashSet();

		while (!(workbag.isEmpty())) {
			SliceCriterion work = (SliceCriterion) workbag.getWork();

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
		slicemap.cleanup();
	}

	/**
	 * Retrieves the statement list for the slice version of given sliced method.
	 *
	 * @param method is the sliced method.
	 *
	 * @return the statement list for the slice version of the given method.
	 */
	private StmtList getSliceStmtListFor(SootMethod method) {
		StmtList result = null;
		SootMethod sliceMethod = getCloneOf(method);
		StmtBody body = (StmtBody) sliceMethod.getBody(jimple);
		result = body.getStmtList();
		return result;
	}

	/**
	 * Clones <code>clazz</code> in terms of inheritence and modifiers only.  The clone class has an empty body.
	 *
	 * @param clazz to clone
	 *
	 * @return the clone of <code>clazz</code>.
	 */
	private SootClass clone(SootClass clazz) {
		SootClass result = new SootClass(clazz.getName(), clazz.getModifiers());

		if (clazz.hasSuperClass()) {
			SootClass superClass = getCloneOf(clazz.getSuperClass());
			result.setSuperClass(superClass);
		}

		for (ca.mcgill.sable.util.Iterator i = clazz.getInterfaces().iterator(); i.hasNext();) {
			SootClass interfaceClass = (SootClass) i.next();
			SootClass slicedInterfaceClass = getCloneOf(interfaceClass);
			result.addInterface(slicedInterfaceClass);
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * <p></p>
	 *
	 * @param sm DOCUMENT ME!
	 * @param sl DOCUMENT ME!
	 */
	private void fixupExceptionList(SootMethod sm, StmtList sl) {
		Set thrownInBody = new HashSet();
		Set thrownAtInterface = (Set) Util.convert("java.util.HashSet", sm.getExceptions());

		for (ca.mcgill.sable.util.Iterator i = sl.iterator(); i.hasNext();) {
			Stmt stmt = (Stmt) i.next();

			if (stmt instanceof ThrowStmt) {
				thrownInBody.add(sliceClazzManager.getClass(((RefType) ((ThrowStmt) stmt).getOp().getType()).className));
			} else {
				InvokeExpr expr = null;

				if (stmt instanceof InvokeStmt) {
					expr = (InvokeExpr) ((InvokeStmt) stmt).getInvokeExpr();
				} else if (stmt instanceof AssignStmt && ((AssignStmt) stmt).getRightOp() instanceof InvokeExpr) {
					expr = (InvokeExpr) ((AssignStmt) stmt).getRightOp();
				}

				if (expr != null) {
					thrownInBody.addAll(Util.convert("java.util.ArrayList", expr.getMethod().getExceptions()));
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
	 * DOCUMENT ME!
	 *
	 * <p></p>
	 */

	/*
	   private void fixupGoto(StmtList clonedSl, StmtList cloneSl, SootMethod clonedMethod, SootMethod cloneMethod) {
	       BasicBlockGraph clonedGraph = clonedGraphMgr.getBasicBlockGraph(controller.getStmtGraph(clonedMethod));
	       Collection gotos = new ArrayList();
	       for(ca.mcgill.sable.util.Iterator k = clonedSl.iterator(); k.hasNext();) {
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
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private void fixupMethods() {
		for (ca.mcgill.sable.util.Iterator i = sliceClazzManager.getClasses().iterator(); i.hasNext();) {
			SootClass cloneClass = (SootClass) i.next();
			SootClass clonedClass = clazzManager.getClass(cloneClass.getName());

			for (ca.mcgill.sable.util.Iterator j = cloneClass.getMethods().iterator(); j.hasNext();) {
				SootMethod cloneMethod = (SootMethod) j.next();
				SootMethod clonedMethod =
					clonedClass.getMethod(cloneMethod.getName(), cloneMethod.getParameterTypes(), cloneMethod.getReturnType());
				JimpleBody clonedBody = (JimpleBody) clonedMethod.getBody(jimple);
				StmtList clonedSl = clonedBody.getStmtList();
				JimpleBody cloneBody = (JimpleBody) cloneMethod.getBody(jimple);
				StmtList cloneSl = cloneBody.getStmtList();

				//fixup traps
				for (ca.mcgill.sable.util.Iterator k = clonedBody.getTraps().iterator(); k.hasNext();) {
					Trap trap = (Trap) k.next();
					int begin = clonedSl.indexOf(trap.getBeginUnit());
					int end = clonedSl.indexOf(trap.getEndUnit());

					for (int l = begin; l < end; l++) {
						if (!(cloneSl.get(l) instanceof NopStmt)) {
							cloneBody.addTrap(jimple.newTrap(trap.getException(), trap.getBeginUnit(), trap.getEndUnit(),
									trap.getHandlerUnit()));
							break;
						}
					}
				}

				/*
				 * fixing up the gotos.  We will just copy the control flow from the cloned method and use post slicing
				 * transformation to prune the code.
				 */
				for (ca.mcgill.sable.util.Iterator k = clonedSl.iterator(); k.hasNext();) {
					Stmt stmt = (Stmt) k.next();

					if (stmt instanceof GotoStmt) {
						int index = clonedSl.indexOf(stmt);
						cloneSl.remove(index);

						Stmt slicedStmt = cloner.cloneASTFragment(stmt, cloneMethod);
						cloneSl.add(index, slicedStmt);
						slicemap.put(stmt, slicedStmt, clonedMethod);
					}
				}

				//fixup exception list
				fixupExceptionList(cloneMethod, cloneSl);

				//prune the code to remove unwanted statements.  This will include unnecessary goto and nop statements.
				Transformations.cleanupCode(cloneBody);
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
	private void sliceArray(ValueBox vBox, Stmt stmt, SootMethod method) {
		ArrayRef value = (ArrayRef) vBox.getValue();
		sliceLocal(value.getBaseBox(), stmt, method);

		if (value.getIndex() instanceof Local) {
			sliceLocal(value.getIndexBox(), stmt, method);
		}

		DependencyAnalysis da = controller.getDAnalysis(Controller.INTERFERENCE_DA);
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
	private void sliceExpr(SliceExpr sExpr) {
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
				for (ca.mcgill.sable.util.Iterator i = value.getUseBoxes().iterator(); i.hasNext();) {
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
	private void sliceField(ValueBox vBox, Stmt stmt, SootMethod method) {
		FieldRef value = (FieldRef) vBox.getValue();

		if (value instanceof InstanceFieldRef) {
			sliceLocal(((InstanceFieldRef) value).getBaseBox(), stmt, method);
		}

		DependencyAnalysis da = controller.getDAnalysis(Controller.INTERFERENCE_DA);
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
	private void sliceHelper(Collection slices, SootMethod method) {
		StmtList slicedSL = ((JimpleBody) method.getBody(jimple)).getStmtList();
		StmtList sliceSL = getSliceStmtListFor(method);

		for (Iterator i = slices.iterator(); i.hasNext();) {
			Pair pair = (Pair) i.next();
			Stmt unsliced = (Stmt) pair.getFirst();

			if (slicemap.getSliceStmt(unsliced, method) == null) {
				Stmt slice = cloner.cloneASTFragment(unsliced, method);
				sliceSL.add(slicedSL.indexOf(unsliced), slice);
				slicemap.put(unsliced, slice, method);
				workbag.addWorkNoDuplicates(new SliceStmt((SootMethod) pair.getSecond(), unsliced, true));
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
	private void sliceLocal(ValueBox vBox, Stmt stmt, SootMethod method) {
		DependencyAnalysis da = controller.getDAnalysis(Controller.METHOD_LOCAL_DATA_DA);
		Collection slices = new HashSet();

		// add the new local
		SootMethod sliceMethod = getCloneOf(method);
		JimpleBody body = (JimpleBody) sliceMethod.getBody(jimple);
		Local local = (Local) vBox.getValue();
		String lName = local.getName();

		if (body.getLocal(lName) == null) {
			body.addLocal(jimple.newLocal(lName, local.getType()));
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
	 *           <code>false</code>, otherwise.
	 */
	private void sliceStmt(Stmt stmt, SootMethod method, boolean inclusive) {
		StmtList slicedSL = ((JimpleBody) method.getBody(jimple)).getStmtList();
		StmtList sliceSL = getSliceStmtListFor(method);

		if (inclusive) {
			InvokeExpr expr = null;

			if (stmt instanceof InvokeStmt) {
				expr = (InvokeExpr) ((InvokeStmt) stmt).getInvokeExpr();
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
					StmtGraph stmtGraph = controller.getStmtGraph(callee);
					BasicBlockGraph bbg = clonedGraphMgr.getBasicBlockGraph(stmtGraph);
					StmtList sl = stmtGraph.getBody().getStmtList();

					for (Iterator j = bbg.getTails().iterator(); j.hasNext();) {
						BasicBlock bb = (BasicBlock) j.next();
						workbag.addWorkNoDuplicates(new SliceStmt(callee, (Stmt) sl.get(bb._TRAILER), true));
					}
				}
			}

			for (ca.mcgill.sable.util.Iterator i = stmt.getUseAndDefBoxes().iterator(); i.hasNext();) {
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
			int index = slicedSL.indexOf(stmt);
			sliceSL.remove(index);
			sliceSL.add(index, slice);
			slicemap.put(stmt, slice, method);
		}

		Collection slices = new HashSet();

		DependencyAnalysis da[] = new DependencyAnalysis[3];
		da[0] = controller.getDAnalysis(Controller.CONTROL_DA);
		da[1] = controller.getDAnalysis(Controller.SYNCHRONIZATION_DA);
		da[2] = controller.getDAnalysis(Controller.DIVERGENCE_DA);

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
			Stmt unsliced = (Stmt) i.next();

			if (slicemap.getSliceStmt(unsliced, method) == null) {
				Stmt slice = cloner.cloneASTFragment(unsliced, method);
				sliceSL.add(slicedSL.indexOf(unsliced), slice);
				slicemap.put(unsliced, slice, method);
				workbag.addWorkNoDuplicates(new SliceStmt(method, unsliced, true));
			}
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
