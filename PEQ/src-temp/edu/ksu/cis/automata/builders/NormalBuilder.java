/*
 * NormalBuilder.java
 *
 * Created on April 17, 2005, 5:17 PM
 */

package edu.ksu.cis.automata.builders;

import edu.ksu.cis.automata.entities.Automata;
import edu.ksu.cis.automata.entities.State;
import edu.ksu.cis.automata.entities.Transition;
import edu.ksu.cis.automata.interfaces.IAutomata;
import edu.ksu.cis.automata.interfaces.INormalBuilder;
import edu.ksu.cis.automata.interfaces.IState;
import edu.ksu.cis.automata.interfaces.ISymbol;

/**
 * The normal fsm builder class.
 * @author ganeshan
 */
public class NormalBuilder implements INormalBuilder {
    
    /** The initial state */
    private IState initialState;
    
    /** Creates a new instance of NormalBuilder */
    public NormalBuilder() {
    }

    /**
     * Creates a transition between the given states on the given symbol.
     * @param srcState The source state of the transition.
     * @param dstnState The destination state of the transition.
     * @param symbol The symbol accepted by the transition.
     * @return IState The destination state.
     */
    public IState createTransition(IState srcState, IState dstnState, ISymbol symbol) {
        final Transition trans = new Transition();
        trans.setSrcState(srcState);
        trans.setDstnState(dstnState);
        trans.setSymbol(symbol);
        ((State) srcState).addExitingTransition(trans);
        ((State) dstnState).addEnteringTransition(trans);
        return dstnState;
    }

    /**
     * Creates a transition from given state on the given symbol.
     * @param srcState The source state of the transition.
     * @param symbol The symbol accepted by the transition.
     * @return IState The destination state.
     */
    public IState createTransition(IState srcState, ISymbol symbol) {
        final State newState = new State();
        final Transition trans = new Transition();
        trans.setSrcState(srcState);
        trans.setDstnState(newState);
        ((State) srcState).addExitingTransition(trans);
        newState.addEnteringTransition(trans);
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
