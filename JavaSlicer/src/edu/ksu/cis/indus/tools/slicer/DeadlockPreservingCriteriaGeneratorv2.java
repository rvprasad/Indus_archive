
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.common.datastructures.Triple;

import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;

import soot.SootMethod;

import soot.jimple.EnterMonitorStmt;


/**
 * This class generates seed slicing criteria to preserve the deadlocking property of the system being sliced by only
 * considering the synchronization statements on escaping objects.  If escape information is not available, then it's
 * behavior is identical to it's superclass.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DeadlockPreservingCriteriaGeneratorv2
  extends DeadlockPreservingCriteriaGenerator {
	/**
	 * @see DeadlockPreservingCriteriaGenerator#shouldGenerateCriteriaFrom(Triple, SlicerTool)
	 */
	protected boolean shouldGenerateCriteriaFrom(final Triple monitorTriple, final SlicerTool slicer) {
		boolean _result = true;
		final EquivalenceClassBasedEscapeAnalysis _ecba = slicer.getECBA();

		if (_ecba != null) {
			final EnterMonitorStmt _enterMonitor = (EnterMonitorStmt) monitorTriple.getFirst();
			final SootMethod _method = (SootMethod) monitorTriple.getThird();

			if (_enterMonitor == null) {
				_result = _method.isStatic() || _ecba.thisEscapes(_method);
			} else {
				_result = _ecba.escapes(_enterMonitor.getOp(), _method);
			}
		}
		return _result;
	}
}

// End of File
