
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

package edu.ksu.cis.bandera.staticanalyses.support;

/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class FastUnionFindElement {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected FastUnionFindElement set;

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean isAtomic() {
		return true;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean isBound() {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public final FastUnionFindElement find() {
		FastUnionFindElement result = this;

		while(result.set != null) {
			result = result.set;
		}

		if(result != this) {
			set = result;
		}
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param e DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean sameType(FastUnionFindElement e) {
		return false;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param e DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean unifyComponents(FastUnionFindElement e) {
		return true;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param e DOCUMENT ME!
	 */
	public final void union(FastUnionFindElement e) {
		FastUnionFindElement a = find();
		FastUnionFindElement b = e.find();

		if(a != b) {
			if(b.isBound()) {
				a.set = b;
			} else { // if a.isBound() or neither is bound
				b.set = a;
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param e DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean unify(FastUnionFindElement e) {
		boolean result = false;
		FastUnionFindElement a;
		FastUnionFindElement b;
		a = find();
		b = e.find();

		if(a == b || a.sameType(b)) {
			result = true;
		} else if(!(a.isAtomic() || b.isAtomic())) {
			a.union(b);
			result = a.unifyComponents(b);
		} else if(!(a.isBound() && b.isBound())) {
			a.union(b);
			result = true;
		}

		return result;
	}
}

/*****
 ChangeLog:

$Log$

*****/
