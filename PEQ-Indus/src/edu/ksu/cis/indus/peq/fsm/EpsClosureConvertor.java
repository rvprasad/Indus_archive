/*
 *
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */
 
package edu.ksu.cis.indus.peq.fsm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.fsm.interfaces.IState;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;

/**
 * @author ganeshan
 *
 * This class is responsible for converting a NFA with epsilon 
 * transitions to one without.
 */
public class EpsClosureConvertor {
    /** 
     * The fsm instance.
     */
    private IFSM fsm;
    
    /** The initial state of the new fsm */
    private State initialState; 
    /**
     * Constructor.
     * @param fsm
     */
    public EpsClosureConvertor(final IFSM fsm) {
     this.fsm = fsm;   
    }
    
    public void processShallow() {
        final Map _sToEcMap = new HashMap();
        final IState _initState = fsm.getInitialState();
        createEpsFreeAutm(_sToEcMap, _initState, new HashSet());
        initialState = (State) _sToEcMap.get(((State) _initState).getEclosure());         
        postCompute(_sToEcMap);
    }
    
    /**
     * Process the fsm to remove epsilon transitions.
     *
     */
    public void process() {
        preCompute();        
        final Map _sToEcMap = new HashMap();
        final IState _initState = fsm.getInitialState();
        createEpsFreeAutm(_sToEcMap, _initState, new HashSet());
        initialState = (State) _sToEcMap.get(((State) _initState).getEclosure());         
        postCompute(_sToEcMap);
    }
    
    /**
     * Creates an epsilon free automata.
     * @param sToEcMap The map of new states representing the 
     *  eps closed states. 
     * @param state The current state.
     * @param reachset The reach set of old states
     */
    private void createEpsFreeAutm(Map sToEcMap, IState state,
            final Set reachset) {
        if (!reachset.contains(state)) {
            reachset.add(state);
            State _newState = null;
            final Set _eSet = ((State) state).getEclosure();
            if (sToEcMap.containsKey(_eSet)) {
                _newState = (State) sToEcMap.get(_eSet);
            } else {
                _newState = new State();
                sToEcMap.put(((State) state).getEclosure(), _newState);
            }

            for (final Iterator _it = _eSet.iterator(); _it.hasNext();) {
                State _eState = (State) _it.next();
                final Set _transSet = _eState.getExitingTransitions();
                for (Iterator iter = _transSet.iterator(); iter.hasNext();) {
                    final ITransition _trans = (ITransition) iter.next();
                    if (!(_trans instanceof EpsTransition)) {
                        State _theState = null;
                        final State _dstnState = (State) _trans.getDstnState();
                        final Object _rObj = sToEcMap.get(_dstnState
                                .getEclosure());
                        if (_rObj == null) {
                            _theState = new State();
                            sToEcMap.put(_dstnState.getEclosure(), _theState);
                        } else {
                            _theState = (State) _rObj;
                        }
                        final Transition _t = new Transition();
                        _t.setSrcState(_newState);
                        _t.setDstnState(_theState);
                        _t.setLabel(_trans.getLabel());
                        _newState.addExitingTransitions(_t);
                        _theState.addEnteringTransitions(_t);
                       /* System.out.print("Creating transition from " + _newState.hashCode() + " : " + _theState.hashCode());
                        System.out.println(" with label " + _t.getLabel().getClass().toString());*/
                        createEpsFreeAutm(sToEcMap, _dstnState, reachset);
                    }

                }
            }
        }
    }

    /**
     * Perform any postcomputation.
     */
    private void preCompute() {
        computeEpsilonClosure();
        
    }
    
    /**
     * Perform any precomputation.
     * @param The map of eps closure to new states.
     */
    private void postCompute(final Map epsMap) {       
        computeFinalStates(epsMap);    
    }

    
    /**
	 * Mark the final states.
	 * @param epsMap The eps to new set map.
	 */
	private void computeFinalStates(final Map epsMap) {		
		final Set _entrySet = epsMap.entrySet();
		for (Iterator iter = _entrySet.iterator(); iter.hasNext();) {
            final Map.Entry _entry = (Map.Entry) iter.next();
            final Set _epsSet = (Set) _entry.getKey();
            final State _state = (State) _entry.getValue();
            for (Iterator iterator = _epsSet.iterator(); iterator.hasNext();) {
                final State _oldState = (State) iterator.next();
                if (_oldState.isFinalState()) {
                    _state.setFinal(true);
                    break;
                }
                
            }
        }
	}
    

    /**
     * Calculate the epsilon closure of the sets.
     */
    private void computeEpsilonClosure() {
       calculateEclosure((State) fsm.getInitialState(), new HashSet());        
    }

    /**
     * Calculate the reach set
     * @param state The current state.
     * @param reachSet The reach set.
     */
    private void calculateEclosure(State state, HashSet reachSet) {
        if (reachSet.contains(state)) {
			return;
		}
		// Calculate eclosure
    	calEclosureForState(state);
		reachSet.add(state);
		final Set _transSet = state.getExitingTransitions();
		for (Iterator iter = _transSet.iterator(); iter.hasNext();) {
			final ITransition _trans = (ITransition) iter.next();
			calculateEclosure((State) _trans.getDstnState(), reachSet);
		}
        
    }

    /**
     * Calculate the eclosure for the specified state.
     * @param state
     * @return Set The set of eclosed states for the given state
     */
    private Set calEclosureForState(State state) {
		if (state.getEclosure().size() > 0) {
			return state.getEclosure();
		}
		final Set _retSet = new HashSet();
		_retSet.add(state);
		final Set _transitionSet = state.getExitingTransitions();
		for (Iterator iter = _transitionSet.iterator(); iter.hasNext();) {
			final ITransition _t = (ITransition) iter.next();
			if (_t instanceof EpsTransition) {
				final State _newState = (State) _t.getDstnState();
				_retSet.addAll(calEclosureForState(_newState));				
			}
		}
		state.setEclosure(_retSet);		
		return _retSet;
	}

    
   

    /**
     * Get the result fsm.
     * @return IFSM The epsilon free fsm.
     */
    public IFSM getResult() {
    	final BasicFSM _bfm = new BasicFSM();
    	_bfm.setInitialState(initialState);
        return _bfm;
    }
}
