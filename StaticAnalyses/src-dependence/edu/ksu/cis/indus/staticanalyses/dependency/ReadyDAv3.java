
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

import soot.SootMethod;

import soot.jimple.InvokeStmt;


/**
 * This class uses symbolic- and escape-analysis as calculated by <code>EquivalenceClassBasedEscapeAnalysis</code> to prune
 * the ready dependency information calculated by it's parent class. This class will also use OFA information if it is
 * configured to do so.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @see edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1
 */
public class ReadyDAv3
  extends ReadyDAv2 {
	/**
	 * Checks if the given <code>wait()</code> call-site is dependent on the <code>notifyXX()</code> call-site according to
	 * rule 2.  The symbolic and escape analysis infomration from {@link
	 * edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis
	 * EquivalenceClassBasedEscapeAnalysis} analysis is used to determine the dependence.   This method will also use OFA
	 * information if it is configured to do so.
	 *
	 * @param wPair is the statement in which <code>java.lang.Object.wait()</code> is invoked.
	 * @param nPair is the statement in which <code>java.lang.Object.notifyXX()</code> is invoked.
	 *
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 *
	 * @pre wPair.getSecond() != null and nPair.getSecond() != null
	 *
	 * @see ReadyDAv1#ifDependentOnByRule4(Pair, Pair)
	 */
	protected boolean ifDependentOnByRule4(final Pair wPair, final Pair nPair) {
		final InvokeStmt _notify = (InvokeStmt) nPair.getFirst();
		final InvokeStmt _wait = (InvokeStmt) wPair.getFirst();
		final SootMethod _wMethod = (SootMethod) wPair.getSecond();
		final SootMethod _nMethod = (SootMethod) nPair.getSecond();
		boolean _result = ecba.areWaitAndNotifyCoupled(_wait, _wMethod, _notify, _nMethod);

		if (_result && getUseOFA()) {
			_result = ifDependentOnBasedOnOFAByRule4(wPair, nPair);
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2004/01/25 15:32:41  venku
   - enabled ready and interference dependences to be OFA aware.
   Revision 1.6  2004/01/21 13:44:09  venku
   - made ready dependence to consider synchronized methods as well.
   - ReadyDAv2 uses escape information for both sorts of inter-thread
     ready DA.
   - ReadyDAv3 uses escape and object flow information for
     monitor based inter-thread ready DA while using symbol-based
     escape information for wait/notify based inter-thread ready DA.
   Revision 1.5  2004/01/06 00:17:00  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.
   Revision 1.4  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.3  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.2  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.1  2003/11/05 08:25:37  venku
   - This version of ReadyDA is based on symbolic analysis
     and escape analysis.
   Revision 1.11  2003/11/03 07:54:29  venku
   - deleted comments.
   Revision 1.10  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.9  2003/08/26 16:53:57  venku
   logging added.
   Revision 1.8  2003/08/25 09:04:31  venku
   It was not a good decision to decide interproceduralness of the
   analyses at construction.  Hence, it now can be controlled via public
   method setInterprocedural().
   Ripple effect.
   Revision 1.7  2003/08/21 01:25:21  venku
    - Renamed src-escape to src-concurrency to as to group all concurrency
      issue related analyses into a package.
    - Renamed escape package to concurrency.escape.
    - Renamed EquivalenceClassBasedAnalysis to EquivalenceClassBasedEscapeAnalysis.
   Changes due to the ripple effect of the above changes are being committed.
   Revision 1.6  2003/08/14 05:10:29  venku
   Fixed documentation links.
   Revision 1.5  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.4  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.2  2003/08/09 23:33:30  venku
    - Enabled ready dependency to be interprocedural.
    - Utilized containsXXX() method in Stmt.
 */
