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

package edu.ksu.cis.peq.constructor.interfaces;

/**
 * @author ganeshan
 * This is the interface for the constructor (label)
 */
public interface IConstructor {    
    
    /** 
     * Indicates if the constructor has a variable.
     * Only true for the fsm transition labels.
     */
    boolean isVariablePresent();
    
    /**
     * Returns the name of the variable
     * @pre isVariablePresent() == true
     * @post Result != null
     */
    String getVariableName();
}
