
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.Value;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * This class uses escape-analysis as calculated by <code>EquivalenceClassBasedEscapeAnalysis</code> to prune the ready
 * dependency information calculated by it's parent class. This class will also use OFA information if it is configured to
 * do so.
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
	protected EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * Checks if the given enter monitor statement/synchronized method  is dependent on the exit monitor
	 * statement/synchronized method according to rule 2.   The results of escape analysis info calculated {@link
	 * edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis
	 * EquivalenceClassBasedEscapeAnalysis} analysis is used to determine the dependence.
	 *
	 * @param enterPair is the enter monitor statement and containg statement pair.
	 * @param exitPair is the exit monitor statement and containg statement pair.
	 *
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 *
	 * @pre enterPair.getSecond() != null and exitPair.getSecond() != null
	 *
	 * @see ReadyDAv1#ifDependentOnByRule2(Pair, Pair)
	 */
	protected boolean ifDependentOnByRule2(final Pair enterPair, final Pair exitPair) {
		boolean _result = super.ifDependentOnByRule2(enterPair, exitPair);

		if (_result) {
			final SootMethod _enterMethod = (SootMethod) enterPair.getSecond();
			final SootMethod _exitMethod = (SootMethod) exitPair.getSecond();
			final Object _o1 = enterPair.getFirst();
			final Object _o2 = exitPair.getFirst();
			boolean _flag1;
			boolean _flag2;

			if (_o1.equals(SYNC_METHOD_PROXY_STMT)) {
				_flag1 = ecba.thisEscapes(_enterMethod);
			} else {
				final Value _enter = ((EnterMonitorStmt) _o1).getOp();
				_flag1 = ecba.escapes(_enter, _enterMethod);
			}

			if (_o2.equals(SYNC_METHOD_PROXY_STMT)) {
				_flag2 = ecba.thisEscapes(_exitMethod);
			} else {
				final Value _exit = ((ExitMonitorStmt) _o2).getOp();
				_flag2 = ecba.escapes(_exit, _exitMethod);
			}
			_result = _flag1 && _flag2;
		}
		return _result;
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
		boolean _result = super.ifDependentOnByRule4(wPair, nPair);

		if (_result) {
			final Value _notify = ((VirtualInvokeExpr) ((InvokeStmt) nPair.getFirst()).getInvokeExpr()).getBase();
			final Value _wait = ((VirtualInvokeExpr) ((InvokeStmt) wPair.getFirst()).getInvokeExpr()).getBase();
			final SootMethod _wMethod = (SootMethod) wPair.getSecond();
			final SootMethod _nMethod = (SootMethod) nPair.getSecond();
			_result = ecba.escapes(_notify, _nMethod) && ecba.escapes(_wait, _wMethod);
		}
		return _result;
	}

	/**
	 * Extracts information as provided by environment at initialization time.  It collects <code>wait</code> and
	 * <code>notifyXX</code> methods as represented in the AST system. It also extract call graph info, pair manaing
	 * service, and environment from the <code>info</code> member.
	 *
	 * @throws InitializationException when call graph info, pair managing service, or environment is not available in
	 * 		   <code>info</code> member.
	 *
	 * @see AbstractAnalysis#setup()
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
   Revision 1.18  2004/01/21 13:44:09  venku
   - made ready dependence to consider synchronized methods as well.
   - ReadyDAv2 uses escape information for both sorts of inter-thread
     ready DA.
   - ReadyDAv3 uses escape and object flow information for
     monitor based inter-thread ready DA while using symbol-based
     escape information for wait/notify based inter-thread ready DA.
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
   Revision 1.13  2003/11/06 03:12:02  venku
   - it was possible for escaping values of classes in different
     branches of the class hierarchy to be declared as ready dependent
     by rule 2 and 4.  Fixed this by calling the super
     ifDependentOnByRuleX() to ensure that the types occur
     in the same branch of the class hierarchy before using escape
     information.
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
