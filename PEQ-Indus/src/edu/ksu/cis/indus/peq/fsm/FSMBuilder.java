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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
import edu.ksu.cis.indus.peq.queryglue.Constructor;
import edu.ksu.cis.indus.peq.queryglue.IIndusConstructorTypes;
import edu.ksu.cis.indus.peq.queryglue.IPEQRegexTypes;
import edu.ksu.cis.indus.peq.queryglue.QueryObject;
import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.fsm.interfaces.IState;

/**
 * @author ganeshan
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class FSMBuilder implements IFSM {

    private QueryObject qObject;

    public FSMBuilder(final QueryObject qObject) {
        this.qObject = qObject;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ksu.cis.peq.fsm.interfaces.IFSM#getInitialState()
     */
    public IState getInitialState() {
        return createFSM();
    }

    /**
     * Converts the given query object into an fsm.
     * 
     * @return IState The starting state of the fsm.
     */
    private IState createFSM() {
        State _initialState = null;
        final List _lst = qObject.getConstructorList();
        if (_lst.size() > 0) {
            final List _stateList = new LinkedList();
            _initialState = new State();
            _stateList.add(_initialState);
            for (Iterator iter = _lst.iterator(); iter.hasNext();) {
                final Constructor _c = (Constructor) iter.next();
                createTransition(_c, _stateList);
            }                   
            ((State) _stateList.get(_stateList.size() - 1)).setFinal(true);
        }

        return _initialState;
    }

    
    /**
     * Adds the given constructor as a transition.
     * @param constructor The constructor to create a transition for.
     * @param stateList The current set of states.
     */
    private void createTransition(final Constructor constructor, final List stateList) {
        final State _currState = (State) stateList.get(stateList.size() - 1);
        final Transition _t = new Transition();
        GeneralConstructor _gc = null;
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
        	
        }
        _gc.setVariableName(constructor.getVariableName());
        _t.setLabel(_gc);
        _t.setSrcState(_currState);
        switch (constructor.getRegexType()) {
        case IPEQRegexTypes.NO_REGEXTYPE:
            final State _newState = new State();
            stateList.add(_newState);
            _currState.addExitingTransitions(_t);
            _newState.addEnteringTransitions(_t);
            _t.setDstnState(_newState);
            break;
        case IPEQRegexTypes.ZERO_OR_MORE:
            _t.setDstnState(_currState);
            if (!_currState.getExitingTransitions().contains(_t)) {
                _currState.addExitingTransitions(_t);
                _currState.addEnteringTransitions(_t);
            }
            break;
        case IPEQRegexTypes.ONE_OR_MORE:
            // TODO Write me
            break;
        case IPEQRegexTypes.ZERO_OR_ONE:
            // TODO Write me
            break;
        }

    }
    
}