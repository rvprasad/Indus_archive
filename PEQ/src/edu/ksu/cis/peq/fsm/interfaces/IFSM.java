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
 * Created on March 8, 2005, 8:23 PM
 */

package edu.ksu.cis.peq.fsm.interfaces;

/**
 * @author ganeshan
 * This is the intereface for the FSM
 */
public interface IFSM {
    
    /**
     * Returns the initial state.
     * @pre IFSM.states > 0
     * @post Result != null
     */
    IState getInitialState();
}
