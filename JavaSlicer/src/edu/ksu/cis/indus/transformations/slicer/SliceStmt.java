
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.transformations.slicer;

import soot.SootMethod;

import soot.jimple.Stmt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.SoftReferenceObjectPool;


/**
 * This class represents a statement as a slice criterion.  This class supports object pooling.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class SliceStmt
  extends AbstractSliceCriterion {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SliceStmt.class);

	/**
	 * A pool of <code>SliceStmt</code> criterion objects.
	 *
	 * @invariant STMT_POOL.borrowObject().oclIsKindOf(SliceStmt)
	 */
	static final ObjectPool STMT_POOL =
		new SoftReferenceObjectPool(new BasePoolableObjectFactory() {
				/**
				 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
				 */
				public Object makeObject() {
					SliceStmt result = new SliceStmt();
					result.pool = STMT_POOL;
					return result;
				}
			});

	/**
	 * The method in which <code>stmt</code> occurs.
	 */
	protected SootMethod method;

	/**
	 * The statement associated with this criterion.
	 */
	protected Stmt stmt;

	/**
	 * {@inheritDoc}
	 *
	 * @return the statement(<code>Stmt</code>) associated with this criterion.
	 *
	 * @post result != null and result.oclIsKindOf(jimple.Stmt)
	 *
	 * @see AbstractSliceCriterion#getCriterion()
	 */
	public Object getCriterion() {
		return stmt;
	}

	/**
	 * Provides the method in which criterion occurs.
	 *
	 * @return the method in which the slice statement occurs.
	 *
	 * @post result != null
	 */
	public SootMethod getOccurringMethod() {
		return method;
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

		if (o != null && o instanceof SliceStmt) {
			SliceStmt temp = (SliceStmt) o;
			result = temp.stmt.equals(stmt);

			if (result) {
				result = temp.method.equals(method) && super.equals(temp);
			}
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
		result = 37 * result + stmt.hashCode();
		result = 37 * result + method.hashCode();
		result = 37 * result + super.hashCode();
		return result;
	}

	/**
	 * Initializes this object.
	 *
	 * @param occurringMethod in which the slice criterion occurs.
	 * @param criterion is the slice criterion.
	 * @param shouldInclude <code>true</code> if the slice criterion should be included in the slice; <code>false</code>,
	 * 		  otherwise.
	 *
	 * @pre method != null and stmt != null
	 */
	protected void initialize(final SootMethod occurringMethod, final Stmt criterion, final boolean shouldInclude) {
		super.initialize(shouldInclude);
		this.method = occurringMethod;
		this.stmt = criterion;
	}

	/**
	 * Retrieves a statement-level slicing criterion object.
	 *
	 * @return a statement-level slicing criterion object.
	 *
	 * @throws RuntimeException if an object could not be retrieved from the pool.
	 *
	 * @post result != null
	 */
	static SliceStmt getSliceStmt() {
		SliceStmt result;

		try {
			result = (SliceStmt) STMT_POOL.borrowObject();
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
   Revision 1.7  2003/08/20 18:31:22  venku
   Documentation errors fixed.
   Revision 1.6  2003/08/18 12:14:13  venku
   Well, to start with the slicer implementation is complete.
   Although not necessarily bug free, hoping to stabilize it quickly.

   Revision 1.5  2003/08/18 05:01:45  venku
   Committing package name change in source after they were moved.

   Revision 1.4  2003/08/17 11:56:18  venku
   Renamed SliceCriterion to AbstractSliceCriterion.
   Formatting, documentation, and specification.

   Revision 1.3  2003/05/22 22:23:49  venku
   Changed interface names to start with a "I".
   Formatting.
 */
