/*
 *
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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
 
package edu.ksu.cis.peq.test.graph;

import java.util.HashSet;
import java.util.Set;

import edu.ksu.cis.peq.graph.interfaces.IEdge;
import edu.ksu.cis.peq.graph.interfaces.INode;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Node implements INode {

    private Set enteringEdges;
    private Set exitingEdges;
    private String nodeName;
    
    public Node(String string) {
        enteringEdges = new HashSet();
        exitingEdges = new HashSet();
        nodeName = string;
    }
    
    public String toString() {
        return nodeName;
    }
    
    public void addEnteringEdge(final IEdge edge) {
        this.enteringEdges.add(edge);
    }
    
    public void addExitingEdge(final IEdge edge) {
        this.exitingEdges.add(edge);
    }
    
    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.graph.interfaces.INode#getEnteringEdges()
     */
    public Set getEnteringEdges() {
       return enteringEdges;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.graph.interfaces.INode#getExitingEdges()
     */
    public Set getExitingEdges() {
        return exitingEdges;
    }   

}
