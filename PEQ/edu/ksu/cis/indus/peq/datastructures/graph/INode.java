/*
 * INode.java
 *
 * Created on December 12, 2004, 8:40 PM
 */

package edu.ksu.cis.indus.peq.datastructures.graph;

import gnu.trove.THashSet;

/**
 *
 * @author  Ganeshan
 */
public interface INode {
    /** Gets the predecessors of the node. 
     * @return THashSet The set of predecessor nodes.     
     * <p>Result.elements.isKindOf(Node)</p>
     */
    THashSet getPredecessors();
    
    /** Gets the successors of the node.
     * @return THashSet The set of successor nodes.
     *<p> Result.elements.isKindOf(Node)</p>
     */    
    THashSet getSuccessors();
}
