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

package edu.ksu.cis.indus.kaveri.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import soot.jimple.Stmt;
import edu.ksu.cis.indus.common.soot.NamedTag;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.driver.EclipseIndusDriver;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.preferencedata.CriteriaData;

/**
 * <p>
 * Partial slice view. This class creates a view that shows the Jimple
 * statements for a given Java statement and indicates if they have a slice tag
 * or not. This view is only active after slicing has been performed.
 * </p>
 */
public class PartialSliceView extends ViewPart {
    /**
     * <p>
     * The table viewer for this view.
     * </p>
     */
    private TableViewer viewer;

    /**
     * The partial statement data.
     */
    private PartialStmtData partialData;

    /**
     * The list of Jimple stmts in the criteria.
     */
    private Map crtList;

    /**
     * <p>
     * The text label for the statement
     * </p>
     */
    Text txt;

    /**
     * View is ready to receive the data.
     */
    private boolean isReady = false;

    /**
     * The constructor.
     */
    public PartialSliceView() {
        isReady = false;
        crtList = new HashMap();
    }

    /**
     * The content provider class is responsible for providing objects to the
     * view. It can wrap existing objects in adapters or simply return objects
     * as-is. These objects may be sensitive to the current input of the view,
     * or ignore it and always show the same content
     *  
     */
    class ViewContentProvider implements IStructuredContentProvider,
            IDeltaListener {
        /**
         * Returns the elements to show in the view.
         * 
         * @param parent
         *            The parent.
         * 
         * @return Object[] The list of statements if any present.
         */
        public Object[] getElements(final Object parent) {
            Object[] _retObj = new Object[0];
            // _lst := Classname, method name, stmt.
            if (parent instanceof PartialStmtData && isReady) {
                final PartialStmtData _psd = (PartialStmtData) parent;
                partialData = _psd;
                final List _lst = _psd.getStmtList();
                final IFile _file = _psd.getJavaFile();

                if (_lst != null && _file != null) {
                    final List _sub = _lst.subList(2, _lst.size());
                    setupCriteriaStmts(_psd);
                    txt.setText(_psd.getSelectedStatement() + " ("
                            + _file.getName() + ")");
                    // Eliminate the first two elements.
                    _retObj = _sub.toArray();
                } else {
                    _retObj = new Object[0];
                }
            }
            return _retObj;
        }

        /**
         * Setup the list of jimple statements which are criteria.
         * 
         * @param psd
         *            The partial statement data.
         */
        private void setupCriteriaStmts(PartialStmtData psd) {
            final String _className = psd.getClassName();
            final String _methodName = psd.getMethodName();
            final int _nLineno = psd.getLineNo();
            final List _jimpleList = psd.getStmtList().subList(2,
                    psd.getStmtList().size());

            final IProject _project = psd.getJavaFile().getProject();
            final CriteriaData _cd = getCriteriaDataForProject(_project);
            if (_cd == null)
                return;
            final List _crtList = _cd.getCriterias();
            for (Iterator iter = _crtList.iterator(); iter.hasNext();) {
                Criteria _crt = (Criteria) iter.next();
                if (_crt.getStrClassName().equals(_className)
                        && _crt.getStrMethodName().equals(_methodName)
                        && _crt.getNLineNo() == _nLineno) {
                    // Found a criteria at the given point, hooray!
                    crtList.put(_jimpleList.get(_crt.getNJimpleIndex()),
                            Boolean.valueOf(_crt.isBConsiderValue()));

                }
            }
        }

        /**
         * Returns the set of criteria stored for the given project.
         * 
         * @param project
         * @return CriteriaData The set of stored criteria.
         */
        private CriteriaData getCriteriaDataForProject(IProject project) {

            final QualifiedName _name = new QualifiedName(
                    "edu.ksu.cis.indus.kaveri", "criterias");
            CriteriaData _data = null;
            final XStream _xstream = new XStream();
            _xstream.alias("CriteriaData", CriteriaData.class);
            try {
                final String _propVal = project.getPersistentProperty(_name);
                if (_propVal == null) {
                    _data = new CriteriaData();
                    _data.setCriterias(new ArrayList());
                } else {
                    _data = (CriteriaData) _xstream.fromXML(_propVal);
                }
            } catch (CoreException _ce) {
                KaveriErrorLog.logException("Core Exception Exception", _ce);
                return null;
            }
            return _data;
        }

        /**
         * Dispose any created resources.
         */
        public void dispose() {
        }

        /**
         * The input has changed. Register for receiving any changes.
         * 
         * @param v
         *            The current viewer
         * @param oldInput
         *            The old input to the view.
         * @param newInput
         *            The new input to the view.
         */
        public void inputChanged(final Viewer v, final Object oldInput,
                final Object newInput) {

            if (oldInput != null) {
                ((PartialStmtData) oldInput).removeListener(this);
            }

            if (newInput != null) {
                ((PartialStmtData) newInput).addListener(this);
            }
        }

        /**
         * The slice statement list has changed. Refresh the view.
         * 
         * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#propertyChanged()
         */
        public void propertyChanged() {
            if (viewer != null && isReady) {
                crtList.clear();
                viewer.refresh();
                final TableColumn _cols[] = viewer.getTable().getColumns();
                for (int _i = 0; _i < _cols.length; _i++) {
                    _cols[_i].pack();
                }
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see edu.ksu.cis.indus.kaveri.views.IDeltaListener#isReady()
         */
        public boolean isReady() {
            return isReady;
        }
    }

    
    /**
     * <p>
     * This class provides the labels for the elements shown in the view.
     * </p>
     * 
     *  
     */
    class ViewLabelProvider extends LabelProvider implements
            ITableLabelProvider, IColorProvider {

        /**
         * Get the image label for the given column.
         * 
         * @param obj
         *            The object for which the image is needed
         * @param index
         *            The column
         * @return Image The image for the given column
         */
        public Image getColumnImage(final Object obj, final int index) {
            return null;
        }

        /**
         * Returns the textual representation of the element to be shown.
         * 
         * @param obj
         *            The object whose value is to be shown
         * @param index
         *            The column number
         * 
         * @return String The textual representation of the object
         */
        public String getColumnText(final Object obj, final int index) {
            String _retString = "";

            if (index == 0) {
                _retString = getText(obj);
            } else {
                if (obj instanceof Stmt) {
                    _retString = "" + isSliceTagPresent((Stmt) obj);
                }
            }
            return _retString;
        }

        /**
         * Returns the image label for the given object.
         * 
         * @param obj
         *            The object for which the image label is needed.
         * @return Image The image for the given object.
         */
        public Image getImage(final Object obj) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                    ISharedImages.IMG_OBJ_ELEMENT);
        }

        /**
         * Indicates if the given Jimple statement has the slice tag.
         * 
         * @param stmt
         *            The jimple statement for which the presence of the tag is
         *            to be tested
         * 
         * @return boolean Whether the statement has the tag or not.
         */
        private boolean isSliceTagPresent(final Stmt stmt) {
            boolean _btagpresent = false;
            final EclipseIndusDriver _driver = KaveriPlugin.getDefault()
                    .getIndusConfiguration().getEclipseIndusDriver();
            final NamedTag _sTag = (NamedTag) stmt.getTag(_driver
                    .getNameOfSliceTag());

            if (_sTag != null) {
                _btagpresent = true;
            }
            return _btagpresent;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
         */
        public Color getForeground(Object element) {
            if (element instanceof Stmt && crtList.keySet().contains(element)) {
                final Boolean _bConsiderVal = (Boolean) crtList.get(element);
                if (_bConsiderVal.booleanValue()) {
                    return KaveriPlugin.getDefault().getIndusConfiguration()
                            .getRManager().getColor(new RGB(0, 0, 189));
                } else {
                    return KaveriPlugin.getDefault().getIndusConfiguration()
                            .getRManager().getColor(new RGB(255, 0, 0));
                }
            }
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
         */
        public Color getBackground(Object element) {

            return null;
        }
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize
     * it.
     * 
     * @param parent
     *            The parent control
     */
    public void createPartControl(final Composite parent) {
        final Composite _comp = new Composite(parent, SWT.NONE);
        final GridLayout _layout = new GridLayout();
        _layout.numColumns = 2;
        _layout.horizontalSpacing = 10;
        _layout.marginWidth = 10;
        _comp.setLayout(_layout);

        final Label _lbl = new Label(_comp, SWT.LEFT);
        _lbl.setText("Statement: ");
        GridData _dataL = new GridData();
        _dataL.horizontalSpan = 1;
        _lbl.setLayoutData(_dataL);

        txt = new Text(_comp, SWT.LEFT);
        _dataL = new GridData();
        _dataL.horizontalSpan = 1;
        _dataL.horizontalAlignment = GridData.FILL;
        _dataL.grabExcessHorizontalSpace = true;
        txt.setLayoutData(_dataL);

        txt.setEditable(false);
        //txt.setText(" ");

        final Table _table = createTable(_comp);
        final GridData _data = new GridData();
        _data.horizontalSpan = 2;
        _data.grabExcessHorizontalSpace = true;
        _data.horizontalAlignment = GridData.FILL;
        _data.grabExcessVerticalSpace = true;
        _data.verticalAlignment = GridData.FILL;
        //_data.grabExcessVerticalSpace = true;
        _table.setLayoutData(_data);

        _comp.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                TableColumn _col1 = _table.getColumn(0);
                _col1.setWidth(_comp.getSize().x * 2 / 3);
                _col1 = _table.getColumn(1);
                _col1.setWidth(_comp.getSize().x / 3);
            }
        });

        viewer = new TableViewer(_table);

        //viewer = new CheckboxTableViewer(_table);
        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        viewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration()
                .getStmtList());
        final IToolBarManager _manager = getViewSite().getActionBars()
                .getToolBarManager();
        fillToolBar(_manager);
    }

    /**
     * Fills the toolbar.
     * 
     * @param manager
     */
    private void fillToolBar(IToolBarManager manager) {
        Action _actionLoad = new Action() {
            public void run() {
                if (isReady) {
                    isReady = false;
                    final ImageDescriptor _desc = AbstractUIPlugin
                            .imageDescriptorFromPlugin(
                                    "edu.ksu.cis.indus.kaveri",
                                    "data/icons/trackView.gif");
                    this.setImageDescriptor(_desc);
                    this.setToolTipText("Track Java Statements (Inactive)");
                } else {
                    final ImageDescriptor _desc = AbstractUIPlugin
                            .imageDescriptorFromPlugin(
                                    "edu.ksu.cis.indus.kaveri",
                                    "data/icons/trackViewAct.gif");
                    this.setImageDescriptor(_desc);
                    isReady = true;
                    this.setToolTipText("Track Java Statements (Active)");
                    viewer.setInput(KaveriPlugin.getDefault()
                            .getIndusConfiguration().getStmtList());
                }
            }
        };

        _actionLoad.setToolTipText("Track Java Statements (Inactive)");
        final ImageDescriptor _desc = AbstractUIPlugin
                .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                        "data/icons/trackView.gif");
        _actionLoad.setImageDescriptor(_desc);
        manager.add(_actionLoad);

        Action _actionAddCriteriaTop = new Action() {
            public void run() {
                if (viewer.getTable().getSelectionIndex() != -1
                        && partialData != null
                        && partialData.getClassName() != null) {
                    final List _stmtList = partialData.getStmtList().subList(2,
                            partialData.getStmtList().size());
                    final Stmt _stmt = (Stmt) _stmtList.get(viewer.getTable()
                            .getSelectionIndex());
                    if (crtList.containsKey(_stmt)) {
                        MessageDialog.openError(null, "Error",
                                "Duplicate Criteria are not allowed!");
                    } else {
                        addToCriteria(viewer.getTable().getSelectionIndex(),
                                false);
                        crtList.put(_stmt, new Boolean(false));
                        viewer.refresh();
                    }
                }
            }

        };

        _actionAddCriteriaTop.setToolTipText("Add as criteria (Control)");
        final ImageDescriptor _descCrt = AbstractUIPlugin
                .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                        "data/icons/addCriteriaTop.gif");
        _actionAddCriteriaTop.setImageDescriptor(_descCrt);
        manager.add(_actionAddCriteriaTop);

        Action _actionAddCriteriaBottom = new Action() {
            public void run() {
                if (viewer.getTable().getSelectionIndex() != -1
                        && partialData != null
                        && partialData.getClassName() != null) {
                    final List _stmtList = partialData.getStmtList().subList(2,
                            partialData.getStmtList().size());
                    final Stmt _stmt = (Stmt) _stmtList.get(viewer.getTable()
                            .getSelectionIndex());
                    if (crtList.containsKey(_stmt)) {
                        MessageDialog.openError(null, "Error",
                                "Duplicate Criteria are not allowed!");
                    } else {
                        addToCriteria(viewer.getTable().getSelectionIndex(),
                                true);
                        crtList.put(_stmt, new Boolean(true));
                        viewer.refresh();
                    }
                }
            }

        };

        _actionAddCriteriaBottom.setToolTipText("Add as criteria (Value)");
        final ImageDescriptor _descCrtBot = AbstractUIPlugin
                .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                        "data/icons/addCriteriaBot.gif");
        _actionAddCriteriaBottom.setImageDescriptor(_descCrtBot);
        manager.add(_actionAddCriteriaBottom);

        Action _actionRemoveCriteria = new Action() {
            public void run() {
                if (viewer.getTable().getSelectionIndex() != -1
                        && partialData != null
                        && partialData.getClassName() != null) {
                    final List _stmtList = partialData.getStmtList().subList(2,
                            partialData.getStmtList().size());
                    final Stmt _stmt = (Stmt) _stmtList.get(viewer.getTable()
                            .getSelectionIndex());
                    if (!crtList.containsKey(_stmt)) {
                        MessageDialog.openError(null, "Error",
                                "Statement is not a criteria");
                    } else {
                        removeCriteria(viewer.getTable().getSelectionIndex());
                        crtList.remove(_stmt);
                        viewer.refresh();
                    }
                }
            }

        };

        _actionRemoveCriteria.setToolTipText("Remove criteria");
        final ImageDescriptor _descCrtRem = AbstractUIPlugin
                .imageDescriptorFromPlugin("edu.ksu.cis.indus.kaveri",
                        "data/icons/remCriteria.gif");
        _actionRemoveCriteria.setImageDescriptor(_descCrtRem);
        manager.add(_actionRemoveCriteria);

    }

    /**
     * Removes the statement as specified by the index from the criteria.
     * 
     * @param selectionIndex
     */
    protected void removeCriteria(int selectionIndex) {
        final IFile _file = partialData.getJavaFile();
        final IResource _resource = _file.getProject();
        final QualifiedName _name = new QualifiedName(
                "edu.ksu.cis.indus.kaveri", "criterias");
        CriteriaData _data = null;
        final XStream _xstream = new XStream(new DomDriver());
        _xstream.alias("CriteriaData", CriteriaData.class);

        try {
            final String _propVal = _resource.getPersistentProperty(_name);

            if (_propVal == null) {
                _data = new CriteriaData();
                _data.setCriterias(new ArrayList());
            } else {
                _data = (CriteriaData) _xstream.fromXML(_propVal);
            }

            for (Iterator iter = _data.getCriterias().iterator(); iter
                    .hasNext();) {
                final Criteria _c = (Criteria) iter.next();
                if (_c.getStrClassName().equals(partialData.getClassName())
                        && _c.getStrMethodName().equals(
                                partialData.getMethodName())
                        && _c.getNLineNo() == partialData.getLineNo()
                        && _c.getNJimpleIndex() == selectionIndex) {
                    _data.getCriterias().remove(_c);
                    break;
                }

            }

            final String _xml = _xstream.toXML(_data);
            _resource.setPersistentProperty(_name, _xml);
        } catch (CoreException _e) {
            SECommons.handleException(_e);
            KaveriErrorLog.logException("Core Exception", _e);
        }

    }

    /**
     * Add the criteria as specified by the given jimple index.
     * 
     * @param considerVal
     *            Consider the value.
     * @param selectionIndex
     */
    private void addToCriteria(int selectionIndex, final boolean considerVal) {
        final IFile _file = partialData.getJavaFile();
        final IResource _resource = _file.getProject();
        final QualifiedName _name = new QualifiedName(
                "edu.ksu.cis.indus.kaveri", "criterias");
        CriteriaData _data = null;
        final XStream _xstream = new XStream(new DomDriver());
        _xstream.alias("CriteriaData", CriteriaData.class);

        try {
            final String _propVal = _resource.getPersistentProperty(_name);

            if (_propVal == null) {
                _data = new CriteriaData();
                _data.setCriterias(new ArrayList());
            } else {
                _data = (CriteriaData) _xstream.fromXML(_propVal);
            }
            final Criteria _c = new Criteria();
            _c.setStrClassName(partialData.getClassName());
            _c.setStrMethodName(partialData.getMethodName());
            _c.setNLineNo(partialData.getLineNo());
            _c.setNJimpleIndex(selectionIndex);
            _c.setBConsiderValue(considerVal);

            _data.getCriterias().add(_c);

            final String _xml = _xstream.toXML(_data);
            _resource.setPersistentProperty(_name, _xml);
        } catch (CoreException _e) {
            KaveriErrorLog.logException("Core Exception", _e);
            SECommons.handleException(_e);
        }
    }

    /**
     * @param _table
     */
    /*
     * private void updateTable(Table _table) { _table.setLinesVisible(true);
     * _table.setHeaderVisible(true);
     * 
     * final TableColumn _col1 = new TableColumn(_table, SWT.NONE);
     * _col1.setText("Statement");
     * 
     * final TableColumn _col2 = new TableColumn(_table, SWT.NONE);
     * _col2.setText("Part of Slice"); _col1.pack(); _col2.pack(); }
     */

    /**
     * Creates the table.
     * 
     * @param parent
     *            The parent composite
     * 
     * @return Table The table
     */
    private Table createTable(final Composite parent) {
        final Table _table = new Table(parent, SWT.SINGLE | SWT.BORDER
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION
                | SWT.HIDE_SELECTION);
        _table.setLinesVisible(true);
        _table.setHeaderVisible(true);

        final TableColumn _col1 = new TableColumn(_table, SWT.NONE);
        _col1.setText("Jimple Statement");

        final TableColumn _col2 = new TableColumn(_table, SWT.NONE);
        _col2.setText("Part of Slice");

        return _table;
    }

    /**
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {
        super.dispose();
    }
}