
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
public abstract class AbstractSliceCriterion {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(AbstractSliceCriterion.class);

	/**
	 * The pool to which this object belongs to.
	 */
	protected ObjectPool pool;

	/**
	 * This indicates if the effect of executing the criterion should be considered for slicing.  By default it takes on  the
	 * value <code>false</code> to indicate execution should not be considered.
	 */
	private boolean considerExecution = false;

	/**
	 * Sets the flag to indicate if the execution of the criterion should be considered during slicing.
	 *
	 * @param shouldConsiderExecution <code>true</code> indicates that the effect of executing this criterion should be
	 * 		  considered while slicing.  This means all the subexpressions of the associated expression are also considered
	 * 		  as slice criteria. <code>false</code> indicates that just the mere effect of the control reaching this
	 * 		  criterion should be considered while slicing.  This means none of the subexpressions of the associated
	 * 		  expression are considered as slice criteria.
	 */
	public void setConsiderExecution(final boolean shouldConsiderExecution) {
		considerExecution = shouldConsiderExecution;
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
			AbstractSliceCriterion t = (AbstractSliceCriterion) o;
			result = t.considerExecution == considerExecution;
		}
		return result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int hash = 17;
		hash = hash * 37 + Boolean.valueOf(considerExecution).hashCode();
		return hash;
	}

	/**
	 * Returns the stored criterion object.
	 *
	 * @return Object representing the criterion.
	 *
	 * @post result != null
	 */
	abstract Object getCriterion();

	/**
	 * Indicates if the effect of execution of criterion should be considered.
	 *
	 * @return <code>true</code> if the effect of execution should be considered; <code>false</code>, otherwise.
	 */
	boolean isConsiderExecution() {
		return considerExecution;
	}

	/**
	 * Performs cleanup.  This will/should be called after this object has been used as slice criterion or it has been
	 * decided  that is no longer required as a slice criterion.
	 *
	 * @throws RuntimeException if the returning of the object to it's pool failed.
	 */
	void finished() {
		if (pool != null) {
			try {
				pool.returnObject(this);
			} catch (Exception e) {
				LOGGER.error("How can this happen?", e);
				throw new RuntimeException(e);
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/12/01 12:12:26  venku
   - added support to carry slice type.
   Revision 1.3  2003/11/24 00:01:14  venku
   - moved the residualizers/transformers into transformation
     package.
   - Also, renamed the transformers as residualizers.
   - opened some methods and classes in slicer to be public
     so that they can be used by the residualizers.  This is where
     published interface annotation is required.
   - ripple effect of the above refactoring.
   Revision 1.2  2003/11/05 08:28:49  venku
   - used more intuitive field names.
   - changed hashcode calculation.
   Revision 1.1  2003/10/13 00:58:04  venku
   - empty log message
   Revision 1.4  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.3  2003/08/18 12:14:13  venku
   - Well, to start with the slicer implementation is complete.
     Although not necessarily bug free, hoping to stabilize it quickly.
   Revision 1.2  2003/08/18 05:01:45  venku
   - Committing package name change in source after they were moved.
   Revision 1.1  2003/08/17 11:56:18  venku
   - Renamed SliceCriterion to AbstractSliceCriterion.
     Formatting, documentation, and specification.
   Revision 1.4  2003/05/22 22:23:50  venku
   - Changed interface names to start with a "I".
     Formatting.
 */
