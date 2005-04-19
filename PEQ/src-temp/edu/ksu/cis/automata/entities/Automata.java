/*
 * Automata.java
 *
 * Created on April 17, 2005, 3:39 PM
 */

package edu.ksu.cis.automata.entities;

import edu.ksu.cis.automata.interfaces.IAutomata;
import edu.ksu.cis.automata.interfaces.IState;
import edu.ksu.cis.automata.interfaces.ISymbol;
import edu.ksu.cis.automata.interfaces.ITransition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The concerete automata implementation.
 * @author ganeshan
 */
public class Automata implements IAutomata {
    
    /**
     * The collection of final states.
     */
    private Collection finalStateCollection;
    
    /**
     * The initial state of the automata.
     */
    private IState initialState;
    
    /**
     * The current state of the automata.
     */ 
    private IState currentState;
    
    /** Creates a new instance of Automata.*/
    public Automata() {
        finalStateCollection = new ArrayList();
    }

    /**
     * Set the initial state of the automata.
     * @param initialState The initial state of the automata.
     */
    public void setInitialState(final IState initialState) {
        this.initialState = initialState;
        currentState = initialState;
    }
    
    /**
     * Performs a transition on the given symbol.     
     * @param transition The transition to take from the current state.
     * @return boolean Indicates if the automata was successful in performing the transition.
     */
    public boolean performTransition(ITransition transition) {
        boolean result = false;
        if (currentState != null) {
            final Set transSet = currentState.getExitingTransitions();
            for (final Iterator iter = transSet.iterator(); iter.hasNext();) {
                final ITransition trans = (ITransition) iter.next();
                if (trans.equals(transition)) {
                    currentState = trans.getDstnState();
                    result = true;
                    break;                   
                }
            }
        }
        return result;
    }

    /**
     * Indicates if the automata can perform a transition on the given symbol in 
     * the current state.
     * @param sym The symbol to perform the transition on.
     * @return Set The collection of transitions that can be performed.
     * @post Result.oclIsKindOf(Collection(ITransition)) and ITransition.match(sym) = true
     */
    public Set canPerformTransition(ISymbol sym) {
        final Set resultSet = new HashSet();
        
        if (currentState != null) {
            final Set transSet = currentState.getExitingTransitions();
            for (final Iterator iter = transSet.iterator(); iter.hasNext();) {
                final ITransition trans = (ITransition) iter.next();
                if(trans.getSymbol().match(sym)) {
                    resultSet.add(trans);
                }
            }
        }
        return resultSet;
    }

    /**
     * Indicates if the automata is in the final state.
     * @return boolean Indicates if the automata is in a final state.
     */
    public boolean isInFinalState() {
        boolean result = false;
        if (currentState != null) {
            result = currentState.isFinalState();
        }
        return result;
    }

    /**
     * Returns the initial state of the automata.
     * @return IState The initial state of the automata.     
     */
    public IState getInitialState() {
        return initialState;
    }

    /**
     * Returns the collection of final states.
     * @return Collection The collection of final automata states
     * @pre collectFinalStates has been called.
     * @post Result.oclIsKindOf(Collection(IState))
     */
    public Collection getFinalStates() {
        return finalStateCollection;
    }

    /**
     * Clones the automata.
     * @return Object The cloned object.
     */
    public Object clone()  {
        Object retValue = null;  
        try {
            retValue = super.clone();
        } catch(CloneNotSupportedException cnse) {
            cnse.printStackTrace();
        }
        return retValue;
    }    
    
    /**
     * Collects the final states of automata.
     * @pre initialState != null.
     * @post finalStateCollection.oclIsKindOf(Collection(IState)) and 
     * IState.isFinalState()  = true.
     */ 
    public void collectFinalStates() {
        final Set reachSet = new HashSet();
        final List _workList = new LinkedList();
        _workList.add(initialState);
        
        while (_workList.size() > 0) {
            final IState state = (IState) _workList.remove(0);
            if (!reachSet.contains(state)) {
                reachSet.add(state);
                if (state.isFinalState()) {
                    finalStateCollection.add(state);
                }
                final Set transSet = state.getExitingTransitions();
                for (final Iterator iter = transSet.iterator(); iter.hasNext();) {
                    final ITransition it = (ITransition) iter.next();
                    _workList.add(it.getDstnState());                    
                }
            }
        }
        
    }
}
