
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

package edu.ksu.cis.indus.staticanalyses.concurrency.independence;

import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;

import soot.SootField;
import soot.SootMethod;

import soot.jimple.ArrayRef;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * This is a specialized version of <code>IndependentStmtDetector</code> that uses sharing information calculated by
 * <code>EquivalenceClassBasedEscapeAnalysis</code> to detect independent statements.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class IndependentStmtDetectorv2
  extends IndependentStmtDetector {
	/**
	 * @see edu.ksu.cis.indus.staticanalyses.concurrency.independence.IndependentStmtDetector#isIndependent(soot.jimple.Stmt,
	 * 		soot.SootMethod)
	 */
	protected boolean isIndependent(final Stmt stmt, final SootMethod method) {
		boolean _result = super.isIndependent(stmt, method);

		if (!_result) {
			if (stmt.containsArrayRef()) {
				final ArrayRef _arrayRef = stmt.getArrayRef();
				_result = !escapeInfo.fieldAccessShared(_arrayRef.getBase(), method, IEscapeInfo.READ_WRITE_SHARED_ACCESS);
			} else if (stmt.containsFieldRef()) {
				final FieldRef _fieldRef = stmt.getFieldRef();
				final SootField _field = _fieldRef.getField();
                _result = _field.isFinal();

				if (!_result) {
                    final String _signature = _field.getSignature();
					if (_fieldRef instanceof InstanceFieldRef) {
                        _result =
							!escapeInfo.fieldAccessShared(((InstanceFieldRef) _fieldRef).getBase(), method,
								_signature, IEscapeInfo.READ_WRITE_SHARED_ACCESS);
					} else {
						_result = !escapeInfo.staticfieldAccessShared(_field.getDeclaringClass(), method, _signature);
					}
				}
			} else if (stmt instanceof EnterMonitorStmt) {
				_result = !escapeInfo.lockUnlockShared(((EnterMonitorStmt) stmt).getOp(), method);
			} else if (stmt.containsInvokeExpr()) {
				final InvokeExpr _invokeExpr = stmt.getInvokeExpr();

				if (_invokeExpr instanceof VirtualInvokeExpr) {
					final VirtualInvokeExpr _vExpr = (VirtualInvokeExpr) _invokeExpr;
					final SootMethod _sm = _invokeExpr.getMethod();
					_result =
						Util.isNotifyMethod(_sm)
						  || (Util.isWaitMethod(_sm) && !escapeInfo.waitNotifyShared(_vExpr.getBase(), method));
				}
			}
		}
		return _result;
	}
}

// End of File
