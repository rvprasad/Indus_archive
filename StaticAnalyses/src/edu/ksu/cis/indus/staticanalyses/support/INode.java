
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

package edu.ksu.cis.indus.staticanalyses.support;

import java.util.Collection;


/**
 * The interface to be implemented by node objects occuring in <code>DirectedGraph</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface INode {
	/**
	 * Retrieves the predecessors of this node.
	 *
	 * @return the collection of predecessors of this node.
	 *
	 * @post result->forall(o | o.oclIsKindOf(INode))
	 * @post result->forall(o | o.getSuccsOf()->includes(this))
	 */
	Collection getPredsOf();

	/**
	 * Retrieves the successors of this node.
	 *
	 * @param forward <code>true</code> implies forward direction(successors); <code>false</code> implies backward direction
	 * 		  (predecessors).
	 *
	 * @return the collection of successors of this node.
	 *
	 * @post result->forall(o | o.oclIsKindOf(INode))
	 * @post forward == true implies result->forall(o | o.getPredsOf()->includes(this))
	 * @post forward == false implies result->forall(o | o.getSuccsOf()->includes(this))
	 */
	Collection getSuccsNodesInDirection(boolean forward);

	/**
	 * Retrieves the set of successor nodes of this node.
	 *
	 * @return the collection of successor nodes(<code>INode</code>) of this node.
	 *
	 * @post result->forall(o | o.oclIsKindOf(INode))
	 * @post result->forall(o | o.getPredsOf()->includes(this))
	 */
	Collection getSuccsOf();
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.4  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/11 06:40:54  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Revision 1.2  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in IWorkBag.
   Revision 1.1  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
