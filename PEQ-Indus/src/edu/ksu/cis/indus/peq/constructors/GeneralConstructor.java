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
 
package edu.ksu.cis.indus.peq.constructors;

import edu.ksu.cis.indus.peq.graph.Edge;
import edu.ksu.cis.peq.constructor.interfaces.IConstructor;
import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;

/**
 * @author ganeshan
 *
 * Represents a constrcutor.
 */
public abstract class GeneralConstructor implements IConstructor {

    
    private String variableName = "";
    
  
    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.constructor.interfaces.IConstructor#isVariablePresent()
     */
    public boolean isVariablePresent() {
        return !variableName.equals("");
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.constructor.interfaces.IConstructor#getVariableName()
     */
    public String getVariableName() {
        return variableName;
    }

    /**
     * @param variableName The variableName to set.
     */
    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
    /**
     * Match the constructors.
     * @param cons
     * @param masterEdge The main edge containing the values.
     * @return
     * @throws IllegalAccessException
     */
    public abstract IFSMToken match(final GeneralConstructor cons, final Edge masterEdge) throws IllegalAccessException;
    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof GeneralConstructor)) {
            return false;
        }
        GeneralConstructor rhs = (GeneralConstructor) object;
        return variableName.equals(rhs.getVariableName());
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int _result = 17;
        _result = 37 * _result + variableName.hashCode();
        return _result;
    }
}
