/*
 * IState.java
 *
 * Created on April 16, 2005, 10:44 PM
 */

package edu.ksu.cis.automata.interfaces;

import java.util.Set;

/**
 * Interface definition for the automata state.
 * @author ganeshan
 */
public interface IState {
    
    /**
     * Returns the set of entering transitions.
     * @return Set The set of entering transitions.
     * @post Set.oclIsKindOf(Collection(ITransition)) and ITransition.getDstnState() = this.
     */
    Set getEnteringTransitions();
    
    /**
     * Returns the set of exiting transitions.
     * @return Set The set of exiting transitions.
     * @post Set.oclIsKindOf(Collection(ITransition)) and ITransition.getSourceState() = this.
     */
    Set getExitingTransitions();
    
    /**
     * Returns the information associated with this state.
     * @return Object The object associated with the state.
     */
    Object getStateInfo();
    
    /**
     * Indicates if this state is a final state.
     * @return boolean Final state indication.
     */
    boolean isFinalState();
}
