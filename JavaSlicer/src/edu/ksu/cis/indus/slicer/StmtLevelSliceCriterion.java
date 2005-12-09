
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

import soot.jimple.Stmt;


/**
 * This class represents statement-level slice criterion.  This class supports object pooling.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class StmtLevelSliceCriterion
  extends AbstractSliceCriterion {
	/** 
	 * The statement associated with this criterion.
	 */
	protected Stmt stmt;

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override public boolean equals(final Object other) {
		if (this == other) {
			return true;
		}

		if (!(other instanceof StmtLevelSliceCriterion)) {
			return false;
		}

		final StmtLevelSliceCriterion _rhs = (StmtLevelSliceCriterion) other;
		return new EqualsBuilder().appendSuper(super.equals(other)).append(stmt, _rhs.stmt).isEquals();
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override public int hashCode() {
		return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(stmt).toHashCode();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		return new ToStringBuilder(this, CustomToStringStyle.HASHCODE_AT_END_STYLE).appendSuper(super.toString())
																					 .append("stmt", this.stmt).toString();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @return the statement(<code>Stmt</code>) associated with this criterion.
	 *
	 * @post result != null and result.oclIsKindOf(jimple.Stmt)
	 *
	 * @see AbstractSliceCriterion#getCriterion()
	 */
	@Override protected Object getCriterion() {
		return stmt;
	}
	/**
	 * Initializes this object.
	 *
	 * @param occurringMethod in which the slice criterion occurs.
	 * @param criterion is the slice criterion.
	 *
	 * @pre method != null and stmt != null
	 */
	void initialize(final SootMethod occurringMethod, final Stmt criterion) {
		super.initialize(occurringMethod);
		this.stmt = criterion;
	}
}

// End of File
