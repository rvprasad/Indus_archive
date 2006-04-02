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
 * This is the AST for the alternation node.
 */
public class OrAST extends BaseAST {

    /**
     * The left node in the alternation.
     */
    private BaseAST leftNode;
    
    /**
     * The right node in the alternation.
     */
    private BaseAST rightNode;
    
    /**
     * The next node in the chain.
     */
    private BaseAST nextNode;

    /* (non-Javadoc)
     * @see edu.ksu.cis.indus.peq.queryast.BaseAST#getNextNode()
     */
    public BaseAST getNextNode() {
        return nextNode;
    }
    
    
    /**
     * @return Returns the leftNode.
     */
    public BaseAST getLeftNode() {
        return leftNode;
    }
    /**
     * @param leftNode The leftNode to set.
     */
    public void setLeftNode(BaseAST leftNode) {
        this.leftNode = leftNode;
    }
    /**
     * @return Returns the rightNode.
     */
    public BaseAST getRightNode() {
        return rightNode;
    }
    /**
     * @param rightNode The rightNode to set.
     */
    public void setRightNode(BaseAST rightNode) {
        this.rightNode = rightNode;
    }
    /**
     * @param nextNode The nextNode to set.
     */
    public void setNextNode(BaseAST nextNode) {
        this.nextNode = nextNode;
    }
}
