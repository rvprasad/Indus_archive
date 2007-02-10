/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

/*
 * Created on Apr 14, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri.sliceactions;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;

import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;

/**
 * Runs a forward slice on the chosen Java statement.
 * 
 * @author Ganeshan
 */
public class ForwardSlice extends BasicSliceFunctions implements
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
     * Indicates the current Java editor instance.
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
                final String _sfConfigKey = "edu.ksu.cis.indus.kaveri.sfConfig";
                String _configName = KaveriPlugin.getDefault()
                        .getPreferenceStore().getString(_sfConfigKey);
                if (_configName.equals("") || !validateConfiguration(_configName)) {
                    MessageDialog.openError(null, "Error", "Please set a configuration for this action from the Indus plugin preference");
                    return;
                }
                final Shell _parentShell = editor.getSite().getShell();
                runSlice(_configName, editor, textSelection, false, _parentShell);
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
