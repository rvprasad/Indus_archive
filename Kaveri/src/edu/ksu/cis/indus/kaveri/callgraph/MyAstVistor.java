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
 
package edu.ksu.cis.indus.kaveri.callgraph;


import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MyAstVistor extends ASTVisitor {
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
     */
  //  String methodName="";
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AssertStatement)
     */
    public boolean visit(AssertStatement node) {
        // TODO Auto-generated method stub
       
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Block)
     */
    public boolean visit(Block node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.BreakStatement)
     */
    public boolean visit(BreakStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ConstructorInvocation)
     */
    public boolean visit(ConstructorInvocation node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ContinueStatement)
     */
    public boolean visit(ContinueStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.DoStatement)
     */
    public boolean visit(DoStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ExpressionStatement)
     */
    public boolean visit(ExpressionStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ForStatement)
     */
    public boolean visit(ForStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.IfStatement)
     */
    public boolean visit(IfStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.LabeledStatement)
     */
    public boolean visit(LabeledStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ReturnStatement)
     */
    public boolean visit(ReturnStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SuperConstructorInvocation)
     */
    public boolean visit(SuperConstructorInvocation node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SwitchCase)
     */
    public boolean visit(SwitchCase node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SwitchStatement)
     */
    public boolean visit(SwitchStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SynchronizedStatement)
     */
    public boolean visit(SynchronizedStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.ThrowStatement)
     */
    public boolean visit(ThrowStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TryStatement)
     */
    public boolean visit(TryStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclarationStatement)
     */
    public boolean visit(TypeDeclarationStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.VariableDeclarationStatement)
     */
    public boolean visit(VariableDeclarationStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.WhileStatement)
     */
    public boolean visit(WhileStatement node) {
        // TODO Auto-generated method stub
        return super.visit(node);
    }
    
    
    /**
     * Constructor, 
     */
    public MyAstVistor(final CompilationUnit cu, final String methodName) {
       // this.cunit = cu;
      //  this.methodName = methodName;
    }
    
}
