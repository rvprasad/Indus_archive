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
 * Created on March 8, 2005, 6:29 PM
 */

package edu.ksu.cis.peq.graph.interfaces;

import java.util.Set;

/**
 * @author ganeshan
 * This interfaces defines the system graph engine.
 */
public interface IGraphEngine {
    
    /**
     * Get the set of initial nodes in the graph.
     * @pre IGraphEngine.nodeCount > 0
     * @post Result.oclIsKindOf(Set(INode))
     */
    Set getInitialNodes();
}
