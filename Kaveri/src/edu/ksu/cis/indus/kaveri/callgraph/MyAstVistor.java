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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MyAstVistor extends ASTVisitor {
    private CompilationUnit cunit;
    
    /**
     * Constructor, 
     */
    public MyAstVistor(final CompilationUnit cu) {
        this.cunit = cu;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.IfStatement)
     */
    public boolean visit(IfStatement node) {
        System.out.println(node + " Line: " + cunit.lineNumber(node.getStartPosition()));        
        return true;
    }
}
