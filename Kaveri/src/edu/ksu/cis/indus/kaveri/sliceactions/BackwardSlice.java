/*
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

/*
 * Created on Apr 14, 2004
 *
 * The backward slicer
 */
package edu.ksu.cis.indus.kaveri.sliceactions;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.jface.viewers.ISelection;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;

/**
 * Runs a backward slice on the chosen Java statement.
 * 
 * @author Ganeshan Runs the backward slice
 */
public class BackwardSlice extends BasicSliceFunctions implements
        IEditorActionDelegate {

    /**
     * The Java editor.
     */
    protected CompilationUnitEditor editor;

    /**
     * The text selection.
     */
    protected ISelection textSelection;
    
    
    /**
     * Sets the current editor.
     * 
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(final IAction action,
            final IEditorPart targetEditor) {
        editor = (CompilationUnitEditor) targetEditor;
    }

    /**
     * Run the backward slice action.
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(final IAction action) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                final String _sbConfigKey = "edu.ksu.cis.indus.kaveri.sbConfig";
                String _configName = KaveriPlugin.getDefault()
                        .getPreferenceStore().getString(_sbConfigKey);
                if (_configName.equals("") || !validateConfiguration(_configName)) {
                    MessageDialog.openError(null, "Error", "Please set a configuration for this action from the Indus plugin preference");
                    return;
                }
                
                final Shell _parentShell = editor.getSite().getShell();
                
                runSlice(_configName, editor, textSelection, true, _parentShell);
            }
        });
    }

    /**
     * Stores the new selection.
     * 
     * @see org.eclipse.ui.IActionDelegate
     *      #selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(final IAction action,
            final ISelection selection) {
        this.textSelection = selection;
    }
}