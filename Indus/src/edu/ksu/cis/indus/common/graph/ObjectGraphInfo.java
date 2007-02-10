/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.common.graph;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.collections.ITransformer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
	@NonNull private final ITransformer<O, N> obj2nodeTransformer;

	/**
	 * This maps objects to their representative nodes.
	 */
	@NonNull @NonNullContainer private final Map<O, N> object2nodes = new HashMap<O, N>();

	/**
	 * Creates an instance of this class.
	 * 
	 * @param object2nodeTransformer to be used by this object. This transformer should return a non-null node as a result of
	 *            every transformation.
	 */
	public ObjectGraphInfo(@NonNull @Immutable final ITransformer<O, N> object2nodeTransformer) {
		obj2nodeTransformer = object2nodeTransformer;
	}

	/**
	 * Retrieves the node for the given object. The result depends on the object-to-node transformer provided to this object
	 * during construction.
	 * 
	 * @param object of interest.
	 * @return a node.
	 */
	@NonNull public N getNode(@Immutable final O object) {
		N _result = queryNode(object);

		if (_result == null) {
			_result = obj2nodeTransformer.transform(object);
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
	@Functional public N queryNode(@Immutable final O o) {
		final N _result = object2nodes.get(o);
		return _result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public boolean removeNode(@NonNull @Immutable final N node) {
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
