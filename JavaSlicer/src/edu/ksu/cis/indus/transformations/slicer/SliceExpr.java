
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.transformations.slicer;

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.SoftReferenceObjectPool;


/**
 * This class represents an expression as a slice criterion.  This class supports object pooling.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class SliceExpr
  extends SliceStmt {
	/**
	 * A pool of <code>SliceExpr</code> criterion objects.
	 *
	 * @invariant EXPr_POOL.borrowObject().oclIsKindOf(SliceExpr)
	 */
	static final ObjectPool EXPR_POOL =
		new SoftReferenceObjectPool(new BasePoolableObjectFactory() {
				/**
				 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
				 */
				public Object makeObject() {
					SliceExpr result = new SliceExpr();
					result.pool = EXPR_POOL;
					return result;
				}
			});

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SliceExpr.class);

	/**
	 * The expression associated with this criterion.
	 */
	protected ValueBox expr;

	/**
	 * {@inheritDoc}
	 *
	 * @return the expression(<code>ValueBox</code>) associated with criterion.
	 *
	 * @post result != null and result.oclIsKindOf(ValueBox)
	 *
	 * @see edu.ksu.cis.bandera.slicer.AbstractSliceCriterion#getCriterion()
	 */
	public Object getCriterion() {
		return expr;
	}

	/**
	 * Provides the statement in which the slice expression occurs.
	 *
	 * @return the statement in which the slice expression occurs.
	 *
	 * @post result != null
	 */
	public Stmt getOccurringStmt() {
		return stmt;
	}

	/**
	 * Checks if the given object is "equal" to this object.
	 *
	 * @param o is the object to be compared.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this object; <code>false</code>, otherwise.
	 */
	public boolean equals(final Object o) {
		boolean result = false;

		if (o != null && o instanceof SliceExpr) {
			SliceExpr temp = (SliceExpr) o;
			result = temp.expr.equals(expr) && super.equals(temp);
		}
		return result;
	}

	/**
	 * Returns the hashcode for this object.
	 *
	 * @return the hashcode for this object.
	 */
	public int hashCode() {
		int result = 17;
		result = 37 * result + expr.hashCode();
		result = 37 * result + super.hashCode();
		return result;
	}

	/**
	 * Initializes this object.
	 *
	 * @param occurringMethod in which the criterion containing statement occurs.
	 * @param occurringStmt in which the criterion containing expression occurs.
	 * @param criterion is the slicing criterion.
	 * @param shouldBeIncluded <code>true</code> if the slice criterion should be included in the slice; <code>false</code>,
	 * 		  otherwise.
	 *
	 * @pre expr != null and stmt != null and method != null
	 */
	protected void initialize(final SootMethod occurringMethod, final Stmt occurringStmt, final ValueBox criterion,
		final boolean shouldBeIncluded) {
		super.initialize(occurringMethod, occurringStmt, shouldBeIncluded);
		this.expr = criterion;
	}

	/**
	 * Retrieves an expression-level slicing criterion object.
	 *
	 * @return an expression-level slicing criterion object.
	 *
	 * @throws RuntimeException if an object could not be retrieved from the pool.
	 *
	 * @post result != null
	 */
	static SliceExpr getSliceExpr() {
		SliceExpr result;

		try {
			result = (SliceExpr) EXPR_POOL.borrowObject();
		} catch (Exception e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("How can this happen?", e);
			}
			throw new RuntimeException(e);
		}
		return result;
	}
}

/*
   ChangeLog:
   
   $Log$
   
   Revision 1.5  2003/08/18 05:01:45  venku
   Committing package name change in source after they were moved.
   
   Revision 1.4  2003/08/17 11:56:18  venku
   Renamed SliceCriterion to AbstractSliceCriterion.
   Formatting, documentation, and specification.
   
   Revision 1.3  2003/05/22 22:23:50  venku
   Changed interface names to start with a "I".
   Formatting.
 */
