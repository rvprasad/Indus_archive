
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import soot.SootMethod;
import soot.Value;

import soot.jimple.AssignStmt;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.support.Pair;


/**
 * This class uses escape-analysis information as calculated by {@link
 * edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis
 * EquivalenceClassBasedEscapeAnalysis} to prune the interference dependence edges as calculated by it's parent class.  This
 * can be further spruced by symbolic-analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @see InterferenceDAv1
 */
public class InterferenceDAv2
  extends InterferenceDAv1 {
	/**
	 * This provide information shared access in the analyzed system.  This is required by the analysis.
	 */
	private EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * Checks if the given array/field access expression is dependent on the given array/field definition expression.
	 *
	 * @param dependent is the array/field read access site.
	 * @param dependee is the array/field write access site.
	 *
	 * @return <code>true</code> if <code>dependent</code> is dependent on <code>dependee</code>; <code>false</code>,
	 * 		   otherwise.
	 *
	 * @pre dependee.getFirst() != null and dependee.getSecond() != null
	 * @pre dependee.getFirst().oclIsTypeOf(AssignStmt) and dependee.getSecond().oclIsTypeOf(SootMethod)
	 * @pre dependent.getFirst() != null and dependent.getSecond() != null
	 * @pre dependent.getFirst().oclIsTypeOf(AssignStmt) and dependent.getSecond().oclIsTypeOf(SootMethod)
	 */
	protected boolean isDependentOn(final Pair dependent, final Pair dependee) {
		SootMethod deMethod = (SootMethod) dependee.getSecond();
		SootMethod dtMethod = (SootMethod) dependent.getSecond();
		Value de = ((AssignStmt) dependee.getFirst()).getLeftOp();
		Value dt = ((AssignStmt) dependent.getFirst()).getRightOp();
		return ecba.isShared(de, deMethod) && ecba.isShared(dt, dtMethod);
	}

	/**
	 * Extracts information provided by the environment via <code>info</code> parameter to {@link #initialize initialize}.
	 *
	 * @throws InitializationException when and instance of pair managing service or interference analysis is not provided.
	 *
	 * @pre info.get(EquivalenceClassBasedEscapeAnalysis.ID) != null
	 *
	 * @see InterferenceDAv1#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		ecba = (EquivalenceClassBasedEscapeAnalysis) info.get(EquivalenceClassBasedEscapeAnalysis.ID);

		if (pairMgr == null) {
			throw new InitializationException(EquivalenceClassBasedEscapeAnalysis.ID + " was not provided in info.");
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.10  2003/09/08 02:28:02  venku
   - ifDependentOn() was changed to isDependentOn().
   Revision 1.9  2003/08/21 03:56:08  venku
   Formatting.
   Revision 1.8  2003/08/21 01:25:21  venku
    - Renamed src-escape to src-concurrency to as to group all concurrency
      issue related analyses into a package.
    - Renamed escape package to concurrency.escape.
    - Renamed EquivalenceClassBasedAnalysis to EquivalenceClassBasedEscapeAnalysis.
   Changes due to the ripple effect of the above changes are being committed.
   Revision 1.7  2003/08/14 05:10:29  venku
   Fixed documentation links.
   Revision 1.6  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.5  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.4  2003/08/09 23:52:54  venku
   - import reorganization
   Revision 1.3  2003/08/09 23:46:11  venku
   Well if the read and write access points are marked as shared, then pessimistically
   they occur in different threads.  In such situation, sequential path between
   these points does not bear any effect unless the escape analysis is thread and
   call-tree sensitive.
 */
