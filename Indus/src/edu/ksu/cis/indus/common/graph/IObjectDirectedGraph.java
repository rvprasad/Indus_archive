
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

package edu.ksu.cis.indus.common.graph;

import org.apache.commons.collections.Transformer;


/**
 * This interface extends directed graph interface to query for an object associated a node of the graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IObjectDirectedGraph
  extends IDirectedGraph {
	/** 
	 * This transformer can be used with Collection Utils to extract the objects from a collection of SimpleNodes.  If the
	 * collection has objects of other type then the transformation will insert null into the collection being operated.
	 */
	Transformer OBJECT_EXTRACTOR =
		new Transformer() {
			/**
			 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
			 */
			public Object transform(final Object input) {
				Object _result = null;

				if (input instanceof IObjectNode) {
					_result = ((IObjectNode) input).getObject();
				}
				return _result;
			}
		};

	/**
	 * This interface facilitates the access to the object in a node.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	public interface IObjectNode
	  extends INode {
		/**
		 * Retrieves the object associated with the node.
		 *
		 * @return the node.
		 */
		Object getObject();
	}

	/**
	 * Returns a node that represents <code>o</code> in this graph.
	 *
	 * @param o is the object being represented by a node in this graph.
	 *
	 * @return the node representing <code>o</code>.
	 *
	 * @pre o != null
	 */
	IObjectNode queryNode(Object o);
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/07/25 10:26:06  venku
   - added a new interface to query values attached to nodes.
   Revision 1.1  2004/07/24 09:57:05  venku
   - added a new interface to extract objects associated with nodes of the graph.
 */
