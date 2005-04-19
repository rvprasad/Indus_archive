/*
 * EpsRemovalTransformer.java
 *
 * Created on April 17, 2005, 12:13 PM
 */

package edu.ksu.cis.automata.transformers;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.ksu.cis.automata.entities.Automata;
import edu.ksu.cis.automata.entities.State;
import edu.ksu.cis.automata.entities.Transition;
import edu.ksu.cis.automata.helpers.EpsClosureCalculator;
import edu.ksu.cis.automata.interfaces.IAutomata;
import edu.ksu.cis.automata.interfaces.IState;
import edu.ksu.cis.automata.interfaces.ITransition;

/**
 * Removes the epsilon transitions from the given NFA and returns the new NFA.
 * The original NFA is unmodified.
 * Note that this does operation does not copy the state information into the new states due to the state subset 
 * collapse performed.
 * @author ganeshan
 */
public class EpsRemovalTransformer {
    
    /**
     * The input automata instance.
     */
    private IAutomata inpAutomata;
    
    /**
     * The new initial state.
     */
    private IState newInitialState;
    
    /**
     * The output automata instance.
     */
    private IAutomata opAutomata;
    
    /** 
     * Creates a new instance of EpsRemovalTransformer. 
     * @param automata The input epsilon NFA.
     */
    public EpsRemovalTransformer(final IAutomata automata) {
        inpAutomata = automata;
    }
    
    /**
     * Perform the conversion to remove the epsilon transitions.
     * @pre inpAutomata != null
     * @post opAutomata != null
     */
    public void process() {
        final Map sToEMap = preCompute(); // Retreive the eclosure for the old automata.
        final Map sToEcMap = new HashMap(); // The map of eclosures of old states to their new states.
        createEpsFreeAutm(sToEcMap, inpAutomata.getInitialState(), new HashSet(), sToEMap);
        final IState oldInitialState = inpAutomata.getInitialState();
        final Set ecSet = (Set) sToEMap.get(oldInitialState);
        newInitialState = (IState) sToEcMap.get(ecSet);
        postCompute(sToEcMap);
    }
    
    /**
     * Perform any pre computations.
     */
    protected Map preCompute() {
        EpsClosureCalculator ecc = new EpsClosureCalculator(inpAutomata);
        ecc.process();
        final Map sToEcMap = ecc.getResults();
        return sToEcMap;
    }
    

    
    /**
     * Creates an epsilon free automata.
     * @param sToEcMap The map of epsilon closures to new states.
     * @param state The current state.
     * @param reachset The reach set of old states
     * @param sToEMap The map of old states to their epsilon closures.
     *  
     */
    private void createEpsFreeAutm(Map sToEcMap, IState state,
            final Set reachset, final Map sToEMap) {
        if (!reachset.contains(state)) {
            reachset.add(state);
            State newState = null;
            final Set eSet = (Set) sToEMap.get(state);
            if (sToEcMap.containsKey(eSet)) {
                newState = (State) sToEcMap.get(eSet);
            } else {
                newState = new State();
                sToEcMap.put(eSet, newState);
            }

            for (final Iterator it = eSet.iterator(); it.hasNext();) {
                State eState = (State) it.next();
                final Set transSet = eState.getExitingTransitions();
                for (Iterator iter = transSet.iterator(); iter.hasNext();) {
                    final ITransition trans = (ITransition) iter.next();
                    if (!(trans.isEpsTransition())) {
                        State theState = null;
                        final State dstnState = (State) trans.getDstnState();
                        final Set dEset = (Set) sToEMap.get(dstnState);
                        final Object _rObj = sToEcMap.get(dEset);
                        if (_rObj == null) {
                            theState = new State();
                            sToEcMap.put(dEset, theState);
                        } else {
                            theState = (State) _rObj;
                        }
                        final Transition t = new Transition();
                        t.setSrcState(newState);
                        t.setDstnState(theState);
                        t.setSymbol(trans.getSymbol());
                        newState.addExitingTransition(t);
                        theState.addEnteringTransition(t);
                        createEpsFreeAutm(sToEcMap, dstnState, reachset, sToEMap);
                    }

                }
            }
        }
    }
    
    /**
     * Perform any post computations.
     * @param sToEcMap The map of eclosures of old states to the new states.
     * @pre sToEcMap.keys.oclIsKindOf(Set(IState)) and sToEcMap.values.oclIsKindOf(IState)
     */
    protected void postCompute(final Map sToEcMap) {
        computeFinalStates(sToEcMap);
        opAutomata = new Automata();
        ((Automata) opAutomata).setInitialState(newInitialState);
        ((Automata) opAutomata).collectFinalStates();
    }
    
    /**
	 * Mark the final states.
	 * @param epsMap The eps closure set to new state map.
	 */
	private void computeFinalStates(final Map epsMap) {		
		final Set entrySet = epsMap.entrySet();
		for (Iterator iter = entrySet.iterator(); iter.hasNext();) {
            final Map.Entry entry = (Map.Entry) iter.next();
            final Set epsSet = (Set) entry.getKey();
            final State state = (State) entry.getValue();
            for (Iterator iterator = epsSet.iterator(); iterator.hasNext();) {
                final State oldState = (State) iterator.next();
                if (oldState.isFinalState()) {
                    state.setFinalState(true);
                    break;
                }
                
            }
        }
	}
    
    /**
     * Returns the processed epsilon free automata.
     * @return IAutomata The epsilon free automata.
     */
    public IAutomata getResult() {
        return opAutomata;
    }
}
