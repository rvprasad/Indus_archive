
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

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.collections.CollectionUtils;

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.InvokeStmt;


/**
 * This class uses symbolic- and escape-analysis as calculated by <code>EquivalenceClassBasedEscapeAnalysis</code> to prune
 * the ready dependency information calculated by it's parent class.
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
	 * This provides object flow information.
	 */
	private IValueAnalyzer ofa;

	/**
	 * Checks if the given enter monitor statement/synchronized method  is dependent on the exit monitor
	 * statement/synchronized method according to rule 2.   This relies on the method with same signature in it's super
	 * class.  Beyond that it uses object flow analysis to further improve  precision.
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
			Object _enterStmt = enterPair.getFirst();
			Object _exitStmt = exitPair.getFirst();
			Collection _col1;
			Collection _col2;
			boolean _syncedStaticMethod1 = false;
			boolean _syncedStaticMethod2 = false;
			final Context _context = new Context();

			if (_enterStmt.equals(SYNC_METHOD_PROXY_STMT)) {
				_syncedStaticMethod1 = _enterMethod.isStatic();

				if (!_syncedStaticMethod1) {
					_context.setRootMethod(_enterMethod);
					_col1 = ofa.getValuesForThis(_context);
				} else {
					_col1 = Collections.EMPTY_LIST;
				}
			} else {
				final EnterMonitorStmt _o1 = (EnterMonitorStmt) _enterStmt;
				_context.setProgramPoint(_o1.getOpBox());
				_context.setStmt(_o1);
				_context.setRootMethod(_enterMethod);
				_col1 = ofa.getValues(_o1.getOp(), _context);
			}

			if (_exitStmt.equals(SYNC_METHOD_PROXY_STMT)) {
				_syncedStaticMethod2 = _exitMethod.isStatic();

				if (!_syncedStaticMethod2) {
					_context.setProgramPoint(null);
					_context.setStmt(null);
					_context.setRootMethod(_exitMethod);
					_col2 = ofa.getValuesForThis(_context);
				} else {
					_col2 = Collections.EMPTY_LIST;
				}
			} else {
				final ExitMonitorStmt _o2 = (ExitMonitorStmt) _exitStmt;
				_context.setProgramPoint(_o2.getOpBox());
				_context.setStmt(_o2);
				_context.setRootMethod(_exitMethod);
				_col2 = ofa.getValues(_o2.getOp(), _context);
			}

			if (_syncedStaticMethod1 ^ _syncedStaticMethod2) {
				/*
				 * if only one of the methods is static and synchronized then we cannot determine RDA as it is possible that
				 * the monitor in the non-static method may actually be on the class object of the class in  which the static
				 * method is defined.  There are many combinations which can be pruned.  No time now. THINK
				 */
				_result = true;
			} else {
				_result = !CollectionUtils.intersection(_col1, _col2).isEmpty();
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
		InvokeStmt notify = (InvokeStmt) nPair.getFirst();
		InvokeStmt wait = (InvokeStmt) wPair.getFirst();
		SootMethod wMethod = (SootMethod) wPair.getSecond();
		SootMethod nMethod = (SootMethod) nPair.getSecond();
		return ecba.isReadyDependent(wait, wMethod, notify, nMethod);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws InitializationException when object flow analysis is not provided.
	 *
	 * @pre info.get(OFAnalyzer.ID) != null and info.get(OFAnalyzer.ID).oclIsTypeOf(OFAnalyzer)
	 *
	 * @see ReadyDAv2#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		ofa = (IValueAnalyzer) info.get(IValueAnalyzer.ID);

		if (ofa == null) {
			throw new InitializationException(IValueAnalyzer.ID + " was not provided in the info.");
		}
	}
}

/*
   ChangeLog:
   $Log$
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
