
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

import edu.ksu.cis.indus.common.CustomToStringStyle;

import edu.ksu.cis.indus.interfaces.AbstractPoolable;

import org.apache.commons.lang.builder.ToStringBuilder;

import soot.SootMethod;


/**
 * This class represents a slice criterion.  This class has support builtin for object pooling.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
abstract class AbstractSliceCriterion
  extends AbstractPoolable
  implements Cloneable,
	  ISliceCriterion {
	/**
	 * The method in which <code>stmt</code> occurs.
	 */
	protected SootMethod method;

	/**
	 * This indicates if the effect of executing the criterion should be considered for slicing.  By default it takes on  the
	 * value <code>false</code> to indicate execution should not be considered.
	 */
	private boolean considerExecution;

	/**
	 * @see ISliceCriterion#setConsiderExecution(boolean)
	 */
	public final void setConsiderExecution(final boolean shouldConsiderExecution) {
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
		boolean _result = false;

		if (o instanceof AbstractSliceCriterion) {
			_result =
				((AbstractSliceCriterion) o).method == method
				  && ((AbstractSliceCriterion) o).considerExecution == considerExecution;
		}
		return _result;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int _hash = 17;
		_hash = _hash * 37 + Boolean.valueOf(considerExecution).hashCode();
		_hash = _hash * 37 + method.hashCode();
		return _hash;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this, CustomToStringStyle.HASHCODE_AT_END_STYLE).append("considerExecution",
			this.considerExecution).append("method", this.method).toString();
	}

	/**
	 * Provides the method in which criterion occurs.
	 *
	 * @return the method in which the slice statement occurs.
	 *
	 * @post result != null
	 */
	protected final SootMethod getOccurringMethod() {
		return method;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	protected final Object clone()
	  throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Initializes this object.
	 *
	 * @param occurringMethod in which the slice criterion occurs.
	 */
	protected final void initialize(final SootMethod occurringMethod) {
		method = occurringMethod;
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
	final boolean isConsiderExecution() {
		return considerExecution;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.16  2004/06/26 10:16:35  venku
   - bug #389. FIXED.
   Revision 1.15  2004/06/24 06:53:53  venku
   - refactored SliceConfiguration
     - added processBooleanProperty()
     - renamed getNamesOfDAToUse() to getIDOfDAToUse()
   - ripple effect
   - made AbstractSliceCriterion package private
   - made ISliceCriterion public
   Revision 1.14  2004/02/25 23:43:46  venku
   - build and check for compilation errors before committing.
   Revision 1.12  2004/01/22 11:43:38  venku
   - while checking for equality we can rely on instanceof for null check.
     null instanceof <anything> is always false.
   - If SliceExpr is the subclass of SliceStmt, then a SliceExpr object
     can be equal to a SliceStmt object if they are equal in terms of SliceStmt
     fields.  Hence, the slicestmt object may not be added to the
     workbag when an identical sliceexpr object exists in the workbag. FIXED.
   Revision 1.11  2004/01/20 00:35:14  venku
   - use the new custom to string style defined in indus.
   Revision 1.10  2004/01/19 08:27:03  venku
   - enabled logging of criteria when they are created in SlicerTool.
   Revision 1.9  2003/12/13 02:29:16  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.8  2003/12/04 12:10:12  venku
   - changes that take a stab at interprocedural slicing.
   Revision 1.7  2003/12/02 19:20:50  venku
   - coding convention and formatting.
   Revision 1.6  2003/12/02 09:42:17  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.5  2003/12/01 12:13:37  venku
   - removed support to carry slice type as it was needed now.
     It can be rolled back on when required. :-)
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
