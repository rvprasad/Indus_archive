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
 * Created on March 8, 2005, 8:55 PM
 */
 
package edu.ksu.cis.peq.queryengine;

import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;
import edu.ksu.cis.peq.graph.interfaces.IEdge;

/**
 * @author ganeshan
 *
 * This is the matcher used by the universal query engine.
 * 
 */
public interface IUQMatcher extends IMatcher {
    /**
     * Create a bad token on the given graph edge.
     * Used by the universal query algorithm.
     * @param edge The graph edge
     * @return IFSMToken The bad fsm token.
     */
    IFSMToken createBadToken(IEdge _edge);
}
