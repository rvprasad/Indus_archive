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
 * Created on March 8, 2005, 6:50 PM
 */

package edu.ksu.cis.peq.fsm.interfaces;

import edu.ksu.cis.peq.constructor.interfaces.IConstructor;

/**
 * @author ganeshan
 * This is the interface for the fsm transition.
 */
public interface ITransition {
    /**
     * Returns the source state of the transition.
     * @pre ITranstion.srcState != null && ITransition.dstnState != null
     */
    IState getSrcState();
    
    /**
     * Returns the destination state of the transition.
     * @pre ITranstion.srcState != null && ITransition.dstnState != null
     */
    IState getDstnState();
    
    /**
     * Returns the constructor (label) associated with the transition.
     * 
     */
    IConstructor getLabel();
}
