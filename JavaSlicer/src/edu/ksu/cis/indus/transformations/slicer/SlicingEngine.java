
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

import ca.mcgill.sable.soot.SootClass;
import ca.mcgill.sable.soot.SootClassManager;
import ca.mcgill.sable.soot.SootField;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.Type;

import ca.mcgill.sable.soot.jimple.ArrayRef;
import ca.mcgill.sable.soot.jimple.FieldRef;
import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.Local;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtBody;
import ca.mcgill.sable.soot.jimple.StmtList;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.soot.jimple.ValueBox;

import edu.ksu.cis.bandera.staticanalyses.dependency.controller.Controller;
import edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis;
import edu.ksu.cis.bandera.staticanalyses.support.Pair;
import edu.ksu.cis.bandera.staticanalyses.support.WorkBag;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;


/**
 * This class accepts slice criterions and generates slices of the given system.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class Slicer
  implements ASTCloner.ASTClonerHelper {
	/**
	 * Forward slice request.
	 */
	public static final String FORWARD_SLICE = "FORWARD_SLICE";

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
	 * 			  (COMPLETE_SLICE)
	 */
	private static final Collection sliceTypes = new HashSet();

	static {
		sliceTypes.add(FORWARD_SLICE);
		sliceTypes.add(BACKWARD_SLICE);
		sliceTypes.add(COMPLETE_SLICE);
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
	private Collection criteria;

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
	private String sliceType = FORWARD_SLICE;

	/**
	 * Clones of the given class.
	 *
	 * @param clazz to be cloned.
	 *
	 * @return the clone of <code>clazz</code>.
	 */
	public SootClass getCloneOf(SootClass clazz) {
		SootClass result = sliceClazzManager.getClass(clazz.getName());

		if(result == null) {
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

		if(!clazz.declaresField(name, type)) {
			clazz.addField(new SootField(name, type, field.getModifiers()));
		}
		return clazz.getField(name, type);
	}

	/**
	 * Clones the given method.  It does <i>not</i> clone the methods body.
	 *
	 * @param slicedMethod is the method to be cloned.
	 *
	 * @return the clone of <code>slicedMethod</code>.
	 */
	public SootMethod getCloneOf(SootMethod slicedMethod) {
		SootClass sc = getCloneOf(slicedMethod.getDeclaringClass());
		SootMethod result =
			sc.getMethod(slicedMethod.getName(), slicedMethod.getParameterTypes(), slicedMethod.getReturnType());

		if(result == null) {
			result =
				new SootMethod(slicedMethod.getName(), slicedMethod.getParameterTypes(), slicedMethod.getReturnType(),
					slicedMethod.getModifiers());

			for(ca.mcgill.sable.util.Iterator i = slicedMethod.getExceptions().iterator(); i.hasNext();) {
				SootClass exception = (SootClass) i.next();
				result.addException(exception);
			}
			result.storeBody(jimple, jimple.newBody(result));
		}
		return result;
	}

	/**
	 * Returns the clone of the class named by <code>clazz</code>.
	 *
	 * @param clazz is the name of the class whose clone is requested.
	 *
	 * @return the clone of the requested class.
	 */
	public SootClass getCloneOfSootClass(String clazz) {
		SootClass sc = clazzManager.getClass(clazz);
		return getCloneOf(sc);
	}

	/**
	 * Resets internal data structures.
	 */
	public void reset() {
		clazzManager = null;

		for(ca.mcgill.sable.util.Iterator i = sliceClazzManager.getClasses().iterator(); i.hasNext();) {
			sliceClazzManager.removeClass((SootClass) i.next());
		}
		criteria.clear();
	}

	/**
	 * Slices the given system for the given criteria in the given <code>direction</code>.
	 *
	 * @param sliceType is the type of slice requested.  This has to be one of <code>XXX_SLICE</code> values defined in this
	 * 		  class.
	 *
	 * @throws IllegalArgumentException when direction is not one of the <code>XXX_SLICE</code> values.
	 * @throws IllegalStateException when the type of the slice criterion is not recognized.
	 */
	public void slice(String sliceType) {
		workbag.clear();
		workbag.addAllWorkNoDuplicates(criteria);

		if(!sliceTypes.contains(sliceType)) {
			throw new IllegalArgumentException("sliceType is not one of XXX_SLICE values defined in this class.");
		}
		this.sliceType = sliceType;

		while(!(workbag.isEmpty())) {
			SliceCriterion work = (SliceCriterion) workbag.getWork();

			if(work instanceof SliceStmt) {
				SliceStmt temp = (SliceStmt) work;
				SootMethod sm = temp.getOccurringMethod();
				sliceStmt((Stmt) temp.getCriterion(), sm, temp.isIncluded());
			} else if(work instanceof SliceExpr) {
				slice((SliceExpr) work);
			} else {
				throw new IllegalStateException(
					"The work piece is not a subtype of edu.ksu.cis.bandera.slicer.SliceCriterion");
			}
		}
		fixupMethods();
	}

	/**
	 * Retrieves the statement list for the slice version of given sliced method. 
	 *
	 * @param method is the sliced method.
	 *
	 * @return the statement list for the slice version of the given sliced method.
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

		if(clazz.hasSuperClass()) {
			SootClass superClass = getCloneOf(clazz.getSuperClass());
			result.setSuperClass(superClass);
		}

		for(ca.mcgill.sable.util.Iterator i = clazz.getInterfaces().iterator(); i.hasNext();) {
			SootClass interfaceClass = (SootClass) i.next();
			SootClass slicedInterfaceClass = getCloneOf(interfaceClass);
			result.addInterface(slicedInterfaceClass);
		}
		return result;
	}

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private void fixupMethods() {
	}

	/**
	 * Generates immediate slice for the given expression.  
	 *
	 * @param sExpr is the expression-level slice criterion.
	 */
	private void slice(SliceExpr sExpr) {
		Stmt stmt = sExpr.getOccurringStmt();
		SootMethod method = sExpr.getOccurringMethod();
		ValueBox expr = (ValueBox) sExpr.getCriterion();

		if(sExpr.isIncluded()) {
			sliceStmt(stmt, method, true);
		} else {
			for(ca.mcgill.sable.util.Iterator i = expr.getValue().getUseBoxes().iterator(); i.hasNext();) {
				ValueBox vBox = (ValueBox) i.next();
				Value value = vBox.getValue();

				if(value instanceof Local) {
					sliceLocal(vBox, stmt, method);
				} else if(value instanceof FieldRef) {
					sliceField(stmt, method);
				} else if(value instanceof ArrayRef) {
					sliceArray(vBox, stmt, method);
				}
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
	}

	/**
	 * Generates immediate slice for the field occurring in the given statement and method.  By nature of Jimple, only one
	 * field can be referred in a statement, hence, the arguments.
	 *
	 * @param stmt in which the field occurs.
	 * @param method in which <code>stmt</code> occurs.
	 */
	private void sliceField(Stmt stmt, SootMethod method) {
		DependencyAnalysis da = controller.getDAnalysis(Controller.INTERFERENCE_DA);
		Collection slices = new HashSet();

		if(sliceType.equals(FORWARD_SLICE) || sliceType.equals(COMPLETE_SLICE)) {
			slices.addAll(da.getDependents(stmt, method));
		} else if(sliceType.equals(BACKWARD_SLICE) || sliceType.equals(COMPLETE_SLICE)) {
			slices.addAll(da.getDependees(stmt, method));
		}

		StmtList slicedSL = ((JimpleBody) method.getBody(jimple)).getStmtList();
		StmtList sliceSL = getSliceStmtListFor(method);

		for(Iterator i = slices.iterator(); i.hasNext();) {
			Pair pair = (Pair) i.next();
			Stmt unsliced = (Stmt) pair.getFirst();

			if(slicemap.getSliceStmt(unsliced, method) == null) {
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

		if(body.getLocal(lName) == null) {
			body.addLocal(jimple.newLocal(lName, local.getType()));
		}

		// slice
		if(sliceType.equals(FORWARD_SLICE) || sliceType.equals(COMPLETE_SLICE)) {
			slices.addAll(da.getDependents(stmt, method));
		} else if(sliceType.equals(BACKWARD_SLICE) || sliceType.equals(COMPLETE_SLICE)) {
			slices.addAll(da.getDependees(new Pair(stmt, vBox), method));
		}

		// update the workbag
		for(Iterator i = slices.iterator(); i.hasNext();) {
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
	 * <code>false</code>, otherwise.
	 */
	private void sliceStmt(Stmt stmt, SootMethod method, boolean inclusive) {
		StmtList slicedSL = ((JimpleBody) method.getBody(jimple)).getStmtList();
		StmtList sliceSL = getSliceStmtListFor(method);

		if(inclusive) {
			for(ca.mcgill.sable.util.Iterator i = stmt.getUseAndDefBoxes().iterator(); i.hasNext();) {
				ValueBox vBox = (ValueBox) i.next();
				Value value = vBox.getValue();

				if(value instanceof Local) {
					sliceLocal(vBox, stmt, method);
				} else if(value instanceof FieldRef) {
					sliceField(stmt, method);
				} else if(value instanceof ArrayRef) {
					sliceArray(vBox, stmt, method);
				}
			}

			Stmt slice = cloner.cloneASTFragment(stmt, method);
			sliceSL.add(slicedSL.indexOf(stmt), slice);
			slicemap.put(stmt, slice, method);
		}

		Collection slices = new HashSet();

		DependencyAnalysis da[] = new DependencyAnalysis[3];
		da[0] = controller.getDAnalysis(Controller.CONTROL_DA);
		da[1] = controller.getDAnalysis(Controller.SYNCHRONIZATION_DA);
		da[2] = controller.getDAnalysis(Controller.DIVERGENCE_DA);

		//da[3] = controller.getDAnalysis(Controller.READY_DA);
		if(sliceType.equals(FORWARD_SLICE) || sliceType.equals(COMPLETE_SLICE)) {
			for(int i = da.length - 1; i >= 0; i--) {
				slices.addAll(da[i].getDependents(stmt, method));
			}
		} else if(sliceType.equals(BACKWARD_SLICE) || sliceType.equals(COMPLETE_SLICE)) {
			for(int i = da.length - 1; i >= 0; i--) {
				slices.addAll(da[i].getDependees(stmt, method));
			}
		}

		for(Iterator i = slices.iterator(); i.hasNext();) {
			Stmt unsliced = (Stmt) i.next();

			if(slicemap.getSliceStmt(unsliced, method) == null) {
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
Revision 1.2  2003/02/18 00:18:49  venku
*** empty log message ***

Revision 1.1.1.1  2003/02/17 23:59:51  venku
Placing JavaSlicer under version control.


*****/
