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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.datastructures.Pair;

import edu.ksu.cis.indus.interfaces.IEscapeInfo;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

/**
 * This class uses symbolic- and escape-analysis information as calculated by {@link
 * edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis
 * EquivalenceClassBasedEscapeAnalysis} to prune the interference dependence edges as calculated by it's parent class. This
 * can be further spruced by symbolic-analysis.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @see InterferenceDAv1
 */
public class InterferenceDAv3
		extends InterferenceDAv2 {

	/**
	 * Creates an instance of this class.
	 * 
	 */
	public InterferenceDAv3() {
		super();
	}

	/**
	 * @see InterferenceDAv1#isArrayDependentOn(Pair, Pair, ArrayRef, ArrayRef)
	 */
	@Override protected boolean isArrayDependentOn(final Pair<Stmt, SootMethod> dependent,
			final Pair<Stmt, SootMethod> dependee, final ArrayRef dependentArrayRef, final ArrayRef dependeeArrayRef) {
		boolean _result = super.isArrayDependentOn(dependent, dependee, dependentArrayRef, dependeeArrayRef);

		if (_result) {
			final SootMethod _deMethod = dependee.getSecond();
			final SootMethod _dtMethod = dependent.getSecond();
			final Value _de = dependeeArrayRef.getBase();
			final Value _dt = dependentArrayRef.getBase();
			_result = ecba.fieldAccessShared(_de, _deMethod, _dt, _dtMethod, IEscapeInfo.READ_WRITE_SHARED_ACCESS);
		}
		return _result;
	}

	/**
	 * @see InterferenceDAv1#isInstanceFieldDependentOn(Pair, Pair, InstanceFieldRef, InstanceFieldRef)
	 */
	@Override protected boolean isInstanceFieldDependentOn(final Pair<Stmt, SootMethod> dependent,
			final Pair<Stmt, SootMethod> dependee, final InstanceFieldRef dependentFieldRef,
			final InstanceFieldRef dependeeFieldRef) {
		boolean _result = super.isInstanceFieldDependentOn(dependent, dependee, dependentFieldRef, dependeeFieldRef);

		if (_result) {
			final SootMethod _deMethod = dependee.getSecond();
			final SootMethod _dtMethod = dependent.getSecond();
			final Value _de = dependeeFieldRef.getBase();
			final Value _dt = dependentFieldRef.getBase();
			_result = ecba.fieldAccessShared(_de, _deMethod, _dt, _dtMethod, IEscapeInfo.READ_WRITE_SHARED_ACCESS);
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.InterferenceDAv2#isStaticFieldDependentOn(Pair,
	 *      edu.ksu.cis.indus.common.datastructures.Pair, soot.jimple.StaticFieldRef, soot.jimple.StaticFieldRef)
	 */
	@Override protected boolean isStaticFieldDependentOn(final Pair<Stmt, SootMethod> dependent,
			final Pair<Stmt, SootMethod> dependee, final StaticFieldRef dependentFieldRef,
			final StaticFieldRef dependeeFieldRef) {
		boolean _result = super.isStaticFieldDependentOn(dependent, dependee, dependentFieldRef, dependeeFieldRef);

		if (_result) {
			final SootField _field = dependeeFieldRef.getField();
			_result = ecba.staticfieldAccessShared(_field.getDeclaringClass(), dependee.getSecond(), _field.getSignature(),
					IEscapeInfo.READ_WRITE_SHARED_ACCESS);
		}
		return _result;
	}

	/**
	 * Extracts information provided by the environment via <code>info</code> parameter to {@link #initialize(java.util.Map)
	 * initialize}.
	 * 
	 * @throws InitializationException when and instance of equivalence class based escape analysis is not provided.
	 * @pre info.get(IEscapeInfo.ID) != null
	 * @see InterferenceDAv1#setup()
	 */
	@Override protected void setup() throws InitializationException {
		super.setup();

		ecba = (IEscapeInfo) info.get(IEscapeInfo.ID);

		if (pairMgr == null) {
			throw new InitializationException(IEscapeInfo.ID + " was not provided in info.");
		}
	}
}

// End of File
