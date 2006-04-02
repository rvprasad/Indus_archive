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
 
package edu.ksu.cis.indus.peq.queryglue;

import antlr.Token;
import antlr.collections.AST;

/**
 * @author ganeshan
 *
 * Represent a concatenation node.
 */
public class AndNode extends ConstructorNode {

    // The left ast node.
    private ConstructorNode leftNode;
    
    // The right ast node.
    private ConstructorNode rightNode;
    
    /* (non-Javadoc)
     * @see antlr.collections.AST#initialize(int, java.lang.String)
     */
    public void initialize(int t, String txt) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see antlr.collections.AST#initialize(antlr.collections.AST)
     */
    public void initialize(AST t) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see antlr.collections.AST#initialize(antlr.Token)
     */
    public void initialize(Token t) {
        // TODO Auto-generated method stub

    }

    /**
     * Return the left node.
     * @return Returns the leftNode.
     */
    public ConstructorNode getLeftNode() {
        return leftNode;
    }
    /**
     * Set the left node.
     * @param leftNode The leftNode to set.
     */
    public void setLeftNode(ConstructorNode leftNode) {
        this.leftNode = leftNode;
    }
    /**
     * get the right node.
     * @return Returns the rightNode.
     */
    public ConstructorNode getRightNode() {
        return rightNode;
    }
    /**
     * set the right node.
     * @param rightNode The rightNode to set.
     */
    public void setRightNode(ConstructorNode rightNode) {
        this.rightNode = rightNode;
    }
}
