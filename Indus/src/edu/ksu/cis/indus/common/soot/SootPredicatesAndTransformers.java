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

package edu.ksu.cis.indus.common.soot;

import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.PredicateUtils;

import soot.Local;
import soot.Value;
import soot.ValueBox;

import soot.jimple.ArrayRef;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.ThisRef;

/**
 * This class contains <i>jakarta commons collections</i> related predicates and transformers that are specific to Soot AST
 * types.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SootPredicatesAndTransformers {

	/**
	 * A predicate used to filter <code>EnterMonitorStmt</code>.
	 */
	public static final Predicate ENTER_MONITOR_STMT_PREDICATE = PredicateUtils.instanceofPredicate(EnterMonitorStmt.class);

	/**
	 * This filter is used to identify AST chunks that may represent references that can escape.
	 */
	public static final Predicate ESCAPABLE_EXPR_FILTER = new Predicate() {

		public boolean evaluate(final Object object) {
			final Value _v = ((ValueBox) object).getValue();
			return _v instanceof StaticFieldRef || _v instanceof InstanceFieldRef || _v instanceof ArrayRef
					|| _v instanceof Local || _v instanceof ThisRef || _v instanceof ParameterRef;
		}
	};

	/**
	 * A predicate used to filter statements with invoke expressions. Filter expression is
	 * <code>((Stmt)o).containsInvokeExpr()</code>.
	 */
	public static final Predicate INVOKING_STMT_PREDICATE = new Predicate() {

		public boolean evaluate(final Object object) {
			return ((Stmt) object).containsInvokeExpr();
		}
	};

	/**
	 * A predicate used to filter <code>EnterMonitorStmt</code>.
	 */
	public static final Predicate NEW_EXPR_PREDICATE = PredicateUtils.instanceofPredicate(NewExpr.class);

	/**
	 * This predicate filters out <code>NullConstant</code> values.
	 */
	public static final Predicate NULL_PREDICATE = new Predicate() {

		public boolean evaluate(final Object object) {
			return object instanceof NullConstant;
		}
	};

	/**
	 * Creates an instance of this class.
	 */
	public SootPredicatesAndTransformers() {
		super();
	}
}

// End of File
