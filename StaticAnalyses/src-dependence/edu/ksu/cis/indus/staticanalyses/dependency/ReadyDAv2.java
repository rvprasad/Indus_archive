
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
import edu.ksu.cis.indus.interfaces.IEscapeInfo;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.BackwardDirectionSensitiveInfo;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.ForwardDirectionSensitiveInfo;
import edu.ksu.cis.indus.staticanalyses.dependency.direction.IDirectionSensitiveInfo;

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
	protected IEscapeInfo ecba;

	/**
	 * Creates an instance of this class.
	 *
	 * @param directionSensitiveInfo that controls the direction.
	 * @param direction of the analysis
	 *
	 * @pre info != null and direction != null
	 */
	protected ReadyDAv2(final IDirectionSensitiveInfo directionSensitiveInfo, final Object direction) {
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
		return new ReadyDAv2(new BackwardDirectionSensitiveInfo(), BACKWARD_DIRECTION);
	}

	/**
	 * Retrieves an instance of ready dependence analysis that calculates information in forward direction.
	 *
	 * @return an instance of ready dependence.
	 *
	 * @post result != null
	 */
	public static ReadyDAv1 getForwardReadyDA() {
		return new ReadyDAv2(new ForwardDirectionSensitiveInfo(), FORWARD_DIRECTION);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#analyze()
	 */
	public void analyze() {
		unstable();

		if (ecba.isStable()) {
			super.analyze();
		}
	}

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
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		ecba = (IEscapeInfo) info.get(IEscapeInfo.ID);

		if (ecba == null) {
			LOGGER.error(IEscapeInfo.ID + " was not provided in info.");
			throw new InitializationException(IEscapeInfo.ID + " was not provided in info.");
		}
	}
}

// End of File
