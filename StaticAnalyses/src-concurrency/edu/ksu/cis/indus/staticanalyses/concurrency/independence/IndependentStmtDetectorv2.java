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
	 *      soot.SootMethod)
	 */
	@Override protected boolean isIndependent(final Stmt stmt, final SootMethod method) {
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
						_result = !escapeInfo.fieldAccessShared(((InstanceFieldRef) _fieldRef).getBase(), method, _signature,
								IEscapeInfo.READ_WRITE_SHARED_ACCESS);
					} else {
						_result = !escapeInfo.staticfieldAccessShared(_field.getDeclaringClass(), method, _signature,
								IEscapeInfo.READ_WRITE_SHARED_ACCESS);
					}
				}
			} else if (stmt instanceof EnterMonitorStmt) {
				_result = !escapeInfo.lockUnlockShared(((EnterMonitorStmt) stmt).getOp(), method);
			} else if (stmt.containsInvokeExpr()) {
				final InvokeExpr _invokeExpr = stmt.getInvokeExpr();

				if (_invokeExpr instanceof VirtualInvokeExpr) {
					final VirtualInvokeExpr _vExpr = (VirtualInvokeExpr) _invokeExpr;
					final SootMethod _sm = _invokeExpr.getMethod();
					_result = Util.isNotifyMethod(_sm)
							|| (Util.isWaitMethod(_sm) && !escapeInfo.waitNotifyShared(_vExpr.getBase(), method));
				}
			}
		}
		return _result;
	}
}

// End of File
