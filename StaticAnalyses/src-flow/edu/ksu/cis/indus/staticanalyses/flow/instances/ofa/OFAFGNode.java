
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.WorkList;

import java.util.Collection;
import java.util.Iterator;


/**
 * This class represents the flow graph node that accumulates objects as their entities would refer to objects at run-time.
 * This is an Object-flow analysis specific implementation.
 * 
 * <p>
 * Created: Thu Jan 31 00:42:34 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
class OFAFGNode
  extends AbstractFGNode {
	/**
	 * Creates a new <code>OFAFGNode</code> instance.
	 *
	 * @param wl the worklist associated with the instance of the framework within which this node exists.
	 *
	 * @pre wl != null
	 */
	public OFAFGNode(final WorkList wl) {
		super(wl);
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @param o the <code>WorkList</code> to be passed to the constructor of this class.
	 *
	 * @return a new instance of this class parameterized by <code>o</code>.
	 *
	 * @pre o != null and o.oclIsKindOf(WorkList)
	 * @post result != null and result.oclIsKindOf(OFAFGNode)
	 */
	public Object getClone(final Object o) {
		return new OFAFGNode((WorkList) o);
	}

	/**
	 * Adds a new work to the worklist to propogate the values in this node to <code>succ</code>.  Only the difference values
	 * are propogated.
	 *
	 * @param succ the successor node that was added to this node.
	 *
	 * @pre succ != null
	 */
	public void onNewSucc(final IFGNode succ) {
		Collection _temp = diffValues(succ);

		if (filter != null) {
			_temp = filter.filter(_temp);
		}

		if (!_temp.isEmpty()) {
			worklist.addWork(SendValuesWork.getWork(succ, _temp));
		}
	}

	/**
	 * Adds a new work to the worklist to propogate <code>value</code> in this node to it's successor nodes.
	 *
	 * @param value the value to be propogated to the successor node.
	 *
	 * @pre value != null
	 */
	public void onNewValue(final Object value) {
		for (final Iterator _i = succs.iterator(); _i.hasNext();) {
			final IFGNode _succ = (IFGNode) _i.next();

			boolean _filterOut = false;

			if (filter != null) {
				_filterOut = filter.filter(value);
			}

			if (!_succ.getValues().contains(value) && !_filterOut) {
				worklist.addWork(SendValuesWork.getWork(_succ, value));
			}
		}
	}

	/**
	 * Adds a new work to the worklist to propogate <code>values</code> in this node to it's successor nodes.
	 *
	 * @param arrivingValues the values to be propogated to the successor node.  The collection contains object of type
	 * 		  <code>Object</code>.
	 *
	 * @pre arrivingValues != null
	 */
	public void onNewValues(final Collection arrivingValues) {
		Collection _temp = arrivingValues;

		if (filter != null) {
			_temp = filter.filter(_temp);
		}

		for (final Iterator _i = succs.iterator(); _i.hasNext();) {
			final IFGNode _succ = (IFGNode) _i.next();

			if (!diffValues(_succ).isEmpty()) {
				worklist.addWork(SendValuesWork.getWork(_succ, _temp));
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.10  2004/04/02 09:58:28  venku
   - refactoring.
     - collapsed flow insensitive and sensitive parts into common classes.
     - coding convention
     - documentation.

   Revision 1.9  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.8  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.7  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.6  2003/08/25 11:26:14  venku
   Major bug: Added the original set of values instead of the filtered values. FIXED.
   Revision 1.5  2003/08/17 11:54:19  venku
   Formatting and documentation.
   Revision 1.4  2003/08/17 11:19:13  venku
   Placed the simple SendValuesWork class into a separate file.
   Extended it with work pool support.
   Amended AbstractWork and WorkList to enable work pool support.
   Revision 1.3  2003/08/17 10:33:03  venku
   WorkList does not inherit from IWorkBag rather contains an instance of IWorkBag.
   Ripple effect of the above change.
   Revision 1.2  2003/08/15 03:39:53  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosened later on in implementaions.
   Renamed a few fields/parameter variables to avoid name confusion.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.6  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
