
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

package edu.ksu.cis.indus.transformations.slicer;

import soot.Body;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.ValueBox;

import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;

import soot.jimple.toolkits.scalar.NopEliminator;

import soot.util.Chain;

import edu.ksu.cis.indus.slicer.SlicingEngine;
import edu.ksu.cis.indus.staticanalyses.support.Util;
import edu.ksu.cis.indus.transformations.common.Cloner;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * This transforms the given system based on the decisions of a slicing engine.  The results of this transformer is a new
 * system with the transformed artifacts.  The parts of the new system are created by cloning the original system.
 * 
 * <p>
 * Things get twisted here. When we say "untransformed" version of a method we refer to the method before slicing.  Hence,
 * when we say "transformed" version of the method we refer to the method after slicing.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class CloningBasedSliceResidualizer
  extends AbstractSliceResidualizer {
	/**
	 * The system resulting from the transformation.
	 */
	protected Scene transformedSystem;

	/**
	 * The system being transformed.
	 */
	protected Scene untransformedSystem;

	/**
	 * The cloner from which the map is extracted.
	 */
	private Cloner cloner;

	/**
	 * This maps statements in transformed version of methods to their counterparts in the untransformed version of the
	 * methods.
	 *
	 * @invariant slicedMethod2stmtMap.keySet()->forAll( o | o.isOclKindOf(SootMethod))
	 * @invariant slicedMethod2stmtMap.values()->forAll( o | o.isOclKindOf(Map(Stmt, Stmt)))
	 * @invariant slicedMethod2stmtMap != null
	 */
	private Map slicedMethod2stmtMap = new HashMap();

	/**
	 * This maps statements in untransformed methods to their counterparts in the transformed version of the methods.
	 *
	 * @invariant unslicedMethod2stmtMap.keySet()->forAll( o | o.isOclKindOf(SootMethod))
	 * @invariant unslicedMethod2stmtMap.values()->forAll( o | o.isOclKindOf(Map(Stmt, Stmt)))
	 * @invariant unslicedMethod2stmtMap != null
	 */
	private Map unslicedMethod2stmtMap = new HashMap();

	/**
	 * This indicates the slice type based on constants defined in <code>edu.ksu.cis.indus.slicer.SlicerTool,SLICE</code>.
	 */
	private Object sliceType;

	/**
	 * Set up the transformer
	 *
	 * @param theCloner manages the clone to clonee mappings.
	 *
	 * @pre theCloner != null
	 */
	public void setCloner(final Cloner theCloner) {
		cloner = theCloner;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformed(soot.SootClass)
	 */
	public SootClass getTransformed(final SootClass clazz) {
		return cloner.getCloneOf(clazz);
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformed(soot.SootField)
	 */
	public SootField getTransformed(final SootField field) {
		return cloner.getCloneOf(field);
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformed(soot.SootMethod)
	 */
	public SootMethod getTransformed(final SootMethod method) {
		return cloner.getCloneOf(method);
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformed(Stmt, SootMethod)
	 */
	public Stmt getTransformed(final Stmt unslicedStmt, final SootMethod unslicedMethod) {
		Stmt result = null;
		Map stmtMap = (Map) unslicedMethod2stmtMap.get(unslicedMethod);

		if (stmtMap != null) {
			result = (Stmt) stmtMap.get(unslicedStmt);
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformed(soot.ValueBox, soot.jimple.Stmt,
	 * 		soot.SootMethod)
	 */
	public ValueBox getTransformed(final ValueBox vBox, final Stmt stmt, final SootMethod method) {
		Stmt temp = getTransformed(stmt, method);
		int index = stmt.getUseAndDefBoxes().indexOf(vBox);
		return (ValueBox) temp.getUseAndDefBoxes().get(index);
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformedClasses()
	 */
	public Collection getTransformedClasses() {
		return transformedSystem.getClasses();
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getTransformedSootClass(java.lang.String)
	 */
	public SootClass getTransformedSootClass(final String className) {
		return transformedSystem.getSootClass(className);
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getUntransformed(soot.SootClass)
	 */
	public SootClass getUntransformed(final SootClass clazz) {
		return untransformedSystem.getSootClass(clazz.getName());
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#getUntransformed(Stmt, SootMethod)
	 */
	public Stmt getUntransformed(final Stmt slicedStmt, final SootMethod slicedMethod) {
		Stmt result = null;
		Map stmtMap = (Map) slicedMethod2stmtMap.get(slicedMethod);

		if (stmtMap != null) {
			result = (Stmt) stmtMap.get(slicedStmt);
		}
		return result;
	}

	/**
	 * Sets the scene to be populated by the slice of the system.
	 *
	 * @param theTransformedSystem will contain the transformed system after transformation.
	 *
	 * @pre theTransformedSystem != null
	 */
	public void setUntransformedSystem(final Scene theTransformedSystem) {
		transformedSystem = theTransformedSystem;
	}

	/**
	 * Correct invalid mappings.  Mappings may be invalidated when transformations external to slicer are applied to  the
	 * slice.  This methods detects such mappings and corrects them.
	 */
	public void completeTransformation() {
		if (sliceType.equals(SlicingEngine.BACKWARD_SLICE)) {
			makeExecutable();
		}
		fixupMethods();

		for (Iterator i = unslicedMethod2stmtMap.keySet().iterator(); i.hasNext();) {
			SootMethod method = (SootMethod) i.next();
			SootMethod slicedMethod = cloner.getCloneOf(method);

			if (slicedMethod == null) {
				slicedMethod2stmtMap.put(slicedMethod, null);
			} else {
				Map unslicedStmt2slicedStmt = (Map) unslicedMethod2stmtMap.get(method);
				Map slicedStmt2unslicedStmt = (Map) slicedMethod2stmtMap.get(slicedMethod);
				PatchingChain untransformedSL = ((JimpleBody) method.getActiveBody()).getUnits();
				PatchingChain transformedSL = ((JimpleBody) slicedMethod.getActiveBody()).getUnits();

				for (Iterator j = untransformedSL.iterator(); j.hasNext();) {
					Stmt stmt = (Stmt) j.next();

					if (!transformedSL.contains(unslicedStmt2slicedStmt.get(stmt))) {
						slicedStmt2unslicedStmt.remove(unslicedStmt2slicedStmt.get(stmt));
						unslicedStmt2slicedStmt.remove(stmt);
					}
				}
			}
		}
	}

	/**
	 * Fixes up the sliced methods.  This includes adjusting the targets of gotos and traps.  This also involves pruning the
	 * exception list at the method interfaces and removing any unwanted or unnecessary statements such as <code>nop</code>.
	 */
	public void fixupMethods() {
		NopEliminator nopTranformation = NopEliminator.v();
		Jimple jimple = Jimple.v();

		for (Iterator i = getTransformedClasses().iterator(); i.hasNext();) {
			SootClass slicedClass = (SootClass) i.next();
			SootClass unslicedClass = getUntransformed(slicedClass);

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
					Unit slicedBeginTrap = (Unit) getTransformed((Stmt) unslicedBeginTrap, unslicedMethod);
					Unit unslicedEndTrap = unslicedTrap.getEndUnit();
					Unit slicedEndTrap = (Unit) getTransformed((Stmt) unslicedEndTrap, unslicedMethod);
					Unit unslicedHandler = unslicedTrap.getHandlerUnit();
					Unit slicedHandler = (Unit) getTransformed((Stmt) unslicedHandler, unslicedMethod);
					slicedTraps.add(jimple.newTrap(getTransformed(unslicedTrap.getException()), slicedBeginTrap,
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
							GotoStmt slicedStmt = (GotoStmt) getTransformed(unslicedStmt, unslicedMethod);
							slicedStmt.setTarget(getSlicedStmt((Stmt) slicedStmt.getTarget(), unslicedMethod));
						} else if (unslicedStmt instanceof IfStmt) {
							IfStmt slicedStmt = (IfStmt) getTransformed(unslicedStmt, unslicedMethod);
							slicedStmt.setTarget(getSlicedStmt(slicedStmt.getTarget(), unslicedMethod));
						} else if (unslicedStmt instanceof LookupSwitchStmt) {
							LookupSwitchStmt slicedStmt = (LookupSwitchStmt) getTransformed(unslicedStmt, unslicedMethod);

							for (int index = 0; index < slicedStmt.getTargetCount(); index++) {
								Stmt target = (Stmt) slicedStmt.getTarget(index);
								slicedStmt.setTarget(index, getSlicedStmt(target, unslicedMethod));
							}

							Stmt target = (Stmt) slicedStmt.getDefaultTarget();
							slicedStmt.setDefaultTarget(getSlicedStmt(target, unslicedMethod));
						} else if (unslicedStmt instanceof TableSwitchStmt) {
							TableSwitchStmt slicedStmt = (TableSwitchStmt) getTransformed(unslicedStmt, unslicedMethod);

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
	 * {@inheritDoc}
	 * 
	 * <p>
	 * This implementation can only handle executable slices of type <code>SlicingEngine.BACKWARD_SLICE</code> and
	 * <code>SlicingEngine.COMPLETE_SLICE</code>. Hence, it will <code>true</code> only in these cases and
	 * <code>false</code> in all other cases.
	 * </p>
	 */
	public boolean handleSliceType(final Object theSliceType, final boolean executableSlice) {
		boolean result =
			(theSliceType.equals(SlicingEngine.BACKWARD_SLICE) || theSliceType.equals(SlicingEngine.COMPLETE_SLICE))
			  && executableSlice;

		if (result) {
			sliceType = theSliceType;
		}
		return result;
	}

	/**
	 * Returns <code>false</code> as the semantics of Jimple would be violated by partial inclusions.
	 *
	 * @return false;
	 *
	 * @see edu.ksu.cis.indus.slicer.ISliceResidualizer#handlesPartialInclusions()
	 */
	public boolean handlesPartialInclusions() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(final Scene theSystem) {
		untransformedSystem = theSystem;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#reset()
	 */
	public void reset() {
		unslicedMethod2stmtMap.clear();
		slicedMethod2stmtMap.clear();
		cloner = null;
		transformedSystem = null;
		untransformedSystem = null;
	}

	/**
	 * @see edu.ksu.cis.indus.transformations.common.ITransformer#transform(soot.jimple.Stmt, soot.SootMethod)
	 */
	public void transform(final Stmt stmt, final SootMethod method) {
		Stmt sliced = cloner.cloneASTFragment(stmt, method);
		PatchingChain unslicedSL = method.getActiveBody().getUnits();
		PatchingChain slicedSL = getSliceStmtListFor(method);

		writeIntoAt(sliced, slicedSL, stmt, unslicedSL);
		addMapping(sliced, stmt, method);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.AbstractSliceResidualizer#transformSeed(soot.jimple.Stmt, soot.SootMethod)
	 */
	protected void transformSeed(final Stmt stmt, final SootMethod method) {
		// TODO: Auto-generated method stub
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.AbstractSliceResidualizer#transformSeed(soot.ValueBox, soot.jimple.Stmt,
	 * 		soot.SootMethod)
	 */
	protected void transformSeed(final ValueBox vb, final Stmt stmt, final SootMethod method) {
		// TODO: Auto-generated method stub
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
		SootMethod slicedMethod = getTransformed(unslicedMethod);
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

	/**
	 * Registers the mapping between statements in the transformed and untransformed versions of a method.
	 *
	 * @param slicedStmt in the transformed version of the method.
	 * @param unslicedStmt in the untransformed version of the method.
	 * @param unslicedMethod in which <code>stmt</code> occurs.
	 *
	 * @pre unslicedStmt != null and slicedStmt != null and unslicedMethod != null
	 * @post getUnslicedStmt(slicedStmt, slicer.getCloneOf(unslicedMethod)) == unslicedStmt
	 * @post getSlicedStmt(unslicedStmt, unslicedMethod) == slicedStmt
	 */
	private void addMapping(final Stmt slicedStmt, final Stmt unslicedStmt, final SootMethod unslicedMethod) {
		Map stmtMap = (Map) unslicedMethod2stmtMap.get(unslicedMethod);

		if (stmtMap != null) {
			stmtMap.put(unslicedStmt, slicedStmt);
		}
		stmtMap = (Map) slicedMethod2stmtMap.get(cloner.getCloneOf(unslicedMethod));

		if (stmtMap != null) {
			stmtMap.put(slicedStmt, unslicedStmt);
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
				thrownInBody.add(getTransformedSootClass(((RefType) ((ThrowStmt) stmt).getOp().getType()).getClassName()));
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
	 * Inserts the given data into the given chain at the same position at which <code>posData</code> is found in
	 * <code>posChain</code>. This is required as the statements of a methods are not maintained in a list into which we can
	 * index directly.
	 *
	 * @param writeData is the data to be inserted into <code>writeChain</code>.
	 * @param writeChain is the chain into which data should be inserted.
	 * @param posData is the data that gives the position at which data should be inserted in <code>writeChain</code>.
	 * @param posChain is the chain relative to which <code>posData</code> gives the insertion position.
	 *
	 * @pre writeData != null and writeChain != null and posData != null and posChain != null
	 * @pre writeChain.size() == posChain.size()
	 * @pre writeChain.contains(posData)
	 */
	private void writeIntoAt(final Stmt writeData, final PatchingChain writeChain, final Object posData,
		final PatchingChain posChain) {
		Object index = null;
		Iterator j = writeChain.iterator();

		for (Iterator i = posChain.iterator(); i.hasNext();) {
			Object temp = i.next();
			index = j.next();

			if (temp.equals(posData)) {
				break;
			}
		}
		writeChain.insertAfter(index, writeData);
		writeChain.remove(index);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.31  2003/11/17 02:23:52  venku
   - documentation.
   - xmlizers require streams/writers to be provided to them
     rather than they constructing them.

   Revision 1.30  2003/11/16 23:12:17  venku
   - coding convention.
   Revision 1.29  2003/11/16 22:55:31  venku
   - added new methods to support processing of seed criteria.
     This is not same as slicing seed criteria of which we do not
     make any distinction.
   Revision 1.28  2003/11/13 14:08:08  venku
   - added a new tag class for the purpose of recording branching information.
   - renamed fixReturnStmts() to makeExecutable() and raised it
     into ISliceResidualizer interface.
   - ripple effect.
   Revision 1.27  2003/11/03 08:02:03  venku
   - ripple effect of changes to ITransformer.
   Revision 1.26  2003/10/21 06:00:19  venku
   - Split slicing type into 2 sets:
        b/w, f/w, and complete
        executable and non-executable.
   - Extended transformer classes to handle these
     classification.
   - Added a new class to house the logic for fixing
     return statements in case of backward executable slice.
   Revision 1.25  2003/10/21 05:22:57  venku
   - moved transformations that were inherent to
     cloning based approach to this class from SlicingEngine.
   Revision 1.24  2003/10/13 00:59:57  venku
   - Split transformations.slicer into 2 packages
      - transformations.slicer
      - slicer
   - Ripple effect of the above changes.
   Revision 1.23  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.22  2003/09/27 01:08:38  venku
   - documentation.
   Revision 1.21  2003/09/26 15:08:35  venku
   - ripple effect of changes in ITransformer.
   Revision 1.20  2003/09/15 07:52:08  venku
   - added a new transformer interface specifically targetted for slicing.
   - implemented the above interface.
   Revision 1.19  2003/08/25 07:17:38  venku
   Exposed initialize() as a public method.
   Removed SlicingTag class and used StringTag instead.
   Revision 1.18  2003/08/21 09:30:31  venku
    - added a new transform() method which can transform at the level of ValueBox.
    - CloningBasedSliceResidualizer does not do anything in this new method.
   Revision 1.17  2003/08/21 07:34:41  venku
   Documentation.
   Revision 1.16  2003/08/20 18:31:22  venku
   Documentation errors fixed.
   Revision 1.15  2003/08/19 12:46:07  venku
   Documentation changes.
   Revision 1.14  2003/08/19 12:44:39  venku
   Changed the signature of ITransformer.getLocal()
   Introduced reset() in ITransformer.
   Ripple effect of the above changes.
   Revision 1.13  2003/08/19 11:59:05  venku
   Patching commit.
   Revision 1.12  2003/08/19 11:52:25  venku
   The following renaming have occurred ITransformMap to ITransformer, SliceMapImpl to SliceTransformer,
   and  Slicer to SliceEngine.
   Ripple effect of the above.
   Revision 1.11  2003/08/19 11:37:41  venku
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
   Revision 1.10  2003/08/18 12:14:13  venku
   Well, to start with the slicer implementation is complete.
   Although not necessarily bug free, hoping to stabilize it quickly.
   Revision 1.9  2003/08/18 05:01:45  venku
   Committing package name change in source after they were moved.
   Revision 1.8  2003/08/18 04:49:47  venku
   Modified SlicerMap to be an specific implementation of ITransformMap specific to the Slicer.
   Revision 1.7  2003/08/18 02:40:23  venku
   It is better to elevate the mapping interface to a Type and implement in SliceMap.
   This is the last commit in that direction.  After this I will move SliceMap to SliceMapImpl.
   Revision 1.6  2003/08/17 11:56:18  venku
   Renamed SliceCriterion to AbstractSliceCriterion.
   Formatting, documentation, and specification.
   Revision 1.5  2003/05/22 22:23:50  venku
   Changed interface names to start with a "I".
   Formatting.
 */
