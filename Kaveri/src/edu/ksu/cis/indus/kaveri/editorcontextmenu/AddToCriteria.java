/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.editorcontextmenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;

import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.PrettySignature;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dialogs.StatementResolver;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.preferencedata.CriteriaData;
import edu.ksu.cis.indus.kaveri.soot.SootConvertor;
import edu.ksu.cis.indus.kaveri.views.PartialSliceView;

/**
 * Implements the add to criteria action.
 * 
 * @author Ganeshan
 */
public class AddToCriteria implements IEditorActionDelegate {
    /**
     * The current java editor.
     */
    CompilationUnitEditor editor;

    /**
     * The current selection.
     */
    ISelection selection;

    /**
     * Indicates the current java editor.
     * 
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
        this.editor = (CompilationUnitEditor) targetEditor;
    }

    /**
     * Add the statement to the criteria if feasible.
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(final IAction action) {
        if (selection instanceof ITextSelection) {
            final ITextSelection _tselection = (ITextSelection) selection;
            final int _nSelLine = _tselection.getEndLine() + 1; // Havent
            // figured out
            // whats the 1
            // for but it works.

            try {
                final IType _type = SelectionConverter.getTypeAtOffset(editor);
                final IJavaElement _element = SelectionConverter.getElementAtOffset(editor);

                if (_element != null && (_element instanceof IMethod)) {
                    final IFile _file = ((IFileEditorInput) editor.getEditorInput()).getFile();
                    processCriteriaForFile(_file, _type, _element, _nSelLine);
                }
            } catch (JavaModelException _e) {
                KaveriErrorLog.logException("Java Model Exception", _e);
                SECommons.handleException(_e);
            }
        }
    }

    /**
     * Stores the current selection in the editor.
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(@SuppressWarnings("unused")
    final IAction action, final ISelection textselection) {
        this.selection = textselection;
    }

    /**
     * Checks for duplicate criterias.
     * 
     * @param data The array of criterias
     * @param criteria The new criteria
     * 
     * @return boolean True if the criteria can be added.
     */
    private boolean isOkToAdd(final CriteriaData data, final Criteria criteria) {
        boolean _isOk = true;
        final List _lst = data.getCriterias();

        for (int _i = 0; _i < _lst.size(); _i++) {
            final Criteria _tcriteria = (Criteria) _lst.get(_i);

            if (criteria.getStrClassName().equals(_tcriteria.getStrClassName())
                    && criteria.getStrMethodName().equals(_tcriteria.getStrMethodName())
                    && criteria.getNLineNo() == _tcriteria.getNLineNo()
                    && criteria.getNJimpleIndex() == _tcriteria.getNJimpleIndex()
                    && criteria.isBConsiderValue() == _tcriteria.isBConsiderValue()) {
                _isOk = false;
                break;
            }
        }
        return _isOk;
    }

    /**
     * Adds the given criteria to the project's set of criterias.
     * 
     * @param file The file in which the criteria is present.
     * @param criteria The criteria chosen.
     */
    private void addToCriteria(final IFile file, final Criteria criteria) {
        final IProject _project = file.getProject();

        try {
            final CriteriaData _data = PartialSliceView.retrieveCriteria(_project);

            if (isOkToAdd(_data, criteria)) {
                _data.getCriterias().add(criteria);
            } else {
                MessageDialog.openError(null, "Duplicate", "Duplicate criteria are not allowed");
            }

            PartialSliceView.saveCriteria(_project, _data);
        } catch (CoreException _e) {
            KaveriErrorLog.logException("Core Exception", _e);
            SECommons.handleException(_e);
        } catch (IOException _e) {
            KaveriErrorLog.logException("Core Exception", _e);
            SECommons.handleException(_e);
        }
    }

    /**
     * Shows the criteria selection dialog and stores the chosen jimple
     * statement.
     * 
     * @param stmtlist The list of Jimple Stmts
     * @param file The Java file in which the criteria is present.
     * @param criteria The criteria the user has chosen.
     */
    private void allowUserToPickCriteria(final List stmtlist, final IFile file, final Criteria criteria) {
        final List _stlist = new ArrayList();

        for (int _i = 2; _i < stmtlist.size(); _i++) {
            _stlist.add(stmtlist.get(_i));
        }

        final IDialogSettings _settings = KaveriPlugin.getDefault().getDialogSettings();
        _settings.put("edu.ksu.indus.kaveri.stmtindex", -1);
        _settings.put("edu.ksu.indus.kaveri.considervalue", true);

        final StatementResolver _sr = new StatementResolver(Display.getDefault().getActiveShell(), _stlist);

        if (_sr.open() == IDialogConstants.OK_ID) {
            final int _chindex = _settings.getInt("edu.ksu.indus.kaveri.stmtindex");

            if (_chindex >= 0 && _chindex <= _stlist.size() - 1) {
                criteria.setNJimpleIndex(_chindex);
            } else {
                criteria.setNJimpleIndex(0);
            }

            final boolean _valuechoice = _settings.getBoolean("edu.ksu.indus.kaveri.considervalue");
            criteria.setBConsiderValue(_valuechoice);
            addToCriteria(file, criteria);
        }
    }

    /**
     * Processes the criteria for the given file and Java Statement.
     * 
     * @param file The Java file in which the criteria was picked
     * @param type The class of the chosen Java statement
     * @param element The IMethod of the chosen Java statement
     * @param nSelLine The selected line number.
     */
    private void processCriteriaForFile(final IFile file, final IType type, final IJavaElement element, final int nSelLine) {
        try {
            final List _stmtlist = SootConvertor.getStmtForLine(file, type, (IMethod) element, nSelLine);

            if (_stmtlist != null && _stmtlist.size() >= 3) {
                final Criteria _c = new Criteria();

                final int _noStmts = _stmtlist.size() - 2;
                _c.setStrClassName(PrettySignature.getSignature(type));
                _c.setStrMethodName(PrettySignature.getSignature(element));
                _c.setNLineNo(nSelLine);

                if (_noStmts >= 1) {
                    allowUserToPickCriteria(_stmtlist, file, _c);
                }
            }
        } catch (NullPointerException _ne) {
            KaveriErrorLog.logException("Null pointer exception", _ne);
            SECommons.handleException(_ne);
        }
    }
}
