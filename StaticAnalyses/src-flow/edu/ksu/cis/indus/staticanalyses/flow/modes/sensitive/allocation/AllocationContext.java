
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
	public Object setAllocationSite(Object site) {
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
	public boolean equals(Object o) {
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
        result = 37 * result + allocationSite.hashCode();
        result = 37 * result + super.hashCode();
		return result;
	}
}

/*
   ChangeLog:
   
   $Log$
   
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
    
   Revision 1.4  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
