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

package edu.ksu.cis.indus.kaveri.peq;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.ResourceManager;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.views.IDeltaListener;
import edu.ksu.cis.indus.kaveri.views.PartialStmtData;
import edu.ksu.cis.indus.peq.customengine.IndusExistentialQueryEngine;
import edu.ksu.cis.indus.peq.customengine.IndusMatcher;
import edu.ksu.cis.indus.peq.customengine.IndusUniversalQueryEngine;

import edu.ksu.cis.indus.peq.fsm.EFreeNFA2DFATransformer;
import edu.ksu.cis.indus.peq.fsm.EpsClosureConvertor;
import edu.ksu.cis.indus.peq.fsm.FSMBuilder$v1_2;
import edu.ksu.cis.indus.peq.graph.GraphBuilder;
import edu.ksu.cis.indus.peq.graph.Node;
import edu.ksu.cis.indus.peq.indusinterface.IndusInterface;
import edu.ksu.cis.indus.peq.queryglue.QueryConvertor;
import edu.ksu.cis.indus.peq.queryglue.QueryObject;
import edu.ksu.cis.peq.fsm.interfaces.IFSM;
import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;
import edu.ksu.cis.peq.graph.interfaces.INode;
import edu.ksu.cis.peq.queryengine.AbstractQueryEngine;
import edu.ksu.cis.peq.queryengine.IQueryProgressListener;
import edu.ksu.cis.peq.queryengine.UniversalQueryEngine$v1;


import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.Bundle;

import soot.SootMethod;
import soot.jimple.Stmt;

/**
 * @author ganeshan
 * 
 * This is the view that enables PEQ related stuff in Kaveri.
 *  
 */
public class PEQView extends ViewPart implements IDeltaListener {
    /**
     * Shows the statement that is the entry point.
     */
    Text txtStatement;

    /**
     * The cached copy of the jimple statements.
     */
    private PartialStmtData cachedPSD;

    /**
     * Indicates if the view is ready.
     */
    private boolean isReady;

    /**
     * The text box for entering the query.
     */
    Combo cmbQuery;

    /**
     * The list of queries.
     */
    private List queryList;
    
    /**
     * The Tree tvLeft for showing the results.
     */
    TreeViewer tvLeft;

    /**
     * The table viewer for showing the mappings.
     */
    TableViewer tvRight;

    /** Query results */
    private Collection queryResults = Collections.EMPTY_LIST;

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl(Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        _comp.setLayout(new GridLayout(2, false));
        
        final boolean _isValid = checkForPlugins();
        if (!_isValid) {
            final Label _lblWarning = new Label(_comp, SWT.CENTER);
            _lblWarning.setText("Unable to find Peq and ANTLR plugins. Peq View will be disabled.");
            final GridData _gl = new GridData(GridData.FILL_BOTH);
            _gl.horizontalSpan = 2;
            _gl.grabExcessHorizontalSpace = true;
            _gl.grabExcessVerticalSpace = true;
            _lblWarning.setLayoutData(_gl);
            return;            
        }
        
        final ResourceManager _rm = KaveriPlugin.getDefault()
                .getIndusConfiguration().getRManager();

        final Label _lblStatement = new Label(_comp, SWT.NONE);
        _lblStatement.setText("Statement:");
        GridData _gd = new GridData();
        _gd.horizontalSpan = 1;
        _lblStatement.setLayoutData(_gd);

        txtStatement = new Text(_comp, SWT.BORDER);
        txtStatement.setEditable(false);
        _gd = new GridData(GridData.FILL_HORIZONTAL);
        _gd.grabExcessHorizontalSpace = true;
        _gd.horizontalSpan = 1;
        txtStatement.setLayoutData(_gd);
        txtStatement.setBackground(_rm.getColor(new RGB(255, 255, 255)));

        final Label _lblQuery = new Label(_comp, SWT.NONE);
        _lblQuery.setText("Query:");
        _gd = new GridData();
        _gd.horizontalSpan = 1;
        _lblQuery.setLayoutData(_gd);

        cmbQuery = new Combo(_comp, SWT.READ_ONLY);
        _gd = new GridData(GridData.FILL_HORIZONTAL);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        cmbQuery.setLayoutData(_gd);

        final Group _grp = new Group(_comp, SWT.BORDER);
        _grp.setText("Results");
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 2;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _grp.setLayoutData(_gd);
        _grp.setLayout(new GridLayout(1, true));

        final SashForm _sForm = new SashForm(_grp, SWT.HORIZONTAL);
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _sForm.setLayoutData(_gd);

        tvLeft = new TreeViewer(_sForm, SWT.SINGLE | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
        final Tree _tree = tvLeft.getTree();
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessVerticalSpace = true;
        _gd.grabExcessHorizontalSpace = true;
        _tree.setLayoutData(_gd);
        tvLeft.setContentProvider(new ViewContentProvider());
        tvLeft.setLabelProvider(new LabelProvider());
        tvLeft.setInput(queryResults);

        tvRight = new TableViewer(_sForm, SWT.SINGLE | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.BORDER);
        final Table _table = tvRight.getTable();
        setupTable(_table);
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessVerticalSpace = true;
        _gd.grabExcessHorizontalSpace = true;
        _table.setLayoutData(_gd);
        tvRight.setContentProvider(new TableContentProvider());
        tvRight.setLabelProvider(new TableLabelProvider());
        _sForm.setWeights(new int[] { 4, 6 });

        tvLeft.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                if (!(event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection)) {
                    final IStructuredSelection _ss = (IStructuredSelection) event
                            .getSelection();
                    final Object _selObject = _ss.getFirstElement();
                    Map _resultMap = null;
                    if (_selObject instanceof TreeParent) {
                        final TreeParent _tp = (TreeParent) _selObject;
                        _resultMap = _tp.getMapping();
                        if (_resultMap == null) {
                        	// Root element
                        	final TreeObject[] _tarr = _tp.getChildren();
                        	if (_tarr != null && _tarr.length > 0) {
                        		final TreeObject _tchild = _tarr[_tarr.length - 1];
                        		_resultMap = _tchild.getMapping();
                        	}
                        }
                    } else if (_selObject instanceof TreeObject) {
                        final TreeObject _tp = (TreeObject) _selObject;
                        _resultMap = _tp.getParent().getMapping();
                    }
                    if (_resultMap != null) {
                        tvRight.setInput(_resultMap);
                    } else {
                        tvRight.setInput("");
                    }
                    final Table _table = tvRight.getTable();
                    for (int i = 0; i < _table.getColumnCount(); i++) {
                        _table.getColumn(i).pack();
                    }
                }

            }

        });

        final IToolBarManager _manager = getViewSite().getActionBars()
                .getToolBarManager();
        fillToolBar(_manager);
        hookDoubleClickListener();
        KaveriPlugin.getDefault().getIndusConfiguration().getStmtList()
                .addListener(this);
        initQueries();
    }

    /**
     * Determines if the appropriate plugins have been installed.
     * @return boolean If all the required plugins are installed.	
     */
    private boolean checkForPlugins() {
        boolean _result = false;
        final Bundle _bundle = Platform.getBundle("edu.ksu.cis.peq");
        if (_bundle != null) {
            final Bundle _antLr = Platform.getBundle("org.antlr");
            if (_antLr != null) {
                _result = true;
            }
        }
        return _result;
    }

    /**
     * Load any saved queries.
     */
    private void initQueries() {
        final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();
        final String _qryKey = "edu.ksu.cis.indus.kaveri.peq.query";
        String _val = _ps.getString(_qryKey);
        if (!_val.equals("")) {
            final XStream _xstream = new XStream(new DomDriver());
            final List _lst = (List) _xstream.fromXML(_val);
            if (_lst != null) {
                for (Iterator iter = _lst.iterator(); iter.hasNext();) {
                    cmbQuery.add(iter.next().toString());                    
                }
                queryList = _lst;
                cmbQuery.select(0);
            }
        }  else {
            queryList = new LinkedList();
        }
        
    }

    /**
     * Hook the double click listener.
     */
    private void hookDoubleClickListener() {
        tvLeft.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                if (!(event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection)) {
                    final Object _obj = ((IStructuredSelection) event
                            .getSelection()).getFirstElement();
                    if (_obj instanceof TreeObject
                            && !(_obj instanceof TreeParent)) {
                        final Pair _info = ((TreeObject) _obj).getInformation();
                        if (_info != null) {
                            final Stmt _stmt = (Stmt) _info.getFirst();
                            final SootMethod _sm = (SootMethod) _info
                                    .getSecond();
                            final int _lineno = SECommons
                                    .getLineNumberForStmt(_stmt);
                            if (_lineno != -1
                                    && cachedPSD.getJavaFile() != null) {
                                final IFile _sampleFile = cachedPSD
                                        .getJavaFile();
                                final IFile _file = SECommons
                                        .getFileContainingClass(_sm,
                                                _sampleFile);

                                final ICompilationUnit _unit = JavaCore
                                        .createCompilationUnitFrom(_file);
                                if (_unit != null) {
                                    final CompilationUnitEditor _editor;
                                    try {
                                        _editor = (CompilationUnitEditor) JavaUI
                                                .openInEditor(_unit);
                                        if (_editor != null) {
                                            final IRegion _region = _editor
                                                    .getDocumentProvider()
                                                    .getDocument(
                                                            _editor
                                                                    .getEditorInput())
                                                    .getLineInformation(
                                                            _lineno - 1);
                                            _editor.selectAndReveal(_region.getOffset(), _region.getLength());
                                        }
                                    } catch (PartInitException e) {
                                        KaveriErrorLog.logException(
                                                "Par Init Exception", e);
                                        SECommons.handleException(e);
                                    } catch (JavaModelException e) {
                                        KaveriErrorLog.logException(
                                                "Java Model Exception", e);
                                        SECommons.handleException(e);
                                    } catch (BadLocationException e) {
                                        KaveriErrorLog.logException(
                                                "Bad Location Exception", e);
                                        SECommons.handleException(e);
                                    }
                                }

                            }
                        }
                    }
                }
            }

        });

    }

    /**
     * Fill the toolbar.
     * 
     * @param manager
     */
    private void fillToolBar(IToolBarManager manager) {

        final IAction _actSwitch = new Action() {
            public void run() {
                if (isReady) {
                    isReady = false;
                    final ImageDescriptor _desc = AbstractUIPlugin
                            .imageDescriptorFromPlugin(
                                    "edu.ksu.cis.indus.kaveri",
                                    "data/icons/trackView.gif");
                    this.setImageDescriptor(_desc);
                    this.setToolTipText("Tracking Disabled");
                } else {
                    final ImageDescriptor _desc = AbstractUIPlugin
                            .imageDescriptorFromPlugin(
                                    "edu.ksu.cis.indus.kaveri",
                                    "data/icons/trackViewAct.gif");
                    this.setImageDescriptor(_desc);
                    isReady = true;
                    this.setToolTipText("Tracking Enabled");
                }

            }
        };

        _actSwitch.setToolTipText("Tracking disabled");
        final ImageDescriptor _swdesc = AbstractUIPlugin
                .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                        "data/icons/trackView.gif");
        _actSwitch.setImageDescriptor(_swdesc);
        manager.add(_actSwitch);

        final IAction _addQuery = new Action() {
            public void run() {
                final Shell _parentShell = getViewSite().getShell();
                final QueryEntryDialog _qed = new QueryEntryDialog(_parentShell);
                if (_qed.open() == IDialogConstants.OK_ID) {
                    final String _newQuery = _qed.getQueryString();
                    if (!queryList.contains(_newQuery)) {
                        queryList.add(_newQuery);
                        cmbQuery.add(_newQuery);
                        cmbQuery.select(cmbQuery.getItemCount() - 1);                        
                    } else {
                        cmbQuery.select(cmbQuery.indexOf(_newQuery));
                    }                                        
                }                
            }
        };
        _addQuery.setToolTipText("Add a query");
        final ImageDescriptor _addqdesc = AbstractUIPlugin
                .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                        "data/icons/addQuery.gif");
        _addQuery.setImageDescriptor(_addqdesc);
        manager.add(_addQuery);

        final IAction _actQuery = new Action() {
            public void run() {
                if (!isReady)
                    return;
                final Shell _parentShell = getViewSite().getShell();
                if (cmbQuery.getText().equals("")) {
                    MessageDialog.openError(_parentShell, "Query Missing",
                            "Please write a query before running the engine");
                    return;
                }
                final String _queryString = cmbQuery.getText();
                IProgressService progressService = PlatformUI.getWorkbench()
                        .getProgressService();
                try {
                    progressService
                            .busyCursorWhile(new IRunnableWithProgress() {
                                public void run(final IProgressMonitor monitor) {
                                    monitor.beginTask("Processing Query", 100);
                                    final PartialStmtData _psd = cachedPSD;
                                    if (_psd != null && _psd.getStmtList() != null
                                            && _psd.getSelectedStatement() != null
                                            && _psd.getStmtList().size() > 2) {
                                        final List _jimpleList = _psd
                                                .getStmtList().subList(
                                                        2,
                                                        _psd.getStmtList()
                                                                .size());
                                        final Stmt _stmt = (Stmt) _jimpleList
                                                .get(_jimpleList.size() - 1);
                                        final SootMethod _sm = (SootMethod) _psd
                                                .getStmtList().get(1);
                                        monitor.worked(5);
                                        final QueryConvertor _qc = new QueryConvertor();
                                        final QueryObject _qo = _qc
                                                .getQueryObject(_queryString);
                                        monitor.worked(15);
                                        if (_qo == null) {
                                            Display.getDefault().asyncExec(
                                                    new Runnable() {
                                                        public void run() {
                                                            MessageDialog
                                                                    .openError(
                                                                            _parentShell,
                                                                            "Query Error",
                                                                            _qc
                                                                                    .getErrorString());
                                                        }
                                                    });

                                        } else {
                                            FSMBuilder$v1_2 _fbuilder = new FSMBuilder$v1_2(
                                                    _qo);
                                            EpsClosureConvertor _ecc = new EpsClosureConvertor(_fbuilder);
                                            _ecc.processShallow();
                                            final IFSM _eFreeFSM = _ecc.getResult();
                                            final EFreeNFA2DFATransformer _efn2dt = new EFreeNFA2DFATransformer(_eFreeFSM);
                            				_efn2dt.process();		
                            				final IFSM _dfaFSM = _efn2dt.getDfaAutomata();
                                            final Collection _rootCollection = new LinkedList();
                                            final Pair _initPair = new Pair(
                                                    _stmt, _sm);
                                            _rootCollection.add(_initPair);
                                            final GraphBuilder _gbuilder = new GraphBuilder(
                                                    _rootCollection);
                                            final IndusMatcher _matcher = new IndusMatcher();
                                            final IQueryProgressListener _listener = new IQueryProgressListener() {

                                                public void queryProgress(
                                                        QueryProgressEvent arg0) {
                                                    monitor.worked(10);
                                                    monitor.setTaskName(arg0
                                                            .getMessage());

                                                }

                                            };
                                            IndusInterface
                                                    .getInstance()
                                                    .setSlicer(
                                                            KaveriPlugin
                                                                    .getDefault()
                                                                    .getSlicerTool());
                                            AbstractQueryEngine _ieeq;
                                            if (_qo.isExistential()) {
                                                _ieeq = new IndusExistentialQueryEngine(
                                                        _gbuilder, _dfaFSM,
                                                        _matcher);
                                                ((IndusExistentialQueryEngine) _ieeq).addListener(_listener); 
                                            } else {
                                                _ieeq = new IndusUniversalQueryEngine(_gbuilder,  _dfaFSM, _matcher);
                                                ((UniversalQueryEngine$v1) _ieeq).addListener(_listener);
                                            }                                             
                                            
                                            _ieeq.execute();
                                            queryResults = _ieeq.getResults();
                                            Display.getDefault().asyncExec(
                                                    new Runnable() {
                                                        public void run() {
                                                            tvLeft
                                                                    .setInput(queryResults);
                                                            tvRight
                                                                    .setInput("");
                                                            final Table _table = tvRight
                                                                    .getTable();
                                                            for (int i = 0; i < _table
                                                                    .getColumnCount(); i++) {
                                                                _table
                                                                        .getColumn(
                                                                                i)
                                                                        .pack();
                                                            }
                                                        }
                                                    });
                                        }

                                    }
                                    monitor.done();
                                }
                            });
                } catch (InvocationTargetException e) {
                    SECommons.handleException(e);
                } catch (InterruptedException e) {
                    SECommons.handleException(e);
                }
            }
        };
        final ImageDescriptor _desc = AbstractUIPlugin
                .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                        "data/icons/runQuery.gif");
        _actQuery.setImageDescriptor(_desc);
        manager.add(_actQuery);
        _actQuery.setToolTipText("Run the Query");

        final IAction _actUpdate = new Action() {
            public void run() {
                if (isReady) {
                    try {
                        cachedPSD = (PartialStmtData) KaveriPlugin.getDefault()
                                .getIndusConfiguration().getStmtList().clone();
                    } catch (CloneNotSupportedException e) {
                        SECommons.handleException(e);
                        return;
                    }
                    if (cachedPSD.getStmtList() != null
                            && cachedPSD.getSelectedStatement() != null
                            && cachedPSD.getStmtList().size() > 2) {
                        txtStatement.setText(cachedPSD.getSelectedStatement());

                        // Reset the table.
                    }
                    queryResults = Collections.EMPTY_LIST;
                    tvLeft.setInput(queryResults);
                    tvRight.setInput("");
                    final Table _table = tvRight.getTable();
                    for (int i = 0; i < _table.getColumnCount(); i++) {
                        _table.getColumn(i).pack();
                    }
                }
            }
        };
        final ImageDescriptor _descUpd = AbstractUIPlugin
                .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                        "data/icons/update.gif");
        _actUpdate.setImageDescriptor(_descUpd);
        manager.add(_actUpdate);
        _actUpdate.setToolTipText("Update the statement");

        final IAction _removeQuery = new Action() {
        	public void run() {
        		final int _index = cmbQuery.getSelectionIndex();
        		if (_index != -1) {
        			cmbQuery.remove(_index);
        			queryList.remove(_index);    
        			if (cmbQuery.getItemCount() > 0) {
        				cmbQuery.select(0);
        			}
        		}
        	}
        };
        final ImageDescriptor _descRemv = AbstractUIPlugin
        .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                "data/icons/rem_co.gif");
        _removeQuery.setImageDescriptor(_descRemv);
        manager.add(_removeQuery);
        _removeQuery.setToolTipText("Remove the query");
    }

    /**
     * Setup the table.
     * 
     * @param table
     *            The table.
     */
    private void setupTable(Table table) {
        final TableColumn _col1 = new TableColumn(table, SWT.NONE);
        _col1.setText("Parameter");

        final TableColumn _col2 = new TableColumn(table, SWT.NONE);
        _col2.setText("Substituition");

        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        for (int _i = 0; _i < table.getColumnCount(); _i++) {
            table.getColumn(_i).pack();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#setFocus()
     */
    public void setFocus() {
    	if (cmbQuery != null) {
        cmbQuery.setFocus();
    	}
    }

    class TreeObject implements IAdaptable {
        private String name;

        private TreeParent parent;

        private Map mapping;

        private Pair information;

        public TreeObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setParent(TreeParent parent) {
            this.parent = parent;
        }

        public TreeParent getParent() {
            return parent;
        }

        public String toString() {
            return getName();
        }

        public Object getAdapter(Class key) {
            return null;
        }

        /**
         * @return Returns the statement.
         */
        public Map getMapping() {
            return mapping;
        }

        public void setMapping(final Map mapping) {
            this.mapping = mapping;
        }

        /**
         * @return Returns the information.
         */
        public Pair getInformation() {
            return information;
        }

        /**
         * @param information
         *            The information to set.
         */
        public void setInformation(Pair information) {
            this.information = information;
        }
    }

    class TreeParent extends TreeObject {
        private ArrayList children;

        public TreeParent(String name) {
            super(name);
            children = new ArrayList();
        }

        public void addChild(TreeObject child) {
            children.add(child);
            child.setParent(this);
        }

        public void removeChild(TreeObject child) {
            children.remove(child);
            child.setParent(null);
        }

        public TreeObject[] getChildren() {
            return (TreeObject[]) children.toArray(new TreeObject[children
                    .size()]);
        }

        public boolean hasChildren() {
            return children.size() > 0;
        }

        /**
         * Removes all the children.
         */
        public void removeAllChildren() {
            for (int i = 0; i < children.size(); i++) {
                ((TreeObject) children.get(i)).setParent(null);
            }
            children.clear();
        }
    }

    /**
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content
     *  
     */
    class ViewContentProvider implements ITreeContentProvider {
        private TreeParent invisibleRoot;

        /**
         * Returns the elements to show in the view.
         * 
         * @param parent
         *            The parent.
         * 
         * @return Object[] The list of statements if any present.
         */
        public Object[] getElements(final Object parent) {
            if (invisibleRoot == null) {
                invisibleRoot = new TreeParent("");
            }
            initialize();
            return getChildren(invisibleRoot);
        }

        public void initialize() {
            int _ctr = 1;
            invisibleRoot.removeAllChildren();
            for (Iterator iter = queryResults.iterator(); iter.hasNext();) {
                final Collection _oneResult = (Collection) iter.next();
                final TreeParent _parent = new TreeParent("Result " + _ctr);
                for (Iterator iterator = _oneResult.iterator(); iterator
                        .hasNext();) {
                    final IFSMToken _token = (IFSMToken) iterator.next();
                    final INode _srcNode = _token.getGraphEdge().getSrcNode();
                    final INode _dstnNode = _token.getGraphEdge().getDstnNode();
                    final TreeParent _tpEdge = new TreeParent("Edge");
                    _tpEdge.setMapping(_token.getSubstituitionMap());
                    final String _msg1 = "Source: " + _srcNode.toString();
                    final String _msg2 = "Destination: " + _dstnNode.toString();

                    final TreeObject _object1 = new TreeObject(_msg1);
                    _object1.setInformation((Pair) ((Node) _srcNode)
                            .getInformation());
                    final TreeObject _object2 = new TreeObject(_msg2);
                    _object2.setInformation((Pair) ((Node) _dstnNode)
                            .getInformation());
                    _tpEdge.addChild(_object1);
                    _tpEdge.addChild(_object2);
                    _parent.addChild(_tpEdge);
                }
                invisibleRoot.addChild(_parent);
                _ctr++;
            }
        }

        /**
         * Dispose any created resources.
         */
        public void dispose() {
            System.gc();

        }

        /**
         * The input has changed. Register for receiving any changes.
         * 
         * @param v
         *            The current tvLeft
         * @param oldInput
         *            The old input to the view.
         * @param newInput
         *            The new input to the view.
         */
        public void inputChanged(final Viewer v, final Object oldInput,
                final Object newInput) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof TreeParent) {
                return ((TreeParent) parentElement).getChildren();
            }
            return new Object[0];
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
            if (element instanceof TreeObject) {
                return ((TreeObject) element).getParent();
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        public boolean hasChildren(Object element) {
            if (element instanceof TreeParent) {
                return ((TreeParent) element).hasChildren();
            } else
                return false;
        }
    }

    class TableContentProvider implements IStructuredContentProvider {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        public Object[] getElements(Object inputElement) {
            if (inputElement instanceof Map) {
                final Set _entrySet = ((Map) inputElement).entrySet();
                return _entrySet.toArray();
            }
            return new Object[0];
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
            // TODO Auto-generated method stub

        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

    }

    class TableLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        public Image getColumnImage(Object element, int columnIndex) {
            // TODO Auto-generated method stub
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        public String getColumnText(Object element, int columnIndex) {
            String _retString = "";
            if (element instanceof Map.Entry) {
                final Map.Entry _entry = (Map.Entry) element;
                switch (columnIndex) {
                case 0:
                    _retString = _entry.getKey().toString();
                    break;
                case 1:
                    _retString = _entry.getValue().toString();
                    break;
                }
            }
            return _retString;
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {
        KaveriPlugin.getDefault().getIndusConfiguration().getStmtList()
                .removeListener(this);
     
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#propertyChanged()
     */
    public void propertyChanged() {
        // cachedPSD =
        // KaveriPlugin.getDefault().getIndusConfiguration().getStmtList();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#isReady()
     */
    public boolean isReady() {
        return isReady;
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
        saveQueries();
        super.saveState(memento);
    }
    /**
     * Save the queries.
     *
     */
    public void saveQueries() {
        final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();
        final String _qryKey = "edu.ksu.cis.indus.kaveri.peq.query";
        if (cmbQuery.getItemCount() > 0) {
            final int _count = cmbQuery.getItemCount();
            final List _lst = new LinkedList();
            for (int i = 0; i < _count; i++) {
                final String _query = cmbQuery.getItem(i);
                if (!_query.equals("") && !_query.equals("Equery default {<> <> }; ")) {
                    _lst.add(_query);
                }
            }
            final XStream _xstream = new XStream(new DomDriver());
            final String _val = _xstream.toXML(_lst);
            _ps.setValue(_qryKey, _val);
            KaveriPlugin.getDefault().savePluginPreferences();
        }
    }
}

