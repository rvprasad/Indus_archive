/*
 * Edge.java
 *
 * Created on November 23, 2004, 10:39 PM
 */

package edu.ksu.cis.indus.peq.datastructures.graph;




/**
 * Base class for an edge.
 * @author  Ganeshan
 */
public class Edge {
    
    /** 
     * The source node of the edge.
     */
    private INode nEdgeSource;
    
    /**
     * The destination node of the edge.
     */
    private INode nEdgeDestination;
    
    /**
     * Creates an instance of the edge.<br>
     * <b>Precondition:</b> nSource, nDestination > 0.
     * @param nSource The source node of the edge.
     * @param nDestination The destination node of the edge.
     */
    public Edge(final INode nSource, final INode nDestination) {
        nEdgeSource = nSource;
        nEdgeDestination = nDestination;
    }
    
    /**
     * Returns the source node of the edge.
     * @return The edge source node.
     */
    public INode getSource() {    
        return nEdgeSource;
    }
    
    /**
     * Returns the destination node of the edge.     
     * @return The edge destination node.
     */
    public INode getDestination() {
        return nEdgeDestination;
    }
}
