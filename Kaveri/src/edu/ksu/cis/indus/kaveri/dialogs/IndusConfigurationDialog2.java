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
 * Created on Apr 1, 2004
 *
 * Displays the configuration choose dialog
 *
 */
package edu.ksu.cis.indus.kaveri.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.jibx.runtime.JiBXException;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.alias.CannotResolveClassException;
import com.thoughtworks.xstream.io.xml.DomDriver;

import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.scoping.ClassSpecification;
import edu.ksu.cis.indus.common.scoping.FieldSpecification;
import edu.ksu.cis.indus.common.scoping.MethodSpecification;
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.callgraph.ContextContentProvider;
import edu.ksu.cis.indus.kaveri.callgraph.ContextDialog;
import edu.ksu.cis.indus.kaveri.callgraph.ContextLabelProvider;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.preferencedata.CriteriaData;
import edu.ksu.cis.indus.kaveri.rootmethodtrapper.RootMethodCollection;
import edu.ksu.cis.indus.kaveri.rootmethodtrapper.RootMethodContentProvider;
import edu.ksu.cis.indus.kaveri.rootmethodtrapper.RootMethodLabelProvider;
import edu.ksu.cis.indus.kaveri.scoping.ScopeDialog;
import edu.ksu.cis.indus.kaveri.scoping.ScopeViewContentProvider;
import edu.ksu.cis.indus.kaveri.scoping.ScopeViewLabelProvider;
import edu.ksu.cis.indus.tools.IToolConfiguration;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * The slice configuration dialog box. Allows you to pick the configuration and
 * the criteria for slicing.
 * 
 * @author Ganeshan
 */
public class IndusConfigurationDialog2 extends Dialog implements IStatusChangeListener {
    /**
     * Checkbox for additive or normal slicing.
     */
    private Button additive;

    /**
     * The collection of possibly modified classpath entries.
     */
    private Set classPathSet;
    /**
     * Configuration combo box.
     */
    private Combo confCombo;

    /**
     * View combo box.
     */
    private Combo viewCombo;

    /**
     * Viewer for the criteria.
     */
    private CheckboxTableViewer crtViewer;
    
    /**
     * Viewer for the context.
     */
    private CheckboxTableViewer ctxViewer;

    /** Viewer for the scope */
    private CheckboxTableViewer scpViewer;
    
    /** Viewer for the root methods */
    private CheckboxTableViewer rootViewer;
    
    /**
     * The Java project to which the file belongs.
     */
    private IJavaProject project;

    /**
     * The id for the j2b interface.
     */
    private static final int J2B_ID = 1337;

    
    
    /**
     * The swt table showing the criteria.
     */
    private Table criteriaTable;

    protected Collection deleteCollection;

    protected RootMethodCollection rmColl;
    
    /**
     * The list of the classpaths 
     */
    private org.eclipse.swt.widgets.List cpList;

    class ViewContentProvider implements IStructuredContentProvider {
        public void inputChanged(Viewer v, Object oldInput, Object newInput) {

        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            IResource _resource;
            final List _retList = new LinkedList();
            final XStream _xstream = new XStream(new DomDriver());
            _xstream
                    .alias(
                            Messages.getString("IndusConfigurationDialog.17"), CriteriaData.class); //$NON-NLS-1$

            try {
                _resource = project.getCorrespondingResource();

                final QualifiedName _name = new QualifiedName(Messages
                        .getString("IndusConfigurationDialog.18"), Messages
                        .getString("IndusConfigurationDialog.19"));

                try {
                    //				_resource.setPersistentProperty(_name, null); // Knocks
                    // out
                    // the stuff
                    final String _propVal = _resource
                            .getPersistentProperty(_name);

                    if (_propVal != null) {
                        final CriteriaData _data = (CriteriaData) _xstream
                                .fromXML(_propVal);
                        final java.util.List _lst = _data.getCriterias();

                        for (int _i = 0; _i < _lst.size(); _i++) {
                            final Criteria _c = (Criteria) _lst.get(_i);
                            _retList.add(_c);
                        }
                    }
                } catch (CannotResolveClassException _crce) {
                    SECommons.handleException(_crce);
                } catch (CoreException _e) {
                    SECommons.handleException(_e);
                }
            } catch (JavaModelException _e1) {
                SECommons.handleException(_e1);
            }
            return _retList.toArray();
        }

    }

    /**
     * The constructor.
     * 
     * @param parent
     *            The parent control
     * @param javaProject
     *            The current Java project.
     */
    public IndusConfigurationDialog2(final Shell parent,
            final IJavaProject javaProject) {
        super(parent);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.project = javaProject;
        deleteCollection = new ArrayList();
        classPathSet = new HashSet();
    }

    /**
     * Configures the shell.
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);        
        newShell.setText(Messages.getString("IndusConfigurationDialog.0")); //$NON-NLS-1$
    }

    /**
     * Creates the run and cancel buttons.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    protected void createButtonsForButtonBar(final Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, Messages
                .getString("IndusConfigurationDialog.29"), true);
        
        createButton(parent, IndusConfigurationDialog2.J2B_ID, "Run J2B", false);
        
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Creates the dialog area .
     * 
     * @param parent
     *            The parent control
     * 
     * @return Control The new control
     */
    protected Control createDialogArea(final Composite parent) {
        final Composite _composite = new Composite(parent, SWT.NONE);
        final GridLayout _layout = new GridLayout();
        _layout.numColumns = 3;
        _composite.setLayout(_layout);
        GridData _gComp = new GridData(GridData.FILL_BOTH);
        _gComp.grabExcessHorizontalSpace = true;
        _gComp.grabExcessVerticalSpace = true;
        _gComp.grabExcessVerticalSpace = true;
        _gComp.horizontalSpan = 1;
        _composite.setLayoutData(_gComp);
        
        
        final Label _confLabel = new Label(_composite, SWT.NONE);
        _confLabel.setText(Messages.getString("IndusConfigurationDialog.1")); //$NON-NLS-1$

        GridData _gdata = new GridData();
        _gdata.horizontalSpan = 1;
        _confLabel.setLayoutData(_gdata);

        confCombo = new Combo(_composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        _gdata = new GridData();
        _gdata.horizontalSpan = 2;
        _gdata.grabExcessHorizontalSpace = true;
        confCombo.setLayoutData(_gdata);
        initializeConfigs(confCombo);      

        
        additive = new Button(_composite, SWT.CHECK);
        _gdata = new GridData();
        _gdata.horizontalSpan = 3;
        additive.setLayoutData(_gdata);
        
        additive.setText("Additive slice display");
                initializeAdditive();

         final TabFolder _folder = new TabFolder(_composite, SWT.NONE);
         _gdata = new GridData(GridData.FILL_BOTH);
         _gdata.grabExcessHorizontalSpace = true;
         _gdata.grabExcessVerticalSpace = true;
         _gdata.horizontalSpan = 3;
         _gdata.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
         _gdata.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4; 
         _folder.setLayoutData(_gdata);
         
         _folder.setLayout(new GridLayout(1, true));
         
         final TabItem  _itemCriteria = new TabItem(_folder, SWT.NONE);
         _itemCriteria.setText("Criteria");
         _itemCriteria.setControl(createCriteriaTab(_folder));
         
         final TabItem _itemScope =  new TabItem(_folder, SWT.NONE);
         _itemScope.setText("Scope");
         _itemScope.setControl(createScopeControl(_folder));
         
         final TabItem _itemContexts = new TabItem(_folder, SWT.NONE);
         _itemContexts.setText("Contexts");
         _itemContexts.setControl(createContextControl(_folder));
         
         final TabItem _itemRootMethods = new TabItem(_folder, SWT.NONE);
         _itemRootMethods.setText("Root Methods");
         _itemRootMethods.setControl(createRootMethodControl(_folder));

         final TabItem _itemClassPath = new TabItem(_folder, SWT.NONE);
         _itemClassPath.setText("Slice Class Path");
         _itemClassPath.setControl(createJavaBuildPathControl(_folder));
         
         
        // Add griddata
        GridData _data = new GridData();
        _data.horizontalSpan = 1;
        _confLabel.setLayoutData(_data);
        _data = new GridData(GridData.FILL_HORIZONTAL);
        _data.horizontalSpan = 2;
        confCombo.setLayoutData(_data);

        // Reset the scope string
        KaveriPlugin.getDefault().getIndusConfiguration()
                .setScopeSpecification("");
        KaveriPlugin.getDefault().getIndusConfiguration()
        		.setDoResidualize(false);
        /*
         * KaveriPlugin.getDefault().getIndusConfiguration().resetChosenContext();
         */
        return _composite;
    }

    /**
     * Create the classpath control.
     * @param folder The parent folder.
     * @return Control
     */
    private Control createJavaBuildPathControl(TabFolder folder) {
        final Composite _comp = new Composite(folder, SWT.NONE); 
        _comp.setLayout(new GridLayout(1, false));
        GridData _gd = new GridData(GridData.FILL_BOTH);        
        _gd.horizontalSpan = 1;
        _comp.setLayoutData(_gd);
       
        final Composite _barComp = new Composite(_comp, SWT.NONE);
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalSpan = 1;
        _barComp.setLayoutData(_gd);
        _barComp.setLayout(new GridLayout(2, false));
        
        cpList = new org.eclipse.swt.widgets.List(_barComp, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL
                | SWT.H_SCROLL);
        _gd = new GridData(GridData.FILL_BOTH);        
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        cpList.setLayoutData(_gd);
        
        initializeClassPathList();
        
        final Composite _rightComp = new Composite(_barComp, SWT.NONE);
        _gd = new GridData(GridData.FILL_VERTICAL);        
        _gd.horizontalSpan = 1;        
        _gd.grabExcessVerticalSpace = true;
        _rightComp.setLayoutData(_gd);
        
        final RowLayout _rl = new RowLayout(SWT.VERTICAL);
        _rl.fill = true;
        _rl.marginHeight = 5;
        _rl.marginWidth = 2;
        _rightComp.setLayout(_rl);
        
        final Dialog _parent = this;
        
        final Button  _btnEdit = new Button(_rightComp, SWT.PUSH);
        _btnEdit.setText("Edit");
        _btnEdit.addSelectionListener(
                new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        if (cpList.getSelectionIndex() != -1) {
                            final String _chosenString = cpList.getSelection()[0];
                            final InputDialog _id = new InputDialog(_parent.getShell(), "Classpath Entry", 
                                    "Change the classpath entry", _chosenString,  null);
                            if (_id.open() == IDialogConstants.OK_ID) {
                                cpList.setItem(cpList.getSelectionIndex(), _id.getValue());
                            }
                        }
                    }
                }
                );
        final Button _btnDelete = new Button(_rightComp, SWT.PUSH);
        _btnDelete.setText("Delete");
        _btnDelete.addSelectionListener(
                new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        if (cpList.getSelectionIndex() != -1) {
                            cpList.remove(cpList.getSelectionIndex());
                        }
                    }
                }
                );
        final Button _btnAdd = new Button(_rightComp, SWT.PUSH);
        _btnAdd.setText("Add jar");
        _btnAdd.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                final Shell _shell = _parent.getShell();
                final FileDialog _fd = new FileDialog(_shell);
                _fd.setFilterExtensions(new String[]{"*.jar"});
                _fd.setText("Pick a library file");
                final String _lib = _fd.open();
                if (_lib != null ) {           
                    java.io.File _fl = new java.io.File(_lib);
                    final String _fileName = _fl.getName();
                    final int _extIndex = _fileName.lastIndexOf(".");
                    if (_extIndex != -1) {
                        final String _ext = _fileName.substring(_extIndex + 1);
                        if (_ext != null && _ext.equals("jar")) {
                            cpList.add(_lib);    
                        } else {
                            MessageDialog.openError(null, "Invalid library", "This is not a java library");
                        }
                    }  else {
                        MessageDialog.openError(null, "Invalid library", "This is not a java library");
                    }                                                           
                    
                }
            }
            });
        
        
        return _comp;
    }
    

    /**
     * Fill the classpath list.
     */
    private void initializeClassPathList() {
        final Set _cpSet = SECommons.getClassPathForProject(project, new HashSet(), false, false);
        for (Iterator iter = _cpSet.iterator(); iter.hasNext();) {
            String _cpEntry = iter.next().toString();            
            cpList.add(_cpEntry);            
        }
        
    }

    /**
     * Create the root method tab.
     * @param folder
     * @return
     */
    private Control createRootMethodControl(TabFolder folder) {
        final Composite _comp = new Composite(folder, SWT.NONE);
        _comp.setLayout(new GridLayout(1, false));
        GridData _gd = new GridData(GridData.FILL_BOTH);        
        _gd.horizontalSpan = 1;
        _comp.setLayoutData(_gd);
        
        final Group _grp = new Group(_comp, SWT.BORDER);
        _grp.setText("Additional root methods");
        
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.grabExcessHorizontalSpace = true;        
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalSpan  = 1;
        //_gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
        //_gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4;
        _grp.setLayoutData(_gd);        
        _grp.setLayout(new GridLayout(1, true));
        
        
        rootViewer = CheckboxTableViewer.newCheckList(_grp, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        final Table _table = rootViewer.getTable();
        setupRootTabTable(_table);      
        initRootMethods();
        rootViewer.setContentProvider(new RootMethodContentProvider());
        rootViewer.setLabelProvider(new RootMethodLabelProvider());
        rootViewer.setInput(rmColl);
        for (int i = 0; i < _table.getColumnCount(); i++) {
            _table.getColumn(i).pack();
        }
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalSpan  = 1;
        _table.setLayoutData(_gd);
        
        final Composite _rComp = new Composite(_comp, SWT.BORDER);
        
        final RowLayout _rl = new RowLayout();
        _rl.pack = false;
        _rComp.setLayout(_rl);
        
        _gd = new GridData(GridData.FILL_HORIZONTAL);
        _gd.grabExcessHorizontalSpace = true;
        _gd.horizontalSpan =  1;
        _rComp.setLayoutData(_gd);

        final Button _btnDelete =  new Button(_rComp, SWT.PUSH);
        _btnDelete.setText("Delete");
     
        _btnDelete.addSelectionListener(
                new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {                        

                            final Object[] _chsObjs = rootViewer.getCheckedElements();

                            for (int i = 0; _chsObjs != null && i < _chsObjs.length; i++) {
                                deleteCollection.add(_chsObjs[i]);
                                rmColl.getRootMethodCollection().remove(_chsObjs[i]);                                
                             }
                            
                            rootViewer.setInput(rmColl);
                            for (int i = 0; i < _table.getColumnCount(); i++) {
                                _table.getColumn(i).pack();
                            }
                            
                        } 
                                            
                }
                );
        
        return _comp;

    }

    /**
     * Initialize the root method collection.
     */
    private void initRootMethods() {
        if (project != null) {
            final IResource _resource;
            try {
                _resource = project.getCorrespondingResource();
            
            final QualifiedName _name = new QualifiedName("edu.ksu.cis.indus.kaveri", "rootMethodCollection");
            final String _propVal =   _resource.getPersistentProperty(_name);
            final XStream _xstream = new XStream(new DomDriver());
            _xstream.alias("RootMethodCollection", RootMethodCollection.class);            
            if (_propVal != null) {
               rmColl = (RootMethodCollection) _xstream.fromXML(_propVal);
            } else {
                rmColl = new RootMethodCollection();
            }
            final String _val = _xstream.toXML(rmColl);
           
                _resource.setPersistentProperty(_name, _val);
            } catch (JavaModelException _e) {
                SECommons.handleException(_e);
                KaveriErrorLog.logException("Java Model Exception", _e);
            } 
            catch (CoreException _e) {
                SECommons.handleException(_e);
                KaveriErrorLog.logException("Core Exception", _e);            
            } 
        }
    }

    /**
     * Setups the root method display table.
     * @param table
     */
    private void setupRootTabTable(Table table) {
        final TableColumn _col0 = new TableColumn(table, SWT.CENTER);
        _col0.setText("!");
        
        final TableColumn _col1 = new TableColumn(table, SWT.NONE);
        _col1.setText("Class");
        
        final TableColumn _col2 = new TableColumn(table, SWT.NONE);
        _col2.setText("Method Signature");
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        GridData _gd = new GridData(GridData.FILL_BOTH);
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;        
        table.setLayoutData(_gd);
        
    }

    /**
     * Create the scope tab.
     * @param folder
     * @return
     */
    private Control createScopeControl(TabFolder folder) {
        final Composite _comp = new Composite(folder, SWT.NONE);
        final GridData _gd1 = new GridData(GridData.FILL_BOTH);
		_gd1.horizontalSpan = 1;
		_gd1.grabExcessHorizontalSpace = true;
		_gd1.grabExcessVerticalSpace = true;
		_comp.setLayoutData(_gd1);
        
        _comp.setLayout(new GridLayout(1, true));

        final Group _grp = new Group(_comp, SWT.BORDER);
        _grp.setText("Pick the elements to be included in the scope");
        _grp.setLayout(new GridLayout(1, true));

        GridData _gd = new GridData();
        _gd.grabExcessHorizontalSpace = true;
        _gd.horizontalSpan = 1;
        _gd.horizontalAlignment = GridData.FILL;
        _gd.grabExcessVerticalSpace = true;
        _gd.verticalAlignment = GridData.FILL;
        //_gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
       // _gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4;
        _grp.setLayoutData(_gd);

        scpViewer = CheckboxTableViewer.newCheckList(_grp, SWT.SINGLE | SWT.FULL_SELECTION
                | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        final Table _table = scpViewer.getTable();
        _table.setLinesVisible(true);
        _table.setHeaderVisible(true);
        
        final TableColumn _col1 = new TableColumn(_table, SWT.CENTER);
        _col1.setText("!");

        final TableColumn _col2 = new TableColumn(_table, SWT.NONE);
        _col2.setText("Type");

        final TableColumn _col3 = new TableColumn(_table, SWT.NONE);
        _col3.setText("Scope Name");

        final TableColumn _col4 = new TableColumn(_table, SWT.NONE);
        _col4.setText("Element Name");
        
        _gd = new GridData();
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalAlignment = GridData.FILL;
        _gd.verticalAlignment = GridData.FILL;
        _table.setLayoutData(_gd);
        scpViewer.setContentProvider(new ScopeViewContentProvider());
        scpViewer.setLabelProvider(new ScopeViewLabelProvider());
        scpViewer.setInput("Input");

        _comp.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                final TableColumn _cols[] = _table.getColumns();
                for (int i = 0; i < _cols.length; i++) {
                    _cols[i].pack();
                }
            }
        });

        return _comp;

    }

    /**
     * Create the context selection tab.
     * @param folder
     * @return
     */
    private Control createContextControl(TabFolder folder) {
        final Composite _comp = new Composite(folder, SWT.NONE);        
        GridLayout _layout = new GridLayout(1, true);		
		_comp.setLayout(_layout);
		
		final GridData _gd1 = new GridData(GridData.FILL_BOTH);
		_gd1.horizontalSpan = 1;
		_gd1.grabExcessHorizontalSpace = true;
		_gd1.grabExcessVerticalSpace = true;
		_comp.setLayoutData(_gd1);
		
        final Group _grp = new Group(_comp, SWT.NONE);
        _grp.setText("Select the call string contexts for the slice");
        final GridData _gd = new GridData(GridData.FILL_BOTH);
        //_gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
        //_gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalSpan = 1;
        _grp.setLayoutData(_gd);
        _grp.setLayout(new FillLayout());

        ctxViewer = CheckboxTableViewer.newCheckList(_grp, SWT.SINGLE | SWT.FULL_SELECTION
                | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        final Table _table = ctxViewer.getTable();
        _table.setHeaderVisible(true);
        _table.setLinesVisible(true);

        final String[] _colNames = { "!", "Call String Source", "Call String End"};
        for (int _i = 0; _i < _colNames.length; _i++) {
            final TableColumn _col = new TableColumn(_table, SWT.NONE);
            _col.setText(_colNames[_i]);
        }
        ctxViewer.setContentProvider(new ContextContentProvider(project));
        ctxViewer.setLabelProvider(new ContextLabelProvider());
        ctxViewer.setInput(KaveriPlugin.getDefault().getIndusConfiguration()
                .getCtxRepository());
        for (int _i = 0; _i < _colNames.length; _i++) {
            _table.getColumn(_i).pack();
        }
                               
        return _comp;

    }
    


    /**
     * Create the criteria tab
     * @param folder
     * @return
     */
    private Control createCriteriaTab(TabFolder folder) {
        final Composite _comp = new Composite(folder, SWT.NONE);
        _comp.setLayout(new GridLayout(1, true));
        GridData _gd1 = new GridData(GridData.FILL_BOTH);
        _gd1.grabExcessHorizontalSpace = true;
        _gd1.grabExcessVerticalSpace = true;
        _gd1.horizontalSpan = 1;
        _comp.setLayoutData(_gd1);
        
        final Group _group = new Group(_comp, SWT.NONE);
        _group.setText(Messages.getString("IndusConfigurationDialog.3")); //$NON-NLS-1$
        final GridLayout _gl = new GridLayout();
        _gl.numColumns = 1;        
        _group.setLayout(_gl);
        
        final GridData _grpData = new GridData(GridData.FILL_BOTH);
        _grpData.horizontalSpan = 1;
        _grpData.grabExcessHorizontalSpace = true;
        _grpData.grabExcessVerticalSpace = true;
        _group.setLayoutData(_grpData);
        
        crtViewer = CheckboxTableViewer.newCheckList(_group, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION);
        //		final FillLayout _fl = new FillLayout(SWT.VERTICAL | SWT.HORIZONTAL);
        //		_group.setLayout(_fl);
        criteriaTable = crtViewer.getTable();

        final GridData _gd = new GridData();
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        //_gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
        _gd.horizontalAlignment = GridData.FILL;
        _gd.verticalAlignment = GridData.FILL;
        //_gd.widthHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH;
        //_gd.heightHint = IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH * 3 / 4;
        criteriaTable.setLayoutData(_gd);

        criteriaTable.setLinesVisible(true);
        criteriaTable.setHeaderVisible(true);

        final String[] _colnames = { "!", "Function", "Line number",
                "Jimple index", "Consider Execution" };

        for (int _i = 0; _i < _colnames.length; _i++) {
            final TableColumn _ti = new TableColumn(criteriaTable, SWT.NULL);
            _ti.setText(_colnames[_i]);
        }
        for (int _i = 0; _i < criteriaTable.getColumnCount(); _i++) {
            criteriaTable.getColumn(_i).pack();
        }

        crtViewer.setContentProvider(new ViewContentProvider());
        crtViewer.setLabelProvider(new CriteriaViewLabelProvider());
        crtViewer.setInput("Input");
        for (int _i = 0; _i < _colnames.length; _i++) {
            criteriaTable.getColumn(_i).pack();
        }
        final Composite _subcomposite2 = new Composite(_comp, SWT.NONE);
        final GridData _subdata2 = new GridData(GridData.FILL_HORIZONTAL);
        _subdata2.horizontalSpan = 1;
        _subcomposite2.setLayoutData(_subdata2);

        final RowLayout _f2 = new RowLayout(SWT.HORIZONTAL);        
        _f2.pack = false;
        _subcomposite2.setLayout(_f2);
        
        final Button _btnDelete = new Button(_subcomposite2, SWT.PUSH);
        _btnDelete.setText(Messages.getString("IndusConfigurationDialog.5")); //$NON-NLS-1$
        handleDelete(_btnDelete, project);
        
        return _comp;
    }

    /**
     * Display and handle the context.
     * 
     * @param callStack
     */
    private void handleContext(Button callStack) {
        callStack.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                ContextDialog _cd = new ContextDialog(new Shell(), project);
                if (_cd.open() == IDialogConstants.OK_ID) {
                    final Collection _ctx = _cd.getCallStrings();
                    
                    if (_ctx.size() > 0) {
                        KaveriPlugin.getDefault().getIndusConfiguration()
                                .addToChosenContext(_ctx);
                    }
                }
            }
        });

    }

    /**
     * The run button has been clicked. Setup the configuration and the criteria
     * for the slice using the IndusConfiguration class.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        final String _selectedConfiguration = confCombo.getText();
        String _configString = null;
        KaveriPlugin.getDefault().getIndusConfiguration()
                .setCurrentConfiguration(_selectedConfiguration);
        setUpCriteria();
        KaveriPlugin.getDefault().getIndusConfiguration().setAdditive(
                additive.getSelection());
        final IPreferenceStore _ps = KaveriPlugin.getDefault()
                .getPreferenceStore();
        final String _additivename = "additiveSliceProperty";
        _ps.setValue(_additivename, additive.getSelection());
        KaveriPlugin.getDefault().savePluginPreferences();
        processContext();
        processScope();
        processRootMethods();
        final String _cpSeparator = System.getProperty("path.separator");
        for (int _i = 0; _i < cpList.getItemCount(); _i++) {
            classPathSet.add(cpList.getItem(_i) + _cpSeparator);
        }
        super.okPressed();
    }

    /**
     * Process the results of operations in the root method tab.
     */
    private void processRootMethods() {
        if (rmColl != null) {
            final XStream _xstream = new XStream(new DomDriver());
            _xstream.alias("RootMethodCollection", RootMethodCollection.class);        
            final QualifiedName _name = new QualifiedName("edu.ksu.cis.indus.kaveri", "rootMethodCollection");
            final String _val = _xstream.toXML(rmColl);
            try {
                final IResource _rs = project.getCorrespondingResource();
                _rs.setPersistentProperty(_name, _val);
            }  
            catch (JavaModelException _e) {
                SECommons.handleException(_e);
                KaveriErrorLog.logException("Java Model Exception", _e);
            } 
            catch (CoreException _e) {
                SECommons.handleException(_e);
                KaveriErrorLog.logException("Core Exception", _e);            
            } 
            
        }
        deleteMarkers();
        
    }

    /**
     * Delete any markers containing root methods.
     */
    private void deleteMarkers() {
        final String _markerId = KaveriPlugin.getDefault().getBundle().getSymbolicName() + "." +
    	"rootMethodMarker";                        
        try {
            final IMarker[] _markers = project.getProject().findMarkers(_markerId, true, IResource.DEPTH_INFINITE);
            final String _classNameKey = "className";
            final String _methodSigKey = "methodSignature";
            final Collection _markersToDelete = new ArrayList();
            for (Iterator iter = deleteCollection.iterator(); iter.hasNext();) {
                final Pair _pair = (Pair) iter.next();
                for (int j = 0; j < _markers.length; j++) {
                    final IMarker _marker = _markers[j];
                    final String _classname = (String) _marker.getAttribute(_classNameKey);
                    final String _methodNameSig = (String) _marker.getAttribute(_methodSigKey);
                    if (_classname != null && _methodSigKey  != null &&
                            _classname.equals(_pair.getFirst().toString()) &&
                                    _methodNameSig.equals(_pair.getSecond().toString())) {
                        _markersToDelete.add(_marker);
                    }    
            }
            
                for (Iterator iterator = _markersToDelete.iterator(); iterator
                .hasNext();) {
                    final IMarker _marker = (IMarker) iterator.next();
                    _marker.delete();                                
                }

            }
        } catch (CoreException e) {
            //SECommons.handleException(e);
            KaveriErrorLog.logException("Unable to find markers", e);
        }
        
        
    }

    /**
     * Process the results of operations in the scope tab.
     */
    private void processScope() {
        final Object _elems[] = scpViewer.getCheckedElements();
        String _scopeSpecHeader = "<indus:scopeSpec xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                + "xmlns:indus=\"http://indus.projects.cis.ksu.edu/indus\""
                + "indus:specName=\"scope_spec\">";
        _scopeSpecHeader += "\n</indus:scopeSpec>";
        String _scopeSpecification = "";
        if (_elems != null && _elems.length > 0) {
        try {
            final SpecificationBasedScopeDefinition _sbsd = SpecificationBasedScopeDefinition
                    .deserialize(_scopeSpecHeader);
            for (int i = 0; i < _elems.length; i++) {
                if (_elems[i] instanceof ClassSpecification) {
                    _sbsd.getClassSpecs().add(_elems[i]);
                } else if (_elems[i] instanceof MethodSpecification) {
                    _sbsd.getMethodSpecs().add(_elems[i]);
                } else if (_elems[i] instanceof FieldSpecification) {
                    _sbsd.getFieldSpecs().add(_elems[i]);
                }
            }
            _scopeSpecification = SpecificationBasedScopeDefinition
                    .serialize(_sbsd);
        } catch (JiBXException _jbe) {
            SECommons.handleException(_jbe);
            KaveriErrorLog.logException("Error deserializing scope spec", _jbe);
            _scopeSpecification = "";
        }
        }
        KaveriPlugin.getDefault().getIndusConfiguration()
        .setScopeSpecification(_scopeSpecification);
    }

    /**
     * Process the results of operations in the context tab.
     */
    private void processContext() {
        final Object[] _elems = ctxViewer.getCheckedElements();
        final Collection _callStrings = new ArrayList();
        if (_elems != null && _elems.length > 0) {
            for (int _i = 0; _i < _elems.length; _i++) {
                _callStrings.add(_elems[_i]);
            }
        }
        
        if(_callStrings.size() > 0) {
            KaveriPlugin.getDefault().getIndusConfiguration().addToChosenContext(_callStrings);
        }
    }

    /**
     * Sets up the criteria.
     */
    private void setUpCriteria() {
        final Object _objCriteria[] = crtViewer.getCheckedElements();
        if (_objCriteria.length > 0) {
            for (int i = 0; i < _objCriteria.length; i++) {
                final Criteria _c = (Criteria) _objCriteria[i];
                KaveriPlugin.getDefault().getIndusConfiguration().setCriteria(
                        _c);
            }
        }
    }

    /**
     * Handles the delete button action.
     * 
     * @param btnDelete
     *            The delete button.
     * @param theproject
     *            The project in which the criteria are present.
     */
    private void handleDelete(final Button btnDelete,
            final IJavaProject theproject) {
        btnDelete.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                if (criteriaTable.getSelectionCount() == 1) {
                    removeSelection(theproject, criteriaTable
                            .getSelectionIndex());
                }
            }

            private void removeSelection(final IJavaProject myproject,
                    final int index) {
                IResource _resource;

                try {
                    _resource = myproject.getCorrespondingResource();

                    final QualifiedName _name = new QualifiedName(Messages
                            .getString("IndusConfigurationDialog.6"), Messages
                            .getString("IndusConfigurationDialog.7"));

                    try {
                        final String _propVal = _resource
                                .getPersistentProperty(_name);

                        if (_propVal != null) {
                            final XStream _xstream = new XStream(
                                    new DomDriver());
                            _xstream.alias(Messages
                                    .getString("IndusConfigurationDialog.8"),
                                    CriteriaData.class);

                            final CriteriaData _data = (CriteriaData) _xstream
                                    .fromXML(_propVal);
                            final java.util.List _lst = _data.getCriterias();
                            final Object _crtList[] = crtViewer
                                    .getCheckedElements();
                            for (int i = 0; i < _crtList.length; i++) {
                                final Criteria _c = (Criteria) _crtList[i];
                                _lst.remove(_c);
                            }

                            final String _xml = _xstream.toXML(_data);
                            _resource.setPersistentProperty(_name, _xml);
                            crtViewer.setInput("Input"); // Refresh
                            for (int _i = 0; _i < criteriaTable
                                    .getColumnCount(); _i++) {
                                criteriaTable.getColumn(_i).pack();
                            }
                        }
                    } catch (CoreException _e) {
                        SECommons.handleException(_e);
                    }
                } catch (JavaModelException _e1) {
                    SECommons.handleException(_e1);
                }
            }
        });
    }

    /**
     * Handles the scope button action.
     * 
     * @param btnScope
     *            The scope button.
     */
    private void handleScope(final Button btnScope) {
        btnScope.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(final SelectionEvent e) {
                final ScopeDialog _sd = new ScopeDialog(Display.getCurrent()
                        .getActiveShell());
                if (_sd.open() == IDialogConstants.OK_ID) {
                    final String _scopeSpec = _sd.getScopeSpecification();
                    KaveriPlugin.getDefault().getIndusConfiguration()
                            .setScopeSpecification(_scopeSpec);
                }

                /*
                 * ScopeSelectionDialog _sld = new
                 * ScopeSelectionDialog(Display.getCurrent().getActiveShell(),
                 * null, null); if (_sld.open() == IDialogConstants.OK_ID) {
                 * final Object _res[] =_sld.getResult(); if (_res != null &&
                 * _res.length > 0) { String _scopeSpec = " <?xml
                 * version=\"1.0\"?>\n";
                 * 
                 * 
                 * _scopeSpec += " <indus:scopeSpec
                 * xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                 * "xmlns:indus=\"http://indus.projects.cis.ksu.edu/indus\" " +
                 * "indus:specName=\"scope_spec\">\n"; for (int i = 0; i <
                 * _res.length; i++) { final IMethod _method = (IMethod)
                 * _res[i]; _scopeSpec += "\n <indus:methodSpec
                 * indus:specName=\"scope2\" indus:methodNameSpec=\"" +
                 * _method.getElementName() + "\">\n"; _scopeSpec += "
                 * <indus:declaringClassSpec indus:scopeExtension=\"IDENTITY\"" +
                 * "indus:nameSpec=\"" +
                 * PrettySignature.getSignature(_method.getParent()) + "\"/>\n";
                 * _scopeSpec += " <indus:returnTypeSpec
                 * indus:scopeExtension=\"PRIMITIVE\" indus:nameSpec=\"void\"
                 * />\n"; _scopeSpec += " <indus:parameterSpecs/>"; _scopeSpec += "
                 * </indus:methodSpec>\n"; } _scopeSpec += "
                 * </indus:scopeSpec>";
                 * KaveriPlugin.getDefault().getIndusConfiguration().setScopeSpecification(_scopeSpec); } }
                 */
            }
        });
    }

    /**
     * Load the previously chosen choice for additive slicing.
     */
    private void initializeAdditive() {
        final String _propName = "additiveSliceProperty";
        final IPreferenceStore _store = KaveriPlugin.getDefault()
                .getPreferenceStore();
        final boolean _value = _store.getBoolean(_propName);
        additive.setSelection(_value);
    }

    /**
     * Initialized the configuration combo box.
     * 
     * @param confsCombo
     *            The configuration combo
     */
    private void initializeConfigs(final Combo confsCombo) {
        final SlicerTool _sTool = KaveriPlugin.getDefault().getSlicerTool();
        if (_sTool.getCurrentConfiguration() == null) {
            KaveriPlugin.getDefault().loadConfigurations();    
        }            
        final SlicerTool _slicetool = KaveriPlugin.getDefault().getSlicerTool();
        final Collection _c = _slicetool.getConfigurations();
        for (final Iterator _t = _c.iterator(); _t.hasNext();) {
            confsCombo.add(((IToolConfiguration) _t.next()).getConfigName());
        }
        final int _activeIndex = confsCombo.indexOf(_slicetool
                .getCurrentConfiguration().getConfigName());
        confsCombo.select(_activeIndex);
    }

    /**
     * Initializes the list showing the criteria.
     * 
     * @param criteriasList
     *            The list of criterias
     * @param javaproject
     *            The project in which the criteria are present.
     */
    private void initializeList(final Table criteriasList,
            final IJavaProject javaproject) {
        IResource _resource;
        criteriasList.removeAll();

        final XStream _xstream = new XStream(new DomDriver());
        _xstream
                .alias(
                        Messages.getString("IndusConfigurationDialog.17"), CriteriaData.class); //$NON-NLS-1$

        try {
            _resource = javaproject.getCorrespondingResource();

            final QualifiedName _name = new QualifiedName(Messages
                    .getString("IndusConfigurationDialog.18"), Messages
                    .getString("IndusConfigurationDialog.19"));

            try {
                //				_resource.setPersistentProperty(_name, null); // Knocks out
                // the stuff
                final String _propVal = _resource.getPersistentProperty(_name);

                if (_propVal != null) {
                    final CriteriaData _data = (CriteriaData) _xstream
                            .fromXML(_propVal);
                    final java.util.List _lst = _data.getCriterias();

                    for (int _i = 0; _i < _lst.size(); _i++) {
                        final Criteria _c = (Criteria) _lst.get(_i);
                        final TableItem _item = new TableItem(criteriasList,
                                SWT.NULL);
                        _item.setText(0, _c.getStrMethodName());
                        _item.setText(1, "" + _c.getNLineNo());
                        _item.setText(2, "" + _c.getNJimpleIndex());
                        _item.setText(3, "" + _c.isBConsiderValue());

                        /*
                         * final String _disp = _c.getStrMethodName() + ":" +
                         * "java line:" + _c.getNLineNo() + ":jimple index:" +
                         * _c.getNJimpleIndex() + ":Consider value: " +
                         * _c.isBConsiderValue(); criteriasList.add(_disp);
                         */
                    }
                    if (_lst.size() == 0) {
                        for (int _i = 0; _i < 5; _i++) {
                            final TableItem _item = new TableItem(
                                    criteriasList, SWT.NULL);
                            _item.setText(new String[] { " ", " ", " ", " " });
                        }
                    }
                    for (int _i = 0; _i < criteriasList.getColumnCount(); _i++) {
                        criteriasList.getColumn(_i).pack();
                    }

                    final int _suggestedTableSize = 200;
                    criteriasList.setSize(criteriasList.computeSize(
                            SWT.DEFAULT, _suggestedTableSize));
                }
            } catch (CannotResolveClassException _crce) {
                SECommons.handleException(_crce);
            } catch (CoreException _e) {
                SECommons.handleException(_e);
            }
        } catch (JavaModelException _e1) {
            SECommons.handleException(_e1);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
     */
    protected void buttonPressed(int buttonId) {       
        super.buttonPressed(buttonId);
        if (buttonId == IndusConfigurationDialog2.J2B_ID) {
            KaveriPlugin.getDefault().getIndusConfiguration().setDoResidualize(true);
            okPressed();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener#statusChanged(org.eclipse.core.runtime.IStatus)
     */
    public void statusChanged(IStatus status) {
               
    }
    
    /**
     * Returns the classpath set.
     * @return
     */
    public Set getClassPathSet() {
        return classPathSet;
    }
}

class CriteriaViewLabelProvider extends LabelProvider implements ITableLabelProvider {
    public String getColumnText(Object obj, int index) {
        String _retString = "";
        if (obj instanceof Criteria) {
            final Criteria _c = (Criteria) obj;
            switch (index) {
            case 0: // Nothing , the checkbox.
                break;
            case 1:
                _retString = _c.getStrMethodName();
                break;
            case 2:
                _retString = _c.getNLineNo() + "";
                break;
            case 3:
                _retString = _c.getNJimpleIndex() + "";
                break;
            case 4:
                _retString = _c.isBConsiderValue() + "";
                break;
            }
        }
        return _retString;
    }

    public Image getColumnImage(Object obj, int index) {
        return null;
    }

    public Image getImage(Object obj) {
        return null;
    }
}