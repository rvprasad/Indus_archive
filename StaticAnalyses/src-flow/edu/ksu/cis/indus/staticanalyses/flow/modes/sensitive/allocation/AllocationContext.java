
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

import edu.ksu.cis.indus.staticanalyses.Context;


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
		Object temp = allocationSite;
		allocationSite = site;

		return temp;
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
		boolean result = false;

		if (o != null && o instanceof AllocationContext) {
			AllocationContext c = (AllocationContext) o;

			if (allocationSite != null) {
				result = allocationSite.equals(c.allocationSite);
			} else {
				result = c.allocationSite == allocationSite;
			}
		}
		return result && super.equals(o);
	}

	/**
	 * Returns the hash code of this object based on the allocation site and other context constituents.
	 *
	 * @return the hash code.
	 */
	public int hashCode() {
		int result = 17;

		if (allocationSite != null) {
			result = 37 * result + allocationSite.hashCode();
		}
		result = 37 * result + super.hashCode();
		return result;
	}
}

/*
   ChangeLog:
   $Log$
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
