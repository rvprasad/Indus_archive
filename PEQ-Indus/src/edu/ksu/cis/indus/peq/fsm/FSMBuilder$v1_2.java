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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.ksu.cis.indus.peq.constructors.ControlDepD;
import edu.ksu.cis.indus.peq.constructors.ControlDepT;
import edu.ksu.cis.indus.peq.constructors.DvgDepD;
import edu.ksu.cis.indus.peq.constructors.DvgDepT;
import edu.ksu.cis.indus.peq.constructors.GeneralConstructor;
import edu.ksu.cis.indus.peq.constructors.IDef;
import edu.ksu.cis.indus.peq.constructors.IUse;
import edu.ksu.cis.indus.peq.constructors.IntfDepD;
import edu.ksu.cis.indus.peq.constructors.IntfDepT;
import edu.ksu.cis.indus.peq.constructors.RDef;
import edu.ksu.cis.indus.peq.constructors.RUse;
import edu.ksu.cis.indus.peq.constructors.ReadyDepD;
import edu.ksu.cis.indus.peq.constructors.ReadyDepT;
import edu.ksu.cis.indus.peq.constructors.SyncDepD;
import edu.ksu.cis.indus.peq.constructors.SyncDepT;
import edu.ksu.cis.indus.peq.constructors.WcConstructor;
import edu.ksu.cis.indus.peq.queryast.AndAST;
import edu.ksu.cis.indus.peq.queryast.BaseAST;
import edu.ksu.cis.indus.peq.queryast.ConstructorAST;
import edu.ksu.cis.indus.peq.queryast.OrAST;
import edu.ksu.cis.indus.peq.queryglue.IIndusConstructorTypes;
import edu.ksu.cis.indus.peq.queryglue.IPEQRegexTypes;
import edu.ksu.cis.indus.peq.queryglue.QueryObject;
import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.fsm.interfaces.IState;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;

/**
 * @author ganeshan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class FSMBuilder$v1_2 implements IFSM {

    private QueryObject qObject;
    private State initialState;

    public FSMBuilder$v1_2(final QueryObject qObject) {
        this.qObject = qObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ksu.cis.peq.fsm.interfaces.IFSM#getInitialState()
     */
    public IState getInitialState() {
    	if (initialState ==null) {
    		initialState = (State) createFSM();    		
        	// epsilon transitions change the final states. Modify accordingly.
        	markFinalStates(initialState, new HashSet());
    	}    	
    	return initialState;
    }
    
    /**
	 * Modify the final states.
	 */
	private void markFinalStates(final State state, final Set reachSet) {		
		if (reachSet.contains(state)) {
			return;
		}
		// Calculate eclosure
    	getEclosure(state);
		reachSet.add(state);
		final Set _epSet = state.getEclosure();
		for (Iterator iter = _epSet.iterator(); iter.hasNext();) {
			final State _state = (State) iter.next();
			if (_state.isFinalState()) {
				state.setFinal(true);
				break;
			}
		}
		final Set _transSet = state.getExitingTransitions();
		for (Iterator iter = _transSet.iterator(); iter.hasNext();) {
			final ITransition _trans = (ITransition) iter.next();
			markFinalStates((State) _trans.getDstnState(), reachSet);
		}
		
	}

	/**
     * Returns the outgoing transition from this state.
     * This is required to do a dynamic epsilon closure.
     * @param state The state to get the transitions from.
     * @return Result.oclIsKindOf(Set(Transition))
     */
    public Set getOutgoingTransitions(final State state) {
    	Set _retSet = new HashSet();
    	final Set _ecloseSet = getEclosure(state);
    	for (Iterator iter = _ecloseSet.iterator(); iter.hasNext();) {
    		final State _state = (State) iter.next();
    		final Set _transitions = _state.getExitingTransitions();
    		for (Iterator iterator = _transitions.iterator(); iterator
					.hasNext();) {
				final ITransition _t = (ITransition) iterator.next();
				if (_t instanceof Transition) {
					_retSet.add(_t);
				}				
			}
		}
    	return _retSet;
    }

    /**
     * Returns the set of states reachable from the given state 
     * through epsilon transitions.
	 * @param state
	 * @return result.oclIsKindOf(Set(State))
	 */
	private Set getEclosure(State state) {
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
				_retSet.addAll(getEclosure(_newState));				
			}
		}
		state.setEclosure(_retSet);		
		return _retSet;
	}

	/**
     * Converts the given query object into an fsm.
     * 
     * @return IState The starting state of the fsm.
     */
    private IState createFSM() {
        State _initialState = null;
        final BaseAST _startNode = qObject.getStartNode();
        if (_startNode != null) {            
            _initialState = new State();
            State _currState = _initialState;
            BaseAST _currAST = _startNode;
            while (_currAST != null) {                
                _currState =  processTransition(_currState, _currAST);
                _currAST = _currAST.getNextNode();
            }
                           
            _currState.setFinal(true);
        }

        return _initialState;
    }

    
    
    
    /**
     * Process the constructor. Handles the alternation. 
     * @param currState The current state.
     * @param currast The current AST constructor to process.
     * 
     * @return State The resulting end state..
     */
    private State processTransition(State currState, BaseAST currAST) {
        if (currAST instanceof OrAST) {
            final OrAST _orast = (OrAST) currAST;
            State _retState = null;
            switch (_orast.getRegexType()) {
            	case IPEQRegexTypes.NO_REGEXTYPE:
            	    _retState = processOr(currState, _orast);
            	    break;
            	case IPEQRegexTypes.ONE_OR_MORE:
            	    _retState = processPlusGroup(currState, _orast);
            	    break;
            	case IPEQRegexTypes.ZERO_OR_MORE:
            	    _retState = processKleeneGroup(currState, _orast);
            	    break;
            	case IPEQRegexTypes.ZERO_OR_ONE:
            	    _retState = processZeroOrOneGroup(currState, _orast);
            	    break;            
            }
                        
            return _retState;
        } else if (currAST instanceof AndAST) {
            final AndAST _aAst = (AndAST) currAST;
            State _retState = null;
            switch(_aAst.getRegexType()) {
    		case IPEQRegexTypes.NO_REGEXTYPE:
    		    _retState = processAnd(currState, _aAst);
    	    	break;
    		case IPEQRegexTypes.ONE_OR_MORE:
    		    _retState = processPlusGroup(currState, _aAst);
    	    	break;
    		case IPEQRegexTypes.ZERO_OR_MORE:
    		    _retState = processKleeneGroup(currState, _aAst);
    	    	break;
    		case IPEQRegexTypes.ZERO_OR_ONE:
    		    _retState = processZeroOrOneGroup(currState, _aAst);
    	    	break;            
            }
            return _retState;
        }
        else {
            final ConstructorAST _cast = (ConstructorAST) currAST;
            return createTransition(currState, _cast);
        }
    }

    /**
     * Process the plus for the compound Ast.
     * @param currState
     * @param ast
     * @return
     */
    private State processPlusGroup(State currState, BaseAST ast) {
        final State _s1 = processTransition(currState, ast);
        return processKleeneGroup(_s1, ast);
    }

    /**
     * Process the kleene for the compound Ast.
     * @param currState
     * @param ast
     * @return
     */
    private State processKleeneGroup(State currState, BaseAST ast) {
        final State _s1 = new State();    	
    	final State _dState = new State();
    	
    	State _s2 = null;
    	if (ast instanceof OrAST) {
    	    _s2 = processOr(_s1,  (OrAST) ast); 
    	} else if (ast instanceof AndAST) {
    	    _s2 = processAnd(_s1, (AndAST) ast);
    	} 
    	
    	
    	
        final EpsTransition _ep1 = new EpsTransition();
        final EpsTransition _ep2 = new EpsTransition();
        final EpsTransition _ep3 = new EpsTransition();
        final EpsTransition _ep4 = new EpsTransition();
        // 1
        _ep1.setSrcState(currState);
        _ep1.setDstnState(_s1);
        currState.addExitingTransitions(_ep1);
        _s1.addEnteringTransitions(_ep1);
        // 2
        _ep2.setSrcState(_s2);
        _ep2.setDstnState(_s1);
        _s1.addEnteringTransitions(_ep2);
        _s2.addExitingTransitions(_ep2);        
        // 3
        _ep3.setSrcState(_s2);
        _ep3.setDstnState(_dState);
        _s2.addExitingTransitions(_ep3);
        _dState.addEnteringTransitions(_ep3);        
        // 4
        _ep4.setSrcState(currState);
        _ep4.setDstnState(_dState);
       currState.addExitingTransitions(_ep4);
       _dState.addEnteringTransitions(_ep4);       
       return _dState;
    }
    
    /**
     * Process the zero or one for the compound Ast.
     * @param currState
     * @param ast
     * @return
     */
    private State processZeroOrOneGroup(State currState, BaseAST ast) {
        
        final EpsTransition _ep = new EpsTransition();
        State _dState = null;
        if (ast instanceof OrAST) {
            _dState = processOr(currState, (OrAST) ast);
        } else if (ast instanceof AndAST) {
            _dState = processAnd(currState, (AndAST) ast);
        }
                        
        // epsilon
        _ep.setSrcState(currState);
        _ep.setDstnState(_dState);
        currState.addExitingTransitions(_ep);
        _dState.addEnteringTransitions(_ep);        
        return _dState;
    }
    
    
    /**
     * Process the concateantion.
     * @param currState
     * @param baseAST
     * @return
     */
    private State processAnd(final State currState, final AndAST andAST) {
        final State _leftState = processTransition(currState, andAST.getLeftNode());
        final State _rightState = processTransition(_leftState, andAST.getRightNode());                    
        return _rightState;
    }
    
    /**
     * Process the alternation.
     * @param currState
     * @param baseAST
     * @return
     */
    private State processOr(final State currState, final OrAST orAST) {
        final State _leftState = processTransition(currState, orAST.getLeftNode());
        final State _rightState = processTransition(currState, orAST.getRightNode());
        final State _newEndState = new State();
        final EpsTransition _ep1 = new EpsTransition();
        final EpsTransition _ep2 = new EpsTransition();
        _ep1.setSrcState(_leftState);
        _ep2.setSrcState(_rightState);
        _ep1.setDstnState(_newEndState);
        _ep2.setDstnState(_newEndState);
        _leftState.addExitingTransitions(_ep1);
        _rightState.addExitingTransitions(_ep2);
        _newEndState.addEnteringTransitions(_ep1);
        _newEndState.addEnteringTransitions(_ep2);            
        return _newEndState;
    }
    /**
     * Adds the given constructor as a transition.
     * @param constructor The constructor to create a transition for.
     * 
     * @return State The end state of the transitions corrssponding to this fragment.
     */
    private State createTransition(final State currState, final ConstructorAST constructor) {              
        GeneralConstructor _gc = null;
        State _retState = null;
        switch (constructor.getConstructType()) {
        	case IIndusConstructorTypes.CDEPD:
        	    _gc = new ControlDepD();
        	    break;
        	case IIndusConstructorTypes.IDEF:
        	    _gc = new IDef();
        	    break;
        	case IIndusConstructorTypes.CDEPT:
        	    _gc = new ControlDepT();
        	    break;
        	case IIndusConstructorTypes.IUSE:
        	    _gc = new IUse();
        	    break;
        	case IIndusConstructorTypes.DDEPD:
        	    _gc = new DvgDepD();
        		break;
        	case IIndusConstructorTypes.DDEPT:
        	    _gc = new DvgDepT();
        		break;
        	case IIndusConstructorTypes.IDEPD:
        	    _gc = new IntfDepD();
        		break;
        	case IIndusConstructorTypes.IDEPT:
        	    _gc = new IntfDepT();
        		break;
        	case IIndusConstructorTypes.RDEF:
        	    _gc = new RDef();
        		break;
        	case IIndusConstructorTypes.RUSE:
        	    _gc = new RUse();
        	    break;
        	case IIndusConstructorTypes.RDEPD:
        	    _gc = new ReadyDepD();
        	    break;
        	case IIndusConstructorTypes.RDEPT:
        	    _gc = new ReadyDepT();
        	    break;
        	case IIndusConstructorTypes.SDEPD:
        	    _gc = new SyncDepD();        	   
        	    break;
        	case IIndusConstructorTypes.SDEPT:
        	    _gc = new SyncDepT();
        	    break;
        	case IIndusConstructorTypes.WC:
        	    _gc = new WcConstructor();
        		break;
        	
        }
        _gc.setVariableName(constructor.getVariableName());
        
        
        switch (constructor.getRegexType()) {
        case IPEQRegexTypes.NO_REGEXTYPE:
            _retState = createNormalTransition(currState, _gc);
            break;
        case IPEQRegexTypes.ZERO_OR_MORE:                                  
            _retState = createKleene(currState, _gc);
            break;
        case IPEQRegexTypes.ONE_OR_MORE:
            _retState = createPlus(currState, _gc);
            break;
        case IPEQRegexTypes.ZERO_OR_ONE:
            _retState = createZeroOrOne(currState, _gc);
            break;
        }
        return _retState;
    }
    
    /**
     * Creates a zero or one transition.
     * @param currState
     * @param gc The constructor.
     * @return State The end state of this fragment.
     */
    private State createZeroOrOne(State currState, GeneralConstructor gc) {
        final State _dState = new State();
        final EpsTransition _ep = new EpsTransition();
        final Transition _t = new Transition();
        // transition
        _t.setLabel(gc);
        _t.setSrcState(currState);
        _t.setDstnState(_dState);
        currState.addExitingTransitions(_t);
        _dState.addEnteringTransitions(_t);
        // epsilon
        _ep.setSrcState(currState);
        _ep.setDstnState(_dState);
        currState.addExitingTransitions(_ep);
        _dState.addEnteringTransitions(_ep);        
        return _dState;
    }

    /**
     * @param currState
     * @param gc The constructor.
     * @return
     */
    private State createPlus(State currState, GeneralConstructor gc) {
        State _newState = null;
        _newState = createNormalTransition(currState, gc);
        _newState = createKleene(_newState, gc); 
        return _newState;
    }

    /**
     * Creates a normal transition.
     * @param currState
     * @param cons
     * @return State The end state of this fragment.
     */
    private State createNormalTransition(final State currState, final GeneralConstructor cons) {
        final Transition _t = new Transition();
    	_t.setLabel(cons);
        _t.setSrcState(currState);
        final State _newState = new State();
        _t.setDstnState(_newState);
        currState.addExitingTransitions(_t);
        _newState.addEnteringTransitions(_t);
        
        return _newState;
    }
    
    /**
     * Creates a kleene transition from the given state.
     * @param currState
     * @param cons The constructor.
     * @return State The end state of the transition fragment.
     */
    private State createKleene(final State currState, final GeneralConstructor cons) {
        final State _s1 = new State();
    	final State _s2 = new State();
    	final State _dState = new State();
    	
    	final Transition _t = new Transition();
    	_t.setLabel(cons);
    	// Main Transition
    	_t.setSrcState(_s1);
        _t.setDstnState(_s2);
        _s1.addExitingTransitions(_t);
        _s2.addEnteringTransitions(_t);
        final EpsTransition _ep1 = new EpsTransition();
        final EpsTransition _ep2 = new EpsTransition();
        final EpsTransition _ep3 = new EpsTransition();
        final EpsTransition _ep4 = new EpsTransition();
        // 1
        _ep1.setSrcState(currState);
        _ep1.setDstnState(_s1);
        currState.addExitingTransitions(_ep1);
        _s1.addEnteringTransitions(_ep1);
        // 2
        _ep2.setSrcState(_s2);
        _ep2.setDstnState(_s1);
        _s1.addEnteringTransitions(_ep2);
        _s2.addExitingTransitions(_ep2);        
        // 3
        _ep3.setSrcState(_s2);
        _ep3.setDstnState(_dState);
        _s2.addExitingTransitions(_ep3);
        _dState.addEnteringTransitions(_ep3);        
        // 4
        _ep4.setSrcState(currState);
        _ep4.setDstnState(_dState);
       currState.addExitingTransitions(_ep4);
       _dState.addEnteringTransitions(_ep4);       
       return _dState;
    }
    
}