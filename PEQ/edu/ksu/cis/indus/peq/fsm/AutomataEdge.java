/*
 * AutomataEdge.java
 *
 * Created on November 23, 2004, 10:58 PM
 */

package edu.ksu.cis.indus.peq.fsm;

import edu.ksu.cis.indus.peq.datastructures.graph.Edge;





/**
 * Represents a transition edge in the automata.
 * @author  Ganeshan
 */
public class AutomataEdge extends Edge {
    /** The label attached to the edge.*/
    private Object objEdgeLabel;
    
    /** 
     * Creates a new instance of TransitionEdge. <br>
     * <b>Precondition:</b> nSource, nDestination > 0 and objLabel != NULL.
     * @param objSource The source node of the edge.
     * @param objDestination The destination node of the edge.
     * @param objLabel The label attached to the edge.
     */
    public AutomataEdge(final IAutomataNode objSource, final IAutomataNode objDestination, final Object objLabel) {
        super(objSource, objDestination);
        objEdgeLabel = objLabel;
    }
    
    /**
     * Returns the label attached to the edge.
     * @return The label attached to the object.
     */
    public Object getEdgeLabel() {
        return objEdgeLabel;
    }   
}
