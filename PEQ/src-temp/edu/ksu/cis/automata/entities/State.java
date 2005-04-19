/*
 * State.java
 *
 * Created on April 17, 2005, 12:30 PM
 */

package edu.ksu.cis.automata.entities;

import edu.ksu.cis.automata.interfaces.IState;
import edu.ksu.cis.automata.interfaces.ITransition;
import java.util.HashSet;
import java.util.Set;

/**
 * The concrete state implementation.
 * @author ganeshan
 */
public class State implements IState {
    /**
     * The set of entering transitions.
     * @inv enteringTransitions.oclIsKindOf(Collection(ITransition)) and 
     * ITransition.getDstnState() = this.
     */
    private Set enteringTransitions;
    
    /**
     * The set of exiting transitions.
     * @inv exitingTransitions.oclIsKindOf(Collection(ITransition)) and 
     * ITransition.getSourceState() = this.
     */
    private Set exitingTransitions;
    
    /**
     * The information associated with the state.
     */
    private Object stateInfo;
    
    /**
     * Indicates if this is a final state.
     */
    private boolean bFinalState;
    
    /** Creates a new instance of State */
    public State() {
        enteringTransitions = new HashSet();
        exitingTransitions = new HashSet();
    }

    /**
     * Returns a string representation of the state.
     * 
     * @return  a string representation of the object.
     */
    public String toString() {

        String retValue;
        
        retValue = "<State id=" + hashCode() + ">\n";
        retValue += "<EnteringTransitions>" + enteringTransitions.size() + "</EnteringTransitions>\n";
        retValue += "<ExitingTransitions>" + exitingTransitions.size() + "</ExitingTransitions>\n";
        retValue += "<stateInfo>" + stateInfo + "</stateInfo>\n";
        retValue += "<isFinal>" + bFinalState + "</isFinal>";
        retValue += "</State>\n";
        
        return retValue;
    }

    /**
     * Returns the set of exiting transitions.
     * @return Set The set of exiting transitions.
     * @post Set.oclIsKindOf(Collection(ITransition)) and ITransition.getSourceState() = this.
     */
    public Set getExitingTransitions() {
        return exitingTransitions;
    }

    /**
     * Returns the set of entering transitions.
     * @return Set The set of entering transitions.
     * @post Set.oclIsKindOf(Collection(ITransition)) and ITransition.getDstnState() = this.
     */
    public Set getEnteringTransitions() {
        return enteringTransitions;
    }

    /**
     * Returns the information associated with this state.
     * @return Object The object associated with the state.
     */
    public Object getStateInfo() {
        return stateInfo;
    }

    /**
     * Add the given transition as an entering transition on the state.
     * @param transition The entering transition.    
     * @pre transition.getDstnState() = this.
     */
    public void addEnteringTransition(final ITransition transition) {
        enteringTransitions.add(transition);
    }

    /**
     * Add the given transition as an exiting transition on the state.
     * @param transition The exiting transition.    
     * @pre transition.getSourceState() = this.
     */
    public void addExitingTransition(final ITransition transition) {
        exitingTransitions.add(transition);
    }

    /**
     * Sets the state information to stateInfo.
     * @param stateInfo The state information.
     */
    public void setStateInfo(Object stateInfo) {
        this.stateInfo = stateInfo;
    }

    /**
     * Set the state to a final state.
     * @param bFinal Indicates whether this state is a final state.
     */
    public void setFinalState(final boolean bFinal) {
        bFinalState = bFinal;
    }
    
    /**
     * Indicates if this state is a final state.
     * @return boolean Final state indication.
     */
    public boolean isFinalState() {
        return bFinalState;
    }
    
}
