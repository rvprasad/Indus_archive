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
 
package edu.ksu.cis.indus.peq.queryast;

/**
 * @author ganeshan
 *
 * This represents a constructor in the query terms.
 */
public class ConstructorAST extends BaseAST {
    private String variableName;
    
    private int constructType;
    
    
    
    private BaseAST nextNode;
    /**
     * @return Returns the constructType.
     */
    public int getConstructType() {
        return constructType;
    }
    /**
     * @param constructType The constructType to set.
     */
    public void setConstructType(int constructType) {
        this.constructType = constructType;
    }
    
    /**
     * @return Returns the variableName.
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
    
    /* (non-Javadoc)
     * @see edu.ksu.cis.indus.peq.queryast.BaseAST#getNextNode()
     */
    public BaseAST getNextNode() {
        return nextNode;
    }
    /**
     * @param nextNode The nextNode to set.
     */
    public void setNextNode(BaseAST nextNode) {
        this.nextNode = nextNode;
    }
}
