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

package edu.ksu.cis.indus.tools.slicer.criteria.predicates;

import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;

import soot.SootMethod;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;

/**
 * This class allows only criteria based on synchronization statements involving escaping lock objects. If escape information
 * is not available, then it's verdict is to generate the criteria from the given entity.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class EscapingSliceCriteriaPredicate
		extends AbstractSliceCriteriaPredicate<Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> {

	/**
	 * {@inheritDoc}
	 */
	public <E1 extends Triple<EnterMonitorStmt, ExitMonitorStmt, SootMethod>> boolean evaluate(final E1 monitorTriple) {
		final boolean _result;

		final IEscapeInfo _escapes = getSlicerTool().getEscapeInfo();

		if (_escapes != null) {
			final EnterMonitorStmt _enterMonitor = monitorTriple.getFirst();
			final SootMethod _method = monitorTriple.getThird();

			if (_enterMonitor == null) {
				_result = _method.isStatic() || _escapes.thisEscapes(_method);
			} else {
				_result = _escapes.escapes(_enterMonitor.getOp(), _method);
			}
		} else {
			_result = true;
		}

		return _result;
	}
}

// End of File
