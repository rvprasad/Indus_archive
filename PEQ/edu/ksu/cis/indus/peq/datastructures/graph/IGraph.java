/*
 * IGraph.java
 *
 * Created on November 24, 2004, 9:30 PM
 */

package edu.ksu.cis.indus.peq.datastructures.graph;

/**
 * Represents the system graph.
 * @author  Ganeshan
 */
public interface IGraph {
    /**
     * Gets the initial node in the system graph.
     * @return IGraphNode The initial node.
     */
    IGraphNode getInitialNode();
}
