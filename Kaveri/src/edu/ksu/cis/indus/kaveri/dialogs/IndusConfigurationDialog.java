
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

import java.util.Collection;
import java.util.Iterator;

import com.thoughtworks.xstream.XStream;

import com.thoughtworks.xstream.alias.CannotResolveClassException;
import com.thoughtworks.xstream.io.xml.DomDriver;

import edu.ksu.cis.indus.kaveri.KaveriPlugin;
import edu.ksu.cis.indus.kaveri.common.SECommons;
import edu.ksu.cis.indus.kaveri.preferencedata.Criteria;
import edu.ksu.cis.indus.kaveri.preferencedata.CriteriaData;
import edu.ksu.cis.indus.kaveri.preferencedata.ViewConfiguration;
import edu.ksu.cis.indus.kaveri.preferencedata.ViewData;
import edu.ksu.cis.indus.kaveri.scoping.ScopeSelectionDialog;



import edu.ksu.cis.indus.tools.IToolConfiguration;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;

import org.eclipse.core.resources.IResource;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.search.PrettySignature;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;

import org.eclipse.jface.preference.IPreferenceStore;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;


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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;


/**
 * The slice configuration dialog box.  Allows you to pick the configuration and the criteria for slicing.
 *
 * @author Ganeshan
 */
public class IndusConfigurationDialog
  extends Dialog {
	/** 
	 * Checkbox for additive or normal slicing.
	 */
	private Button additive;

	/** 
	 * Configuration combo box.
	 */
	private Combo confCombo;

	/** 
	 * View combo box.
	 */
	private Combo viewCombo;

	/** 
	 * The Java project to which the file belongs.
	 */
	private IJavaProject project;

	/** 
	 * The swt table showing the criteria.
	 */
	private Table criteriaTable;

	/**
	 * The constructor.
	 *
	 * @param parent The parent control
	 * @param javaProject The current Java project.
	 */
	public IndusConfigurationDialog(final Shell parent, final IJavaProject javaProject) {
		super(parent);
		this.project = javaProject;
	}

	/**
	 * Configures the shell.
	 *
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(final Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("IndusConfigurationDialog.0"));  //$NON-NLS-1$
	}

	/**
	 * Creates the run and cancel buttons.
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, Messages.getString("IndusConfigurationDialog.29"), true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Creates the dialog area .
	 *
	 * @param parent The parent control
	 *
	 * @return Control The new control
	 */
	protected Control createDialogArea(final Composite parent) {
		final Composite _composite = new Composite(parent, SWT.NONE);
		final GridLayout _layout = new GridLayout();
		_layout.numColumns = 3;
		_composite.setLayout(_layout);

		final Label _confLabel = new Label(_composite, SWT.NONE);
		_confLabel.setText(Messages.getString("IndusConfigurationDialog.1"));  //$NON-NLS-1$

		GridData _gdata = new GridData();
		_gdata.horizontalSpan = 1;
		_confLabel.setLayoutData(_gdata);

		confCombo = new Combo(_composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		_gdata = new GridData();
		_gdata.horizontalSpan = 2;
		_gdata.grabExcessHorizontalSpace = true;
		confCombo.setLayoutData(_gdata);
		initializeConfigs(confCombo);
		

		final Label _viewLabel = new Label(_composite, SWT.NONE);
		_viewLabel.setText(Messages.getString("IndusConfigurationDialog.2"));  //$NON-NLS-1$
		_gdata = new GridData();
		_gdata.horizontalSpan = 1;
		_viewLabel.setLayoutData(_gdata);
		viewCombo = new Combo(_composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		_gdata = new GridData();
		_gdata.horizontalSpan = 1;
		_gdata.grabExcessHorizontalSpace = true;
		viewCombo.setLayoutData(_gdata);
		initializeViews(viewCombo);

		additive = new Button(_composite, SWT.CHECK);
		additive.setText("Additive slice display");
		_gdata = new GridData();
		_gdata.horizontalSpan = 1;
		additive.setLayoutData(_gdata);
		initializeAdditive();

		final Group _group = new Group(_composite, SWT.NONE);
		_group.setText(Messages.getString("IndusConfigurationDialog.3"));  //$NON-NLS-1$
		final GridLayout _gl = new GridLayout();
		_gl.numColumns = 1;
		_group.setLayout(_gl);
		
//		final FillLayout _fl = new FillLayout(SWT.VERTICAL | SWT.HORIZONTAL);
//		_group.setLayout(_fl);
		criteriaTable = new Table(_group, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		final GridData _gd = new GridData();
		_gd.horizontalSpan = 1;
		_gd.grabExcessHorizontalSpace = true;
		_gd.grabExcessVerticalSpace = true;
		_gd.horizontalAlignment = GridData.FILL;
		_gd.verticalAlignment = GridData.FILL;
		criteriaTable.setLayoutData(_gd);
		
		criteriaTable.setLinesVisible(true);
		criteriaTable.setHeaderVisible(true);

		//criteriaList = new List(_group, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		final int _constant1 = 600;
		final int _constant2 = 1200;
		criteriaTable.setBounds(0, 0, _constant1, _constant2);
		//criteriaList.setBounds(0, 0, _constant1, _constant2);
		initializeList(criteriaTable, project);  // changed from criteriaList

		final Composite _subcomposite2 = new Composite(_composite, SWT.NONE);
		final GridData _subdata2 = new GridData(GridData.FILL_HORIZONTAL);
		_subdata2.horizontalSpan = 3;
		_subcomposite2.setLayoutData(_subdata2);

		final RowLayout _f2 = new RowLayout(SWT.HORIZONTAL);
		final int _constant3 = 30;
		_f2.spacing = _constant3;
		_subcomposite2.setLayout(_f2);

		final Button _btnDelete = new Button(_subcomposite2, SWT.PUSH);
		_btnDelete.setText(Messages.getString("IndusConfigurationDialog.5"));  //$NON-NLS-1$
		handleDelete(_btnDelete, project);
		
		final Button _btnScope = new Button(_subcomposite2, SWT.PUSH);
		_btnScope.setText("Setup Scope");
		handleScope(_btnScope);

		// Add griddata
		GridData _data = new GridData();
		_data.horizontalSpan = 1;
		_confLabel.setLayoutData(_data);
		_data = new GridData(GridData.FILL_HORIZONTAL);
		_data.horizontalSpan = 2;
		confCombo.setLayoutData(_data);

		final GridData _grpData = new GridData(GridData.FILL_HORIZONTAL);
		_grpData.horizontalSpan = 3;
		_group.setLayoutData(_grpData);
		// Reset the scope string
		KaveriPlugin.getDefault().getIndusConfiguration().setScopeSpecification("");
		return _composite;
	}

	/**
	 * The run button has been clicked.  Setup the configuration and the criteria for the slice using the IndusConfiguration
	 * class.
	 *
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		final String _selectedConfiguration = confCombo.getText();
		String _configString = null;
		KaveriPlugin.getDefault().getIndusConfiguration().setCurrentConfiguration(_selectedConfiguration);
		setUpCriteria();		
		KaveriPlugin.getDefault().getIndusConfiguration().setAdditive(additive.getSelection());
		final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();
		final String _additivename = "additiveSliceProperty";
		_ps.setValue(_additivename, additive.getSelection());
		KaveriPlugin.getDefault().savePluginPreferences();
		super.okPressed();
	}

	/**
	 * Sets up the criteria.
	 */
	private void setUpCriteria() {
		final int[] _currentsels = criteriaTable.getSelectionIndices();
		//KaveriPlugin.getDefault().getIndusConfiguration().getCriteria().clear();

		if (_currentsels != null && _currentsels.length >= 1) {
			IResource _resource = project.getResource();
			final XStream _xstream = new XStream(new DomDriver());
			_xstream.alias(Messages.getString("IndusConfigurationDialog.17"), CriteriaData.class);  //$NON-NLS-1$

			try {
				_resource = project.getCorrespondingResource();

				final QualifiedName _name =
					new QualifiedName(Messages.getString("IndusConfigurationDialog.18"),
						Messages.getString("IndusConfigurationDialog.19"));  //$NON-NLS-1$ //$NON-NLS-2$

				try {
					final String _propVal = _resource.getPersistentProperty(_name);

					if (_propVal != null) {
						final CriteriaData _data = (CriteriaData) _xstream.fromXML(_propVal);
						final java.util.List _lst = _data.getCriterias();
						KaveriPlugin.getDefault().getIndusConfiguration().getCriteria().clear();

						for (int _i = 0; _i < _currentsels.length && _currentsels[_i] < _lst.size(); _i++) {
							final int _index = _currentsels[_i];
							final Criteria _c = (Criteria) _lst.get(_index);

							KaveriPlugin.getDefault().getIndusConfiguration().setCriteria(_c);
						}
					}
				} catch (CoreException _e) {
					SECommons.handleException(_e);
				}
			} catch (JavaModelException _e1) {
				SECommons.handleException(_e1);
			}
		}
	}

	/**
	 * Handles the delete button action.
	 *
	 * @param btnDelete The delete button.
	 * @param theproject The project in which the criteria are present.
	 */
	private void handleDelete(final Button btnDelete, final IJavaProject theproject) {
		btnDelete.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(final SelectionEvent e) {
					if (criteriaTable.getSelectionCount() == 1) {
						removeSelection(theproject, criteriaTable.getSelectionIndex());
						criteriaTable.remove(criteriaTable.getSelectionIndex());
					}
				}

				private void removeSelection(final IJavaProject myproject, final int index) {
					IResource _resource;

					try {
						_resource = myproject.getCorrespondingResource();

						final QualifiedName _name =
							new QualifiedName(Messages.getString("IndusConfigurationDialog.6"),
								Messages.getString("IndusConfigurationDialog.7"));

						try {
							final String _propVal = _resource.getPersistentProperty(_name);

							if (_propVal != null) {
								final XStream _xstream = new XStream(new DomDriver());
								_xstream.alias(Messages.getString("IndusConfigurationDialog.8"), CriteriaData.class);

								final CriteriaData _data = (CriteriaData) _xstream.fromXML(_propVal);
								final java.util.List _lst = _data.getCriterias();
								_lst.remove(index);

								final String _xml = _xstream.toXML(_data);
								_resource.setPersistentProperty(_name, _xml);
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
	 * @param btnScope The scope button.
	 */
	private void handleScope(final Button btnScope) {
		btnScope.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				ScopeSelectionDialog _sld = new ScopeSelectionDialog(Display.getCurrent().getActiveShell(),
						 null, null);
				if (_sld.open() == IDialogConstants.OK_ID) {
					final Object _res[] =_sld.getResult();
					if (_res != null && _res.length > 0) {
						String _scopeSpec = "<?xml version=\"1.0\"?>\n";
						
					
						_scopeSpec += "<indus:scopeSpec xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
							"xmlns:indus=\"http://indus.projects.cis.ksu.edu/indus\" " +
							"indus:specName=\"scope_spec\">\n";												
						for (int i = 0; i < _res.length; i++) {
							final IMethod _method = (IMethod) _res[i];							 
							_scopeSpec += "\n<indus:methodSpec indus:specName=\"scope2\" indus:methodNameSpec=\"" + 
								 _method.getElementName()  +  "\">\n";
							_scopeSpec += "<indus:declaringClassSpec indus:scopeExtension=\"IDENTITY\""  +
									"indus:nameSpec=\"" + PrettySignature.getSignature(_method.getParent())  + "\"/>\n";							
						    _scopeSpec += "<indus:returnTypeSpec indus:scopeExtension=\"PRIMITIVE\" indus:nameSpec=\"void\" />\n";																									
						    _scopeSpec += " <indus:parameterSpecs/>";
							_scopeSpec += "</indus:methodSpec>\n";
						}
						_scopeSpec += "</indus:scopeSpec>";
						KaveriPlugin.getDefault().getIndusConfiguration().setScopeSpecification(_scopeSpec);
					}
				}
			}
		});
	}

	/**
	 * Load the previously chosen choice for additive slicing.
	 */
	private void initializeAdditive() {
		final String _propName = "additiveSliceProperty";
		final IPreferenceStore _store = KaveriPlugin.getDefault().getPreferenceStore();
		final boolean _value = _store.getBoolean(_propName);		
		additive.setSelection(_value);
	}

	/**
	 * Initialized the configuration combo box.
	 *
	 * @param confsCombo The configuration combo
	 */
	private void initializeConfigs(final Combo confsCombo) {
		KaveriPlugin.getDefault().loadConfigurations();
		final SlicerTool _slicetool = KaveriPlugin.getDefault().getSlicerTool();
		final Collection _c = _slicetool.getConfigurations();
		for (final Iterator _t = _c.iterator(); _t.hasNext();) {
			confsCombo.add(((IToolConfiguration) _t.next()).getConfigName());
		}		
		final int _activeIndex = confsCombo.indexOf(_slicetool.getActiveConfiguration().getConfigName());
		confsCombo.select(_activeIndex);		
	}

	/**
	 * Initializes the list showing the criteria.
	 *
	 * @param criteriasList The list of criterias
	 * @param javaproject The project in which the criteria are present.
	 */
	private void initializeList(final Table criteriasList, final IJavaProject javaproject) {
		IResource _resource;
		criteriasList.removeAll();

		final XStream _xstream = new XStream(new DomDriver());
		_xstream.alias(Messages.getString("IndusConfigurationDialog.17"), CriteriaData.class);  //$NON-NLS-1$

		try {
			_resource = javaproject.getCorrespondingResource();

			final QualifiedName _name =
				new QualifiedName(Messages.getString("IndusConfigurationDialog.18"),
					Messages.getString("IndusConfigurationDialog.19"));

			try {
				//				_resource.setPersistentProperty(_name, null); // Knocks out
				// the stuff
				final String _propVal = _resource.getPersistentProperty(_name);

				if (_propVal != null) {
					final CriteriaData _data = (CriteriaData) _xstream.fromXML(_propVal);
					final java.util.List _lst = _data.getCriterias();
					final String[] _colnames = { "Function", "Line number", "Jimple index", "Consider Execution" };

					for (int _i = 0; _i < _colnames.length; _i++) {
						final TableColumn _ti = new TableColumn(criteriasList, SWT.NULL);
						_ti.setText(_colnames[_i]);
					}

					for (int _i = 0; _i < _lst.size(); _i++) {
						final Criteria _c = (Criteria) _lst.get(_i);
						final TableItem _item = new TableItem(criteriasList, SWT.NULL);
						_item.setText(0, _c.getStrMethodName());
						_item.setText(1, "" + _c.getNLineNo());
						_item.setText(2, "" + _c.getNJimpleIndex());
						_item.setText(3, "" + _c.isBConsiderValue());

						/*final String _disp = _c.getStrMethodName()
						   + ":" + "java line:"
						   + _c.getNLineNo()
						   + ":jimple index:"
						   + _c.getNJimpleIndex()
						   + ":Consider value: " + _c.isBConsiderValue();
						   criteriasList.add(_disp);*/
					}
					if (_lst.size() == 0) {
						for (int _i = 0; _i < 5; _i++) {
							final TableItem _item = new TableItem(criteriasList, SWT.NULL);
							_item.setText(new String[] {" ", " ", " ", " "});
						}	
					}					
					for (int _i = 0; _i < _colnames.length; _i++) {
						criteriasList.getColumn(_i).pack();
					}

					final int _suggestedTableSize = 200;
					criteriasList.setSize(criteriasList.computeSize(SWT.DEFAULT, _suggestedTableSize));
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

	/**
	 * Initializes the view combo box.
	 *
	 * @param viewsCombo The view combo
	 */
	private void initializeViews(final Combo viewsCombo) {
		viewsCombo.removeAll();

		final XStream _xstream = new XStream(new DomDriver());
		_xstream.alias(Messages.getString("IndusConfigurationDialog.13"), ViewConfiguration.class);  //$NON-NLS-1$

		final String _viewname = Messages.getString("IndusConfigurationDialog.14");  //$NON-NLS-1$
		final IPreferenceStore _ps = KaveriPlugin.getDefault().getPreferenceStore();
		final String _prefval = _ps.getString(_viewname);

		if (_prefval != null && !_prefval.equals("")) {  //$NON-NLS-1$

			final ViewConfiguration _vc = (ViewConfiguration) _xstream.fromXML(_prefval);
			final java.util.List _lst = _vc.getList();

			for (int _i = 0; _i < _lst.size(); _i++) {
				final ViewData _vd = (ViewData) _lst.get(_i);
				viewsCombo.add(Messages.getString("IndusConfigurationDialog.16") + _i);  //$NON-NLS-1$
			}
		}

		if (viewCombo.getItemCount() > 0) {
			viewsCombo.select(0);
		}
	}
}
