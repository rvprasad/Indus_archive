
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

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.VirtualInvokeExpr;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.support.Pair;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class uses escape-analysis as calculated by <code>EquivalenceClassBasedEscapeAnalysis</code> to prune the ready
 * dependency information calculated by it's parent class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @see edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1
 */
public class ReadyDAv2
  extends ReadyDAv1 {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ReadyDAv2.class);

	/**
	 * This provides information to prune ready dependence edges.
	 */
	private EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * Checks if the given enter-monitor statement is dependent on the exit-monitor statement according to rule 2. The
	 * results of escape analysis info calculated {@link
	 * edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis
	 * EquivalenceClassBasedEscapeAnalysis} analysis is used to determine the dependence.
	 *
	 * @param enterPair is the enter monitor statement.
	 * @param exitPair is the exit monitor statement.
	 *
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 *
	 * @pre enterPair.getSecond() != null and exitPair.getSecond() != null
	 *
	 * @see ReadyDAv1#ifDependentOnByRule2(Pair, Pair)
	 */
	protected boolean ifDependentOnByRule2(final Pair enterPair, final Pair exitPair) {
		boolean result = super.ifDependentOnByRule2(enterPair, exitPair);

		if (result) {
			Value enter = ((EnterMonitorStmt) enterPair.getFirst()).getOp();
			Value exit = ((ExitMonitorStmt) exitPair.getFirst()).getOp();
			SootMethod enterMethod = (SootMethod) enterPair.getSecond();
			SootMethod exitMethod = (SootMethod) exitPair.getSecond();
			result = ecba.escapes(enter, enterMethod) && ecba.escapes(exit, exitMethod);
		}
		return result;
	}

	/**
	 * Checks if the given <code>wait()</code> call-site is dependent on the <code>notifyXX()</code> call-site according to
	 * rule 2.  The results of escape analysis info calculated by <code>EquivalenceClassbasedAnalysis</code>analysis is used
	 * to determine the dependence.
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
		boolean result = super.ifDependentOnByRule4(wPair, nPair);

		if (result) {
			Value notify = ((VirtualInvokeExpr) ((InvokeStmt) nPair.getFirst()).getInvokeExpr()).getBase();
			Value wait = ((VirtualInvokeExpr) ((InvokeStmt) wPair.getFirst()).getInvokeExpr()).getBase();
			SootMethod wMethod = (SootMethod) wPair.getSecond();
			SootMethod nMethod = (SootMethod) nPair.getSecond();
			result = ecba.escapes(notify, nMethod) && ecba.escapes(wait, wMethod);
		}
		return result;
	}

	/**
	 * Extracts information as provided by environment at initialization time.  It collects <code>wait</code> and
	 * <code>notifyXX</code> methods as represented in the AST system. It also extract call graph info, pair manaing
	 * service, and environment from the <code>info</code> member.
	 *
	 * @throws InitializationException when call graph info, pair managing service, or environment is not available in
	 * 		   <code>info</code> member.
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		ecba = (EquivalenceClassBasedEscapeAnalysis) info.get(EquivalenceClassBasedEscapeAnalysis.ID);

		if (ecba == null) {
			LOGGER.error(EquivalenceClassBasedEscapeAnalysis.ID + " was not provided in info.");
			throw new InitializationException(EquivalenceClassBasedEscapeAnalysis.ID + " was not provided in info.");
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.12  2003/11/05 08:25:12  venku
   - This version of Ready DA is only based on escape information.
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
