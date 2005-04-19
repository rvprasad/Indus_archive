/*
 * ITransition.java
 *
 * Created on April 16, 2005, 10:44 PM
 */

package edu.ksu.cis.automata.interfaces;

/**
 * Interface definition for the automata transition.
 * @author ganeshan
 */
public interface ITransition {
    /**
     * Returns the source state of the transition.
     * @return IState The source state of this transition.
     */
    IState getSourceState();
    
    /**
     * Returns the destination state of the transition.
     * @return IState The destination state of this transition.
     */
    IState getDstnState();
    
    /**
     * Returns the symbol associated with this transition.
     * @return ISymbol The symbol on which the transition will be followed.
     */
    ISymbol getSymbol();
    
    /**
     * Indicates if the given transition is an epsilon transition.
     */
    boolean isEpsTransition();
}
