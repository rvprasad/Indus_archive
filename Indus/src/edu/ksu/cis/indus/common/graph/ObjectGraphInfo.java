
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.common.graph.IDirectedGraph.INode;
import edu.ksu.cis.indus.common.graph.IObjectDirectedGraph.IObjectNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.Transformer;

/**
 * This maintains information pertaining to an object graph.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
final class ObjectGraphInfo
  extends GraphInfo {
	/** 
	 * This maps objects to their representative nodes.
	 *
	 * @invariant object2nodes.oclIsTypeOf(Map(Object, SimpleNode))
	 */
	private final Map object2nodes = new HashMap();

	/** 
	 * This transforms objects to nodes.
	 */
	private final Transformer obj2nodeTransformer;

	/**
	 * Creates an instance of this class.
	 *
	 * @param object2nodeTransformer to be used by this object.
	 *
	 * @pre object2nodeTransformer != null
	 */
	public ObjectGraphInfo(final Transformer object2nodeTransformer) {
		obj2nodeTransformer = object2nodeTransformer;
	}

	/**
	 * Retrieves the node for the given object.  The result depends on the object-to-node transformer provided to this object
	 * during construction.
	 *
	 * @param object of interest.
	 *
	 * @return a node.
	 */
	public INode getNode(final Object object) {
		INode _result = queryNode(object);

		if (_result == null) {
			_result = (INode) obj2nodeTransformer.transform(object);
			object2nodes.put(object, _result);
			addNode(_result);
		}
		return _result;
	}

	/**
	 * Retrieves the node for the given object if one exists.
	 *
	 * @param o is the object of interest.
	 *
	 * @return the node if one exists.
	 */
	public IObjectNode queryNode(final Object o) {
		final IObjectNode _result = (IObjectNode) object2nodes.get(o);
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.GraphInfo#removeNode(INode)
	 */
	public boolean removeNode(final INode node) {
		final Iterator _i = object2nodes.entrySet().iterator();
		final int _iEnd = object2nodes.entrySet().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Map.Entry _e = (Map.Entry) _i.next();

			if (_e.getValue() == node) {
				_i.remove();
				break;
			}
		}
		return super.removeNode(node);
	}
}

// End of File
