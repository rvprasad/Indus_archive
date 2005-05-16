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

import java.util.HashSet;
import java.util.Set;

import edu.ksu.cis.peq.fsm.interfaces.IState;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class State implements IState {
    private Set enteringTransitions;
    private Set exitingTransitions;
    private boolean isFinal;
    private String name;
    
    public State(String name) {
        this.name = name;
        enteringTransitions = new HashSet();
        exitingTransitions = new HashSet();
        isFinal = false;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.IState#getEnteringTransitions()
     */
    public Set getEnteringTransitions() {
        return enteringTransitions;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.IState#getExitingTransitions()
     */
    public Set getExitingTransitions() {
        return exitingTransitions;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.IState#isFinalState()
     */
    public boolean isFinalState() {
        return isFinal;
    }

    public void addEnteringTransitions(ITransition trans) {
        this.enteringTransitions.add(trans);
    }
    
    public void addExitingTransitions(ITransition trans) {
        this.exitingTransitions.add(trans);
    }
    
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }
    
    public String toString() {
        return name;
    }
    
}
