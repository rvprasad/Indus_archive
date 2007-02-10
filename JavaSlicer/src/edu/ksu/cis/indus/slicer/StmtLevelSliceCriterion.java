
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
