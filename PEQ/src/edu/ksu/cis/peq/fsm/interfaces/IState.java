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
 * Created on March 8, 2005, 6:49 PM
 */

package edu.ksu.cis.peq.fsm.interfaces;

import java.util.Set;

/**
 * @author ganeshan
 * This is the interface for the fsm state.
 *
 */
public interface IState {

    /**
     * Returns the set of incoming transitions.
     * @post Result.oclIsKindOf(Set(ITransition)) and ITransition.srcState = this
     */
    Set getEnteringTransitions();
    
    /**
     * Returns the set of outgoing transitions.
     * @post Result.oclIsKindOf(Set(ITransition)) and ITransition.dstnState = this
     */
    Set getExitingTransitions();
    
    /**
     * Indicates if the state is the final state.     
     * @post Result = true if the state if final, false otherwise
     */
    boolean isFinalState();
}

