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
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Transition implements ITransition {

    State srcState;
    State dstnState;
    IConstructor label;
    
    public Transition() {
    }
    
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
        return label;
    }

    public void setDstnState(State dstnState) {
        this.dstnState = dstnState;
    }
    public void setLabel(IConstructor label) {
        this.label = label;
    }
    public void setSrcState(State srcState) {
        this.srcState = srcState;
    }      
    
}
