
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.flow.modes.sensitive.symbolic;

import edu.ksu.cis.bandera.staticanalyses.flow.Context;


/**
 * This class adds support symbol sensitive information to be captured in a context.  It is a matter of software
 * implementation that both this class and <code>AllocationContext</code> exist when a single class in which one piece of data
 * could have represented both a symbol and an allocation site.  It only makes it clear to implement it this way.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SymbolicContext
  extends Context {
	/**
	 * The symbol component of the context.  This is relevant in the symbol sensitive mode of analysis.  In particular, when
	 * dealing with instance fields and arrays.
	 */
	protected Object allocationSite;

	/**
	 * Creates a new SymbolicContext object.
	 */
	public SymbolicContext() {
		super();
	}

	/**
	 * Sets the symbol in this context.
	 *
	 * @param site the symbol in this context.
	 *
	 * @return the symbol previously represented by this context.
	 */
	public Object setAllocationSite(Object site) {
		Object temp = allocationSite;
		allocationSite = site;

		return temp;
	}

	/**
	 * Returns the symbol in this context.
	 *
	 * @return the symbol in this context.
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
		boolean result = super.equals(o);

		if(result && o instanceof SymbolicContext) {
			SymbolicContext c = (SymbolicContext) o;

			if(allocationSite != null && c.allocationSite != null) {
				result &= allocationSite.equals(c.allocationSite);
			} else if(allocationSite != null) {
				result &= allocationSite.equals(c.allocationSite);
			} else {
				result &= c.allocationSite.equals(allocationSite);
			}
		}
		return result;
	}
}

/*****
 ChangeLog:

$Log$

*****/
