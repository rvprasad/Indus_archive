/*
 * EpsRemovalTransformer.java
 *
 * Created on April 17, 2005, 12:13 PM
 */
 
package edu.ksu.cis.automata.helpers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.ksu.cis.automata.interfaces.IAutomata;
import edu.ksu.cis.automata.interfaces.IState;
import edu.ksu.cis.automata.interfaces.ITransition;

/**
 * 
 * Computes the epsilon closures for the states in the given
 * automata.
 * @author ganeshan
 *
 */
public class EpsClosureCalculator {

    /**
     * The map of automata states to eclosures.
     *  @inv Map.keys.oclIsKindOf(IState) and Map.value.oclIsKindOf(Collection(IState)).
     */
    private Map sToEclsMap;
    
    /**
     * The source automata.
     */
    private IAutomata srcAutomata;
    
    /**
     * Construct the calculator instance.
     * @param atm The source automata.
     * @pre atm != null.
     */
    public EpsClosureCalculator(final IAutomata atm) {
        srcAutomata = atm;
        sToEclsMap = new HashMap();
    }
    
    /**
     * Process the automata to get the eclosure map.
     *
     */
    public void process() {
        final IState initState = srcAutomata.getInitialState();
        calculateEclosure(initState, new HashSet());
    }
    
    
    /**
     * Calculate the reach set
     * @param state The current state.
     * @param reachSet The reach set.
     */
    private void calculateEclosure(final IState state, final Set reachSet) {
        if (reachSet.contains(state)) {
			return;
		}
		// Calculate eclosure
    	calEclosureForState(state);
		reachSet.add(state);
		final Set transSet = state.getExitingTransitions();
		for (Iterator iter = transSet.iterator(); iter.hasNext();) {
			final ITransition trans = (ITransition) iter.next();
			calculateEclosure(trans.getDstnState(), reachSet);
		}
        
    }

    /**
     * Calculate the eclosure for the specified state.
     * @param state The state to calculate the eclosure for.
     * @return Set The set of eclosed states for the given state
     */
    private Set calEclosureForState(IState state) {
		if (sToEclsMap.containsKey(state)) {
			return (Set) sToEclsMap.get(state);
		}
		final Set retSet = new HashSet();
		retSet.add(state);
		final Set transitionSet = state.getExitingTransitions();
		for (Iterator iter = transitionSet.iterator(); iter.hasNext();) {
			final ITransition t = (ITransition) iter.next();
			if (t.isEpsTransition()) {				
				retSet.addAll(calEclosureForState(t.getDstnState()));				
			}
		}
		
		sToEclsMap.put(state, retSet);
		
		return retSet;
	}
    
    /**
     * Get the eclosure map.
     * @return Map The eclosure map.
     * @post Map.keys.oclIsKindOf(IState) and Map.value.oclIsKindOf(Collection(IState)).
     */
    public Map getResults() {
       return sToEclsMap; 
    }
}
