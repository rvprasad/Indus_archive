
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

import java.util.Collection;
import java.util.Iterator;


/**
 * This is an abstract implementation of <code>IEdgeLabelledObjectDirectedGraphBuilder</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractEdgeLabelledObjectDirectedGraphBuilder
  extends AbstractObjectDirectedGraphBuilder
  implements IEdgeLabelledObjectDirectedGraphBuilder {
	/**
	 * Creates an instance of this class.
	 */
	public AbstractEdgeLabelledObjectDirectedGraphBuilder() {
		super();
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledObjectDirectedGraphBuilder#addEdgeFromTo(java.util.Collection,
	 * 		java.lang.Object, java.lang.Object)
	 */
	public void addEdgeFromTo(final Collection sources, final Object label, final Object dest) {
		final Iterator _i = sources.iterator();
		final int _iEnd = sources.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Object _src = _i.next();
			addEdgeFromTo(_src, label, dest);
		}
	}

	/**
	 * @see edu.ksu.cis.indus.common.graph.IEdgeLabelledObjectDirectedGraphBuilder#addEdgeFromTo(java.lang.Object,
	 * 		java.lang.Object, java.util.Collection)
	 */
	public void addEdgeFromTo(final Object src, final Object label, final Collection destinations) {
		final Iterator _i = destinations.iterator();
		final int _iEnd = destinations.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final Object _dest = _i.next();
			addEdgeFromTo(src, label, _dest);
		}
	}
}

// End of File
