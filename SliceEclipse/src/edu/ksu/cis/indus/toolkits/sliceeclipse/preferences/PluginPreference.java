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

package edu.ksu.cis.indus.toolkits.sliceeclipse.preferences;

import com.thoughtworks.xstream.XStream;

import edu.ksu.cis.indus.common.soot.ExceptionFlowSensitiveStmtGraphFactory;

import edu.ksu.cis.indus.staticanalyses.tokens.TokenUtil;

import edu.ksu.cis.indus.toolkits.sliceeclipse.SliceEclipsePlugin;
import edu.ksu.cis.indus.toolkits.sliceeclipse.common.SECommons;
import edu.ksu.cis.indus.toolkits.sliceeclipse.dialogs.ConfigurationDialog;
import edu.ksu.cis.indus.toolkits.sliceeclipse.dialogs.ViewDialog;
import edu.ksu.cis.indus.toolkits.sliceeclipse.preferencedata.SliceConfigurationHolder;
import edu.ksu.cis.indus.toolkits.sliceeclipse.preferencedata.ViewConfiguration;

import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;

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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * A preference page for the error-handling of a simple HTML editor.
 */
public class PluginPreference extends PreferencePage implements
		IWorkbenchPreferencePage {
	/**
	 * <p>
	 * Height if the list.
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
	private static final int LIST_HEIGHT_IN_DLUS = LIST_HEIGHT_IN_CHARS
			* VERTICAL_DIALOG_UNITS_PER_CHAR;

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
	 * <p>
	 * The list.
	 * </p>
	 */
	private List exemptTagsList;

	/**
	 * Creates a new PluginPreference object.
	 */
	public PluginPreference() {
		super();

		// Set the preference store for the preference page.
		final IPreferenceStore _store = SliceEclipsePlugin.getDefault()
				.getPreferenceStore();
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
	 * The user has pressed Ok or Apply. Store/apply this page's values
	 * appropriately.
	 * 
	 * @return boolean Ok can go through.
	 */
	public boolean performOk() {
		cf1.store();
		cf2.store();
		cf3.store();
		cf4.store();
		cf5.store();
		showMarker.store();
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

		final TabItem _item2 = new TabItem(_folder, SWT.NONE);
		_item2.setText(Messages.getString("PluginPreference.1")); //$NON-NLS-1$
		_item2.setControl(createColor(_folder));

		final TabItem _item3 = new TabItem(_folder, SWT.NONE);
		_item3.setText(Messages.getString("PluginPreference.2")); //$NON-NLS-1$
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
		return SliceEclipsePlugin.getDefault().getPreferenceStore();
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
		SliceEclipsePlugin.getDefault().savePluginPreferences();
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
	 * @param folder
	 *            The tab folder
	 * 
	 * @return Control The created control
	 */
	private Control createColor(final TabFolder folder) {
		final Composite _comp = new Composite(folder, SWT.NONE);
		final GridLayout _layout = new GridLayout();
		_layout.numColumns = 1;
		_comp.setLayout(_layout);

		final Group _group = new Group(_comp, SWT.NONE);
		_group.setText(Messages.getString("PluginPreference.3")); //$NON-NLS-1$
		_group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		_group.setLayout(new RowLayout(SWT.VERTICAL));
		cf1 = new ColorFieldEditor(
				Messages.getString("PluginPreference.4"),
				Messages.getString("PluginPreference.5"), 
				_group); //$NON-NLS-1$ //$NON-NLS-2$
		cf1.setPreferencePage(this);
		cf1.setPreferenceStore(getPreferenceStore());
		cf1.load();
		cf2 = new ColorFieldEditor(
				Messages.getString("PluginPreference.6"),
				Messages.getString("PluginPreference.7"),
				_group); //$NON-NLS-1$ //$NON-NLS-2$
		cf2.setPreferencePage(this);
		cf2.setPreferenceStore(getPreferenceStore());
		cf2.load();
		cf3 = new ColorFieldEditor(
				Messages.getString("PluginPreference.8"),
				Messages.getString("PluginPreference.9"), 
				_group); //$NON-NLS-1$ //$NON-NLS-2$
		cf3.setPreferencePage(this);
		cf3.setPreferenceStore(getPreferenceStore());
		cf3.load();
		cf4 = new ColorFieldEditor(
				Messages.getString("PluginPreference.10"), 
				Messages.getString("PluginPreference.11"),
				_group); //$NON-NLS-1$ //$NON-NLS-2$
		cf4.setPreferencePage(this);
		cf4.setPreferenceStore(getPreferenceStore());
		cf4.load();
		cf5 = new ColorFieldEditor(
				Messages.getString("PluginPreference.12"), 
				Messages.getString("PluginPreference.13"), 
				_group); //$NON-NLS-1$ //$NON-NLS-2$
		cf5.setPreferencePage(this);
		cf5.setPreferenceStore(getPreferenceStore());
		cf5.load();
		showMarker = new BooleanFieldEditor(
				Messages.getString("PluginPreference.14"), 
				Messages.getString("PluginPreference.15"), 
				_comp); //$NON-NLS-1$ //$NON-NLS-2$
		showMarker.setPreferencePage(this);
		showMarker.setPreferenceStore(getPreferenceStore());
		showMarker.load();
		return _comp;
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
		final Composite _comp = new Composite(folder, SWT.NONE);
		final GridLayout _layout = new GridLayout();
		_layout.numColumns = 1;
		_comp.setLayout(_layout);
		exemptTagsList = new List(_comp, SWT.BORDER);

		//
		// Create a data that takes up the extra space
		// in the dialog and spans both columns.
		final GridData _listData = new GridData(GridData.FILL_HORIZONTAL);
		_listData.horizontalSpan = 1;
		_listData.heightHint = convertVerticalDLUsToPixels(LIST_HEIGHT_IN_DLUS);
		exemptTagsList.setLayoutData(_listData);

		final Composite _comp1 = new Composite(_comp, SWT.NONE);
		_comp1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		initializeList(exemptTagsList);

		final RowLayout _rl = new RowLayout();
		final int _spacing = 20;
		_rl.spacing = _spacing;
		_comp1.setLayout(_rl);

		final Button _btnCreate = new Button(_comp1, SWT.PUSH);
		_btnCreate.setText(Messages.getString("PluginPreference.16")); //$NON-NLS-1$
		handleCreate(_btnCreate);

		final Button _btnImport = new Button(_comp1, SWT.PUSH);
		_btnImport.setText(Messages.getString("PluginPreference.17")); //$NON-NLS-1$
		handleImport(_btnImport);

		final Button _btnExport = new Button(_comp1, SWT.PUSH);
		_btnExport.setText(Messages.getString("PluginPreference.18")); //$NON-NLS-1$
		handleExport(_btnExport);

		final Button _btnDelete = new Button(_comp1, SWT.PUSH);
		_btnDelete.setText(Messages.getString("PluginPreference.19")); //$NON-NLS-1$
		handleDelete(_btnDelete);

		final Button _btnEdit = new Button(_comp1, SWT.PUSH);
		_btnEdit.setText("Edit"); //$NON-NLS-1$
		handleEdit(_btnEdit);

		return _comp;
	}

	/**
	 * Handles the edit button.
	 * 
	 * @param edit
	 *            The edit button.
	 */
	private void handleEdit(final Button edit) {
		edit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent se) {
				final int _index = exemptTagsList.getSelectionIndex();
				if (_index != -1 && _index != 0) {
					editandchange(_index - 1);
				}
			}

			private void editandchange(final int index) {
				final SliceConfigurationHolder _sch = getSliceConfigurations();
				final java.util.List _lst = _sch.getList();
				if (index < _lst.size()) {
					final String _configuration = (String) _lst.get(index);
					final SlicerTool _stool = new SlicerTool(TokenUtil
							.getTokenManager(),
							new ExceptionFlowSensitiveStmtGraphFactory());
					_stool.destringizeConfiguration(_configuration);
					final ConfigurationDialog _cd = new ConfigurationDialog(Display
							.getDefault().getActiveShell(), _stool);
					if (_cd.open() == IDialogConstants.OK_ID) {
						_lst.set(index, _stool.stringizeConfiguration());
						saveConfigurations(_sch);
					}
				}
			}

			/**
			 * Saves the configuration.
			 * 
			 * @param sch
			 *            The configuration holder.
			 */
			private void saveConfigurations(final SliceConfigurationHolder sch) {
				final IPreferenceStore _ps = SliceEclipsePlugin.getDefault()
						.getPreferenceStore();
				final String _name = Messages.getString("PluginPreference.27"); //$NON-NLS-1$
				String _prefvals = _ps.getString(_name);
				final XStream _xstream = new XStream();
				_xstream
						.alias(
								Messages.getString("PluginPreference.28"), 
								SliceConfigurationHolder.class); //$NON-NLS-1$						
				_prefvals = _xstream.toXML(sch);
				_ps.setValue(_name, _prefvals);
				exemptTagsList.removeAll();
				initializeList(exemptTagsList);

			}
		});

	}

	/**
	 * Gets the SliceConfigurationHolder from the plugin.
	 * 
	 * @return SliceConfigurationHolder The configuration holder.
	 */
	public SliceConfigurationHolder getSliceConfigurations() {
		final IPreferenceStore _ps = SliceEclipsePlugin.getDefault()
				.getPreferenceStore();
		final String _name = Messages.getString("PluginPreference.27"); //$NON-NLS-1$
		final String _prefvals = _ps.getString(_name);
		final XStream _xstream = new XStream();
		_xstream
				.alias(
						Messages.getString("PluginPreference.28"), SliceConfigurationHolder.class); //$NON-NLS-1$

		SliceConfigurationHolder _sch = null;

		if (_prefvals.equals("")) { //$NON-NLS-1$
			_sch = new SliceConfigurationHolder();
			_sch.setList(new ArrayList());
		} else {
			_sch = (SliceConfigurationHolder) _xstream.fromXML(_prefvals);
		}
		return _sch;
	}

	/**
	 * Creates the view tab.
	 * 
	 * @param folder
	 *            The tab folder for this control
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
		_btnCreate.setText(Messages.getString("PluginPreference.20")); //$NON-NLS-1$
		_btnCreate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent se) {
				final ViewDialog _vd = new ViewDialog(Display.getCurrent()
						.getActiveShell(), -1);

				if (_vd.open() == IDialogConstants.OK_ID) {
					initializeViewList(_viewList);
				}
			}
		});

		final Button _btnRemove = new Button(_rowComp, SWT.PUSH);
		_btnRemove.setText(Messages.getString("PluginPreference.21")); //$NON-NLS-1$
		_btnRemove.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent se) {
				if (_viewList.getSelectionCount() == 1) {
					final int _index = _viewList.getSelectionIndex();
					final XStream _xstream = new XStream();
					_xstream
							.alias(
									Messages.getString("PluginPreference.22"), ViewConfiguration.class); //$NON-NLS-1$

					final String _viewname = Messages
							.getString("PluginPreference.23"); //$NON-NLS-1$
					final IPreferenceStore _ps = SliceEclipsePlugin
							.getDefault().getPreferenceStore();
					String _prefval = _ps.getString(_viewname);

					if (!_prefval.equals(Messages.getString(""))) { //$NON-NLS-1$
						final ViewConfiguration _vc = (ViewConfiguration) _xstream
								.fromXML(_prefval);
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
		_btnEdit.setText(Messages.getString("PluginPreference.25")); //$NON-NLS-1$
		handleEdit(_btnEdit, _viewList);
		return _comp;
	}

	/**
	 * Handles the create button function.
	 * 
	 * @param btnCreate
	 *            The create button.
	 */
	private void handleCreate(final Button btnCreate) {
		btnCreate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				final String _defaultConfig = SliceEclipsePlugin.getDefault()
						.getPreferenceStore().getString(
								Messages.getString("PluginPreference.26")); //$NON-NLS-1$
				final SlicerTool _stool = new SlicerTool(TokenUtil
						.getTokenManager(),
						new ExceptionFlowSensitiveStmtGraphFactory());
				_stool.destringizeConfiguration(_defaultConfig);

				//String newconfig = driver.showGUI();
				final String _newconfig = handleDisplay(_stool);

				if (_newconfig != null) {
					createConfig(_newconfig);
				}
			}

			private String handleDisplay(final SlicerTool stool) {
				final ConfigurationDialog _cd = new ConfigurationDialog(Display
						.getCurrent().getActiveShell(), stool);
				String _returnVal = null;

				if (_cd.open() == IDialogConstants.OK_ID) {
					_returnVal = stool.stringizeConfiguration();
				}
				return _returnVal;
			}

			private void createConfig(final String newconfig) {
				final IPreferenceStore _ps = SliceEclipsePlugin.getDefault()
						.getPreferenceStore();
				final String _name = Messages.getString("PluginPreference.27"); //$NON-NLS-1$
				String _prefvals = _ps.getString(_name);
				final XStream _xstream = new XStream();
				_xstream
						.alias(
								Messages.getString("PluginPreference.28"), SliceConfigurationHolder.class); //$NON-NLS-1$

				SliceConfigurationHolder _sch = null;

				if (_prefvals.equals("")) { //$NON-NLS-1$
					_sch = new SliceConfigurationHolder();
					_sch.setList(new ArrayList());
				} else {
					_sch = (SliceConfigurationHolder) _xstream
							.fromXML(_prefvals);
				}
				_sch.getList().add(newconfig);
				_prefvals = _xstream.toXML(_sch);
				_ps.setValue(_name, _prefvals);
				exemptTagsList.removeAll();
				initializeList(exemptTagsList);
			}
		});
	}

	/**
	 * Handles the delete button.
	 * 
	 * @param btnDelete
	 *            The delete button
	 */
	private void handleDelete(final Button btnDelete) {
		btnDelete.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (exemptTagsList.getSelectionCount() == 1) {
					int _index = exemptTagsList.getSelectionIndex();
					final String _selectedString = exemptTagsList
							.getSelection()[0];

					if (_selectedString != null
							&& !_selectedString.equals(Messages
									.getString("PluginPreference.30"))) { //$NON-NLS-1$
						_index--; // Account for defaultConfiguration

						final IPreferenceStore _ps = SliceEclipsePlugin
								.getDefault().getPreferenceStore();
						final String _name = Messages
								.getString("PluginPreference.31"); //$NON-NLS-1$
						String _prefvals = _ps.getString(_name);
						final XStream _xstream = new XStream();
						_xstream
								.alias(
										Messages.getString("PluginPreference.32"), 
												SliceConfigurationHolder.class); //$NON-NLS-1$

						SliceConfigurationHolder _sch = null;
						_sch = (SliceConfigurationHolder) _xstream
								.fromXML(_prefvals);
						_sch.getList().remove(_index);
						_prefvals = _xstream.toXML(_sch);
						_ps.setValue(_name, _prefvals);
						exemptTagsList.removeAll();
						initializeList(exemptTagsList);
					}
				}
			}
		});
	}

	/**
	 * Handles the view edit action.
	 * 
	 * @param btnEdit
	 *            The edit button
	 * @param viewList
	 *            The view list.
	 */
	private void handleEdit(final Button btnEdit, final List viewList) {
		btnEdit.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				final int _index = viewList.getSelectionIndex();

				if (_index != -1) {
					final ViewDialog _dialog = new ViewDialog(Display
							.getCurrent().getActiveShell(), _index);

					if (_dialog.open() == IDialogConstants.OK_ID) {
						initializeViewList(viewList);
					}
				}
			}
		});
	}

	/**
	 * Handles the export button.
	 * 
	 * @param btnExport
	 *            The export button
	 */
	private void handleExport(final Button btnExport) {
		btnExport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				if (exemptTagsList.getSelectionIndex() != -1) {
					final FileDialog _fd = new FileDialog(Display.getCurrent()
							.getActiveShell(), SWT.SAVE);
					final String _path = _fd.open();

					if (_path != null) {
						final String _selectedString = exemptTagsList
								.getSelection()[0];
						String _val = null;

						if (_selectedString.equals(Messages
								.getString("PluginPreference.33"))) { //$NON-NLS-1$
							_val = SliceEclipsePlugin
									.getDefault()
									.getPreferenceStore()
									.getString(
											Messages
													.getString("PluginPreference.34")); //$NON-NLS-1$
						} else {
							final IPreferenceStore _ps = SliceEclipsePlugin
									.getDefault().getPreferenceStore();
							final String _name = Messages
									.getString("PluginPreference.35"); //$NON-NLS-1$
							final String _prefvals = _ps.getString(_name);
							final XStream _xstream = new XStream();
							_xstream
									.alias(
											Messages.getString("PluginPreference.36"), 
											SliceConfigurationHolder.class); //$NON-NLS-1$

							SliceConfigurationHolder _sch = null;

							try {
								_sch = (SliceConfigurationHolder) _xstream
										.fromXML(_prefvals);

								final int _index = exemptTagsList
										.getSelectionIndex() - 1; // handle

								// defaultconfig
								_val = (String) _sch.getList().get(_index);

								final FileWriter _fw = new FileWriter(_path);
								_fw.write(_val);
								_fw.close();
							} catch (IOException _e1) {
								SECommons.handleException(_e1);
								return;
							}
						}
					}
				}
			}
		});
	}

	/**
	 * Handles the import function.
	 * 
	 * @param btnImport
	 *            THe import button
	 */
	private void handleImport(final Button btnImport) {
		btnImport.addSelectionListener(new SelectionAdapter() {
			private String parseXmlFile(final String path) {
				final StringBuffer _parsedString = new StringBuffer();
				String _returnString = null;

				try {
					final BufferedReader _br = new BufferedReader(
							new FileReader(new File(path)));

					while (_br.ready()) {
						_parsedString.append(_br.readLine());
					}
					_returnString = _parsedString.toString();
				} catch (FileNotFoundException _fe) {
					_fe.printStackTrace();
					_returnString = null;
				} catch (IOException _ie) {
					_ie.printStackTrace();
					_returnString = null;
				}
				return _returnString;
			}

			public void widgetSelected(final SelectionEvent e) {
				final FileDialog _dialog = new FileDialog(SliceEclipsePlugin
						.getDefault().getWorkbench().getDisplay()
						.getActiveShell(), SWT.OPEN);
				final String[]  _strfilter = new String[1];
				_strfilter[0] = Messages
						.getString("PluginPreference.37");
				_dialog.setFilterExtensions(_strfilter); //$NON-NLS-1$

				final String _path = _dialog.open();

				if (_path != null) {
					final String _configData = parseXmlFile(_path);
					final IPreferenceStore _ps = SliceEclipsePlugin
							.getDefault().getPreferenceStore();
					final String _name = Messages
							.getString("PluginPreference.38"); //$NON-NLS-1$
					String _prefvals = _ps.getString(_name);
					final XStream _xstream = new XStream();
					_xstream
							.alias(
									Messages.getString("PluginPreference.39"), SliceConfigurationHolder.class); //$NON-NLS-1$

					SliceConfigurationHolder _sch = null;

					if (_prefvals.equals("")) { //$NON-NLS-1$
						_sch = new SliceConfigurationHolder();
						_sch.setList(new ArrayList());
					} else {
						_sch = (SliceConfigurationHolder) _xstream
								.fromXML(_prefvals);
					}
					_sch.getList().add(_configData);
					_prefvals = _xstream.toXML(_sch);
					_ps.setValue(_name, _prefvals);
					exemptTagsList.removeAll();
					initializeList(exemptTagsList);
				}
			}
		});
	}

	/**
	 * Initilizes the configuration list.
	 * 
	 * @param list
	 *            The configuration list
	 */
	private void initializeList(final List list) {
		list.add(Messages.getString("PluginPreference.41")); //$NON-NLS-1$

		final IPreferenceStore _ps = SliceEclipsePlugin.getDefault()
				.getPreferenceStore();
		final String _name = Messages.getString("PluginPreference.42"); //$NON-NLS-1$

		//_ps.setValue(_name, "");
		final String _prefvals = _ps.getString(_name);

		if (!_prefvals.equals("")) { //$NON-NLS-1$
			final XStream _xstream = new XStream();
			_xstream
					.alias(
							Messages.getString("PluginPreference.44"), SliceConfigurationHolder.class); //$NON-NLS-1$

			final SliceConfigurationHolder _sch = (SliceConfigurationHolder) _xstream
					.fromXML(_prefvals);
			final java.util.List _lst = _sch.getList();
			final SlicerTool _stool = new SlicerTool(TokenUtil
					.getTokenManager(),
					new ExceptionFlowSensitiveStmtGraphFactory());
			for (int _i = 0; _i < _lst.size(); _i++) {
				final String _config = (String) _lst.get(_i);
				_stool.destringizeConfiguration(_config);
				//list.add("Configuration" + _i);
				list.add(_stool.getActiveConfiguration().getConfigName());
			}
		}
	}

	/**
	 * Initialized the views.
	 * 
	 * @param viewList
	 *            The view list
	 */
	private void initializeViewList(final List viewList) {
		viewList.removeAll();

		final XStream _xstream = new XStream();
		_xstream
				.alias(
						Messages.getString("PluginPreference.45"), ViewConfiguration.class); //$NON-NLS-1$

		final String _viewname = Messages.getString("PluginPreference.46"); //$NON-NLS-1$
		final IPreferenceStore _ps = SliceEclipsePlugin.getDefault()
				.getPreferenceStore();

		//_ps.setValue(_viewname, "");
		final String _prefval = _ps.getString(_viewname);

		if (_prefval != null && !_prefval.equals("")) { //$NON-NLS-1$
			final ViewConfiguration _vc = (ViewConfiguration) _xstream
					.fromXML(_prefval);
			final java.util.List _lst = _vc.getList();

			for (int _i = 0; _i < _lst.size(); _i++) {
				viewList.add(Messages.getString("PluginPreference.48") + _i); //$NON-NLS-1$
			}
		}
	}

	/** (non-Javadoc).
	 * @see org.eclipse.jface.preference.PreferencePage#noDefaultAndApplyButton()
	 */
	protected void noDefaultAndApplyButton() {
		super.noDefaultAndApplyButton();
	}
}