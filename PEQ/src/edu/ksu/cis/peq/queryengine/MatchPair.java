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
 *
 * Created on March 8, 2005, 9:55 PM
 */
 
package edu.ksu.cis.peq.queryengine;


import java.util.Map;

import edu.ksu.cis.peq.fsm.interfaces.IMatchStateToken;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;

/**
 * @author ganeshan
 *
 * Concrete implementation of the match pair.
 */
public class MatchPair implements IMatchStateToken {

    /**
     * The transition.
     */    
    private ITransition transition;
    
    /** Indicates if the token is empty */
    private boolean isEmpty;
    
    /**
     * The substitution map.
     */
    private Map substMap;
    
    /** 
     * Indicates whether the token is empty.
     * @return boolean Indicates if the token is empty.
     * @see edu.ksu.cis.peq.fsm.interfaces.IMatchStateToken#isEmpty()
     */
    public boolean isEmpty() {
       return isEmpty;
    }

    /** Return the transition edge.
     * @return ITransition The state transition.
     * @see edu.ksu.cis.peq.fsm.interfaces.IMatchStateToken#getTransitionEdge()
     */
    public ITransition getTransitionEdge() {
       return transition;
    }

    /** Return the substitution map.
     * @see edu.ksu.cis.peq.fsm.interfaces.IMatchStateToken#getSubstituitionMap()
     * @return Map The substitution map.
     */
    public Map getSubstituitionMap() {
        return substMap;
    }

    /**
     * Set the transition.
     * @param transition The transition to set.
     */
    public void setTransition(ITransition transition) {
        this.transition = transition;
    }    
    
    /**
     * Set the substitution map.
     * @param substMap The substMap to set.
     */
    public void setSubstMap(Map substMap) {
        this.substMap = substMap;
    }
}
