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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import edu.ksu.cis.indus.common.collections.CollectionUtils;

import java.util.Collection;

import soot.SootMethod;
import soot.Value;
import soot.jimple.DefinitionStmt;

/**
 * This implementation provides program-point-relative intra-thread calling contexts based on equivalence-class based
 * information and context coupling.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DataAliasBasedCallingContextRetrieverV3
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
	 * {@inheritDoc}
	 */
	@Override protected boolean shouldConsiderCallerSideToken(final Collection<Object> callerSideEntities) {
		final DefinitionStmt _stmt = (DefinitionStmt) getInfoFor(Identifiers.SRC_ENTITY);
		final Value _value;

		if (_stmt.containsArrayRef()) {
			_value = _stmt.getArrayRef();
		} else {
			_value = _stmt.getFieldRef();
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
