
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

package edu.ksu.cis.indus.staticanalyses.support;

import java.util.List;


/**
 * This class provides the basic implementation for elements to be used in fast-union-find algorithm as  defined by Aho,
 * Ullman, and Sethi in the "Dragon" book.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class FastUnionFindElement {
	/**
	 * This is the set to which this element belongs to.
	 */
	protected FastUnionFindElement set;

	/**
	 * A sequence of children elements of this element.
	 */
	protected List children;

	/**
	 * This is the type associated with this element.
	 */
	protected Object type;

	/**
	 * Checks if this element has any children.  This implementation has none, hence, always returns <code>true</code>.
	 *
	 * @return <code>true</code>.
	 */
	public boolean isAtomic() {
		return children == null || children.size() == 0;
	}

	/**
	 * Checks if this element is bound to a type.  This implementation does not deal with types, hence, always returns
	 * <code>false</code>.
	 *
	 * @return <code>false</code>
	 */
	public boolean isBound() {
		return type != null;
	}

	/**
	 * Retrieves the element that represents the equivalence class to which this element belongs to.
	 *
	 * @return the representative element.
	 *
	 * @post result != null
	 */
	public final FastUnionFindElement find() {
		FastUnionFindElement result = this;

		while (result.set != null) {
			result = result.set;
		}

		if (result != this) {
			set = result;
		}
		return result;
	}

	/**
	 * Retrieves the type of this element.
	 *
	 * @return the type of this element.
	 */
	public Object getType() {
		return find().type;
	}

	/**
	 * Checks if the given element and this element represent the same type. As this implementation does not deal with types,
	 * it assumes no two element belong to the same type, hence, always returns <code>false</code>.
	 *
	 * @param e is the element to checked for equivalence.
	 *
	 * @return <code>false</code>
	 *
	 * @pre e != null
	 */
	public boolean sameType(final FastUnionFindElement e) {
		return false;
	}

	/**
	 * Unifies the given element with this element.
	 *
	 * @param e is the element to be unified with this element.
	 *
	 * @return <code>true</code> if this element was unified with the given element; <code>false</code>, otherwise.
	 *
	 * @pre e != null
	 */
	public final boolean unify(final FastUnionFindElement e) {
		boolean result = false;
		FastUnionFindElement a = find();
		FastUnionFindElement b = e.find();

		if (a == b || a.sameType(b)) {
			result = true;
		} else if (!(a.isAtomic() || b.isAtomic())) {
			a.union(b);
			result = a.unifyChildren(b);
		} else if (!(a.isBound() && b.isBound())) {
			a.union(b);
			result = true;
		}

		return result;
	}

	/**
	 * Unifies the children of this element with that of the given element.
	 *
	 * @param e is the element whose children needs to be unified with that of this element.
	 *
	 * @return <code>true</code>, if the tree rooted at this element and at <code>e</code> was unified successfully;
	 * 		   <code>false</code>, otherwise.
	 *
	 * @pre e != null
	 */
	public boolean unifyChildren(final FastUnionFindElement e) {
		boolean result = false;

		if (children != null && e.children != null && children.size() == e.children.size()) {
			result = true;

			for (int i = children.size() - 1; i >= 0 && result; i--) {
				FastUnionFindElement c1 = (FastUnionFindElement) children.get(i);
				FastUnionFindElement c2 = (FastUnionFindElement) e.children.get(i);
				result &= c1.unify(c2);
			}
		}
		return result;
	}

	/**
	 * Merges the equivalence classes containing <code>e</code> and this element.
	 *
	 * @param e is the element in an equivalence class that needs to be merged.
	 *
	 * @pre e != null
	 */
	public final void union(final FastUnionFindElement e) {
		FastUnionFindElement a = find();
		FastUnionFindElement b = e.find();

		if (a != b) {
			if (b.isBound()) {
				a.set = b;
			} else {  // if a.isBound() or neither is bound
				b.set = a;
			}
		}
	}
}

/*
   ChangeLog:

   $Log$

   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.

   Revision 1.4  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
