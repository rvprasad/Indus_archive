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

package edu.ksu.cis.indus.toolkits.sliceeclipse.popup.actions;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.actions.SelectionConverter;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.search.PrettySignature;
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

import com.thoughtworks.xstream.XStream;

import edu.ksu.cis.indus.toolkits.eclipse.SootConvertor;
import edu.ksu.cis.indus.toolkits.sliceeclipse.SliceEclipsePlugin;
import edu.ksu.cis.indus.toolkits.sliceeclipse.dialogs.StatementResolver;
import edu.ksu.cis.indus.toolkits.sliceeclipse.preferencedata.Criteria;
import edu.ksu.cis.indus.toolkits.sliceeclipse.preferencedata.CriteriaData;

/**
 * Adds the chosen line to the criteria.
 * @author Ganeshan
 * 
 * 
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
     * (non-Javadoc).
     * 
     * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
     *      org.eclipse.ui.IEditorPart)
     */
    public void setActiveEditor(final IAction action, final IEditorPart targetEditor) {
        this.editor = (CompilationUnitEditor) targetEditor;
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(final IAction action) {

        if (selection instanceof ITextSelection) {
            final ITextSelection _tselection = (ITextSelection) selection;
            final String _text = _tselection.getText();
            final int _nSelLine = _tselection.getEndLine() + 1; // Havent
                                                                // figured out
                                                                // whats the 1
                                                                // for but it works.
            try {
                final IType _type = SelectionConverter.getTypeAtOffset(editor);
                final IJavaElement _element = SelectionConverter
                        .getElementAtOffset(editor);
                if (_element != null && (_element instanceof IMethod)) {
                    
                    final IFile _file = (IFile) ((IFileEditorInput) editor
                            .getEditorInput()).getFile();
                    processCriteriaForFile(_file, _type, _element, _nSelLine);
                    
                }
            } catch (JavaModelException _e) {
                _e.printStackTrace();
            }
        }

    }

    /**
     * Processes the criteria for the given file and Java Statement.
	 * @param file The Java file in which the criteria was picked
	 * @param type The class of the chosen Java statement
	 * @param element The IMethod of the chosen Java statement
	 * @param nSelLine The selected line number.
	 */
	private void processCriteriaForFile(final IFile file, final IType type, final IJavaElement element, final int nSelLine) {
        final SootConvertor _sc;
        final String _className = type.getElementName();
        final ArrayList _stmtlist = SootConvertor.getStmtForLine(file,
                type, (IMethod) element, nSelLine);
        if (_stmtlist != null && _stmtlist.size() >= 3) {

            final ArrayList _storeLst = new ArrayList();
            final int _noStmts = _stmtlist.size() - 2;
            _storeLst.add(PrettySignature.getSignature(type));
            _storeLst.add(PrettySignature.getSignature(element));
            _storeLst.add(new Integer(nSelLine));

            if (_noStmts > 1) {
            	allowUserToPickCriteria(_stmtlist, file, _storeLst);

			}

        }
		
	}

	/**
	 * Shows the criteria selection dialog and allows the user to pick Jimple criteria.
	 * @param stmtlist The list of Jimple Stmts
	 * @param file The Java file in which the criteria is present.
	 * @param storeLst The ArrayList of the criteria, format: classname, methodname, line number, index of chosen Jimple Stmt 
	 */
	private void allowUserToPickCriteria(final ArrayList stmtlist, final IFile file, final ArrayList storeLst) {
        final ArrayList _stlist = new ArrayList();
        for (int _i = 2; _i < stmtlist.size(); _i++) {
            _stlist.add(stmtlist.get(_i));
        }
        final IDialogSettings _settings = SliceEclipsePlugin
                .getDefault().getDialogSettings();
        _settings.put(
                "edu.ksu.indus.sliceeclipse.stmtindex", -1);
        final StatementResolver _sr = new StatementResolver(
                Display.getDefault().getActiveShell(),
                _stlist);
        if (_sr.open() == IDialogConstants.OK_ID) {
			final int _chindex = _settings
					.getInt("edu.ksu.indus.sliceeclipse.stmtindex");
			if (_chindex >= 0
					&& _chindex <= _stlist.size() - 1) {
				storeLst.add(new Integer(_chindex));
			} else {
				storeLst.add(new Integer(0));
			}
			addToCriteria(file, storeLst);
		}
		
	}

	/**
	 * Adds the given criteria to the project's set of criterias.
	 * 
	 * @param file
	 *            The file in which the criteria is present.
	 * @param stmtlist
	 *            Format: classname, methodname, linenumber, index of the chosen
	 *            jimple stmt among the list of matching stmts.
	 */
    private void addToCriteria(final IFile file, final ArrayList stmtlist) {
        final Criteria _criteria = new Criteria();
        _criteria
                .setFileName(file.getName() + "@" + System.currentTimeMillis());
        _criteria.setCriteria(stmtlist);
        _criteria.setDisabled(false);
        final IProject _project = file.getProject();
        final IResource _resource = _project;
        final QualifiedName _name = new QualifiedName(
                "edu.ksu.cis.indus.sliceeclipse", "criterias");
        CriteriaData _data = null;
        final XStream _xstream = new XStream();
        _xstream.alias("CriteriaData", CriteriaData.class);
        try {
            final String _propVal = _resource.getPersistentProperty(_name);
            if (_propVal == null) {
                _data = new CriteriaData();
                _data.setCriterias(new ArrayList());                
            } else {
                _data = (CriteriaData) _xstream.fromXML(_propVal);                
            }
            if (isOkToAdd(_data, _criteria)) {
				_data.getCriterias().add(_criteria);
			} else {
				MessageDialog.openError(null, "Duplicate",
						"Duplicate criteria are not allowed");
			}
            final String _xml = _xstream.toXML(_data);
            _resource.setPersistentProperty(_name, _xml);
        } catch (CoreException _e) {
            _e.printStackTrace();
        }
    }

    /**
     * Checks for duplicate criterias.
	 * @param data The array of criterias
	 * @param criteria The new criteria
	 * @return boolean True if the criteria can be added.
	 */
	private boolean isOkToAdd(final CriteriaData data, final Criteria criteria) {
		boolean _isOk = true;
		final ArrayList _lst = data.getCriterias();
		final ArrayList _clist = criteria.getCriteria();
		for (int _i = 0; _i < _lst.size(); _i++) {
			final Criteria _tcriteria = (Criteria) _lst.get(_i);
			final ArrayList _tlist = _tcriteria.getCriteria();
			if (_tlist.get(0).equals(_clist.get(0))
					&& _tlist.get(1).equals(_clist.get(1))
					&& _tlist.get(2).equals(_clist.get(2))
					&& _tlist.get(3).equals(_clist.get(3))) {
				_isOk = false;
				break;
			}
		}
		return _isOk;
	}

	/**
     * (non-Javadoc).
     * 
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(final IAction action, final ISelection textselection) {
        this.selection = textselection;
    }

}