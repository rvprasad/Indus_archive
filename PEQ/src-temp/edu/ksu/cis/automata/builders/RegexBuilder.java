/*
 * RegexBuilder.java
 *
 * Created on April 17, 2005, 3:33 PM
 */

package edu.ksu.cis.automata.builders;

import edu.ksu.cis.automata.entities.Automata;
import edu.ksu.cis.automata.entities.State;
import edu.ksu.cis.automata.entities.Transition;
import edu.ksu.cis.automata.interfaces.IAutomata;
import edu.ksu.cis.automata.interfaces.IRegexBuilder;
import edu.ksu.cis.automata.interfaces.IState;
import edu.ksu.cis.automata.interfaces.ISymbol;

/**
 * The concrete regex builder.
 * @author ganeshan
 */
public class RegexBuilder implements IRegexBuilder {
    /** The initial state */
    private IState initialState;
    
    /** Creates a new instance of RegexBuilder */
    public RegexBuilder() {
    }

    /**
     * Create a zero or one transition based on the given symbol from the current state.
     * @param symbol The symbol for the transtion.
     * @param currState The state from which to create the transtion.
     * @return IState The final state of the transition.
     */
    public IState createZeroOrOne(ISymbol symbol, IState currState) {
        final State dState = new State();
        final Transition ep = new Transition();
        final Transition t = new Transition();
        ep.setEpsTransition(true);
        // transition
        t.setSymbol(symbol);
        t.setSrcState(currState);
        t.setDstnState(dState);
        ((State) currState).addExitingTransition(t);
        dState.addEnteringTransition(t);
        // epsilon
        ep.setSrcState(currState);
        ep.setDstnState(dState);
        ((State) currState).addExitingTransition(ep);
        dState.addEnteringTransition(ep);        
        return dState;
    }

    /**
     * Create a one or more transition based on the given symbol from the current state.
     * @param symbol The symbol for the transtion.
     * @param currState The state from which to create the transtion.
     * @return IState The final state of the transition.
     */
    public IState createOneOrMore(ISymbol symbol, IState currState) {
        IState newState = null;
        newState = createNormal(symbol, currState);
        newState = createKleene(symbol, newState); 
        return newState;
    }

    /**
     * Create a normal transition based on the given symbol from the current state.
     * @param symbol The symbol for the transtion.
     * @param currState The state from which to create the transtion.
     * @return IState The final state of the transition.
     */
    public IState createNormal(ISymbol symbol, IState currState) {
        final Transition t = new Transition();
    	t.setSymbol(symbol);
        t.setSrcState(currState);
        final State newState = new State();
        t.setDstnState(newState);
        ((State) currState).addExitingTransition(t);
        ((State) newState).addEnteringTransition(t);
        
        return newState;
    }

    /**
     * Create a kleene transition based on the given symbol from the current state.
     * @param symbol The symbol for the transtion.
     * @param currState The state from which to create the transtion.
     * @return IState The final state of the transition.
     */
    public IState createKleene(ISymbol symbol, IState currState) {
        final State s1 = new State();
    	final State s2 = new State();
    	final State dState = new State();
    	
    	final Transition t = new Transition();
    	t.setSymbol(symbol);
    	// Main Transition
    	t.setSrcState(s1);
        t.setDstnState(s2);
        s1.addExitingTransition(t);
        s2.addEnteringTransition(t);
        final Transition ep1 = new Transition();
        ep1.setEpsTransition(true);
        final Transition ep2 = new Transition();
        ep2.setEpsTransition(true);
        final Transition ep3 = new Transition();
        ep3.setEpsTransition(true);
        final Transition ep4 = new Transition();
        ep4.setEpsTransition(true);
        // 1
        ep1.setSrcState(currState);
        ep1.setDstnState(s1);
        ((State) currState).addExitingTransition(ep1);
        s1.addEnteringTransition(ep1);
        // 2
        ep2.setSrcState(s2);
        ep2.setDstnState(s1);
        s1.addEnteringTransition(ep2);
        s2.addExitingTransition(ep2);        
        // 3
        ep3.setSrcState(s2);
        ep3.setDstnState(dState);
        s2.addExitingTransition(ep3);
        dState.addEnteringTransition(ep3);        
        // 4
        ep4.setSrcState(currState);
        ep4.setDstnState(dState);
        ((State) currState).addExitingTransition(ep4);
       dState.addEnteringTransition(ep4);       
       return dState;
    }

    /**
     * Create an alternation transition based on the given symbols from the current state.
     * @param symbolOne The first symbol.
     * @param symbolTwo The second symbol.
     * @param currState The state from which to create the transtion.
     * @return IState The final state of the transition.
     */
    public IState createAlternation(ISymbol symbolOne, ISymbol symbolTwo, IState currState) {
        final State newState = new State();
        final Transition t1 = new Transition();
        final Transition t2 = new Transition();
        
        t1.setSrcState(currState);
        t1.setDstnState(newState);
        t1.setSymbol(symbolOne);
        ((State) currState).addExitingTransition(t1);
        newState.addEnteringTransition(t1);
        
        
        t2.setSrcState(currState);
        t2.setDstnState(newState);
        t2.setSymbol(symbolTwo);
        ((State) currState).addExitingTransition(t2);
        newState.addEnteringTransition(t2);
        
        return newState;
    }

    /**
     * Finalize the automata creation.
     * @return IAutomata The final automata.
     */
    public IAutomata finalizeAtm() {
        final Automata atm = new Automata();
        atm.setInitialState(initialState);
        atm.collectFinalStates();
        return atm;
    }

    /**
     * Create and return the initial state of the automata.
     * @return IState The initial state of the automata.
     */
    public IState initialize() {
        initialState = new State();
        return initialState;
    }

}
