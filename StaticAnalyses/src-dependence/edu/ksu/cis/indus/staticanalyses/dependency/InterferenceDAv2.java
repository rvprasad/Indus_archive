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

import soot.SootMethod;
import soot.Value;

import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

/**
 * This class uses escape-analysis information as calculated by {@link
 * edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis
 * EquivalenceClassBasedEscapeAnalysis} to prune the interference dependence edges as calculated by it's parent class. This
 * can be further spruced by symbolic-analysis.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @see InterferenceDAv1
 */
public class InterferenceDAv2
		extends InterferenceDAv1 {

	/**
	 * This provide information shared access in the analyzed system. This is required by the analysis.
	 */
	protected IEscapeInfo ecba;

	/**
	 * Creates an instance of this class.
	 * 
	 */
	public InterferenceDAv2() {
		super();
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#analyze()
	 */
	@Override public void analyze() {
		unstable();

		if (ecba.isStable()) {
			super.analyze();
		}
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
			_result = ecba.escapes(_de, _deMethod) && ecba.escapes(_dt, _dtMethod);
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
			_result = ecba.escapes(_de, _deMethod) && ecba.escapes(_dt, _dtMethod);
		}
		return _result;
	}

	/**
	 * @see InterferenceDAv1#isStaticFieldDependentOn(Pair, Pair, StaticFieldRef, StaticFieldRef)
	 */
	@Override protected boolean isStaticFieldDependentOn(final Pair<Stmt, SootMethod> dependent,
			final Pair<Stmt, SootMethod> dependee, final StaticFieldRef dependentFieldRef,
			final StaticFieldRef dependeeFieldRef) {
		final boolean _result = super.isStaticFieldDependentOn(dependent, dependee, dependentFieldRef, dependeeFieldRef);

		if (_result) {
			ecba.escapes(dependeeFieldRef.getField().getDeclaringClass(), dependee.getSecond());
		}
		return _result;
	}

	/**
	 * Extracts information provided by the environment via <code>info</code> parameter to {@link #initialize(java.util.Map)
	 * initialize}.
	 * 
	 * @throws InitializationException when and instance of pair managing service or interference analysis is not provided.
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
