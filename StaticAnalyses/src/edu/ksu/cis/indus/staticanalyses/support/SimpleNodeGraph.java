
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


/**
 * This is a simple concrete implementation of <code>DirectedGraph</code> interface.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class SimpleNodeGraph
  extends MutableDirectedGraph {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SimpleNodeGraph.class);

	/**
	 * The sequence of nodes in this graph.  They are stored in the order that the nodes are created.
	 *
	 * @invariant nodes.oclIsTypeOf(Sequence(SimpleNode))
	 */
	private List nodes = new ArrayList();

	/**
	 * This maps objects to their representative nodes.
	 *
	 * @invariant object2nodes.oclIsTypeOf(Map(Object, SimpleNode))
	 */
	private Map object2nodes = new HashMap();

	/**
	 * This is a simple concrete implementation of <code>INode</code> interface.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public class SimpleNode
	  extends MutableDirectedGraph.MutableNode {
		/**
		 * The object being represetned by this node.
		 */
		public final Object _object;

		/**
		 * Creates a new SimpleNode object.
		 *
		 * @param o is the object to be represented by this node.
		 */
		SimpleNode(final Object o) {
			super(new HashSet(), new HashSet());
			this._object = o;
		}

		/**
		 * Returns the stringized representation of this object.
		 *
		 * @return stringized representation.
		 *
		 * @post result != null
		 */
		public String toString() {
			return _object.toString();
		}
	}

	/**
	 * Returns a node that represents <code>o</code> in this graph.  If no such node exists, then a new node is created.
	 *
	 * @param o is the object being represented by a node in this graph.
	 *
	 * @return the node representing <code>o</code>.
	 *
	 * @throws NullPointerException when the given object is <code>null</code>.
	 *
	 * @pre o != null
	 * @post object2nodes$pre.get(o) == null implies inclusion
	 * @post inclusion: nodes->includes(result) and heads->includes(result) and tails->includes(result) and
	 * 		 object2nodes.get(o) == result
	 * @post result != null
	 */
	public MutableNode getNode(final Object o) {
		if (o == null) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("object to be represented cannot be null.");
			}
			throw new NullPointerException("object to be represented cannot be null.");
		}

		MutableNode result = (MutableNode) object2nodes.get(o);

		if (result == null) {
			result = new SimpleNode(o);
			object2nodes.put(o, result);
			nodes.add(result);
			heads.add(result);
			tails.add(result);
            hasSpanningForest = false;
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.DirectedGraph#getNodes()
	 */
	public List getNodes() {
		return Collections.unmodifiableList(nodes);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.DirectedGraph#size()
	 */
	public int size() {
		return nodes.size();
	}

	/**
	 * @see MutableDirectedGraph#containsNodes(edu.ksu.cis.indus.staticanalyses.support.INode)
	 */
	protected boolean containsNodes(final INode node) {
		return nodes.contains(node);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/08/24 08:13:11  venku
   Major refactoring.
    - The methods to modify the graphs were exposed.
    - The above anamoly was fixed by supporting a new class MutableDirectedGraph.
    - Each Mutable graph extends this graph and exposes itself via
      suitable interface to restrict access.
    - Ripple effect of the above changes.

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
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 1.5  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
