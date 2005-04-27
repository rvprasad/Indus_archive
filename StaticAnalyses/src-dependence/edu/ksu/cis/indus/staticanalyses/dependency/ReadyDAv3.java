
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.staticanalyses.dependency.direction.BackwardDirectionSensitiveInfo;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.ForwardDirectionSensitiveInfo;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.IDirectionSensitiveInfo;

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.MonitorStmt;


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
public final class ReadyDAv3
  extends ReadyDAv2 {
	/**
	 * Creates an instance of this class.
	 *
	 * @param directionSensitiveInfo that controls the direction.
	 * @param direction of the analysis
	 *
	 * @pre info != null and direction != null
	 */
	private ReadyDAv3(final IDirectionSensitiveInfo directionSensitiveInfo, final Object direction) {
		super(directionSensitiveInfo, direction);
	}

	/**
	 * Retrieves an instance of ready dependence analysis that calculates information in backward direction.
	 *
	 * @return an instance of ready dependence.
	 *
	 * @post result != null
	 */
	public static ReadyDAv1 getBackwardReadyDA() {
		return new ReadyDAv3(new BackwardDirectionSensitiveInfo(), BACKWARD_DIRECTION);
	}

	/**
	 * Retrieves an instance of ready dependence analysis that calculates information in forward direction.
	 *
	 * @return an instance of ready dependence.
	 *
	 * @post result != null
	 */
	public static ReadyDAv1 getForwardReadyDA() {
		return new ReadyDAv3(new ForwardDirectionSensitiveInfo(), FORWARD_DIRECTION);
	}

	/**
	 * Checks if the given enter monitor statement/synchronized method is dependent on the exit monitor
	 * statement/synchronized method according to rule 2.   The results of escape analysis info calculated {@link
	 * edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis
	 * EquivalenceClassBasedEscapeAnalysis} analysis along with lock entities information is used to  determine the
	 * dependence.
	 *
	 * @param enterPair is the enter monitor statement and containg statement pair.
	 * @param exitPair is the exit monitor statement and containg statement pair.
	 *
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 *
	 * @pre enterPair.getSecond() != null and exitPair.getSecond() != null
	 *
	 * @see ReadyDAv2#ifDependentOnByRule2(Pair, Pair)
	 */
	protected boolean ifDependentOnByRule2(final Pair enterPair, final Pair exitPair) {
		boolean _result = super.ifDependentOnByRule2(enterPair, exitPair);

		if (_result) {
			final SootMethod _enterMethod = (SootMethod) enterPair.getSecond();
			final SootMethod _exitMethod = (SootMethod) exitPair.getSecond();

			if (!(_enterMethod.isStatic() && _exitMethod.isStatic())) {
				final MonitorStmt _enter = (MonitorStmt) enterPair.getFirst();
				final MonitorStmt _exit = (MonitorStmt) exitPair.getFirst();
				_result = ecba.areMonitorsCoupled(_enter, _enterMethod, _exit, _exitMethod);
			}
		}
		return _result;
	}

	/**
	 * Checks if the given <code>wait()</code> call-site is dependent on the <code>notifyXX()</code> call-site according to
	 * rule 2.  The symbolic and escape analysis infomration from {@link
	 * edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis
	 * EquivalenceClassBasedEscapeAnalysis} analysis is used to determine the dependence.
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
			final InvokeStmt _notify = (InvokeStmt) nPair.getFirst();
			final InvokeStmt _wait = (InvokeStmt) wPair.getFirst();
			final SootMethod _wMethod = (SootMethod) wPair.getSecond();
			final SootMethod _nMethod = (SootMethod) nPair.getSecond();
			_result = ecba.areWaitAndNotifyCoupled(_wait, _wMethod, _notify, _nMethod);
		}
		return _result;
	}
}

// End of File
