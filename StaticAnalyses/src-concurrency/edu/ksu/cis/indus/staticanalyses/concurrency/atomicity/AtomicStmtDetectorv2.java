
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

package edu.ksu.cis.indus.staticanalyses.concurrency.atomicity;

import edu.ksu.cis.indus.common.MembershipPredicate;
import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.Triple;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.ParameterRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;


/**
 * This is a specialized version of <code>AtomicStmtDetector</code> that uses sharing information calculated by
 * <code>EquivalenceClassBasedEscapeAnalysis</code> to detect atomic statements.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class AtomicStmtDetectorv2
  extends AtomicStmtDetector {
	/** 
	 * This maps statements to a collection of triples of value in the statement, the statement, and the occuring method.
	 *
	 * @invariant stmtMap.oclIsKindOf(Map(Stmt, Collection(Triple(Value, Stmt, SootMethod))))
	 */
	private final Map stmtMap = new HashMap();

	/** 
	 * This maps types to a collection of triples of value in the statement, the statement, and the occuring method.
	 *
	 * @invariant typeMap.oclIsKindOf(Map(Type, Collection(Triple(Value, Stmt, SootMethod))))
	 */
	private final Map typeMap = new HashMap();

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
		super.callback(vBox, context);

		final Stmt _stmt = context.getStmt();

		if (!isAtomic(_stmt)) {
			final Value _v = vBox.getValue();
			final SootMethod _currentMethod = context.getCurrentMethod();
			final Triple _triple = new Triple(_v, _stmt, _currentMethod);
			CollectionsUtilities.putIntoSetInMap(typeMap, _v.getType(), _triple);
			CollectionsUtilities.putIntoSetInMap(stmtMap, _stmt, _triple);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public void consolidate() {
		super.consolidate();

		final Collection _discards2 = new HashSet();
		final Predicate _filter2 = new MembershipPredicate(false, _discards2);
		final Set _types = typeMap.keySet();
		final Iterator _i = _types.iterator();
		final int _iEnd = _types.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Object _type = _i.next();
			final Collection _triples = (Collection) typeMap.get(_type);

			for (final Iterator _j = _triples.iterator(); _j.hasNext();) {
				final Triple _triple1 = (Triple) _j.next();
				boolean _threadLocal = true;
				_discards2.add(_triple1);

				for (final Iterator _k = IteratorUtils.filteredIterator(_triples.iterator(), _filter2); _k.hasNext();) {
					final Triple _triple2 = (Triple) _k.next();
					final boolean _shared =
						ecba.shared((Value) _triple1.getFirst(), (SootMethod) _triple1.getThird(),
							(Value) _triple2.getFirst(), (SootMethod) _triple2.getThird());
					_threadLocal &= !_shared;

					if (_shared) {
						stmtMap.remove(_triple2.getSecond());
					}
				}

				if (!_threadLocal) {
					stmtMap.remove(_triple1.getSecond());
				}
			}
		}
		atomicStmts.addAll(stmtMap.keySet());

		typeMap.clear();
		stmtMap.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		super.hookup(ppc);
		ppc.register(StaticFieldRef.class, this);
		ppc.register(InstanceFieldRef.class, this);
		ppc.register(ArrayRef.class, this);
		ppc.register(Local.class, this);
		ppc.register(ParameterRef.class, this);
		ppc.register(CaughtExceptionRef.class, this);
		ppc.register(ThisRef.class, this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		super.unhook(ppc);
		ppc.unregister(InstanceFieldRef.class, this);
		ppc.unregister(ArrayRef.class, this);
		ppc.unregister(Local.class, this);
		ppc.unregister(ParameterRef.class, this);
		ppc.unregister(CaughtExceptionRef.class, this);
		ppc.unregister(ThisRef.class, this);
	}
}

// End of File
