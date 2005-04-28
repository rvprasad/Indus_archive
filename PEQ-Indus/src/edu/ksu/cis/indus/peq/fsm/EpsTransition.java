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

import edu.ksu.cis.peq.constructor.interfaces.IConstructor;
import edu.ksu.cis.peq.fsm.interfaces.IState;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;

/**
 * @author ganeshan
 *
 * Denotes an epsilon transition.
 */
public class EpsTransition implements ITransition {

    private State srcState;
    private State dstnState;
    
    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.ITransition#getSrcState()
     */
    public IState getSrcState() {
        return srcState;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.ITransition#getDstnState()
     */
    public IState getDstnState() {
        return dstnState;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.ITransition#getLabel()
     */
    public IConstructor getLabel() {   
        return null;
    }

    /**
     * @param dstnState The dstnState to set.
     */
    public void setDstnState(State dstnState) {
        this.dstnState = dstnState;
    }
    /**
     * @param srcState The srcState to set.
     */
    public void setSrcState(State srcState) {
        this.srcState = srcState;
    }
}
