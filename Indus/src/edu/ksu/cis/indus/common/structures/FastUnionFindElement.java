
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

package edu.ksu.cis.indus.common.structures;

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
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.

   Revision 1.6  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.5  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.4  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/11 07:13:58  venku
 *** empty log message ***
     Revision 1.2  2003/08/11 04:20:19  venku
     - Pair and Triple were changed to work in optimized and unoptimized mode.
     - Ripple effect of the previous change.
     - Documentation and specification of other classes.
     Revision 1.1  2003/08/07 06:42:16  venku
     Major:
      - Moved the package under indus umbrella.
      - Renamed isEmpty() to hasWork() in IWorkBag.
     Revision 1.4  2003/05/22 22:18:31  venku
     All the interfaces were renamed to start with an "I".
     Optimizing changes related Strings were made.
 */
