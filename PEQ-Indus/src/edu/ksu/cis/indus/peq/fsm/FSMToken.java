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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;
import edu.ksu.cis.peq.fsm.interfaces.ITransition;
import edu.ksu.cis.peq.graph.interfaces.IEdge;
/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class FSMToken implements IFSMToken {
    private IEdge theedge;
    private ITransition thetransition;
    private Map substMap = new HashMap();
    private IFSMToken parent;
    private boolean isEmpty;
        
    
    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.IFSMToken#getGraphEdge()
     */
    public IEdge getGraphEdge() {
        return theedge;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.IFSMToken#isEmpty()
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.IFSMToken#getTransitionEdge()
     */
    public ITransition getTransitionEdge() {
        return thetransition;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.IFSMToken#getSubstituitionMap()
     */
    public Map getSubstituitionMap() {
        return substMap;
    }

    /* (non-Javadoc)
     * @see edu.ksu.cis.peq.fsm.interfaces.IFSMToken#getParent()
     */
    public IFSMToken getParent() {
        return parent;
    }

    public void setParent(IFSMToken parent) {
        this.parent = parent;
    }
    public void setTheedge(IEdge theedge) {
        this.theedge = theedge;
    }
    public void setThetransition(ITransition thetransition) {
        this.thetransition = thetransition;
    }
    public void setEmpty(boolean isEmpty) {
        this.isEmpty = isEmpty;
    }
  
    /**
     * @see java.lang.Object#equals(Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof FSMToken)) {
            return false;
        }
        FSMToken rhs = (FSMToken) object;
        return new EqualsBuilder().appendSuper(super.equals(object)).append(
                this.substMap, rhs.substMap).append(this.isEmpty, rhs.isEmpty)
                .append(this.theedge, rhs.theedge).append(this.parent,
                        rhs.parent).append(this.thetransition,
                        rhs.thetransition).isEquals();
    }
    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return new HashCodeBuilder(-1787161439, -1879020859).appendSuper(
                super.hashCode()).append(this.substMap).append(this.isEmpty)
                .append(this.theedge).append(this.parent).append(
                        this.thetransition).toHashCode();
    }
}
