/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.datastructures.Pair;

import edu.ksu.cis.indus.staticanalyses.dependency.direction.BackwardDirectionSensitiveInfo;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.ForwardDirectionSensitiveInfo;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.IDirectionSensitiveInfo;

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.InvokeStmt;

/**
 * This class uses symbolic- and escape-analysis as calculated by <code>EquivalenceClassBasedEscapeAnalysis</code> to prune
 * the ready dependency information calculated by it's parent class. This class will also use OFA information if it is
 * configured to do so.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @see edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1
 */
public final class ReadyDAv3
		extends ReadyDAv2 {

	/**
	 * Creates an instance of this class.
	 * 
	 * @param directionSensitiveInfo that controls the direction.
	 * @param direction of the analysis
	 * @pre info != null and direction != null
	 */
	private ReadyDAv3(final IDirectionSensitiveInfo directionSensitiveInfo, final Direction direction) {
		super(directionSensitiveInfo, direction);
	}

	/**
	 * Retrieves an instance of ready dependence analysis that calculates information in backward direction.
	 * 
	 * @return an instance of ready dependence.
	 * @post result != null
	 */
	public static ReadyDAv1 getBackwardReadyDA() {
		return new ReadyDAv3(new BackwardDirectionSensitiveInfo(), Direction.BACKWARD_DIRECTION);
	}

	/**
	 * Retrieves an instance of ready dependence analysis that calculates information in forward direction.
	 * 
	 * @return an instance of ready dependence.
	 * @post result != null
	 */
	public static ReadyDAv1 getForwardReadyDA() {
		return new ReadyDAv3(new ForwardDirectionSensitiveInfo(), Direction.FORWARD_DIRECTION);
	}

	/**
	 * Checks if the given enter monitor statement/synchronized method is dependent on the exit monitor statement/synchronized
	 * method according to rule 2. The results of escape analysis info calculated {@link
	 * edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis
	 * EquivalenceClassBasedEscapeAnalysis} analysis along with lock entities information is used to determine the dependence.
	 * 
	 * @param enterPair is the enter monitor statement and containg statement pair.
	 * @param exitPair is the exit monitor statement and containg statement pair.
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 * @pre enterPair.getSecond() != null and exitPair.getSecond() != null
	 * @see ReadyDAv2#ifDependentOnByRule2(Pair, Pair)
	 */
	@Override protected boolean ifDependentOnByRule2(final Pair<EnterMonitorStmt, SootMethod> enterPair,
			final Pair<ExitMonitorStmt, SootMethod> exitPair) {
		boolean _result = super.ifDependentOnByRule2(enterPair, exitPair);

		if (_result) {
			final SootMethod _enterMethod = enterPair.getSecond();
			final SootMethod _exitMethod = exitPair.getSecond();

			if (!(_enterMethod.isStatic() && _exitMethod.isStatic())) {
				final EnterMonitorStmt _enter = enterPair.getFirst();
				final ExitMonitorStmt _exit = exitPair.getFirst();
				_result = ecba.areMonitorsCoupled(_enter, _enterMethod, _exit, _exitMethod);
			}
		}
		return _result;
	}

	/**
	 * Checks if the given <code>wait()</code> call-site is dependent on the <code>notifyXX()</code> call-site according
	 * to rule 2. The symbolic and escape analysis infomration from {@link
	 * edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis
	 * EquivalenceClassBasedEscapeAnalysis} analysis is used to determine the dependence.
	 * 
	 * @param wPair is the statement in which <code>java.lang.Object.wait()</code> is invoked.
	 * @param nPair is the statement in which <code>java.lang.Object.notifyXX()</code> is invoked.
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 * @pre wPair.getSecond() != null and nPair.getSecond() != null
	 * @see ReadyDAv1#ifDependentOnByRule4(Pair, Pair)
	 */
	@Override protected boolean ifDependentOnByRule4(final Pair<InvokeStmt, SootMethod> wPair,
			final Pair<InvokeStmt, SootMethod> nPair) {
		boolean _result = super.ifDependentOnByRule4(wPair, nPair);

		if (_result) {
			final InvokeStmt _notify = nPair.getFirst();
			final InvokeStmt _wait = wPair.getFirst();
			final SootMethod _wMethod = wPair.getSecond();
			final SootMethod _nMethod = nPair.getSecond();
			_result = ecba.areWaitAndNotifyCoupled(_wait, _wMethod, _notify, _nMethod);
		}
		return _result;
	}
}

// End of File
