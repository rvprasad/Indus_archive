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

import edu.ksu.cis.peq.constructor.interfaces.IConstructor;
import edu.ksu.cis.peq.graph.interfaces.IEdge;
import edu.ksu.cis.peq.graph.interfaces.INode;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Edge implements IEdge {

    private Node srcNode;
    private Node dstnNode;
    private IConstructor cons;
    private String name;
    /**
     * @param string
     */
    public Edge(String string) {
        name = string;
    }

    public String toString() {
        return name;
    }
    
    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.graph.interfaces.IEdge#getSrcNode()
     */
    public INode getSrcNode() {
        return srcNode;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.graph.interfaces.IEdge#getDstnNode()
     */
    public INode getDstnNode() {
        return dstnNode;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.graph.interfaces.IEdge#getConstructor()
     */
    public IConstructor getConstructor() {
        return cons;
    }

    /**
     * @param cons The cons to set.
     */
    public void setConstructor(IConstructor cons) {
        this.cons = cons;
    }
    /**
     * @param dstnNode The dstnNode to set.
     */
    public void setDestnNode(Node dstnNode) {
        this.dstnNode = dstnNode;
    }
    /**
     * @param srcNode The srcNode to set.
     */
    public void setSrcNode(Node srcNode) {
        this.srcNode = srcNode;
    }
}
