
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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import edu.ksu.cis.indus.processing.Context;

import soot.SootMethod;
import soot.Value;

import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Stmt;


/**
 * This implementation provides program-point-relative calling context based on read/write access-based sharing as opposed
 * to mere read/read sharing between threads.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ThreadEscapeInfoBasedCallingContextRetrieverV2
  extends ThreadEscapeInfoBasedCallingContextRetriever {
	/**
	 * Creates an instance of this class.
	 */
	public ThreadEscapeInfoBasedCallingContextRetrieverV2() {
	}

	/**
	 * @see ThreadEscapeInfoBasedCallingContextRetriever#considerProgramPoint(Context)
	 */
	protected boolean considerProgramPoint(final Context context) {
		boolean _result = super.considerProgramPoint(context);
		final Object _entity = getInfoFor(SRC_ENTITY);

		if (_result && _entity instanceof Stmt) {
			final Stmt _stmt = (Stmt) _entity;

			if (_stmt != null) {
				Value _val = null;

				if (_stmt.containsArrayRef()) {
					_val = _stmt.getArrayRef().getBase();
				} else if (_stmt.containsFieldRef()) {
					final FieldRef _fieldRef = _stmt.getFieldRef();

					if (_fieldRef instanceof InstanceFieldRef) {
						_val = ((InstanceFieldRef) _fieldRef).getBase();
					}
				}

				if (_val != null) {
					_result =
						getECBA().shared(context.getProgramPoint().getValue(), context.getCurrentMethod(), _val,
							(SootMethod) getInfoFor(SRC_METHOD));
				}
			}
		}

		return _result;
	}
}

// End of File
