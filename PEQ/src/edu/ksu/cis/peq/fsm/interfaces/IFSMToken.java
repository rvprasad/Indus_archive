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
 * Created on March 8, 2005, 8:39 PM
 */

package edu.ksu.cis.peq.fsm.interfaces;

import edu.ksu.cis.peq.graph.interfaces.IEdge;
import java.util.Map;

/**
 * @author ganeshan
 * This represents the result of the matching function.
 *
 */
public interface IFSMToken {
    /**
     * Get the graph edge that was matched,
     */
    IEdge getGraphEdge();
    
    /**
     * Indicates if this token is empty => No match was possible.
     */
    boolean isEmpty();
    
    /**
     * Get the fsm transition that was matched.
     */
    ITransition getTransitionEdge();
    
    /**
     * Get the variable substitution map.
     * @post Result.oclisKindOf(HashMap) and Result.size == 0 <=> isEmpty() == true
     */
    Map getSubstituitionMap();
}
