
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

import edu.ksu.cis.indus.common.collections.CollectionsUtilities;
import edu.ksu.cis.indus.common.graph.INode;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;


/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SimpleEdgeGraph
  extends SimpleNodeGraph {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private final Map node2outEdges = new HashMap();

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param node DOCUMENT ME!
	 * @param label DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getDestOfOutgoingEdgeLabelled(INode node, ILabel label) {
		return (Collection) MapUtils.getObject(((Map) MapUtils.getObject(node2outEdges, node)), label);
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param node DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Map getSuccsOf(INode node) {
		processNode((IObjectNode) node);
		return CollectionsUtilities.getMapFromMap(node2outEdges, node);
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param src DOCUMENT ME!
	 * @param label DOCUMENT ME!
	 * @param dest DOCUMENT ME!
	 */
	public void addEdgeFromTo(INode src, ILabel label, INode dest) {
		CollectionsUtilities.putIntoSetInMap(CollectionsUtilities.getMapFromMap(node2outEdges, src), label, dest);
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param node DOCUMENT ME!
	 * @param label DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public boolean hasOutgoingEdgeLabelled(INode node, ILabel label) {
		return ((Map) MapUtils.getObject(node2outEdges, node)).containsKey(label);
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param node DOCUMENT ME!
	 */
	protected void processNode(IObjectNode node) {
	}
}

// End of File
