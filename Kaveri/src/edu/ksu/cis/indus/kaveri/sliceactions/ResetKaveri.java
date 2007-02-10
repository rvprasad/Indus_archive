/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/
 
package edu.ksu.cis.indus.kaveri.sliceactions;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.decorator.IndusDecorator;
import edu.ksu.cis.indus.kaveri.presentation.AddIndusAnnotation;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import soot.G;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ResetKaveri implements IEditorActionDelegate {

    /* (non-Javadoc)
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction, org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(IAction action, IEditorPart targetEditor) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        removeAnnotations();
        G.reset();
        G.reset();
        KaveriPlugin.getDefault().getIndusConfiguration().resetAll();
        final IndusDecorator _decorator = IndusDecorator.getIndusDecorator();
        if (_decorator != null) {
            _decorator.refesh();
        }

    }
    
    /**
     * Removes all current slice annotations on all open Java editors.
     */
    protected void removeAnnotations() {
        final IWorkbenchWindow[] _windows = PlatformUI.getWorkbench()
                .getWorkbenchWindows();
        final AddIndusAnnotation _manager = KaveriPlugin.getDefault()
                .getIndusConfiguration().getIndusAnnotationManager();

        for (int _i = 0; _windows != null && _i < _windows.length; _i++) {
            final IWorkbenchWindow _window = _windows[_i];
            final IWorkbenchPage[] _pages = _window.getPages();

            for (int _j = 0; _pages != null && _j < _pages.length; _j++) {
                final IEditorReference[] _references = _pages[_j]
                        .getEditorReferences();

                for (int _k = 0; _references != null && _k < _references.length; _k++) {
                    final IEditorReference _reference = _references[_k];
                    final String _id = _reference.getId();

                    if (_id.equals("org.eclipse.jdt.ui.CompilationUnitEditor")) {
                        final CompilationUnitEditor _edPart = (CompilationUnitEditor) _reference
                                .getEditor(false);

                        if (_edPart != null) {
                            _manager.setEditor(_edPart, false);
                        }
                    }
                }
            }
        }
    }


    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub

    }

}
