
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.interfaces.IAtomicityInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.concurrency.escape.EquivalenceClassBasedEscapeAnalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.collections.Predicate;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.ParameterRef;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;


/**
 * This class provides atomicity detection that is based on escape analysis information.  Atomicity is the property that
 * ensures the execution of a statement will only affect the state of the thread that executes it and not other threads.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class AtomicStmtDetector
  extends AbstractProcessor implements IAtomicityInfo {
	/** 
	 * This filter is used to identify AST chunks that may represent references that can escape.
	 */
	private static final Predicate FILTER =
		new Predicate() {
			public boolean evaluate(final Object object) {
				final Value _v = ((ValueBox) object).getValue();
				return _v instanceof StaticFieldRef || _v instanceof InstanceFieldRef || _v instanceof ArrayRef
				  || _v instanceof Local || _v instanceof ThisRef || _v instanceof ParameterRef;
			}
		};

	/** 
	 * The collection of atomic statements.
	 *
	 * @invariant atomicStmts.oclIsKindOf(Collection(Stmt))
	 */
	protected final Collection atomicStmts = new ArrayList();

	/** 
	 * The escape analysis to use.
	 */
	protected EquivalenceClassBasedEscapeAnalysis ecba;

	/**
	 * Checks if the statement is atomic.
	 *
	 * @param stmt to be checked.
	 *
	 * @return <code>true</code> if <code>stmt</code> is atomic; <code>false</code>, otherwise.
	 *
	 * @pre stmt != null
	 */
	public final boolean isAtomic(final Stmt stmt) {
		return atomicStmts.contains(stmt);
	}

	/**
	 * Sets the escape analysis to use.
	 *
	 * @param analysis to use.
	 *
	 * @pre analysis != null
	 */
	public final void setEscapeAnalysis(final EquivalenceClassBasedEscapeAnalysis analysis) {
		ecba = analysis;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		boolean _escapes = false;

		if (!(stmt instanceof ReturnVoidStmt
			  || stmt instanceof ReturnStmt
			  || stmt instanceof ThrowStmt
			  || stmt instanceof GotoStmt
			  || stmt instanceof RetStmt
			  || stmt instanceof IdentityStmt)) {
			final SootMethod _currentMethod = context.getCurrentMethod();

			for (final Iterator _i = IteratorUtils.filteredIterator(stmt.getUseAndDefBoxes().iterator(), FILTER);
				  _i.hasNext() && !_escapes;) {
				final ValueBox _vb = (ValueBox) _i.next();
				_escapes = ecba.escapes(_vb.getValue(), _currentMethod);
			}
		}

		if (!_escapes) {
			atomicStmts.add(stmt);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.registerForAllStmts(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#reset()
	 */
	public void reset() {
		super.reset();
		atomicStmts.clear();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return CollectionsUtilities.prettyPrint(atomicStmts);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregisterForAllStmts(this);
	}
}

// End of File
