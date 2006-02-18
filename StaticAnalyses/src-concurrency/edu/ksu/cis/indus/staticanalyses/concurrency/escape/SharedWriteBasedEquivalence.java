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

import edu.ksu.cis.indus.common.collections.MapUtils;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;
import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;

/**
 * This class contains the logic to calculate equivalence classes of shared-write statements in the system. Two shared-write
 * statement belong to the same equivalence class if they may write to the same field/array cell.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SharedWriteBasedEquivalence
		extends AbstractProcessor {

	/**
	 * This is the collection of definition statements involving array reference or field reference.
	 * 
	 * @invariant defStmts->forall(o | o.containsArrayRef() or o.containsFieldRef())
	 */
	private final Collection<Pair<AssignStmt, SootMethod>> defStmts;

	/**
	 * This provides escape information.
	 */
	private final IEscapeInfo einfo;

	/**
	 * This maps a shared write statement to the collection of shared write statements that are in the same equivalence class
	 * as the key.
	 * 
	 * @invariant write2writes.keySet()->forall(o | o.getFirst().getLeftOp().oclIsKindOf(ArrayRef) or
	 *            o.getFirst().getLeftOp().oclIsKindOf(StaticFieldRef) or
	 *            o.getFirst().getLeftOp().oclIsKindOf(InstanceFieldRef))
	 */
	private final Map<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>> write2writes;

	/**
	 * Creates an instance of this class.
	 * 
	 * @param escapeInfo to be used.
	 */
	public SharedWriteBasedEquivalence(final IEscapeInfo escapeInfo) {
		einfo = escapeInfo;
		write2writes = new HashMap<Pair<AssignStmt, SootMethod>, Collection<Pair<AssignStmt, SootMethod>>>();
		defStmts = new HashSet<Pair<AssignStmt, SootMethod>>();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	@Override public void callback(final ValueBox vBox, final Context context) {
		final Stmt _stmt = context.getStmt();

		if (((AssignStmt) _stmt).getLeftOpBox() == vBox) {
			final SootMethod _currentMethod = context.getCurrentMethod();
			final Pair<AssignStmt, SootMethod> _pair = new Pair<AssignStmt, SootMethod>((AssignStmt) _stmt, _currentMethod);
			defStmts.add(_pair);
		}
	}

	/**
	 * Calculates write-write based dependence.
	 */
	@Override public void consolidate() {
		final Iterator<Pair<AssignStmt, SootMethod>> _i = defStmts.iterator();
		final int _iEnd = defStmts.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Pair<AssignStmt, SootMethod> _p1 = _i.next();
			final DefinitionStmt _s1 = _p1.getFirst();
			final SootMethod _m1 = _p1.getSecond();
			final Iterator<Pair<AssignStmt, SootMethod>> _j = defStmts.iterator();

			for (int _jIndex = 0; _jIndex < _iEnd; _jIndex++) {
				final Pair<AssignStmt, SootMethod> _p2 = _j.next();
				final DefinitionStmt _s2 = _p2.getFirst();
				final SootMethod _m2 = _p2.getSecond();

				if (writeWriteExecutionDependence(_s1, _m1, _s2, _m2)) {
					MapUtils.putIntoSetInMap(write2writes, _p1, _p2);
					MapUtils.putIntoSetInMap(write2writes, _p2, _p1);
				}
			}
		}

		defStmts.clear();
	}

	/**
	 * Retrieves the shared writes that belong to the same equivalence class as the given shared write.
	 * 
	 * @param pair of interest.
	 * @return a collection of lock acquisition.
	 * @pre pair.getFirst().getLeftOp().oclIsKindOf(InstanceFieldRef) or
	 *      pair.getFirst().getLeftOp().oclIsKindOf(StaticFieldRef) or pair.getFirst().getLeftOp().oclIsKindOf(ArrayRef)
	 * @post result != null
	 * @post result->forall(o |
	 */
	public Collection<Pair<AssignStmt, SootMethod>> getSharedWritesInEquivalenceClassOf(
			final Pair<AssignStmt, SootMethod> pair) {
		return Collections.unmodifiableCollection(MapUtils.queryCollection(write2writes, pair));
	}

	/**
	 * Retrieves the shared writes that belong to a non-singleton equivalence class.
	 * 
	 * @return a collection of lock acquisition.
	 * @post result.getFirst().oclIsKindOf(AssignStmt)
	 * @post result.getFirst().getLeftOp().oclIsKindOf(InstanceFieldRef) or
	 *       result.getFirst().getLeftOp().oclIsKindOf(StaticFieldRef) or result.getFirst().getLeftOp().oclIsKindOf(ArrayRef)
	 * @post result != null
	 */
	public Collection<Pair<AssignStmt, SootMethod>> getSharedWritesInNonSingletonEquivalenceClass() {
		return Collections.unmodifiableCollection(write2writes.keySet());
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(AssignStmt.class, this);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		return new ToStringBuilder(this).append("defStmts", defStmts).append("write2writes", write2writes).toString();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(AssignStmt.class, this);
	}

	/**
	 * Checks if the given definition statements are dependent based on the fact that they may update the same field on an
	 * object or the same cell of an array.
	 * 
	 * @param s1 is one statement of interest.
	 * @param m1 is the method containing <code>s1</code>.
	 * @param s2 is the second statement of interest.
	 * @param m2 is the method containing <code>s2</code>.
	 * @return <code>true</code> if both <code>s1</code> and <code>s2</code> contain either array/field ref in the
	 *         l-position; <code>false</code>, otherwise.
	 * @pre s1 != null and m1 != null and s2 != null and m2 != null
	 */
	private boolean writeWriteExecutionDependence(final DefinitionStmt s1, final SootMethod m1, final DefinitionStmt s2,
			final SootMethod m2) {
		boolean _result = false;

		if (s1.containsArrayRef() && s2.containsArrayRef()) {
			_result = einfo.fieldAccessShared(s1.getArrayRef().getBase(), m1, s2.getArrayRef().getBase(), m2,
					IEscapeInfo.WRITE_WRITE_SHARED_ACCESS);
		} else if (s1.containsFieldRef() && s2.containsFieldRef()) {
			final FieldRef _fieldRef2 = s2.getFieldRef();
			final FieldRef _fieldRef1 = s1.getFieldRef();
			final SootField _field1 = _fieldRef1.getField();

			if (_field1.equals(_fieldRef2.getField())) {
				if (_fieldRef1 instanceof InstanceFieldRef && _fieldRef2 instanceof InstanceFieldRef) {
					_result = einfo.fieldAccessShared(((InstanceFieldRef) _fieldRef1).getBase(), m1,
							((InstanceFieldRef) _fieldRef2).getBase(), m2, IEscapeInfo.WRITE_WRITE_SHARED_ACCESS);
				} else if (_fieldRef1 instanceof StaticFieldRef && _fieldRef2 instanceof StaticFieldRef) {
					_result = !EquivalenceClassBasedEscapeAnalysis.canHaveAliasSet(_field1.getType())
							|| einfo.fieldAccessShared(_fieldRef1, m1, _fieldRef2, m2, IEscapeInfo.WRITE_WRITE_SHARED_ACCESS);
				}
			}
		}
		return _result;
	}
}

// End of File
