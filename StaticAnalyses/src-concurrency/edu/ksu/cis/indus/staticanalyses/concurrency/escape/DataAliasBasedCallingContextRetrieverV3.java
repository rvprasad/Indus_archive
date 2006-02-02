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

import edu.ksu.cis.indus.common.collections.CollectionUtils;

import java.util.Collection;

import soot.SootMethod;
import soot.Value;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;

/**
 * This implementation provides program-point-relative intra-thread calling contexts based on equivalence-class based
 * information and context coupling.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
class DataAliasBasedCallingContextRetrieverV3
		extends DataAliasBasedCallingContextRetrieverV2 {

	/**
	 * Creates an instance of this instance.
	 * 
	 * @param callingContextLengthLimit <i>refer to the constructor of the super class</i>.
	 */
	public DataAliasBasedCallingContextRetrieverV3(final int callingContextLengthLimit) {
		super(callingContextLengthLimit);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param callerSideEntities DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	@Override protected boolean shouldConsiderCallerSideToken(final Collection<Object> callerSideEntities) {
		final DefinitionStmt _stmt = (DefinitionStmt) getInfoFor(Identifiers.SRC_ENTITY);
		final Value _value;

		if (_stmt.containsArrayRef()) {
			_value = _stmt.getArrayRef().getBase();
		} else {
			final FieldRef _fr = _stmt.getFieldRef();
			if (_fr instanceof StaticFieldRef) {
				_value = _fr;
			} else {
				_value = ((InstanceFieldRef) _fr).getBase();
			}
		}

		boolean _result = true;
		final AliasSet _as = ecba.queryAliasSetFor(_value, (SootMethod) getInfoFor(Identifiers.SRC_METHOD));
		if (_as != null) {
			final Collection<?> _o = _as.getIntraProcRefEntities();
			if (_o != null) {
				_result = CollectionUtils.containsAny(callerSideEntities, _o);
			}
		}
		return _result;
	}
}

// End of File
