/*
 * GraphEngine.java
 *
 * Created on December 12, 2004, 8:07 PM
 */

package edu.ksu.cis.indus.peq.ge;

import edu.ksu.cis.indus.peq.datastructures.graph.IGraph;
import edu.ksu.cis.indus.peq.datastructures.graph.IGraphNode;
import edu.ksu.cis.indus.peq.fsm.datastructures.common.IGraphToken;

/**
 *
 * @author  Ganeshan
 */
public abstract class AbstractGraphEngineFactory {
    
	/**
	 * The instance of the system graph.
	 */
    private IGraph objGraph;
    
    /** Creates a new instance of GraphEngine. 
     * @param objSysGraph The system graph.
     */
    public AbstractGraphEngineFactory(final IGraph objSysGraph) {
        this.objGraph = objSysGraph;
    }
    
    /**
     * Returns the first node in the system graph.
     * @return IGraphNode The first node in the system
     */
    public IGraphNode getFirstNode() {
       return objGraph.getInitialNode();
    }
    
    /**
     * Returns the destination node based on a token.
     * @param objSrcNode The source node.
     * @param objToken The token to be used in fetching the destination node.
     * @return IGraphNode The destination node based on the given token.
     */
    public abstract IGraphNode getDestNodeBasedOnToken(IGraphNode objSrcNode, IGraphToken objToken);
    
}
