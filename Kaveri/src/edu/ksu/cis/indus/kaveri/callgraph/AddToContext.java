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

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.internal.ui.callhierarchy.CallHierarchyViewPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * @author ganeshan
 *
 * 
 * 
 */
public class AddToContext implements IViewActionDelegate {

    private IStructuredSelection sSel;
    private CallHierarchyViewPart callViewPart;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {        
        this.callViewPart = (CallHierarchyViewPart) view;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (sSel != null) {
            if (sSel.getFirstElement() instanceof MethodWrapper) {
                MethodWrapper _mw = (MethodWrapper) (sSel.getFirstElement());
                final IJavaElement _method = (IJavaElement) _mw.getAdapter(IJavaElement.class);
                if (_method != null && _method.getElementType() == IJavaElement.METHOD) {
                    final IMethod _jmethod = (IMethod) _method;
                    final ASTParser _parser = ASTParser.newParser(AST.JLS2);
                    _parser.setSource(_jmethod.getCompilationUnit());
                    final CompilationUnit _cu = (CompilationUnit) _parser.createAST(null);
                    _cu.accept(new MyAstVistor(_cu));
                }
            }
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            final IStructuredSelection _ssl = (IStructuredSelection) selection;
           this.sSel = _ssl;
        }

    }

}
