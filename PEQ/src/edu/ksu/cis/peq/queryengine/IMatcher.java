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
import edu.ksu.cis.peq.fsm.interfaces.ITransition;
import edu.ksu.cis.peq.graph.interfaces.IEdge;


/**
 * @author ganeshan
 * This is the interface for the label matcher.
 */
public interface IMatcher {
    
        /**
         * Matches the labels in the edge and transition if possible.
         * @pre edge != null and transition != null
         * @post Result.isEmpty() => No match was possible else returns the mapping in the token
         */
        IFSMToken getMatch(final IEdge edge, final ITransition transition);
        
        /**
         * Merges the maps in the token if there is no conflict.
         * @pre sourceToken != null and childToken != null
         * @post Result.isEmpty() => Substitutionmap not mergeable, !Result.isEmpty() => Result.getParent() = sourceToken.
         */
        IFSMToken merge(final IFSMToken sourceToken, final IFSMToken childToken);
}
