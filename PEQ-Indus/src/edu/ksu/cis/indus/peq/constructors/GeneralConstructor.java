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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
/**
 * @author ganeshan
 *
 * Represents a constrcutor.
 */
public abstract class GeneralConstructor implements IConstructor {

    
    private String variableName = "";
    
    /**
     * The dependence type represented by this cosntructor.
     */
    private Object depType;
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
        return new EqualsBuilder().appendSuper(super.equals(object)).append(
                this.depType, rhs.depType).append(this.variableName,
                rhs.variableName).isEquals();
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new HashCodeBuilder(111365751, 570326667).appendSuper(
                super.hashCode()).append(this.depType)
                .append(this.variableName).toHashCode();
    }
}
