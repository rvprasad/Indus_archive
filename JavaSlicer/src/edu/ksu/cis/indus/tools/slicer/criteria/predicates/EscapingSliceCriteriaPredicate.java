
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

import edu.ksu.cis.indus.staticanalyses.concurrency.ConcurrencyHelper;
import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;


/**
 * This class allows only criteria based on synchronization statements involving escaping lock objects.  If escape
 * information is not available, then it's verdict is to generate the criteria from the given entity.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class EscapingSliceCriteriaPredicate
  extends AbstractSliceCriteriaPredicate {
	/**
	 * @see org.apache.commons.collections.Predicate#evaluate(Object)
	 */
	public boolean evaluate(final Object entity) {
		final boolean _result;

		if (ConcurrencyHelper.isMonitorTriple(entity)) {
			final Triple _monitorTriple = (Triple) entity;
			final EquivalenceClassBasedEscapeAnalysis _ecba = getSlicerTool().getECBA();

			if (_ecba != null) {
				final EnterMonitorStmt _enterMonitor = (EnterMonitorStmt) _monitorTriple.getFirst();
				final SootMethod _method = (SootMethod) _monitorTriple.getThird();

				if (_enterMonitor == null) {
					_result = _method.isStatic() || _ecba.thisEscapes(_method);
				} else {
					_result = _ecba.escapes(_enterMonitor.getOp(), _method);
				}
			} else {
				_result = true;
			}
		} else {
			_result = false;
		}

		return _result;
	}
}

// End of File
