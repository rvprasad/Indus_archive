/*
 * PEQ, a parameteric regular path query library
 * Copyright (c) 2005 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 *
 * Created on March 8, 2005, 6:45 PM
 */
 
package edu.ksu.cis.indus.peq.fsm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.ksu.cis.automata.interfaces.IAutomata;
import edu.ksu.cis.automata.interfaces.IState;
import edu.ksu.cis.peq.constructor.interfaces.IConstructor;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;

/**
 * @author ganeshan
 *
 * Converts an epsilon free nfa to a dfa.
 */
public class EFreeNFA2DFATransformer {
    /**
     * The source automata.
     */
    private IAutomata srcAutomata;
    
    /**
     * The dfa automata for the source automata.
     * 
     */
    private IAutomata dfaAutomata;
    
    /**
     * Constructor.
     * @param src
     * @pre src is an epsilon free nfa and src != null.
     * 
     */
    public EFreeNFA2DFATransformer(final IAutomata src) {
        srcAutomata = src;
    }
    
    /**
     * Convert the nfa automata to dfa. 
     *
     */
    public void process() {
        preprocess();
        postprocess();
    }
    
    
    
    /**
     * Perform any pre processing.
     */
    private void preprocess() {
       final Set reachSet = new HashSet();
       final IState state = srcAutomata.getInitialState();
       processForDFA(state, reachSet, new HashMap());
    }
    
    /**
     * Process the given state.
     * @param state
     * @param reachSet
     * @param stateSet2NewStateMap The map of states sets to new states.
     */
    private void processForDFA(IState state, Set reachSet, final Map stateSet2NewStateMap) {
        if (!reachSet.contains(state)) {
            reachSet.add(state);
            final Set transSet = state.getExitingTransitions();
            final Set symSet = new HashSet();
            for (Iterator iter = transSet.iterator(); iter.hasNext();) {
              final ITransition trans = (ITransition) iter.next();
              symSet.add(trans.getLabel());                
            }
            final Map symToStateMap = new HashMap();
            for (Iterator iter = symSet.iterator(); iter.hasNext();) {
                final IConstructor _c = (IConstructor) iter.next();
                for (final Iterator iterator = transSet.iterator(); iterator.hasNext();) {
                    final ITransition trans = (ITransition) iterator.next();
                    if (trans.getLabel().equals(_c)) {
                        Set _s = (Set) symToStateMap.get(_c);
                        if (_s == null) {
                            _s = new HashSet();
                            symToStateMap.put(_c, _s);
                        }
                        _s.add(trans.getDstnState());
                    }
                }                
            } 
            // TODO Add the state creation logic.
        }
        
    }

    /**
     * Perform any post processing. 
     */
    private void postprocess() {
       
        
    }


    /**
     * Returns the new dfa automata.
     * @return Returns the dfaAutomata.
     * @pre process() has been called on this instance.
     * @post IAutomata The dfa automata for the given automata.
     */
    public IAutomata getDfaAutomata() {
        return dfaAutomata;
    }
}
