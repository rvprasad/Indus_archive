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
