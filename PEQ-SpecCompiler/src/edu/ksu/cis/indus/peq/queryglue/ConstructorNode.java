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

package edu.ksu.cis.indus.peq.queryglue;
 
import antlr.BaseAST;
import antlr.Token;
import antlr.collections.AST;

/**
 * @author ganeshan
 *
 * The basic query AST node.
 */
public class ConstructorNode extends BaseAST {
    
    private String variableName;
    
    private int constructorType;
    
    private int regexOperator;
    
    
    
  
    /* (non-Javadoc)
     * @see antlr.collections.AST#initialize(int, java.lang.String)
     */
    public void initialize(int t, String txt) {
        

    }

    /* (non-Javadoc)
     * @see antlr.collections.AST#initialize(antlr.collections.AST)
     */
    public void initialize(AST t) {
    }

    /* (non-Javadoc)
     * @see antlr.collections.AST#initialize(antlr.Token)
     */
    public void initialize(Token t) {        

    }
    
    

    /**
     * @return Returns the constructorType.
     */
    public int getConstructorType() {
        return constructorType;
    }
    /**
     * @param constructorType The constructorType to set.
     */
    public void setConstructorType(int constructorType) {
        this.constructorType = constructorType;
    }
    /**
     * @return Returns the regexOperator.
     */
    public int getRegexOperator() {
        return regexOperator;
    }
    /**
     * @param regexOperator The regexOperator to set.
     */
    public void setRegexOperator(int regexOperator) {
        this.regexOperator = regexOperator;
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
    
    public ConstructorNode getNextConstructor() {        
         return (ConstructorNode) this.getNextSibling();        
    }
}
