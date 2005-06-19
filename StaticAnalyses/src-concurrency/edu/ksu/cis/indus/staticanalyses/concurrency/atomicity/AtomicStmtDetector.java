
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
import edu.ksu.cis.indus.common.soot.SootPredicatesAndTransformers;

import edu.ksu.cis.indus.interfaces.IAtomicityInfo;
import edu.ksu.cis.indus.interfaces.IEscapeInfo;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.commons.collections.IteratorUtils;

import soot.SootClass;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * This class provides atomicity detection that is based on escape analysis information.  Atomicity is the property that
 * ensures the execution of a statement will only affect the state of the thread that executes it and not other threads.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class AtomicStmtDetector
  extends AbstractProcessor
  implements IAtomicityInfo {
	/** 
	 * A collection of names of immutable classes. 
	 */
	private static final Collection IMMUTABLE_CLASS_NAMES;

	static {
		IMMUTABLE_CLASS_NAMES = new ArrayList();
		IMMUTABLE_CLASS_NAMES.add("java.lang.String");
		IMMUTABLE_CLASS_NAMES.add("java.lang.Integer");
		IMMUTABLE_CLASS_NAMES.add("java.lang.Float");
		IMMUTABLE_CLASS_NAMES.add("java.lang.Double");
		IMMUTABLE_CLASS_NAMES.add("java.lang.Boolean");
		IMMUTABLE_CLASS_NAMES.add("java.lang.Byte");
		IMMUTABLE_CLASS_NAMES.add("java.lang.Long");
		IMMUTABLE_CLASS_NAMES.add("java.lang.Short");
		IMMUTABLE_CLASS_NAMES.add("java.lang.Character");
	}

	/** 
	 * The collection of atomic statements.
	 *
	 * @invariant atomicStmts.oclIsKindOf(Collection(Stmt))
	 */
	protected final Collection atomicStmts = new ArrayList();

	/** 
	 * The escape analysis to use.
	 */
	protected IEscapeInfo escapeInfo;

	/** 
	 * This indicates if the currently being processed scope is atomic.  
	 */
	private boolean atomicScope;

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
	public final void setEscapeAnalysis(final IEscapeInfo analysis) {
		escapeInfo = analysis;
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IIdentification#getIds()
	 */
	public final Collection getIds() {
		return Collections.singleton(ID);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.jimple.Stmt, edu.ksu.cis.indus.processing.Context)
	 */
	public final void callback(final Stmt stmt, final Context context) {
		if (atomicScope || (!stmt.containsArrayRef() && !stmt.containsFieldRef() && !stmt.containsInvokeExpr())
			  || isAtomic(stmt, context.getCurrentMethod())) {
			atomicStmts.add(stmt);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessor#callback(soot.SootClass)
	 */
	public final void callback(final SootClass clazz) {
		super.callback(clazz);
		atomicScope = IMMUTABLE_CLASS_NAMES.contains(clazz.getName());
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public final void hookup(final ProcessingController ppc) {
        ppc.register(this);
		ppc.registerForAllStmts(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#reset()
	 */
	public final void reset() {
		super.reset();
		atomicStmts.clear();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public final String toString() {
		return CollectionsUtilities.prettyPrint(atomicStmts);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public final void unhook(final ProcessingController ppc) {
        ppc.unregister(this);
		ppc.unregisterForAllStmts(this);
	}

	/**
	 * Checks if the given statement is atomic based on escape information.
	 *
	 * @param stmt to be tested.
	 * @param method in which the statement occurs.
	 *
	 * @return <code>true</code> if the statement is atomic; <code>false</code>, otherwise.
	 *
	 * @pre stmt != null and method != null
	 */
	protected boolean isAtomic(final Stmt stmt, final SootMethod method) {
		boolean atomic = !stmt.containsInvokeExpr();

		for (final Iterator _i =
				IteratorUtils.filteredIterator(stmt.getUseAndDefBoxes().iterator(),
					SootPredicatesAndTransformers.ESCAPABLE_EXPR_FILTER); _i.hasNext() && atomic;) {
			final ValueBox _vb = (ValueBox) _i.next();
			atomic = !escapeInfo.escapes(_vb.getValue(), method);
		}
		return atomic;
	}
}

// End of File
