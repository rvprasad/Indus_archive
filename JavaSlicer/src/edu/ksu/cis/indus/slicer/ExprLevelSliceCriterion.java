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

package edu.ksu.cis.indus.slicer;

import edu.ksu.cis.indus.common.CustomToStringStyle;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;

/**
 * This class represents expression-level slice criterion. This class supports object pooling.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class ExprLevelSliceCriterion
		extends AbstractSliceCriterion {

	/**
	 * The statement associated with this criterion.
	 */
	protected Stmt stmt;

	/**
	 * The expression associated with this criterion.
	 */
	protected ValueBox expr;

	/**
	 * @see java.lang.Object#equals(Object)
	 */
	@Override public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}

		if (!(object instanceof ExprLevelSliceCriterion)) {
			return false;
		}

		final ExprLevelSliceCriterion _rhs = (ExprLevelSliceCriterion) object;
		return new EqualsBuilder().appendSuper(super.equals(object)).append(this.expr, _rhs.expr)
				.append(this.stmt, _rhs.stmt).isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override public int hashCode() {
		return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(stmt).append(expr).toHashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		return new ToStringBuilder(this, CustomToStringStyle.HASHCODE_AT_END_STYLE).appendSuper(super.toString()).append(
				"stmt", this.stmt).append("expr", this.expr).toString();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return the expression(<code>ValueBox</code>) associated with criterion.
	 * @post result != null and result.oclIsKindOf(ValueBox)
	 * @see AbstractSliceCriterion#getCriterion()
	 */
	@Override protected Object getCriterion() {
		return expr;
	}

	/**
	 * Provides the statement in which the slice expression occurs.
	 *
	 * @return the statement in which the slice expression occurs.
	 * @post result != null
	 */
	protected final Stmt getOccurringStmt() {
		return stmt;
	}

	/**
	 * Initializes this object.
	 *
	 * @param occurringMethod in which the criterion containing statement occurs.
	 * @param occurringStmt in which the criterion containing expression occurs.
	 * @param criterion is the slicing criterion.
	 * @pre expr != null and stmt != null and method != null
	 */
	void initialize(final SootMethod occurringMethod, final Stmt occurringStmt, final ValueBox criterion) {
		super.initialize(occurringMethod);
		this.stmt = occurringStmt;
		this.expr = criterion;
	}
}

// End of File
