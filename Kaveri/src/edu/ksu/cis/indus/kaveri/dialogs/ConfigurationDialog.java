/*******************************************************************************
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2007 SAnToS Laboratory, Kansas State University
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 *******************************************************************************/

package edu.ksu.cis.indus.kaveri.dialogs;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.jibx.runtime.JiBXException;

import edu.ksu.cis.indus.common.scoping.ClassSpecification;
import edu.ksu.cis.indus.common.scoping.FieldSpecification;
import edu.ksu.cis.indus.common.scoping.MethodSpecification;
import edu.ksu.cis.indus.common.scoping.ScopeExtensionEnum;
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.common.scoping.TypeSpecification;
import edu.ksu.cis.indus.kaveri.KaveriErrorLog;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.preferences.Messages;
import edu.ksu.cis.indus.kaveri.scoping.IScopeDialogMorphConstants;
import edu.ksu.cis.indus.kaveri.scoping.ScopePropertiesSelectionDialog;
import edu.ksu.cis.indus.tools.IToolConfiguration;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * This dialog allows you to create and edit the slice configuration.
 * 
 * @author Ganeshan
 */
public class ConfigurationDialog extends Dialog {

    /**
     * <p>
     * The checkbox table tvLeft.
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
     * The SB slice configuration combo.
     */
    private Combo cmbBackSliceConfig;

    /**
     * The SF slice configuration combo.
     */
    private Combo cmbFwdSliceConfig;

    /**
     * The configuration when the page is displayed. Used to restore in case of
     * cancel.
     */
    private String currConfig;

    /**
     * Constructor.
     * 
     * @param shell The parent shell.
     * @param tool The slicer tool.
     */
    public ConfigurationDialog(final Shell shell) {
        super(shell);
        isExceptionStateDirty = false;
    }

    /**
     * Sets the title.
     * 
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    protected void configureShell(final Shell arg0) {
        super.configureShell(arg0);
        arg0.setText(edu.ksu.cis.indus.kaveri.dialogs.Messages.getString("ConfigurationDialog.0")); //$NON-NLS-1$
    }

    /**
     * Creates the dialog area.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(final Composite parent) {
        final Composite _top = new Composite(parent, SWT.LEFT);

        // Sets the layout data for the top composite's
        // place in its parent's layout.
        _top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        // Sets the layout for the top composite's
        // children to populate.
        _top.setLayout(new GridLayout());

        final TabFolder _folder = new TabFolder(_top, SWT.NONE);
        _folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

        final TabItem _item1 = new TabItem(_folder, SWT.NONE);
        _item1.setText(Messages.getString("PluginPreference.0")); //$NON-NLS-1$
        _item1.setControl(createConfig(_folder));

        final TabItem _itemScope = new TabItem(_folder, SWT.NONE);
        _itemScope.setText("Scope");
        _itemScope.setControl(createScope(_folder));

        final TabItem _itemButtonConfigs = new TabItem(_folder, SWT.NONE);
        _itemButtonConfigs.setText("Slice Button Configurations");
        _itemButtonConfigs.setControl(createSliceButtonConfig(_folder));

        return _top;
    }

    /**
     * Creates the sliced button configurations.
     * 
     * @param folder The tab folder
     * @return Control The created control
     */
    private Control createSliceButtonConfig(final TabFolder folder) {
        // KaveriPlugin.getDefault().setupDefaultColors();
        final Composite _comp = new Composite(folder, SWT.NONE);
        final GridLayout _layout = new GridLayout();
        _layout.numColumns = 1;
        _comp.setLayout(_layout);
        try {
            final Group _grp1 = new Group(_comp, SWT.BORDER);
            _grp1.setText("Select the configurations used by the slice editor actions");
            GridData _gd = new GridData(GridData.FILL_BOTH);
            _gd.horizontalSpan = 1;
            _gd.grabExcessHorizontalSpace = true;
            _gd.grabExcessVerticalSpace = true;
            _grp1.setLayoutData(_gd);

            _grp1.setLayout(new GridLayout(2, false));
            final Label _lblBackwardSlice = new Label(_grp1, SWT.LEFT);
            _lblBackwardSlice.setText("Backward Slice Action Configuration");
            _gd = new GridData();
            _gd.horizontalSpan = 1;
            _lblBackwardSlice.setLayoutData(_gd);

            cmbBackSliceConfig = new Combo(_grp1, SWT.DROP_DOWN | SWT.READ_ONLY);
            _gd = new GridData(GridData.FILL_HORIZONTAL);
            _gd.horizontalSpan = 1;
            _gd.grabExcessHorizontalSpace = true;
            cmbBackSliceConfig.setLayoutData(_gd);
            initializeConfigs(cmbBackSliceConfig, false);

            cmbBackSliceConfig.addFocusListener(new FocusListener() {

                public void focusGained(final FocusEvent e) {
                }

                public void focusLost(final FocusEvent e) {
                    sliceActionConfigStoreHelper("edu.ksu.cis.indus.kaveri.sbConfig", cmbBackSliceConfig);
                }
            });

            final Label _lblForwardSlice = new Label(_grp1, SWT.LEFT);
            _lblForwardSlice.setText("Forward Slice Action Configuration");
            _gd = new GridData();
            _gd.horizontalSpan = 1;
            _lblForwardSlice.setLayoutData(_gd);

            cmbFwdSliceConfig = new Combo(_grp1, SWT.DROP_DOWN | SWT.READ_ONLY);
            _gd = new GridData(GridData.FILL_HORIZONTAL);
            _gd.horizontalSpan = 1;
            _gd.grabExcessHorizontalSpace = true;
            cmbFwdSliceConfig.setLayoutData(_gd);
            initializeConfigs(cmbFwdSliceConfig, true);

            cmbFwdSliceConfig.addFocusListener(new FocusListener() {

                public void focusGained(final FocusEvent e) {
                }

                public void focusLost(final FocusEvent e) {
                    sliceActionConfigStoreHelper("edu.ksu.cis.indus.kaveri.sfConfig", cmbFwdSliceConfig);
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
     * @param confsCombo The configuration combo
     * @param fwdDirection The type of the configuration to display.
     */
    private void initializeConfigs(final Combo confsCombo, final boolean fwdDirection) {
        // KaveriPlugin.getDefault().loadConfigurations();
        final SlicerTool _slicetool = KaveriPlugin.getDefault().getSlicerTool();
        final Collection _c = _slicetool.getConfigurations();
        for (final Iterator _t = _c.iterator(); _t.hasNext();) {
            confsCombo.add(((IToolConfiguration) _t.next()).getConfigName());
        }
        final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();
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
        } else if (!_c.isEmpty()) {
            confsCombo.select(0);
        }
    }

    /**
     * Creates the configuration control tab.
     * 
     * @param folder The Tabfolder for this control
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
     * @param folder The Tabfolder for this control
     * @return Control The control
     */
    private Control createScope(final TabFolder folder) {
        final Composite _comp = new Composite(folder, SWT.NONE);
        _comp.setLayoutData(new GridData(GridData.FILL_BOTH));

        initializeScopeSpecification();

        final GridLayout _layout = new GridLayout();
        _layout.numColumns = 1;
        _comp.setLayout(_layout);

        viewer = CheckboxTableViewer.newCheckList(_comp, SWT.SINGLE | SWT.V_SCROLL | SWT.FULL_SELECTION);

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
        // setupScopeEntries(_table);
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
        // TODO: We should save scope specification in plugin specific workspace
        // local space as done for criteria.
        final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();
        final String _scopeSpecKey = "edu.ksu.cis.indus.kaveri.scope";
        String _scopeSpec = _ps.getString(_scopeSpecKey);
        try {
            if (_scopeSpec.equals("")) {
                sbsd = new SpecificationBasedScopeDefinition();
            } else {
                sbsd = SpecificationBasedScopeDefinition.deserialize(_scopeSpec);
            }
        } catch (JiBXException _jbe) {
            SECommons.handleException(_jbe);
            KaveriErrorLog.logException("JiBx Exception", _jbe);
            sbsd = null;
        }

    }

    /**
     * Handle the delete operation.
     * 
     * @param delete DOCUMENT ME!
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
     * @param addClasses DOCUMENT ME!
     */
    private void handleScopeAdd(Button addClasses) {
        addClasses.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                if (sbsd == null) { return; }

                ScopePropertiesSelectionDialog _spsd = new ScopePropertiesSelectionDialog(Display.getCurrent()
                        .getActiveShell(), IScopeDialogMorphConstants.SCOPE_NAME_REGEX, "");
                _spsd.setStrDefaultClassName("java.lang.*");
                if (_spsd.open() == IDialogConstants.OK_ID) {
                    final ClassSpecification _cs = new ClassSpecification();
                    _cs.setInclusion(true);
                    _cs.setName(_spsd.getStrScopeName());

                    final TypeSpecification _ts = new TypeSpecification();
                    _ts.setScopeExtension(ScopeExtensionEnum.valueOf(_spsd.getStrChoice()));
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
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    protected void cancelPressed() {
        super.cancelPressed();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    protected void okPressed() {
        final String _scopeKey = "edu.ksu.cis.indus.kaveri.scope";
        if (sbsd != null) {
            try {
                final String _scopeSpec = SpecificationBasedScopeDefinition.serialize(sbsd);
                KaveriPlugin.getDefault().getPreferenceStore().setValue(_scopeKey, _scopeSpec);
                KaveriPlugin.getDefault().getIndusConfiguration().getInfoBroadcaster().update();
            } catch (JiBXException _jbe) {
                SECommons.handleException(_jbe);
                KaveriErrorLog.logException("Jibx Exception", _jbe);
            }

        }

        storeSliceActionConfigurations();

        KaveriPlugin.getDefault().storeConfiguration();
        KaveriPlugin.getDefault().savePluginPreferences();
        super.okPressed();
    }

    /**
     * Store the slice button configurations.
     */
    private void storeSliceActionConfigurations() {
        sliceActionConfigStoreHelper("edu.ksu.cis.indus.kaveri.sbConfig", cmbBackSliceConfig);
        sliceActionConfigStoreHelper("edu.ksu.cis.indus.kaveri.sfConfig", cmbFwdSliceConfig);
    }

    private void sliceActionConfigStoreHelper(final String key, final Combo cmb) {
        final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();
        final String _config = cmb.getText();
        if (!_config.equals("")) {
            _ps.setValue(key, _config);
        }
    }

    class ViewContentProvider implements IStructuredContentProvider {

        public void inputChanged(@SuppressWarnings("unused")
        Viewer v, @SuppressWarnings("unused")
        Object oldInput, @SuppressWarnings("unused")
        Object newInput) {
        }

        public void dispose() {
        }

        public Object[] getElements(Object parent) {
            if (parent != null && parent instanceof SpecificationBasedScopeDefinition) {
                final SpecificationBasedScopeDefinition _sbsd = (SpecificationBasedScopeDefinition) parent;
                final List _lstSpecs = new LinkedList();
                final Collection _collClassSpecs = _sbsd.getClassSpecs();
                for (Iterator iter = _collClassSpecs.iterator(); iter.hasNext();) {
                    final ClassSpecification _cs = (ClassSpecification) iter.next();
                    _lstSpecs.add(_cs);

                }
                final Collection _collMethodSpecs = _sbsd.getMethodSpecs();
                for (Iterator iter = _collMethodSpecs.iterator(); iter.hasNext();) {
                    final MethodSpecification _ms = (MethodSpecification) iter.next();
                    _lstSpecs.add(_ms);

                }
                final Collection _collFieldSpecs = _sbsd.getFieldSpecs();
                for (Iterator iter = _collFieldSpecs.iterator(); iter.hasNext();) {
                    final FieldSpecification _fs = (FieldSpecification) iter.next();
                    _lstSpecs.add(_fs);
                }
                return _lstSpecs.toArray();

            }
            return new Object[0];
        }
    }

    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

        public String getColumnText(Object obj, int index) {
            if (index == 0) { return ""; }
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

        public Image getColumnImage(@SuppressWarnings("unused")
        Object obj, @SuppressWarnings("unused")
        int index) {
            return null;
        }

        public Image getImage(@SuppressWarnings("unused")
        Object obj) {
            return null;
        }
    }
}
