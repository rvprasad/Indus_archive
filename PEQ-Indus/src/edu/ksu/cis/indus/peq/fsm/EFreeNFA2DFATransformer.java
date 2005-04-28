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

import edu.ksu.cis.peq.constructor.interfaces.IConstructor;
import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.fsm.interfaces.IState;
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
    private IFSM srcAutomata;
    
    /**
     * The dfa automata for the source automata.
     * 
     */
    private IFSM dfaAutomata;
    
    
    /**
     * The map of sets of old states to new automata states.
     * @inv stateSet2NewStateMap.keys.oclIsKindOf(Set(State)) and 
     * stateSet2NewStateMap.values.oclIsKindOf(State)
     */
    final Map stateSet2NewStateMap = new HashMap();
    
    /**
     * The new initial state.
     */
    private State newInitState;
    
    /**
     * Constructor.
     * @param src
     * @pre src is an epsilon free nfa and src != null.
     * 
     */
    public EFreeNFA2DFATransformer(final IFSM src) {
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
       final Set _initSet = new HashSet();
       _initSet.add(state);       
       processForDFA(_initSet, reachSet, stateSet2NewStateMap);
       newInitState = (State) stateSet2NewStateMap.get(_initSet);
    }
    
    /**
     * Process the given state.
     * @param initSet The set of joined states.
     * @param reachSet
     * @param stateSet2NewStateMap The map of states sets to new states.
     */
    private void processForDFA(final Set initSet, Set reachSet, final Map stateSet2NewStateMap) {
        if (!reachSet.contains(initSet)) {
            reachSet.add(initSet);
            final Map label2StateSetMap = new HashMap();
            for (Iterator iter = initSet.iterator(); iter.hasNext();) {
				final State _state = (State) iter.next();
				final Set _transSet = _state.getExitingTransitions();
				for (Iterator iterator = _transSet.iterator(); iterator
						.hasNext();) {
					final ITransition _trans = (ITransition) iterator.next();
					final IConstructor _label = _trans.getLabel();
					Set _l2SSet = (Set) label2StateSetMap.get(_label);
					if (_l2SSet == null) {
						_l2SSet = new HashSet();
						label2StateSetMap.put(_label, _l2SSet);
					} 
					_l2SSet.add(_trans.getDstnState());
				}
			}            
            final Set _entrySet = label2StateSetMap.entrySet();
            State _srcState = (State) stateSet2NewStateMap.get(initSet);
            if (_srcState == null) {
            	_srcState = new State();
            	stateSet2NewStateMap.put(initSet, _srcState);
            }
            for (Iterator iter = _entrySet.iterator(); iter.hasNext();) {
            	final Map.Entry _entry = (Map.Entry) iter.next();
            	final IConstructor _c = (IConstructor) _entry.getKey();
            	final Set _stateSet = (Set) _entry.getValue();
            	State _dstnState = (State) stateSet2NewStateMap.get(_stateSet);
                if (_dstnState == null) {
                	_dstnState = new State();
                	stateSet2NewStateMap.put(_stateSet, _dstnState);
                }	
                final Transition _t = new Transition();
                _t.setLabel(_c);
                _t.setSrcState(_srcState);
                _t.setDstnState(_dstnState);
                _srcState.addExitingTransitions(_t);
                _dstnState.addEnteringTransitions(_t);
                System.out.print("Creating transition from " + _srcState.hashCode() + " : " + _dstnState.hashCode());
                System.out.println(" with label " + _t.getLabel().getClass().toString());
                processForDFA(_stateSet, reachSet, stateSet2NewStateMap);
			}
            
        }
        
    }

    /**
     * Perform any post processing. 
     * @pre preprocess() has been called.
     */
    private void postprocess() {
       markFinalStates();        
    }


    /**
	 * Mark the final states.	
	 */
	private void markFinalStates() {
		final Set _entrySet = stateSet2NewStateMap.entrySet();
		for (Iterator iter = _entrySet.iterator(); iter.hasNext();) {
			final Map.Entry _entry = (Map.Entry) iter.next();
			final Set _stateSet = (Set) _entry.getKey();
			final State _theState = (State) _entry.getValue();
			for (Iterator iterator = _stateSet.iterator(); iterator.hasNext();) {				
				final IState _state = (IState) iterator.next();
				if (_state.isFinalState()) {
					_theState.setFinal(true);
					break;
				}
			}
			
		}
		
	}

	/**
     * Returns the new dfa automata.
     * @return Returns the dfaAutomata.
     * @pre process() has been called on this instance.
     * @post IFSM The dfa automata.
     */
    public IFSM getDfaAutomata() {
    	final BasicFSM _bfm = new BasicFSM();
    	_bfm.setInitialState(newInitState);
        return _bfm;
    }
}
