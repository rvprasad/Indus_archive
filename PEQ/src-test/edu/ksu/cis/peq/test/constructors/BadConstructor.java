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
 
package edu.ksu.cis.peq.test.constructors;

import edu.ksu.cis.peq.constructor.interfaces.IConstructor;

/**
 * @author ganeshan
 *
 * Dummy constructor that rejects everything.
 */
public class BadConstructor implements IConstructor {

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.constructor.interfaces.IConstructor#isVariablePresent()
     */
    public boolean isVariablePresent() {
        return false;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.constructor.interfaces.IConstructor#getVariableName()
     */
    public String getVariableName() {
        return "";
    }

}
