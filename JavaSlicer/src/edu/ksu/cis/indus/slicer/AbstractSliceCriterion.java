
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
import org.apache.commons.pool.ObjectPool;


/**
 * This class represents a slice criterion.  This class has support builtin for object pooling.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
abstract class AbstractSliceCriterion {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractSliceCriterion.class);

	/**
	 * The pool to which this object belongs to.
	 */
	protected ObjectPool pool;

	/**
	 * This indicates if this criterion is included in the slice or not.
	 */
	protected boolean inclusive;

	/**
	 * Returns the stored criterion object.
	 *
	 * @return Object representing the criterion.
	 *
	 * @post result != null
	 */
	public abstract Object getCriterion();

	/**
	 * Indicates if this criterion is included in the slice or not.
	 *
	 * @return <code>true</code> if this criterion is included in the slice; <code>false</code>, otherwise.
	 */
	public boolean isIncluded() {
		return inclusive;
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

		if (o != null && o instanceof AbstractSliceCriterion) {
			result = ((AbstractSliceCriterion) o).inclusive == inclusive;
		}
		return result;
	}

	/**
	 * Returns the hashcode for this object.
	 *
	 * @return the hashcode for this object.
	 */
	public int hashCode() {
		int result;

		if (inclusive) {
			result = Boolean.TRUE.hashCode();
		} else {
			result = Boolean.FALSE.hashCode();
		}
		return result;
	}

	/**
	 * Initializes this object.
	 *
	 * @param shouldBeIncluded <code>true</code> indicates this criterion should be included in the slice;
	 * 		  <code>false</code>, otherwise.
	 */
	protected void initialize(final boolean shouldBeIncluded) {
		this.inclusive = shouldBeIncluded;
	}

	/**
	 * Performs cleanup.  This will/should be called after this object has been used as slice criterion or it has been
	 * decided  that is no longer required as a slice criterion.
	 *
	 * @throws RuntimeException if the returning of the object to it's pool failed.
	 */
	void sliced() {
		if (pool != null) {
			try {
				pool.returnObject(this);
			} catch (Exception e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("How can this happen?", e);
				}
				throw new RuntimeException(e);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.

   Revision 1.3  2003/08/18 12:14:13  venku
   Well, to start with the slicer implementation is complete.
   Although not necessarily bug free, hoping to stabilize it quickly.
   Revision 1.2  2003/08/18 05:01:45  venku
   Committing package name change in source after they were moved.
   Revision 1.1  2003/08/17 11:56:18  venku
   Renamed SliceCriterion to AbstractSliceCriterion.
   Formatting, documentation, and specification.
   Revision 1.4  2003/05/22 22:23:50  venku
   Changed interface names to start with a "I".
   Formatting.
 */
