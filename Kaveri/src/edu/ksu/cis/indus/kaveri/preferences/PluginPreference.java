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

package edu.ksu.cis.indus.kaveri.preferences;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.ksu.cis.indus.common.scoping.ClassSpecification;
import edu.ksu.cis.indus.common.scoping.FieldSpecification;
import edu.ksu.cis.indus.common.scoping.MethodSpecification;
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.common.scoping.TypeSpecification;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.preferencedata.ExceptionListStore;
import edu.ksu.cis.indus.kaveri.scoping.IScopeDialogMorphConstants;
import edu.ksu.cis.indus.kaveri.scoping.ScopePropertiesSelectionDialog;
import edu.ksu.cis.indus.tools.IToolConfiguration;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
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

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
//import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jibx.runtime.JiBXException;

import com.thoughtworks.xstream.XStream;

/**
 * The Indus preference page. Allows for management of configurations, views and
 * dependence colors.
 */
public class PluginPreference extends PreferencePage implements
        IWorkbenchPreferencePage {

    /**
     * <p>
     * The checkbox table viewer.
     * </p>
     */
    private CheckboxTableViewer viewer;

    /**
     * The table showing the list of exceptions.
     */
    private Table exceptionTable; 
    
    /**
     * The set of scope specifications.
     */
    private SpecificationBasedScopeDefinition sbsd;
    
    /**
     * Indicates whether the exception list was modified.
     */
    private boolean isExceptionStateDirty;
    
    /**
     * The exception store.
     */
    private ExceptionListStore exceptionStore;
    
    /**
     * The configuration when the page is displayed. Used to restore in case of cancel.
     */
    private String currConfig;

    /**
     * Creates a new PluginPreference object.
     */
    public PluginPreference() {
        super();

        // Set the preference store for the preference page.
        final IPreferenceStore _store = KaveriPlugin.getDefault()
                .getPreferenceStore();
        setPreferenceStore(_store);
        isExceptionStateDirty = false;
    }

    /**
     * Initializes the plugin.
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(final IWorkbench workbench) {
    }

    /**
     * The user has pressed Ok or Apply. Store/apply this page's values
     * appropriately.
     * 
     * @return boolean Ok can go through.
     */
    public boolean performOk() {
        final String _scopeKey = "edu.ksu.cis.indus.kaveri.scope";
        if (sbsd != null) {
            try {
                final String _scopeSpec = SpecificationBasedScopeDefinition
                        .serialize(sbsd);
                KaveriPlugin.getDefault().getPreferenceStore().setValue(
                        _scopeKey, _scopeSpec);
            } catch (JiBXException _jbe) {
                SECommons.handleException(_jbe);
                KaveriErrorLog.logException("Jibx Exception", _jbe);
            }

        }
        if (exceptionStore != null && isExceptionStateDirty) {
            final String _exceptionKey = "edu.ksu.cis.indus.kaveri.exceptionignorelist";
            final XStream _xstream = new XStream();
            _xstream.alias("ExceptionListStore", ExceptionListStore.class);
            final String _val = _xstream.toXML(exceptionStore);
            KaveriPlugin.getDefault().getPreferenceStore().setValue(
                    _exceptionKey, _val);            
                KaveriPlugin.getDefault().createNewSlicer(exceptionStore.getExceptionCollection());            
        }
        
        
        KaveriPlugin.getDefault().storeConfiguration();
        KaveriPlugin.getDefault().savePluginPreferences();
        return super.performOk();
    }

    /**
     * Creates the main dialoig area.
     * 
     * @see org.eclipse.jface.preference.
     *      PreferencePage#createContents(Composite)
     */
    protected Control createContents(final Composite parent) {
        final Composite _top = new Composite(parent, SWT.LEFT);

        // Sets the layout data for the top composite's
        // place in its parent's layout.
        _top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // Sets the layout for the top composite's
        // children to populate.
        _top.setLayout(new GridLayout());

        final TabFolder _folder = new TabFolder(_top, SWT.NONE);
        _folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.FILL_VERTICAL));

        final TabItem _item1 = new TabItem(_folder, SWT.NONE);
        _item1.setText(Messages.getString("PluginPreference.0")); //$NON-NLS-1$
        _item1.setControl(createConfig(_folder));

        final TabItem _itemScope = new TabItem(_folder, SWT.NONE);
        _itemScope.setText("Scope");
        _itemScope.setControl(createScope(_folder));

        final TabItem _itemButtonConfigs = new TabItem(_folder, SWT.NONE);
        _itemButtonConfigs.setText("Slice Button Configurations");
        _itemButtonConfigs.setControl(createSliceButtonConfig(_folder));

        final TabItem _itemExceptions = new  TabItem(_folder, SWT.NONE);
        _itemExceptions.setText("Exceptions");
        _itemExceptions.setControl(createExceptionIgnoreSet(_folder));
        
        /*
         * final TabItem _item2 = new TabItem(_folder, SWT.NONE);
         * _item2.setText(Messages.getString("PluginPreference.1"));
         * //$NON-NLS-1$ _item2.setControl(createColor(_folder));
         * 
         * final TabItem _item3 = new TabItem(_folder, SWT.NONE);
         * _item3.setText(Messages.getString("PluginPreference.2"));
         * //$NON-NLS-1$ _item3.setControl(createView(_folder));
         * //item1.setControl(exemptTagsList);
         */

        return _top;
    }

    /**
     * Displays the set of exceptions that are ignored.
     * @param folder
     * @return
     */
    private Control createExceptionIgnoreSet(TabFolder folder) {
        final Composite _comp = new Composite(folder, SWT.NONE);
        _comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        final GridLayout _layout = new GridLayout();
        _layout.numColumns = 1;
        _comp.setLayout(_layout);
        
        final Group _grp = new Group(_comp, SWT.BORDER);
        _grp.setText("Exception Ignore List");
        GridData _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _grp.setLayoutData(_gd);
        _grp.setLayout(new GridLayout(1, true));
        
        exceptionTable = new Table(_grp, SWT.FULL_SELECTION | SWT.SINGLE);
        exceptionTable.setLinesVisible(true);
        exceptionTable.setHeaderVisible(true);
        _gd = new GridData(GridData.FILL_BOTH);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        exceptionTable.setLayoutData(_gd);
        
        /*final TableColumn _col1 = new TableColumn(exceptionTable, SWT.CENTER);
        _col1.setText("!");*/
        final TableColumn _col2 = new TableColumn(exceptionTable, SWT.NONE);
        _col2.setText("Fully Qualified Exception Name");
        
        initExceptionList();
        
        for (int _i =0; _i < exceptionTable.getColumnCount(); _i++) {
            exceptionTable.getColumn(_i).pack();
        }
        
        final Composite _rowComp = new Composite(_comp, SWT.BORDER);
        _gd = new GridData(GridData.FILL_HORIZONTAL);
        _gd.horizontalSpan = 1;
        _gd.grabExcessHorizontalSpace = true;
        _rowComp.setLayoutData(_gd);
        
        _rowComp.setLayout(new RowLayout());
        final Button _btnAdd = new Button(_rowComp, SWT.PUSH);
        _btnAdd.setText("Add Exception");
        _btnAdd.addSelectionListener(
                new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        final Shell _shell = Display.getCurrent().getActiveShell();
                        
                        IInputValidator _val = new IInputValidator() {

                            public String isValid(String newText) {
                                final Status _status = (Status) JavaConventions.validateJavaTypeName(newText);
                                if (_status.getSeverity() == IStatus.ERROR || _status.getSeverity() == IStatus.WARNING) {
                                    return _status.getMessage();
                                }
                                return null;
                            }
                            
                        };
                       final InputDialog _id = new InputDialog(_shell, "Exception Name", 
                               "Enter the fully qualified name of the exception", 
                               "java.lang.IllegalArgumentException", _val);
                       if (_id.open() == IDialogConstants.OK_ID) {                                           
                           if (exceptionStore.addException(_id.getValue())) {
                               final TableItem _item = new TableItem(exceptionTable, SWT.NONE);
                               _item.setText(0, _id.getValue());
                               _item.setData(_id.getValue());    
                               isExceptionStateDirty = true;
                           }
                       }
                    }
                }
                );
        final Button _btnDelete = new Button(_rowComp, SWT.PUSH);
        _btnDelete.setText("Delete");
        _btnDelete.addSelectionListener(
                new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent e) {
                        if (exceptionTable.getSelectionCount() == 1) {
                            	final TableItem _item = exceptionTable.getSelection()[0];
                            	final String _exceptionName = (String)  _item.getData();
                            	exceptionStore.removeException(_exceptionName);
                            	exceptionTable.remove(exceptionTable.getSelectionIndex());
                            	isExceptionStateDirty = true;
                        }
                    }
                }
                );
        
        return _comp;
    }

    /**
     * Initialize the exception ignore table.
     */
    private void initExceptionList() {
        final String _exceptionKey = "edu.ksu.cis.indus.kaveri.exceptionignorelist";
        final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();
        String _val  = _ps.getString(_exceptionKey);
        final XStream _xstream = new XStream();
        _xstream.alias("ExceptionListStore", ExceptionListStore.class);
        if (_val.equals("")) {
            exceptionStore  = new ExceptionListStore();            
            _val = _xstream.toXML(exceptionStore);
        } else {
            exceptionStore = (ExceptionListStore) _xstream.fromXML(_val);
        }
        for (Iterator iter = exceptionStore.getExceptionCollection().iterator(); iter.hasNext();) {
            final String _exName  = (String) iter.next();
            final TableItem _ti = new TableItem(exceptionTable, SWT.NONE);
            _ti.setText(0, _exName);
            
        }
        
    }

    /**
     * Returns the default preference store.
     * 
     * @see org.eclipse.jface.preference.PreferencePage#doGetPreferenceStore()
     */
    protected IPreferenceStore doGetPreferenceStore() {
        return KaveriPlugin.getDefault().getPreferenceStore();
    }

    /**
     * Remove the default the apply buttons.
     * 
     * @see org.eclipse.jface.preference.PreferencePage#noDefaultAndApplyButton()
     */
    protected void noDefaultAndApplyButton() {
        super.noDefaultAndApplyButton();
    }

    /**
     * Performs the apply operation.
     * 
     * @see org.eclipse.jface.preference.PreferencePage#performApply()
     */
    protected void performApply() {
        final String _scopeKey = "edu.ksu.cis.indus.kaveri.scope";
        if (sbsd != null) {
            try {
                final String _scopeSpec = SpecificationBasedScopeDefinition
                        .serialize(sbsd);
                KaveriPlugin.getDefault().getPreferenceStore().setValue(
                        _scopeKey, _scopeSpec);
            } catch (JiBXException _jbe) {
                SECommons.handleException(_jbe);
                KaveriErrorLog.logException("Jibx Exception", _jbe);
            }

        }
        KaveriPlugin.getDefault().storeConfiguration();
        KaveriPlugin.getDefault().savePluginPreferences();
        if (exceptionStore != null && isExceptionStateDirty) {
            final String _exceptionKey = "edu.ksu.cis.indus.kaveri.exceptionignorelist";
            final XStream _xstream = new XStream();
            _xstream.alias("ExceptionListStore", ExceptionListStore.class);
            final String _val = _xstream.toXML(exceptionStore);
            KaveriPlugin.getDefault().getPreferenceStore().setValue(
                    _exceptionKey, _val);            
                KaveriPlugin.getDefault().createNewSlicer(exceptionStore.getExceptionCollection());            
        }
        super.performApply();
    }

    /**
     * The user has pressed "Restore defaults". Restore all default preferences.
     */
    protected void performDefaults() {
        // getDefaultExemptTagsPreference() is a convenience
        // method which retrieves the default preference from
        // the preference store.
        super.performDefaults();
    }

    /**
     * Creates the sliced button configurations.
     * 
     * @param folder
     *            The tab folder
     * 
     * @return Control The created control
     */
    private Control createSliceButtonConfig(final TabFolder folder) {
        //KaveriPlugin.getDefault().setupDefaultColors();
        final Composite _comp = new Composite(folder, SWT.NONE);
        final GridLayout _layout = new GridLayout();
        _layout.numColumns = 1;
        _comp.setLayout(_layout);
        try {
            //  KaveriPlugin.getDefault().loadDefaultConfigurations();
            final SlicerTool _stool = KaveriPlugin.getDefault().getSlicerTool();

            final Group _grp1 = new Group(_comp, SWT.BORDER);
            _grp1
                    .setText("Select the configurations used by the slice editor actions");
            GridData _gd = new GridData(GridData.FILL_BOTH);
            _gd.horizontalSpan = 1;
            _gd.grabExcessHorizontalSpace = true;
            _gd.grabExcessVerticalSpace = true;
            _grp1.setLayoutData(_gd);

            _grp1.setLayout(new GridLayout(2, false));
            final Label _lblBackwardSlice = new Label(_grp1, SWT.LEFT);
            _lblBackwardSlice.setText("SB Slice Configuration");
            _gd = new GridData();
            _gd.horizontalSpan = 1;
            _lblBackwardSlice.setLayoutData(_gd);

            final Combo _cmbBckCombo = new Combo(_grp1, SWT.DROP_DOWN
                    | SWT.READ_ONLY);
            _gd = new GridData(GridData.FILL_HORIZONTAL);
            _gd.horizontalSpan = 1;
            _gd.grabExcessHorizontalSpace = true;
            _cmbBckCombo.setLayoutData(_gd);
            initializeConfigs(_cmbBckCombo, false);

            final Label _lblForwardSlice = new Label(_grp1, SWT.LEFT);
            _lblForwardSlice.setText("SF Slice Configuration");
            _gd = new GridData();
            _gd.horizontalSpan = 1;
            _lblForwardSlice.setLayoutData(_gd);

            final Combo _cmbFwdCombo = new Combo(_grp1, SWT.DROP_DOWN
                    | SWT.READ_ONLY);
            _gd = new GridData(GridData.FILL_HORIZONTAL);
            _gd.horizontalSpan = 1;
            _gd.grabExcessHorizontalSpace = true;
            _cmbFwdCombo.setLayoutData(_gd);
            initializeConfigs(_cmbFwdCombo, true);

            final IPreferenceStore _ps = KaveriPlugin.getDefault()
                    .getPreferenceStore();
            final String _sbConfigKey = "edu.ksu.cis.indus.kaveri.sbConfig";
            final String _sfConfigKey = "edu.ksu.cis.indus.kaveri.sfConfig";

            _cmbBckCombo.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    final String _newConfig = _cmbBckCombo.getText();
                    _ps.setValue(_sbConfigKey, _newConfig);
                }
            });

            _cmbFwdCombo.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    final String _newConfig = _cmbFwdCombo.getText();
                    _ps.setValue(_sfConfigKey, _newConfig);

                }
            });

        } catch (IllegalArgumentException _ile) {
            KaveriErrorLog.logException("Illega Argument Exception", _ile);
            SECommons.handleException(_ile);
        }

        return _comp;
    }

    /**
     * Initialized the configuration combo box.
     * 
     * @param confsCombo
     *            The configuration combo
     * @param fwdDirection
     *            The type of the configuration to display.
     */
    private void initializeConfigs(final Combo confsCombo,
            final boolean fwdDirection) {
        //KaveriPlugin.getDefault().loadConfigurations();
        final SlicerTool _slicetool = KaveriPlugin.getDefault().getSlicerTool();
        final Collection _c = _slicetool.getConfigurations();
        for (final Iterator _t = _c.iterator(); _t.hasNext();) {
            confsCombo.add(((IToolConfiguration) _t.next()).getConfigName());
        }
        final IPreferenceStore _ps = KaveriPlugin.getDefault()
                .getPreferenceStore();
        String _sbfPreferenceKey = "";

        if (fwdDirection) {
            _sbfPreferenceKey = "edu.ksu.cis.indus.kaveri.sfConfig";
        } else {
            _sbfPreferenceKey = "edu.ksu.cis.indus.kaveri.sbConfig";
        }

        String _sbConfigPreference = _ps.getString(_sbfPreferenceKey);
        if (!_sbConfigPreference.equals("")) {
            final int _activeIndex = confsCombo.indexOf(_sbConfigPreference);
            confsCombo.select(_activeIndex);            
        }
    }

    /**
     * Creates the configuration control tab.
     * 
     * @param folder
     *            The Tabfolder for this control
     * 
     * @return Control The control
     */
    private Control createConfig(final TabFolder folder) {
        try {
            final SlicerTool _sTool = KaveriPlugin.getDefault().getSlicerTool();
            if (_sTool.getActiveConfiguration() == null) {
                KaveriPlugin.getDefault().loadConfigurations();    
            }                       
            currConfig = _sTool.getActiveConfiguration().getConfigName();
            final Composite _comp = new Composite(folder, SWT.NONE);
            final GridLayout _layout = new GridLayout();
            _layout.numColumns = 1;
            _comp.setLayout(_layout);
            final SlicerTool _stool = KaveriPlugin.getDefault().getSlicerTool();
            _stool.getConfigurator().initialize(_comp);
            _comp.pack();
            return _comp;
        } catch (IllegalArgumentException _ile) {
            KaveriErrorLog.logException("Illegal Configuration", _ile);
            SECommons.handleException(_ile);
        }
        return null;
    }

    /**
     * Creates the scope control tab.
     * 
     * @param folder
     *            The Tabfolder for this control
     * 
     * @return Control The control
     */
    private Control createScope(final TabFolder folder) {
        final Composite _comp = new Composite(folder, SWT.NONE);
        _comp.setLayoutData(new GridData(GridData.FILL_BOTH));

        initializeScopeSpecification();

        final GridLayout _layout = new GridLayout();
        _layout.numColumns = 1;
        _comp.setLayout(_layout);

        viewer = CheckboxTableViewer.newCheckList(_comp, SWT.SINGLE
                | SWT.V_SCROLL | SWT.FULL_SELECTION);

        final Table _table = viewer.getTable();
        _table.setHeaderVisible(true);
        _table.setLinesVisible(true);

        final TableColumn _col0 = new TableColumn(_table, SWT.CENTER);
        _col0.setText("!");

        final TableColumn _col1 = new TableColumn(_table, SWT.NONE);
        _col1.setText("Type");

        final TableColumn _col2 = new TableColumn(_table, SWT.NONE);
        _col2.setText("Scope Name");

        final TableColumn _col3 = new TableColumn(_table, SWT.NONE);
        _col3.setText("Element Name");

        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());

        folder.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
                final TableColumn _cols[] = _table.getColumns();
                for (int i = 0; i < _cols.length; i++) {
                    _cols[i].pack();
                }
            }
        });

        GridData _gd = new GridData();
        _gd.grabExcessHorizontalSpace = true;
        _gd.grabExcessVerticalSpace = true;
        _gd.horizontalSpan = 1;
        _gd.horizontalAlignment = GridData.FILL;
        _gd.verticalAlignment = GridData.FILL;
        _table.setLayoutData(_gd);
        viewer.setInput(sbsd);
        //setupScopeEntries(_table);
        // Buttons
        final Composite _rowComp = new Composite(_comp, SWT.BORDER);
        _gd = new GridData();
        _gd.horizontalSpan = 1;
        _gd.horizontalAlignment = GridData.FILL;
        _gd.grabExcessHorizontalSpace = true;
        _rowComp.setLayoutData(_gd);

        
        final RowLayout _rl = new RowLayout();
        _rl.pack = false;    
        _rowComp.setLayout(_rl);

        final Button _btAddClasses = new Button(_rowComp, SWT.PUSH);
        _btAddClasses.setText("Add Classes");

        handleScopeAdd(_btAddClasses);

        final Button _btDelete = new Button(_rowComp, SWT.PUSH);
        _btDelete.setText("Delete");
        handleScopeDelete(_btDelete);
        return _comp;
    }

    /**
     * Initialize the variable sbsd with the specification stored in the
     * project.
     */
    private void initializeScopeSpecification() {
        final IPreferenceStore _ps = KaveriPlugin.getDefault()
                .getPreferenceStore();
        final String _scopeSpecKey = "edu.ksu.cis.indus.kaveri.scope";
        String _scopeSpec = _ps.getString(_scopeSpecKey);
        if (_scopeSpec.equals("")) {
            _scopeSpec = "<indus:scopeSpec xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
                    + "xmlns:indus=\"http://indus.projects.cis.ksu.edu/indus\""
                    + "indus:specName=\"scope_spec\">";
            _scopeSpec += "\n</indus:scopeSpec>";
        }
        try {
            sbsd = SpecificationBasedScopeDefinition.deserialize(_scopeSpec);
        } catch (JiBXException _jbe) {
            SECommons.handleException(_jbe);
            KaveriErrorLog.logException("JiBx Exception", _jbe);
            sbsd = null;
        }

    }

    /**
     * Handle the delete operation.
     * 
     * @param delete
     */
    private void handleScopeDelete(Button delete) {
        delete.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                final Object _chosenObj[] = viewer.getCheckedElements();
                if (sbsd != null) {
                    for (int i = 0; i < _chosenObj.length; i++) {
                        if (_chosenObj[i] instanceof ClassSpecification) {
                            sbsd.getClassSpecs().remove(_chosenObj[i]);
                        } else if (_chosenObj[i] instanceof MethodSpecification) {
                            sbsd.getMethodSpecs().remove(_chosenObj[i]);
                        } else if (_chosenObj[i] instanceof FieldSpecification) {
                            sbsd.getFieldSpecs().remove(_chosenObj[i]);
                        }
                    }
                    viewer.setInput(sbsd);
                }
            }
        });
    }

    /**
     * Handle the class scope add action.
     * 
     * @param addClasses
     *  
     */
    private void handleScopeAdd(Button addClasses) {
        addClasses.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (sbsd == null) {
                    return;
                }

                ScopePropertiesSelectionDialog _spsd = new ScopePropertiesSelectionDialog(
                        Display.getCurrent().getActiveShell(),
                        IScopeDialogMorphConstants.SCOPE_NAME_REGEX, "");
                _spsd.setStrDefaultClassName("java.lang.*");
                if (_spsd.open() == IDialogConstants.OK_ID) {
                    final ClassSpecification _cs = new ClassSpecification();
                    _cs.setInclusion(true);
                    _cs.setName(_spsd.getStrScopeName());

                    final TypeSpecification _ts = new TypeSpecification();
                    _ts.setScopeExtension(_spsd.getStrChoice());
                    _ts.setNamePattern(_spsd.getStrClassRegex());
                    _cs.setTypeSpec(_ts);
                    final Collection _collClass = sbsd.getClassSpecs();
                    _collClass.add(_cs);
                    viewer.setInput(sbsd);
                }

            }
        });

    }

    /**
     * Cancels the effect on any new configuration creation.
     * 
     * @see org.eclipse.jface.preference.IPreferencePage#performCancel()
     */
    public boolean performCancel() {
        KaveriPlugin.getDefault().loadConfigurations();        
        KaveriPlugin.getDefault().getSlicerTool().setActiveConfiguration(currConfig);
        return super.performCancel();
    }
}

class ViewContentProvider implements IStructuredContentProvider {
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
    }

    public void dispose() {
    }

    public Object[] getElements(Object parent) {
        if (parent != null
                && parent instanceof SpecificationBasedScopeDefinition) {
            final SpecificationBasedScopeDefinition _sbsd = (SpecificationBasedScopeDefinition) parent;
            final List _lstSpecs = new LinkedList();
            final Collection _collClassSpecs = _sbsd.getClassSpecs();
            for (Iterator iter = _collClassSpecs.iterator(); iter.hasNext();) {
                final ClassSpecification _cs = (ClassSpecification) iter.next();
                _lstSpecs.add(_cs);

            }
            final Collection _collMethodSpecs = _sbsd.getMethodSpecs();
            for (Iterator iter = _collMethodSpecs.iterator(); iter.hasNext();) {
                final MethodSpecification _ms = (MethodSpecification) iter
                        .next();
                _lstSpecs.add(_ms);

            }
            final Collection _collFieldSpecs = _sbsd.getFieldSpecs();
            for (Iterator iter = _collFieldSpecs.iterator(); iter.hasNext();) {
                final FieldSpecification _fs = (FieldSpecification) iter.next();
                _lstSpecs.add(_fs);
            }
            return _lstSpecs.toArray();

        } else {
            return new Object[0];
        }

    }
}

class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
    public String getColumnText(Object obj, int index) {
        if (index == 0) {
            return "";
        }
        if (obj instanceof ClassSpecification) {
            final ClassSpecification _cs = (ClassSpecification) obj;
            switch (index) {
            case 1:
                return "Class";
            case 2:
                return _cs.getName();
            case 3:
                return _cs.getTypeSpec().getNamePattern();
            }
        } else if (obj instanceof MethodSpecification) {
            final MethodSpecification _ms = (MethodSpecification) obj;
            switch (index) {
            case 1:
                return "Method";
            case 2:
                return _ms.getName();
            case 3:
                return _ms.getMethodNameSpec();
            }

        } else if (obj instanceof FieldSpecification) {
            final FieldSpecification _fs = (FieldSpecification) obj;
            switch (index) {
            case 1:
                return "Field";
            case 2:
                return _fs.getName();
            case 3:
                return _fs.getFieldNameSpec();
            }

        } else {
            return "";
        }
        return getText(obj);
    }

    public Image getColumnImage(Object obj, int index) {
        return null;
    }

    public Image getImage(Object obj) {
        return null;
    }
}