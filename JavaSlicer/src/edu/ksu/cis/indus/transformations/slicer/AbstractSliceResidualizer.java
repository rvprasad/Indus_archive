
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

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;

import edu.ksu.cis.indus.slicer.AbstractSliceCriterion;
import edu.ksu.cis.indus.slicer.SliceExpr;
import edu.ksu.cis.indus.slicer.SliceStmt;
import edu.ksu.cis.indus.transformations.common.AbstractTransformer;

import java.util.Collection;
import java.util.Iterator;


/**
 * DOCUMENT ME!
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractSliceResidualizer
  extends AbstractTransformer
  implements ISliceResidualizer {
	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public void makeExecutable() {
		// TODO: implement me!
	}

	/**
	 * Marks the given criteria as included in the slice.  {@inheritDoc}
	 *
	 * @param seedcriteria DOCUMENT ME!
	 *
	 * @see edu.ksu.cis.indus.slicer.ISliceResidualizer#processSeedCriteria(java.util.Collection)
	 */
	public final void processSeedCriteria(final Collection seedcriteria) {
		for (Iterator i = seedcriteria.iterator(); i.hasNext();) {
			AbstractSliceCriterion crit = (AbstractSliceCriterion) i.next();

			if (crit instanceof SliceExpr) {
				SliceExpr expr = (SliceExpr) crit;
				transformSeed((ValueBox) expr.getCriterion(), expr.getOccurringStmt(), expr.getOccurringMethod());
			} else if (crit instanceof SliceStmt) {
				SliceStmt stmt = (SliceStmt) crit;
				transformSeed((Stmt) stmt.getCriterion(), stmt.getOccurringMethod());
			}
			crit.sliced();
		}
	}

	/**
	 * {@inheritDoc} Does nothing.
	 */
	public void transform(final ValueBox vBox, final Stmt stmt, final SootMethod method) {
		// does nothing
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param stmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 */
	protected abstract void transformSeed(final Stmt stmt, final SootMethod method);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param vb DOCUMENT ME!
	 * @param stmt DOCUMENT ME!
	 * @param method DOCUMENT ME!
	 */
	protected abstract void transformSeed(final ValueBox vb, final Stmt stmt, final SootMethod method);
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/11/19 23:56:23  venku
   - documentation.

   Revision 1.3  2003/11/16 22:55:31  venku
   - added new methods to support processing of seed criteria.
     This is not same as slicing seed criteria of which we do not
     make any distinction.
   Revision 1.2  2003/11/13 14:08:08  venku
   - added a new tag class for the purpose of recording branching information.
   - renamed fixReturnStmts() to makeExecutable() and raised it
     into ISliceResidualizer interface.
   - ripple effect.
   Revision 1.1  2003/10/21 06:00:19  venku
   - Split slicing type into 2 sets:
        b/w, f/w, and complete
        executable and non-executable.
   - Extended transformer classes to handle these
     classification.
   - Added a new class to house the logic for fixing
     return statements in case of backward executable slice.
 */
