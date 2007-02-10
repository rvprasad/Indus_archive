/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.views;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.infoView.CriteriaViewContentProvider;
import edu.ksu.cis.indus.kaveri.infoView.CriteriaViewLabelProvider;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.preferencedata.CriteriaData;

/**
 * @author ganeshan
 * 
 * Displays the list of criteria for the project.
 */
public class CriteriaView extends ViewPart {

    TableViewer crtViewer;

    private IAction remove;

    private IAction removeAll;

    /**
     * Create the actions.
     */
    private void createActions() {
        removeAll = new Action() {
            public void run() {
                final CriteriaData _cd = getCurrentCriteriaList();
                if (_cd != null) {
                    crtViewer.getTable().selectAll();
                    final List _lst = _cd.getCriterias();
                    final IStructuredSelection _ssl = (IStructuredSelection) crtViewer.getSelection();
                    for (final Iterator _t = _ssl.iterator(); _t.hasNext();) {
                        final Criteria _c = (Criteria) _t.next();
                        _lst.remove(_c);
                    }
                    _cd.setCriteria(_lst);
                    final IProject _prj = KaveriPlugin.getDefault().getIndusConfiguration().getCrtMaintainer().getProject();
                    saveNewCriteria(_prj, _cd);
                    crtViewer.getTable().removeAll();
                    crtViewer.refresh();
                    KaveriPlugin.getDefault().getIndusConfiguration().getStmtList().update();
                }
            }
        };

        removeAll.setText("Remove All");

        remove = new Action() {
            public void run() {
                final CriteriaData _cd = getCurrentCriteriaList();
                if (_cd != null && !crtViewer.getSelection().isEmpty()) {
                    final List _lst = _cd.getCriterias();
                    final IStructuredSelection _ssl = (IStructuredSelection) crtViewer.getSelection();
                    for (final Iterator _t = _ssl.iterator(); _t.hasNext();) {
                        final Criteria _c = (Criteria) _t.next();
                        _lst.remove(_c);
                    }
                    _cd.setCriteria(_lst);
                    final IProject _prj = KaveriPlugin.getDefault().getIndusConfiguration().getCrtMaintainer().getProject();
                    saveNewCriteria(_prj, _cd);
                    crtViewer.getTable().removeAll();
                    crtViewer.refresh();
                    KaveriPlugin.getDefault().getIndusConfiguration().getStmtList().update();
                }
            }
        };
        remove.setText("Remove");

    }

    /**
     * Create the popup menu.
     */
    private void createMenu() {
        final MenuManager _popMenuMangager = new MenuManager("#Popup");
        _popMenuMangager.setRemoveAllWhenShown(true);
        _popMenuMangager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(remove);
                manager.add(removeAll);
                manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
            }

        });
        final Menu _mnu = _popMenuMangager.createContextMenu(crtViewer.getControl());
        crtViewer.getControl().setMenu(_mnu);

    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        layoutControls(parent);
    }

    /**
     * Returns the current set of criteria.
     * 
     * @return DOCUMENT ME!
     */
    protected CriteriaData getCurrentCriteriaList() {
        final CriteriaListMaintainer _m = KaveriPlugin.getDefault().getIndusConfiguration().getCrtMaintainer();
        final IProject _prj = _m.getProject();
        if (_prj != null) {
            try {
                return PartialSliceView.retrieveCriteria(_prj);
            } catch (CoreException _e) {
                SECommons.handleException(_e);
            } catch (IOException _e) {
                SECommons.handleException(_e);
            }
        }
        return null;
    }

    /**
     * Hook double click.
     */
    private void hookDoubleClick() {
        crtViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(@SuppressWarnings("unused")
            DoubleClickEvent event) {
                final ISelection _sel = crtViewer.getSelection();
                if (!_sel.isEmpty() && _sel instanceof IStructuredSelection) {
                    final Criteria _c = (Criteria) ((IStructuredSelection) _sel).getFirstElement();
                    if (_c != null) {
                        final CriteriaListMaintainer _clm = KaveriPlugin.getDefault().getIndusConfiguration()
                                .getCrtMaintainer();
                        final IFile _file = _clm.getJavaFile();
                        if (_file != null) {
                            final ICompilationUnit _unit = JavaCore.createCompilationUnitFrom(_file);
                            if (_unit == null) { return; }
                            try {
                                final CompilationUnitEditor _editor = (CompilationUnitEditor) JavaUI.openInEditor(_unit);
                                if (_editor != null) {
                                    final int _lineno = _c.getNLineNo();
                                    final IRegion _region = _editor.getDocumentProvider().getDocument(
                                            _editor.getEditorInput()).getLineInformation(_lineno - 1);
                                    _editor.selectAndReveal(_region.getOffset(), _region.getLength());
                                }
                            } catch (PartInitException e) {
                                SECommons.handleException(e);
                                KaveriErrorLog.logException("Par init exception", e);
                            } catch (JavaModelException e) {
                                SECommons.handleException(e);
                                KaveriErrorLog.logException("Java model exception", e);
                            } catch (BadLocationException e) {
                                SECommons.handleException(e);
                                KaveriErrorLog.logException("Bad location", e);
                            }
                        }
                    }
                }

            }

        });

    }

    protected Composite layoutControls(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        _comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        _comp.setLayout(new GridLayout(1, true));

        crtViewer = new TableViewer(_comp, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
        final Table _table = crtViewer.getTable();
        final GridData _gd = new GridData(GridData.FILL_BOTH);
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalSpan = 1;
        _table.setLayoutData(_gd);
        setupTable(_table);
        crtViewer.setContentProvider(new CriteriaViewContentProvider());
        crtViewer.setLabelProvider(new CriteriaViewLabelProvider());
        crtViewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration().getCrtMaintainer());
        for (int _i = 0; _i < _table.getColumnCount(); _i++) {
            _table.getColumn(_i).pack();
        }
        createActions();
        createMenu();
        hookDoubleClick();
        return _comp;
    }

    /**
     * Save the new criteria.
     * 
     * @param prj DOCUMENT ME!
     * @param cd DOCUMENT ME!
     */
    protected void saveNewCriteria(IProject prj, CriteriaData cd) {
        if (prj != null) {
            try {
                PartialSliceView.saveCriteria(prj, cd);
            } catch (IOException _e) {
                SECommons.handleException(_e);
            }
        }

    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {

    }

    /**
     * Setup the table.
     * 
     * @param table The table.
     */
    private void setupTable(Table criteriaTable) {
        final String[] _colnames = {"Function", "Line number", "Jimple index", "Consider Execution"};

        for (int _i = 0; _i < _colnames.length; _i++) {
            final TableColumn _ti = new TableColumn(criteriaTable, SWT.NULL);
            _ti.setText(_colnames[_i]);
        }

        criteriaTable.setHeaderVisible(true);
        criteriaTable.setLinesVisible(true);

        for (int _i = 0; _i < criteriaTable.getColumnCount(); _i++) {
            criteriaTable.getColumn(_i).pack();
        }

    }
}
