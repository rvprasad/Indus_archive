/*
 * IOracle.java
 *
 * Created on December 12, 2004, 8:30 PM
 */

package edu.ksu.cis.indus.peq.ge;

import edu.ksu.cis.indus.peq.datastructures.graph.IGraphNode;
import edu.ksu.cis.indus.peq.fsm.datastructures.common.IConstructor;
import edu.ksu.cis.indus.peq.fsm.datastructures.common.IGraphToken;

/**
 *
 * @author  Ganeshan
 */
public interface IOracle {
	
    /**
     * Indicates if a path is available from the current node satisying the 
     * given constructor.
     * @param objNode The current node.
     * @param objConstructor The current constructor.
     * @return IGraphToken The next graph node token.
     */
    IGraphToken isPathAvailable(final IGraphNode objNode, final IConstructor objConstructor);
}
