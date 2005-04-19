/*
 * IAutomata.java
 * Interface for the finite automata.
 * Created on April 16, 2005, 10:42 PM
 */

package edu.ksu.cis.automata.interfaces;

import java.util.Collection;
import java.util.Set;

/**
 * Represents the interface for the finite automata.
 * @author ganeshan
 * @version 0.1
 */
public interface IAutomata extends Cloneable {
    
    
    /**
     * Returns the initial state of the automata.
     * @return IState The initial state of the automata.     
     */
    IState getInitialState();
    
    /**
     * Returns the collection of final states.
     * @return Collection The collection of final automata states
     * @post Result.oclIsKindOf(Collection(IState))
     */
    Collection getFinalStates();
    
    /**
     * Indicates if the automata is in the final state.
     * @return boolean Indicates if the automata is in a final state.
     */
    public boolean isInFinalState();
    
    /**
     * Indicates if the automata can perform a transition on the given symbol in 
     * the current state.
     * @param sym The symbol to perform the transition on.
     * @return Set The set of possible transitions that can be performed.
     * @post Result.oclIsKindOf(Collection(ITransition)) and ITransition.match(sym) = true
     */
    Set canPerformTransition(ISymbol sym);
    
    /**
     * Performs a transition on the given symbol.     
     * @param transition The transition to take from the current state.
     * @return boolean Indicates if the automata was successful in performing the transition.
     */
    boolean performTransition(ITransition transition);
    
    /**
     * Clones the automata.
     * @return Object The cloned object.
     */
    Object clone();
    
 }
