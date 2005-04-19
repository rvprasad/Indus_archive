/*
 * INormalBuilder.java
 *
 * Created on April 17, 2005, 3:22 PM
 */

package edu.ksu.cis.automata.interfaces;

/**
 * Builder interface for a custom fsm construction.
 * @author ganeshan
 */
public interface INormalBuilder extends IAtmBuilder{
        
    /**
     * Creates a transition from given state on the given symbol.
     * @param srcState The source state of the transition.
     * @param symbol The symbol accepted by the transition.
     * @return IState The destination state.
     */
    IState createTransition(final IState srcState, final ISymbol symbol);
    
    /**
     * Creates a transition between the given states on the given symbol.
     * @param srcState The source state of the transition.
     * @param dstnState The destination state of the transition.
     * @param symbol The symbol accepted by the transition.
     * @return IState The destination state.
     */
    IState createTransition(final IState srcState, final IState dstnState, final ISymbol symbol);
}
