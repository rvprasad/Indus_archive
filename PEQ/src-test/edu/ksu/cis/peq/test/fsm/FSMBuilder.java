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
 
package edu.ksu.cis.peq.test.fsm;

import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.fsm.interfaces.IState;
import edu.ksu.cis.peq.test.constructors.Constructor1;
import edu.ksu.cis.peq.test.constructors.Constructor2;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FSMBuilder implements IFSM {
    private State initialState;
    
    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.IFSM#getInitialState()
     */
    public IState getInitialState() {
        createFSM();
        return initialState;
    }

    /**
     * 
     */
    private void createFSM() {
        initialState = new State("initState");
        State s1 = new State("State 1");
        State s2 = new State("State 2");
        s2.setFinal(true);
        
        final Transition t1 = new Transition("Transition 1");
        t1.setSrcState(initialState);
        t1.setDstnState(s1);
        t1.setLabel(new Constructor1());
        
        final Transition t2 = new Transition("Transition 2");
        t2.setSrcState(s1);
        t2.setDstnState(s2);
        t2.setLabel(new Constructor2());
               
        initialState.addExitingTransitions(t1);
        s1.addExitingTransitions(t2);        
    }

}
