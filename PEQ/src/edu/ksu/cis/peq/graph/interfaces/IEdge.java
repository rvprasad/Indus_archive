/*
 * PEQ, a parameteric regular path query library
 * Copyright (c) 2005 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 *
 * Created on March 8, 2005, 6:11 PM
 */

package edu.ksu.cis.peq.graph.interfaces;

import edu.ksu.cis.peq.constructor.interfaces.IConstructor;

/**
 * @author ganeshan
 * This interface represents an edge in the system graph.
 */
public interface IEdge {
    /**
     * Returns the source node of this edge.
     * @pre IEdge.srcNode != null && IEdge.dstnNode != null
     */
    INode getSrcNode();
    
    /**
     * Returns the destination node of this edge.
     * @pre IEdge.srcNode != null && IEdge.dstnNode != null
     */
    INode getDstnNode();
    
    /**
     * Returns the constructor (label) associated with this edge.
     */
    IConstructor getConstructor();
}
