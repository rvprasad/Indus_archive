
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

package edu.ksu.cis.indus.common.datastructures;

import java.util.List;


/**
 * This class provides the basic implementation for elements to be used in fast-union-find algorithm as  defined by Aho,
 * Ullman, and Sethi in the "Dragon" book.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class FastUnionFindElement {
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
	 * Checks if this element has any children. 
	 *
	 * @return <code>true</code> if it has children; <code>false</code>, otherwise.
	 */
	public final boolean isAtomic() {
		return children == null || children.size() == 0;
	}

	/**
	 * Checks if this element is bound to a type.  
	 *
	 * @return <code>true</code> if it is bound to a type; <code>false</code>, otherwise. 
	 */
	public final boolean isBound() {
		return type != null;
	}

	/**
	 * Retrieves the type of this element.
	 *
	 * @return the type of this element.
	 */
	public final Object getType() {
		return find().type;
	}

	/**
	 * Retrieves the element that represents the equivalence class to which this element belongs to.
	 *
	 * @return the representative element.
	 *
	 * @post result != null
	 */
	public final FastUnionFindElement find() {
		FastUnionFindElement _result = this;

		while (_result.set != null) {
			_result = _result.set;
		}

		if (_result != this) {
			set = _result;
		}
		return _result;
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
	public final boolean sameType(final FastUnionFindElement e) {
		return e.type.equals(type);
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
		boolean _result = false;
		final FastUnionFindElement _a = find();
		final FastUnionFindElement _b = e.find();

		if (_a == _b || _a.sameType(_b)) {
			_result = true;
		} else if (!(_a.isAtomic() || _b.isAtomic())) {
			_a.union(_b);
			_result = _a.unifyChildren(_b);
		} else if (!(_a.isBound() && _b.isBound())) {
			_a.union(_b);
			_result = true;
		}

		return _result;
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
	public final boolean unifyChildren(final FastUnionFindElement e) {
		boolean _result = false;

		if (children != null && e.children != null && children.size() == e.children.size()) {
			_result = true;

			for (int _i = children.size() - 1; _i >= 0 && _result; _i--) {
				final FastUnionFindElement _c1 = (FastUnionFindElement) children.get(_i);
				final FastUnionFindElement _c2 = (FastUnionFindElement) e.children.get(_i);
				_result &= _c1.unify(_c2);
			}
		}
		return _result;
	}

	/**
	 * Merges the equivalence classes containing <code>e</code> and this element.
	 *
	 * @param e is the element in an equivalence class that needs to be merged.
	 *
	 * @pre e != null
	 */
	public final void union(final FastUnionFindElement e) {
		final FastUnionFindElement _a = find();
		final FastUnionFindElement _b = e.find();

		if (_a != _b) {
			if (_b.isBound()) {
				_a.set = _b;
			} else {  // if a.isBound() or neither is bound
				_b.set = _a;
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/01/06 00:17:10  venku
   - Classes pertaining to workbag in package indus.graph were moved
     to indus.structures.
   - indus.structures was renamed to indus.datastructures.

   Revision 1.2  2003/12/13 02:28:54  venku
   - Refactoring, documentation, coding convention, and
     formatting.

   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
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
    empty log message 
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
