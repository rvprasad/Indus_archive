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
 * This is the base class for the AST nodes.
 */
public abstract class BaseAST {
    private int regexType;
    /**
     * Get the next AST node in the chain.
     * @return BaseAST The next ast node.
     */
    public abstract BaseAST getNextNode();
    
    /**
     * @return Returns the regexType.
     */
    public int getRegexType() {
        return regexType;
    }
    /**
     * @param regexType The regexType to set.
     */
    public void setRegexType(int regexType) {
        this.regexType = regexType;
    }
}
