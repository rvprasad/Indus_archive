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

package edu.ksu.cis.indus.kaveri.dependence;


import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import soot.SootMethod;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dependence.filters.MainFilter;

import edu.ksu.cis.indus.kaveri.views.DependenceHistoryData;
import edu.ksu.cis.indus.kaveri.views.DependenceStackData;
import edu.ksu.cis.indus.kaveri.views.PartialStmtData;

/**
 * @author ganeshan
 * 
 * This view keeps track of the dependencies.
 */
public class DependenceTrackingView extends ViewPart {

    /**
     * Used to display the Java and underlying Jimple.
     */
    private TreeViewer tvLeft;

    /**
     * Used to display the dependence information.
     */
    private TreeViewer tvRight;

    /**
     * Filter out Control dependence.
     */
    private ViewerFilter controlFilterFwd, controlFilterBck;

    /**
     * Filter out Data dependence.
     */
    private ViewerFilter dataFilterFwd, dataFilterBck;

    /**
     * Filter out Interference dependence.
     */
    private ViewerFilter interferenceFilterFwd, interferenceFilterBck;

    /**
     * Filter out Ready dependence.
     */
    private ViewerFilter readyFilterFwd, readyFilterBck;

    /**
     * Filter out Synchronization dependence.
     */
    private ViewerFilter synchFilterFwd, synchFilterBck;

    /**
     * Filter out Divergence dependence.
     */
    private ViewerFilter divergenceFilterFwd, divergenceFilterBck;

    private Action controlFilterActionFwd, controlFilterActionBck;

    private Action dataFilterActionFwd, dataFilterActionBck;

    private Action readyFilterActionFwd, readyFilterActionBck;

    private Action interferenceFilterActionFwd, interferenceFilterActionBck;

    private Action divergenceFilterActionFwd, divergenceFilterActionBck;

    private Action synchFilterActionFwd, synchFilterActionBck;

    private Action expandAll, contractAll;

    /**
     * Whether this view is active.
     */
    private boolean isActive = false;

    

    /** The input to the right pane */
    private DependenceStmtData dsd;

    class StandarLabelProvider extends LabelProvider {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        public String getText(Object element) {
            return element.toString();
        }
    }

    /**
     * Constructor.
     */
    public DependenceTrackingView() {
        dsd = new DependenceStmtData();
    
    }

    /**
     * Create the dialog areas.
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        _comp.setLayout(new GridLayout(1, true));

        final SashForm _sForm = new SashForm(_comp, SWT.HORIZONTAL);
        GridData _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _sForm.setLayoutData(_gd);

        final Composite _lComp = new Composite(_sForm, SWT.NONE);
        _lComp.setLayout(new GridLayout(1, true));

        final Label _lblLeft = new Label(_lComp, SWT.LEFT);
        _lblLeft.setText("Statement");
        _gd = new GridData(GridData.FILL_HORIZONTAL);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _lblLeft.setLayoutData(_gd);
        _lblLeft.setFont(parent.getFont());

        tvLeft = new TreeViewer(_lComp, SWT.SINGLE | SWT.BORDER);
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        tvLeft.getTree().setLayoutData(_gd);
        tvLeft.getTree().setFont(parent.getFont());

        final Composite _rComp = new Composite(_sForm, SWT.NONE);
        _rComp.setLayout(new GridLayout(1, true));

        final Label _lblRight = new Label(_rComp, SWT.LEFT);
        _lblRight.setText("Dependence");
        _gd = new GridData(GridData.FILL_HORIZONTAL);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _lblRight.setLayoutData(_gd);
        _lblRight.setFont(parent.getFont());

        tvRight = new TreeViewer(_rComp, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        tvRight.getTree().setLayoutData(_gd);
        tvRight.getTree().setFont(parent.getFont());

        _sForm.setWeights(new int[] { 4, 6 });
        final IToolBarManager _manager = getViewSite().getActionBars()
                .getToolBarManager();
        fillToolBar(_manager);

        tvLeft.setContentProvider(new DepTrkStmtLstContentProvider());
        tvLeft.setLabelProvider(new StandarLabelProvider());
        tvLeft.setInput(KaveriPlugin.getDefault().getIndusConfiguration()
                .getStmtList());
        tvLeft.setAutoExpandLevel(1);

        tvRight.setContentProvider(new DepTrkDepLstContentProvider());
        tvRight.setLabelProvider(new StandarLabelProvider());
        tvRight.setInput(dsd);
        hookListeners();
        createFilters();
        hookFiltersFwd();
        hookFiltersBck();
        createActions();
        createMenus();
        hookDoubleClickListeners();
    }

    /**
     * Create any actions.
     */
    private void createActions() {
        expandAll = new Action() {
            public void run() {
                tvRight.expandAll();
            }
        };
        expandAll.setText("Expand All");

        contractAll = new Action() {
            public void run() {
                tvRight.collapseAll();
            }
        };
        contractAll.setText("Collapse All");
    }

    /**
     * Create the dependee actions.
     */
    private void hookFiltersBck() {
        controlFilterActionBck = new Action("Control") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(controlFilterBck);
                } else {
                    tvRight.addFilter(controlFilterBck);
                }
            }
        };
        controlFilterActionBck.setChecked(true);

        dataFilterActionBck = new Action("Data") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(dataFilterBck);
                } else {
                    tvRight.addFilter(dataFilterBck);
                }
            }
        };
        dataFilterActionBck.setChecked(true);

        interferenceFilterActionBck = new Action("Interference") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(interferenceFilterBck);
                } else {
                    tvRight.addFilter(interferenceFilterBck);
                }
            }
        };
        interferenceFilterActionBck.setChecked(true);

        readyFilterActionBck = new Action("Ready") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(readyFilterBck);
                } else {
                    tvRight.addFilter(readyFilterBck);
                }
            }
        };
        readyFilterActionBck.setChecked(true);

        synchFilterActionBck = new Action("Synchronization") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(synchFilterBck);
                } else {
                    tvRight.addFilter(synchFilterBck);
                }
            }
        };
        synchFilterActionBck.setChecked(true);

        divergenceFilterActionBck = new Action("Divergence") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(divergenceFilterBck);
                } else {
                    tvRight.addFilter(divergenceFilterBck);
                }
            }
        };
        divergenceFilterActionBck.setChecked(true);

    }

    /**
     * Handle double click in the right pane.
     */
    private void hookDoubleClickListeners() {
        tvRight.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                if (!(event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection)) {
                    final IStructuredSelection _ssl = (IStructuredSelection) event
                            .getSelection();
                    if (_ssl.getFirstElement() instanceof RightPaneTreeObject) {
                        final RightPaneTreeObject _rto = (RightPaneTreeObject) _ssl
                                .getFirstElement();
                        final SootMethod _sm = _rto.getSm();
                        if (_sm == null)
                            return;
                        final String _className = _sm.getDeclaringClass()
                                .getName();
                        final int nLineNo = _rto.getLineNumber();
                        final IFile _file = SECommons.getFileContainingClass(
                                _sm, dsd.getJavaFile());
                        if (_file != null && nLineNo != -1) {
                            final ICompilationUnit _unit = JavaCore
                                    .createCompilationUnitFrom(_file);
                            if (_unit != null) {
                                final CompilationUnitEditor _editor;
                                try {
                                    _editor = (CompilationUnitEditor) JavaUI
                                            .openInEditor(_unit);                                
                                if (_editor != null) {
                                    final IRegion _region = _editor
                                            .getDocumentProvider().getDocument(
                                                    _editor.getEditorInput())
                                            .getLineInformation(nLineNo - 1);
                                    final String _text = _editor
                                            .getDocumentProvider().getDocument(
                                                    _editor.getEditorInput())
                                            .get(_region.getOffset(),
                                                    _region.getLength());
                                    final String _trimmedString = _text.trim();
                                    PartialStmtData _psd = KaveriPlugin
                                            .getDefault()
                                            .getIndusConfiguration()
                                            .getStmtList();
                                    final DependenceHistoryData _dhd = KaveriPlugin.getDefault().getIndusConfiguration().getDepHistory();
                                    
                                    final DependenceStackData _depS = new DependenceStackData(
                                            _psd);
                                    Pair _pair = null;
                                    _editor.selectAndReveal(
                                            _region.getOffset(), _region
                                                    .getLength());
                                    
                                    if (_dhd.getSize() == 0) {
                                        _pair = new Pair(_depS, "Starting Program Point");
                                        KaveriPlugin.getDefault()
                                        .getIndusConfiguration()
                                        .setDepHistory(_pair);
                                        
                                    } else {
                                        final DependenceStackData _dOlS = (DependenceStackData) _dhd.getCurrentItem().getFirst();
                                        if (!_dOlS.equals(_depS)) {
                                            _pair = new Pair(_depS, "Starting Program Point");
                                            KaveriPlugin.getDefault()
                                            .getIndusConfiguration()
                                            .setDepHistory(_pair);
                                        }                                            
                                    }
                                    _psd = KaveriPlugin
                                    .getDefault()
                                    .getIndusConfiguration()
                                    .getStmtList();
                                    RightPaneTreeParent _rtp = _rto.getParent();
                                    if (_rtp.getSm() !=  null) {
                                        _rtp = _rtp.getParent();
                                    }
                                    String _depLink = _rtp.toString() + " ";
                                    if (_rtp.getParent().toString().equals("Dependents")) {
                                        _depLink += "Dependent";
                                    } else if (_rtp.getParent().toString().equals("Dependees")) {
                                        _depLink += "Dependee";
                                    }
                                    final DependenceStackData _dCurr = new DependenceStackData(_psd);
                                    _pair = new Pair(_dCurr, _depLink);
                                    KaveriPlugin.getDefault().getIndusConfiguration().setDepHistory(_pair);
                                                                                                                                                                                                                       
                                }
                                
                                } catch (PartInitException e) {
                                    KaveriErrorLog.logException("Par Init Exception", e);
                                  SECommons.handleException(e);
                                } catch (JavaModelException e) {
                                    KaveriErrorLog.logException("Java Model Exception", e);
                                    SECommons.handleException(e);
                                } catch (BadLocationException e) {
                                    KaveriErrorLog.logException("Bad Location Exception", e);
                                    SECommons.handleException(e);
                                }

                            }
                        }
                    }
                }

            }

        });

    }

    /**
     * Create the filters.
     */
    private void createFilters() {
        controlFilterFwd = new MainFilter("Control", true);
        controlFilterBck = new MainFilter("Control", false);

        dataFilterFwd = new MainFilter("Data", true);
        dataFilterBck = new MainFilter("Data", false);

        interferenceFilterFwd = new MainFilter("Interference", true);
        interferenceFilterBck = new MainFilter("Interference", false);

        readyFilterFwd = new MainFilter("Ready", true);
        readyFilterBck = new MainFilter("Ready", false);

        divergenceFilterFwd = new MainFilter("Divergence", true);
        divergenceFilterBck = new MainFilter("Divergence", false);

        synchFilterFwd = new MainFilter("Synchronization", true);
        synchFilterBck = new MainFilter("Synchronization", false);
    }

    /**
     * Add the filters,
     */
    private void hookFiltersFwd() {
        controlFilterActionFwd = new Action("Control") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(controlFilterFwd);
                } else {
                    tvRight.addFilter(controlFilterFwd);
                }
            }
        };
        controlFilterActionFwd.setChecked(true);

        dataFilterActionFwd = new Action("Data") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(dataFilterFwd);
                } else {
                    tvRight.addFilter(dataFilterFwd);
                }
            }
        };
        dataFilterActionFwd.setChecked(true);

        interferenceFilterActionFwd = new Action("Interference") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(interferenceFilterFwd);
                } else {
                    tvRight.addFilter(interferenceFilterFwd);
                }
            }
        };
        interferenceFilterActionFwd.setChecked(true);

        readyFilterActionFwd = new Action("Ready") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(readyFilterFwd);
                } else {
                    tvRight.addFilter(readyFilterFwd);
                }
            }
        };
        readyFilterActionFwd.setChecked(true);

        synchFilterActionFwd = new Action("Synchronization") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(synchFilterFwd);
                } else {
                    tvRight.addFilter(synchFilterFwd);
                }
            }
        };
        synchFilterActionFwd.setChecked(true);

        divergenceFilterActionFwd = new Action("Divergence") {
            public void run() {
                if (this.isChecked()) {
                    tvRight.removeFilter(divergenceFilterFwd);
                } else {
                    tvRight.addFilter(divergenceFilterFwd);
                }
            }
        };
        divergenceFilterActionFwd.setChecked(true);

    }

    /**
     * Create the filter menus.
     *  
     */
    private void createMenus() {
        final IMenuManager _mainMnuManager = getViewSite().getActionBars()
                .getMenuManager();
        _mainMnuManager.setRemoveAllWhenShown(true);
        _mainMnuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager mgr) {
                fillMainMenu(mgr);
            }
        });
        fillMainMenu(_mainMnuManager);

        final MenuManager _popMenuMangager = new MenuManager("#Popup");
        _popMenuMangager.setRemoveAllWhenShown(true);
        _popMenuMangager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                manager.add(expandAll);
                manager.add(contractAll);
                manager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));
            }

        });
        final Menu _mnu = _popMenuMangager.createContextMenu(tvRight
                .getControl());
        tvRight.getControl().setMenu(_mnu);
    }

    /**
     * Fill the menu.
     * 
     * @param mnuMainMenu
     */
    protected void fillMainMenu(IMenuManager mnuMainMenu) {
        final IMenuManager _filterMenu = new MenuManager("Filters");
        mnuMainMenu.add(_filterMenu);

        final IMenuManager _dependeeMenu = new MenuManager("Dependees");
        final IMenuManager _dependentsMenu = new MenuManager("Dependents");
        _filterMenu.add(_dependeeMenu);
        _filterMenu.add(_dependentsMenu);

        _dependeeMenu.add(controlFilterActionBck);
        _dependeeMenu.add(dataFilterActionBck);
        _dependeeMenu.add(interferenceFilterActionBck);
        _dependeeMenu.add(divergenceFilterActionBck);
        _dependeeMenu.add(readyFilterActionBck);
        _dependeeMenu.add(synchFilterActionBck);

        _dependentsMenu.add(controlFilterActionFwd);
        _dependentsMenu.add(dataFilterActionFwd);
        _dependentsMenu.add(interferenceFilterActionFwd);
        _dependentsMenu.add(divergenceFilterActionFwd);
        _dependentsMenu.add(readyFilterActionFwd);
        _dependentsMenu.add(synchFilterActionFwd);

    }

    /**
     * Add listeners to user actions.
     */
    private void hookListeners() {
        tvLeft.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                if (!(event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection)) {
                    final IStructuredSelection _ss = (IStructuredSelection) event
                            .getSelection();
                    if (_ss.getFirstElement() instanceof LeftPaneTreeParent) {
                        final LeftPaneTreeParent _tp = (LeftPaneTreeParent) _ss
                                .getFirstElement();
                        dsd.setupData(KaveriPlugin.getDefault()
                                .getIndusConfiguration().getStmtList(), -1);
                    } else if (_ss.getFirstElement() instanceof LeftPaneTreeObject) {
                        final LeftPaneTreeObject _to = (LeftPaneTreeObject) _ss
                                .getFirstElement();
                        dsd.setupData(KaveriPlugin.getDefault()
                                .getIndusConfiguration().getStmtList(), _to
                                .getJimpleIndex());
                    }
                }

            }

        });

    }

    /**
     * Fill the toolbar.
     * 
     * @param manager
     *            The Toolbar manager.
     */
    private void fillToolBar(IToolBarManager _manager) {
        final Action _changeView = new Action() {
            public void run() {
                if (isActive) {
                    isActive = false;
                    final ImageDescriptor _desc = AbstractUIPlugin
                            .imageDescriptorFromPlugin(
                                    "edu.ksu.cis.indus.kaveri",
                                    "data/icons/trackView.gif");
                    this.setImageDescriptor(_desc);
                    this.setToolTipText("Track Java Statements (Inactive)");
                    ((DepTrkStmtLstContentProvider) tvLeft.getContentProvider())
                            .setActive(isActive);
                } else {
                    isActive = true;
                    final ImageDescriptor _desc = AbstractUIPlugin
                            .imageDescriptorFromPlugin(
                                    "edu.ksu.cis.indus.kaveri",
                                    "data/icons/trackViewAct.gif");
                    this.setImageDescriptor(_desc);
                    this.setToolTipText("Track Java Statements (Active)");
                    ((DepTrkStmtLstContentProvider) tvLeft.getContentProvider())
                            .setActive(isActive);
                    tvLeft.setInput(KaveriPlugin.getDefault()
                            .getIndusConfiguration().getStmtList());
                }
            }
        };

        _changeView.setToolTipText("Track Java Statements (Inactive)");
        final ImageDescriptor _desc = AbstractUIPlugin
                .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                        "data/icons/trackView.gif");
        _changeView.setImageDescriptor(_desc);
        _manager.add(_changeView);
    }

    /**
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
        tvLeft.getControl().setFocus();

    }

}