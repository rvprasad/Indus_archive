/*
 * IFSABuilder.java
 *
 * Created on November 23, 2004, 5:01 PM
 */

package edu.ksu.cis.indus.peq.fsm;

import gnu.trove.THashSet;

/**
 * This is the interface for building the finite state automata.
 * @author  Ganeshan
 */
public interface IFSABuilder {
   
    /**
     * Sets the number of states to nStateCount. <br>   
     * <b>Precondition:</b> true.<br>
     * <b>Postcondition:</b> The automata is set for nStateCount states.
     * @param nStateCount The number of states in the automata.   
     */
   void setNumberOfStates(final int nStateCount); 
   
   /**
    * Sets the start state of the automata to nStartState. <br>
    * <b>Precondition:</b> Automata.States != 0, nStartState > 0 and nStartState <= Automata.States. <br>
    * <b>Postcondition:</b> The start state is set to nStartState.            
    * @param nStartState The start state of the automata.
    */
   void setStartState(final int nStartState);
   
   /**
    * Returns the start state of the automata. <br>
    * <b>Precondition:</b> Automata.states > 0 and Automata.StartState != -1.<br>
    * <b>Postcondition:</b> Returns the start state of the automata or -1 if no start
    * state has been declared.
    * @return The start state of the automata.
    */ 
   int getStartState();
   
   /**
    * Sets the Final state of the automata to nStartState. <br>
    * <b>Precondition:</b> Automata.States != 0, nFinalState > 0 and nFinalState <= Automata.States. <br>
    * <b>Postcondition:</b> The final state is set to nFinalState.    
    * @param nFinalState The final state of the automata.
    */
   void setFinalState(final int nFinalState);
   
   /**
    * Returns the final state of the automata. <br>
    * <b>Precondition:</b> Automata.states > 0 and Automata.FinalState != -1.<br>
    * <b>Postcondition:</b> Returns the final state of the automata or -1 if no final
    * state has been declared.
    * @return The final state of the automata.
    */ 
   int getFinalState();
   
   
   /**
    * Returns the label applied to the given state, if present. <br>
    * <b>Precondition:</b> nState > 0 and nState <= Automata.States<br>
    * <b>Postcondition:</b> Returns the label applied to the given state.      
    * @param nState The state from which to retreive the label.
    * @return The label applied to the state. Returns NULL if no label is present.
    */
   Object getStateLabel(final int nState);
   
   /**
    * Adds a transition between the specified states. <br>
    * <b>Precondition: </b> nTransStartState, nTransEndState > 0 and nTransStartState, nTransEndState <= Automata.States and
    * objLabelObject != NULL and nTransitionId.isUnique<br>
    * <b>Postcondition:</b> Adds the given transition to the automata.    
    * @param nTransStartState The starting state of the transition.
    * @param nTransEndState The ending state of the transition.
    * @param nTransitionId The id to identify the transition.
    * @param objLabelObject The labeling applied to the transition.
    */
   void addTransition(final int nTransStartState, final int nTransEndState, 
   		final int nTransitionId, final Object objLabelObject);
   

   /**
    * Returns the set of automata edges from the state nState.<br>
    * <b>Precondition:</b> nState > 0 and nState <= Automata.States.
    * <b>Postcondition:</b> THashSet.elements.oclIsKindOf(AutomataEdge).
    * @param nState The state from which the transitions are to be obtained.
    * @return The set of all automata edges from the state.
    */
   THashSet getTransitionsFromState(int nState);
}
