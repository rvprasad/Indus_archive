
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

import edu.ksu.cis.indus.common.datastructures.Pair;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;

import soot.SootMethod;
import soot.Value;

import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;


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
	 * @see InterferenceDAv1#isArrayDependentOn(Pair, Pair, ArrayRef, ArrayRef)
	 */
	protected boolean isArrayDependentOn(final Pair dependent, final Pair dependee, final ArrayRef dependentArrayRef,
		final ArrayRef dependeeArrayRef) {
		boolean _result = super.isArrayDependentOn(dependent, dependee, dependentArrayRef, dependeeArrayRef);

		if (_result) {
			final SootMethod _deMethod = (SootMethod) dependee.getSecond();
			final SootMethod _dtMethod = (SootMethod) dependent.getSecond();
			final Value _de = dependeeArrayRef.getBase();
			final Value _dt = dependentArrayRef.getBase();
			_result = ecba.escapes(_de, _deMethod) && ecba.escapes(_dt, _dtMethod);
		}
		return _result;
	}

	/**
	 * @see InterferenceDAv1#isFieldDependentOn(Pair, Pair, InstanceFieldRef, InstanceFieldRef)
	 */
	protected boolean isFieldDependentOn(final Pair dependent, final Pair dependee, final InstanceFieldRef dependentFieldRef,
		final InstanceFieldRef dependeeFieldRef) {
		boolean _result = super.isFieldDependentOn(dependent, dependee, dependentFieldRef, dependeeFieldRef);

		if (_result) {
			final SootMethod _deMethod = (SootMethod) dependee.getSecond();
			final SootMethod _dtMethod = (SootMethod) dependent.getSecond();
			final Value _de = dependeeFieldRef.getBase();
			final Value _dt = dependentFieldRef.getBase();
			_result = ecba.escapes(_de, _deMethod) && ecba.escapes(_dt, _dtMethod);
		}
		return _result;
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
   Revision 1.19  2004/02/12 21:32:21  venku
   - refactored the code to test the escaping/sharing status of the
     base rather than the field/array location (bummer).

   Revision 1.18  2004/01/25 15:32:41  venku
   - enabled ready and interference dependences to be OFA aware.
   Revision 1.17  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.16  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.15  2003/12/08 12:15:58  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.14  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.13  2003/11/06 03:07:22  venku
   - it was possible for escaping values of different types to
     be declared as interfering.  Fixed this by calling the
     super isDependentOn() to ensure type equality and
     then use escape information.
   Revision 1.12  2003/09/29 13:37:25  venku
 *** empty log message ***
           Revision 1.11  2003/09/28 03:16:48  venku
           - I don't know.  cvs indicates that there are no differences,
             but yet says it is out of sync.
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
