
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

package edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation;

import edu.ksu.cis.indus.processing.Context;


/**
 * This class adds support allocation site sensitive information to be captured in a context.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class AllocationContext
  extends Context {
	/** 
	 * The allocation-site component of the context.  This is relevant in the allocation-site sensitive mode of analysis.  In
	 * particular, when dealing with instance fields and arrays.
	 */
	protected Object allocationSite;

	/**
	 * Creates a new AllocationContext object.
	 */
	public AllocationContext() {
		super();
	}

	/**
	 * Sets the allocation site in this context.
	 *
	 * @param site the allocation site in this context.
	 *
	 * @return the allocation site previously represented by this context.
	 */
	public Object setAllocationSite(final Object site) {
		final Object _temp = allocationSite;
		allocationSite = site;

		return _temp;
	}

	/**
	 * Returns the allocation site in this context.
	 *
	 * @return the allocation site in this context.
	 */
	public Object getAllocationSite() {
		return allocationSite;
	}

	/**
	 * Checks if <code>o</code> is equal to this object.
	 *
	 * @param o to be checked for equality.
	 *
	 * @return <code>true</code> if <code>o</code> is equal to this object; <code>false</code>, otherwise.
	 */
	public boolean equals(final Object o) {
		boolean _result = false;

		if (o != null && o instanceof AllocationContext) {
			final AllocationContext _c = (AllocationContext) o;

			if (allocationSite != null) {
				_result = allocationSite.equals(_c.allocationSite);
			} else {
				_result = _c.allocationSite == allocationSite;
			}
		}
		return _result && super.equals(o);
	}

	/**
	 * Returns the hash code of this object based on the allocation site and other context constituents.
	 *
	 * @return the hash code.
	 */
	public int hashCode() {
		int _result = 17;

		if (allocationSite != null) {
			_result = 37 * _result + allocationSite.hashCode();
		}
		_result = 37 * _result + super.hashCode();
		return _result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return super.toString() + "\tAllocation Site: " + allocationSite;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2003/12/02 09:42:39  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.6  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.5  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.4  2003/08/13 08:02:28  venku
   Fixed Checkstyle formatting errors.
   Revision 1.3  2003/08/12 19:03:47  venku
   Spruced up documentation and specification.
   Changed equals() and hashCode().
   Revision 1.2  2003/08/12 18:47:50  venku
   Spruced up documentation and specification.
   Changed equals() and hashCode() in AllocationContext.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.4  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
