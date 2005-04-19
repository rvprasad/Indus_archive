/*
 * IRegexBuilder.java
 *
 * Created on April 17, 2005, 2:31 PM
 */

package edu.ksu.cis.automata.interfaces;

/**
 * This is the interface for builders that perform regex based fsm creation.
 * @author ganeshan
 */
public interface IRegexBuilder extends IAtmBuilder {
    
   
     /**
     * Create a normal transition based on the given symbol from the current state.
     * @param symbol The symbol for the transtion.
     * @param currState The state from which to create the transtion.
     * @return IState The final state of the transition.
     */
    IState createNormal(final ISymbol symbol, final IState currState);
    
    /**
     * Create a kleene transition based on the given symbol from the current state.
     * @param symbol The symbol for the transtion.
     * @param currState The state from which to create the transtion.
     * @return IState The final state of the transition.
     */
    IState createKleene(final ISymbol symbol, final IState currState);
    
    /**
     * Create a zero or one transition based on the given symbol from the current state.
     * @param symbol The symbol for the transtion.
     * @param currState The state from which to create the transtion.
     * @return IState The final state of the transition.
     */
    IState createZeroOrOne(final ISymbol symbol, final IState currState);
    
    /**
     * Create a one or more transition based on the given symbol from the current state.
     * @param symbol The symbol for the transtion.
     * @param currState The state from which to create the transtion.
     * @return IState The final state of the transition.
     */
    IState createOneOrMore(final ISymbol symbol, final IState currState);
    
    /**
     * Create an alternation transition based on the given symbols from the current state.
     * @param symbolOne The first symbol.
     * @param symbolTwo The second symbol.
     * @param currState The state from which to create the transtion.
     * @return IState The final state of the transition.
     */
    IState createAlternation(final ISymbol symbolOne, final ISymbol symbolTwo, final IState currState);
}
