
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;



import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.dialogs.ViewDialog;
import edu.ksu.cis.indus.kaveri.preferencedata.ViewConfiguration;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;


import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;


/**
 * The Indus preference page. Allows for management of configurations, views and dependence colors.
 */
public class PluginPreference
  extends PreferencePage
  implements IWorkbenchPreferencePage {
	/** 
	 * <p>
	 * Height of the list.
	 * </p>
	 */
	private static final int LIST_HEIGHT_IN_CHARS = 10;

	/** 
	 * <p>
	 * Number of dialog units per chanracter.
	 * </p>
	 */
	private static final int VERTICAL_DIALOG_UNITS_PER_CHAR = 8;

	/** 
	 * <p>
	 * Physical height of the list box.
	 * </p>
	 */
	private static final int LIST_HEIGHT_IN_DLUS = LIST_HEIGHT_IN_CHARS * VERTICAL_DIALOG_UNITS_PER_CHAR;

	/** 
	 * <p>
	 * The editor for the showMarker preference.
	 * </p>
	 */
	private BooleanFieldEditor showMarker;

	/** 
	 * <p>
	 * The editor for controlDependence color.
	 * </p>
	 */
	private ColorFieldEditor cf1;

	/** 
	 * <p>
	 * The editor for controlDependence color.
	 * </p>
	 */
	private ColorFieldEditor cf2;

	/** 
	 * <p>
	 * The editor for readyDependence color.
	 * </p>
	 */
	private ColorFieldEditor cf3;

	/** 
	 * <p>
	 * The editor for synchronizationDependence color.
	 * </p>
	 */
	private ColorFieldEditor cf4;

	/** 
	 * <p>
	 * The editor for interferenceDependence color.
	 * </p>
	 */
	private ColorFieldEditor cf5;

	

	/**
	 * Creates a new PluginPreference object.
	 */
	public PluginPreference() {
		super();

		// Set the preference store for the preference page.
		final IPreferenceStore _store = KaveriPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(_store);
	}

	/**
	 * Initializes the plugin.
	 *
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(final IWorkbench workbench) {
	}

	/**
	 * The user has pressed Ok or Apply. Store/apply this page's values appropriately.
	 *
	 * @return boolean Ok can go through.
	 */
	public boolean performOk() {
		KaveriPlugin.getDefault().storeConfiguration();
		cf1.store();
		cf2.store();
		cf3.store();
		cf4.store();
		cf5.store();
		showMarker.store();
		KaveriPlugin.getDefault().savePluginPreferences();
		return super.performOk();
	}

	/**
	 * Creates the main dialoig area.
	 *
	 * @see org.eclipse.jface.preference. PreferencePage#createContents(Composite)
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
		_folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

		final TabItem _item1 = new TabItem(_folder, SWT.NONE);
		_item1.setText(Messages.getString("PluginPreference.0"));  //$NON-NLS-1$
		_item1.setControl(createConfig(_folder));

		final TabItem _item2 = new TabItem(_folder, SWT.NONE);
		_item2.setText(Messages.getString("PluginPreference.1"));  //$NON-NLS-1$
		_item2.setControl(createColor(_folder));

		final TabItem _item3 = new TabItem(_folder, SWT.NONE);
		_item3.setText(Messages.getString("PluginPreference.2"));  //$NON-NLS-1$
		_item3.setControl(createView(_folder));
		//item1.setControl(exemptTagsList);
		return _top;
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
		cf1.store();
		cf2.store();
		cf3.store();
		cf4.store();
		cf5.store();
		showMarker.store();		
		KaveriPlugin.getDefault().storeConfiguration();
		KaveriPlugin.getDefault().savePluginPreferences();
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
	 * Creates the color tab.
	 *
	 * @param folder The tab folder
	 *
	 * @return Control The created control
	 */
	private Control createColor(final TabFolder folder) {
		KaveriPlugin.getDefault().setupDefaultColors();
		final Composite _comp = new Composite(folder, SWT.NONE);
		final GridLayout _layout = new GridLayout();
		_layout.numColumns = 1;
		_comp.setLayout(_layout);

		final Group _group = new Group(_comp, SWT.NONE);
		_group.setText(Messages.getString("PluginPreference.3"));  //$NON-NLS-1$
		_group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		_group.setLayout(new RowLayout(SWT.VERTICAL));
		cf1 = new ColorFieldEditor(Messages.getString("PluginPreference.4"), 
				Messages.getString("PluginPreference.5"), _group);
		cf1.setPreferencePage(this);
		cf1.setPreferenceStore(getPreferenceStore());
		cf1.load();
		cf2 = new ColorFieldEditor(Messages.getString("PluginPreference.6"), 
				Messages.getString("PluginPreference.7"), _group);
		cf2.setPreferencePage(this);
		cf2.setPreferenceStore(getPreferenceStore());
		cf2.load();
		cf3 = new ColorFieldEditor(Messages.getString("PluginPreference.8"), 
				Messages.getString("PluginPreference.9"), _group);
		cf3.setPreferencePage(this);
		cf3.setPreferenceStore(getPreferenceStore());
		cf3.load();
		cf4 = new ColorFieldEditor(Messages.getString("PluginPreference.10"), 
				Messages.getString("PluginPreference.11"),
				_group);  //$NON-NLS-1$ //$NON-NLS-2$
		cf4.setPreferencePage(this);
		cf4.setPreferenceStore(getPreferenceStore());
		cf4.load();
		cf5 = new ColorFieldEditor(Messages.getString("PluginPreference.12"), 
				Messages.getString("PluginPreference.13"),
				_group);  //$NON-NLS-1$ //$NON-NLS-2$
		cf5.setPreferencePage(this);
		cf5.setPreferenceStore(getPreferenceStore());
		cf5.load();
		showMarker =
			new BooleanFieldEditor(Messages.getString("PluginPreference.14"), 
					Messages.getString("PluginPreference.15"), _comp);
		showMarker.setPreferencePage(this);
		showMarker.setPreferenceStore(getPreferenceStore());
		showMarker.load();
		return _comp;
	}

	/**
	 * Creates the configuration control tab.
	 *
	 * @param folder The Tabfolder for this control
	 *
	 * @return Control The control
	 */
	private Control createConfig(final TabFolder folder) {
		try {
		KaveriPlugin.getDefault().loadDefaultConfigurations();		
		final Composite _comp = new Composite(folder, SWT.NONE);
		final GridLayout _layout = new GridLayout();
		_layout.numColumns = 1;
		_comp.setLayout(_layout);
		final SlicerTool _stool = KaveriPlugin.getDefault().getSlicerTool();
		_stool.getConfigurator().initialize(_comp);
		return _comp;
		} catch (IllegalArgumentException _ile) {		
			SECommons.handleException(_ile);
		}
		return null;
	}

	/**
	 * Creates the view tab.
	 *
	 * @param folder The tab folder for this control
	 *
	 * @return Control The view control tab
	 */
	private Control createView(final TabFolder folder) {
		final Composite _comp = new Composite(folder, SWT.NONE);
		_comp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final GridLayout _layout = new GridLayout();
		_layout.numColumns = 3;
		_comp.setLayout(_layout);

		final List _viewList = new List(_comp, SWT.BORDER);

		// Create a data that takes up the extra space
		// in the dialog and spans both columns.
		final GridData _listData = new GridData(GridData.FILL_HORIZONTAL);
		_listData.heightHint = convertVerticalDLUsToPixels(LIST_HEIGHT_IN_DLUS);
		_listData.horizontalSpan = 3;
		_viewList.setLayoutData(_listData);
		initializeViewList(_viewList);

		final Composite _rowComp = new Composite(_comp, SWT.NONE);
		_rowComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final RowLayout _rl = new RowLayout();
		_rowComp.setLayout(_rl);

		final Button _btnCreate = new Button(_rowComp, SWT.PUSH);
		_btnCreate.setText(Messages.getString("PluginPreference.20"));  //$NON-NLS-1$
		_btnCreate.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent se) {
					final ViewDialog _vd = new ViewDialog(Display.getCurrent().getActiveShell(), -1);

					if (_vd.open() == IDialogConstants.OK_ID) {
						initializeViewList(_viewList);
					}
				}
			});

		final Button _btnRemove = new Button(_rowComp, SWT.PUSH);
		_btnRemove.setText(Messages.getString("PluginPreference.21"));  //$NON-NLS-1$
		_btnRemove.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent se) {
					if (_viewList.getSelectionCount() == 1) {
						final int _index = _viewList.getSelectionIndex();
						final XStream _xstream = new XStream(new DomDriver());
						_xstream.alias(Messages.getString("PluginPreference.22"), ViewConfiguration.class);  //$NON-NLS-1$

						final String _viewname = Messages.getString("PluginPreference.23");  //$NON-NLS-1$
						final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();
						String _prefval = _ps.getString(_viewname);

						if (!_prefval.equals(Messages.getString(""))) {  //$NON-NLS-1$

							final ViewConfiguration _vc = (ViewConfiguration) _xstream.fromXML(_prefval);
							final java.util.List _lst = _vc.getList();
							_lst.remove(_index);
							_prefval = _xstream.toXML(_vc);
							_ps.setValue(_viewname, _prefval);
							_viewList.removeAll();
							initializeViewList(_viewList);
						}
					}
				}
			});

		final Button _btnEdit = new Button(_rowComp, SWT.PUSH);
		_btnEdit.setText(Messages.getString("PluginPreference.25"));  //$NON-NLS-1$
		handleEdit(_btnEdit, _viewList);
		return _comp;
	}

	/**
	 * Handles the view edit action.
	 *
	 * @param btnEdit The edit button
	 * @param viewList The view list.
	 */
	private void handleEdit(final Button btnEdit, final List viewList) {
		btnEdit.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent e) {
					final int _index = viewList.getSelectionIndex();

					if (_index != -1) {
						final ViewDialog _dialog = new ViewDialog(Display.getCurrent().getActiveShell(), _index);

						if (_dialog.open() == IDialogConstants.OK_ID) {
							initializeViewList(viewList);
						}
					}
				}
			});
	}

	/**
	 * Initialized the views.
	 *
	 * @param viewList The view list
	 */
	private void initializeViewList(final List viewList) {
		viewList.removeAll();

		final XStream _xstream = new XStream(new DomDriver());
		_xstream.alias(Messages.getString("PluginPreference.45"), ViewConfiguration.class);  //$NON-NLS-1$

		final String _viewname = Messages.getString("PluginPreference.46");  //$NON-NLS-1$
		final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();

		//_ps.setValue(_viewname, "");
		final String _prefval = _ps.getString(_viewname);

		if (_prefval != null && !_prefval.equals("")) {  //$NON-NLS-1$

			final ViewConfiguration _vc = (ViewConfiguration) _xstream.fromXML(_prefval);
			final java.util.List _lst = _vc.getList();

			for (int _i = 0; _i < _lst.size(); _i++) {
				viewList.add(Messages.getString("PluginPreference.48") + _i);  //$NON-NLS-1$
			}
		}
	}
	
	/** 
	 * Cancels the effect on any new configuration creation.
	 * @see org.eclipse.jface.preference.IPreferencePage#performCancel()
	 */
	public boolean performCancel() {
		KaveriPlugin.getDefault().loadConfigurations();
		return super.performCancel();
	}
}
