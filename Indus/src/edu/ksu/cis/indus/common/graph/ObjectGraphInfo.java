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
 * @param <N> the type of the node in the associated graph.
 * @param <O> the type of the object stored in the nodes of the graph.
 */
final class ObjectGraphInfo<N extends IObjectNode<N, O>, O>
		extends GraphInfo<N> {

	/**
	 * This transforms objects to nodes.
	 */
	private final Transformer obj2nodeTransformer;

	/**
	 * This maps objects to their representative nodes.
	 */
	private final Map<O, N> object2nodes = new HashMap<O, N>();

	/**
	 * Creates an instance of this class.
	 * 
	 * @param object2nodeTransformer to be used by this object.
	 * @pre object2nodeTransformer != null
	 */
	public ObjectGraphInfo(final Transformer object2nodeTransformer) {
		obj2nodeTransformer = object2nodeTransformer;
	}

	/**
	 * Retrieves the node for the given object. The result depends on the object-to-node transformer provided to this object
	 * during construction.
	 * 
	 * @param object of interest.
	 * @return a node.
	 */
	public N getNode(final O object) {
		N _result = queryNode(object);

		if (_result == null) {
			_result = (N) obj2nodeTransformer.transform(object);
			object2nodes.put(object, _result);
			addNode(_result);
		}
		return _result;
	}

	/**
	 * Retrieves the node for the given object if one exists.
	 * 
	 * @param o is the object of interest.
	 * @return the node if one exists.
	 */
	public N queryNode(final O o) {
		final N _result = object2nodes.get(o);
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.GraphInfo#removeNode(INode)
	 */
	@Override public boolean removeNode(final N node) {
		final Iterator<Map.Entry<O, N>> _i = object2nodes.entrySet().iterator();
		final int _iEnd = object2nodes.entrySet().size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Map.Entry<O, N> _e = _i.next();

			if (_e.getValue() == node) {
				_i.remove();
				break;
			}
		}
		return super.removeNode(node);
	}
}

// End of File
