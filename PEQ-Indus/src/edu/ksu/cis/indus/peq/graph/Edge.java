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
 
package edu.ksu.cis.indus.peq.graph;

import edu.ksu.cis.peq.constructor.interfaces.IConstructor;
import edu.ksu.cis.peq.graph.interfaces.IEdge;
import edu.ksu.cis.peq.graph.interfaces.INode;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
/**
 * @author ganeshan
 *
 * This represents an edge in the program dependency graph.
 */
public class Edge implements IEdge {

    /**
     * The source node of the edge.
     */
    private INode srcNode;
    
    /**
     * The destination node of the edge.
     */
    private INode dstnNode;
    
    /**
     * The constructor associated with the edge.
     */
    private IConstructor constructor;
    
    /** 
     * Get the source node.
     * @see edu.ksu.cis.peq.graph.interfaces.IEdge#getSrcNode()
     */
    public INode getSrcNode() {
        return srcNode;
    }

    /**
     *  Get the destination node.     
     * @see edu.ksu.cis.peq.graph.interfaces.IEdge#getDstnNode()
     */
    public INode getDstnNode() {
        return dstnNode;
    }

    /** 
     * Get the constructor.
     * @see edu.ksu.cis.peq.graph.interfaces.IEdge#getConstructor()
     */
    public IConstructor getConstructor() {
       return constructor;
    }

    
    /**
     * Set the constructor.
     * @param constructor The constructor to set.
     */
    public void setConstructor(IConstructor constructor) {
        this.constructor = constructor;
    }
    
    /**
     * Set the destination node.
     * @param dstnNode The destination tnode to set.
     */
    public void setDstnNode(Node dstnNode) {
        this.dstnNode = dstnNode;        
    }
    /**
     * Set the source node.
     * @param srcNode The source node to set.
     */
    public void setSrcNode(Node srcNode) {
        this.srcNode = srcNode;        
    }
    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof Edge)) {
            return false;
        }
        Edge rhs = (Edge) object;
        return new EqualsBuilder().appendSuper(super.equals(object)).append(
                this.constructor, rhs.constructor).append(this.dstnNode,
                rhs.dstnNode).append(this.srcNode, rhs.srcNode).isEquals();
    }
   
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new HashCodeBuilder(1315725089, -376481015).appendSuper(
                super.hashCode()).append(this.constructor)
                .append(this.dstnNode).append(this.srcNode).toHashCode();
    }
}
