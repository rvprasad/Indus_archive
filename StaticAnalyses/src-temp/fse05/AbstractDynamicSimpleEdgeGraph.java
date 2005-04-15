
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

package fse05;

import edu.ksu.cis.indus.common.graph.AbstractEdgeLabelledDirectedGraph;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.graph.SimpleEdgeGraph;

import java.util.Map;


/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractDynamicSimpleEdgeGraph
  extends SimpleEdgeGraph {
	/**
	 * @see AbstractEdgeLabelledDirectedGraph#getPredsOf(edu.ksu.cis.indus.common.graph.INode)
	 */
	public Map getPredsOf(final INode node) {
		processNode(node);
		return super.getPredsOf(node);
	}

	/**
	 * @see AbstractEdgeLabelledDirectedGraph#getSuccsOf(edu.ksu.cis.indus.common.graph.INode)
	 */
	public Map getSuccsOf(final INode node) {
		processNode(node);
		return super.getSuccsOf(node);
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param node DOCUMENT ME!
	 */
	protected abstract void processNode(INode node);
}

// End of File
