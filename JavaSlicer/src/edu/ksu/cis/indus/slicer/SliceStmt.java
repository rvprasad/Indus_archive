
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

package edu.ksu.cis.indus.slicer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;

import org.apache.commons.pool.impl.SoftReferenceObjectPool;

import soot.SootMethod;

import soot.jimple.Stmt;


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
	 * A pool of <code>SliceStmt</code> criterion objects.
	 *
	 * @invariant STMT_POOL.borrowObject().oclIsKindOf(SliceStmt)
	 */
	static final ObjectPool STMT_POOL =
		new SoftReferenceObjectPool(new BasePoolableObjectFactory() {
				/**
				 * @see org.apache.commons.pool.PoolableObjectFactory#makeObject()
				 */
				public final Object makeObject() {
					final SliceStmt _result = new SliceStmt();
					_result.setPool(STMT_POOL);
					return _result;
				}
			});

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SliceStmt.class);

	/**
	 * The statement associated with this criterion.
	 */
	protected Stmt stmt;

	/**
	 * Checks if the given object is "equal" to this object.
	 *
	 * @param o is the object to be compared.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this object; <code>false</code>, otherwise.
	 */
	public boolean equals(final Object o) {
		boolean result = false;

		if (o != null && super.equals(o) && o instanceof SliceStmt) {
			final SliceStmt _temp = (SliceStmt) o;
			result = _temp.stmt == stmt;
		}
		return result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int hash = super.hashCode();
		hash = 37 * hash + stmt.hashCode();
		return hash;
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
	protected Object getCriterion() {
		return stmt;
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

/*
   ChangeLog:
   $Log$
   Revision 1.7  2003/12/02 19:20:50  venku
   - coding convention and formatting.
   Revision 1.6  2003/12/02 09:42:17  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.5  2003/12/01 12:20:14  venku
   - ripple effect of adding setConsider..() method to super class.
   - restricted the access to all methods.
   Revision 1.4  2003/11/24 00:01:14  venku
   - moved the residualizers/transformers into transformation
     package.
   - Also, renamed the transformers as residualizers.
   - opened some methods and classes in slicer to be public
     so that they can be used by the residualizers.  This is where
     published interface annotation is required.
   - ripple effect of the above refactoring.
   Revision 1.3  2003/11/05 08:28:49  venku
   - used more intuitive field names.
   - changed hashcode calculation.
   Revision 1.2  2003/11/03 08:03:25  venku
   - changed the way 2 instances are compared for equality.
   Revision 1.1  2003/10/13 00:58:03  venku
   - empty log message
   Revision 1.8  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.7  2003/08/20 18:31:22  venku
   - Documentation errors fixed.
   Revision 1.6  2003/08/18 12:14:13  venku
   - Well, to start with the slicer implementation is complete.
     Although not necessarily bug free, hoping to stabilize it quickly.
   Revision 1.5  2003/08/18 05:01:45  venku
   - Committing package name change in source after they were moved.
   Revision 1.4  2003/08/17 11:56:18  venku
   - Renamed SliceCriterion to AbstractSliceCriterion.
     Formatting, documentation, and specification.
   Revision 1.3  2003/05/22 22:23:49  venku
   - Changed interface names to start with a "I".
     Formatting.
 */
