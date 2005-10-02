/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.tools;

import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

/**
 * This class provides a graphical interface to configure composite tool configurations. It typically provides the support to
 * pick each configuration and delegates the configuration of each set of configuration information to specific configurators.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CompositeToolConfigurator
		extends AbstractToolConfigurator {

	/**
	 * This is composite on which the child configurator will be displayed.
	 */
	private Composite childComposite;

	/**
	 * This is the child configurator to be used to configure each instance of configuration.
	 * 
	 * @invariant childConfigurator != null
	 */
	private IToolConfigurator childConfigurator;

	/**
	 * This is the composite configuration being configured.
	 * 
	 * @invariant compositeConfiguration != null
	 */
	private CompositeToolConfiguration compositeConfiguration;

	/**
	 * This combo presents the available configurations.
	 */
	private Combo configCombo;

	/**
	 * This is the index that is selected in the combo.
	 */
	private int selectedIndex;

	/**
	 * The factory used to create a new configuration instance.
	 * 
	 * @invariant toolConfigFactory != null
	 */
	private IToolConfigurationFactory toolConfigFactory;

	/**
	 * Creates a new CompositeToolConfigurator object.
	 * 
	 * @param compositeConfigs is the composite configuration.
	 * @param child is the configurator to be used for each configuration instance.
	 * @param factory is used to create tool configuration of a specific type when there exists none.
	 * @pre compositeConfigs != null and child != null and factory != null
	 */
	public CompositeToolConfigurator(final CompositeToolConfiguration compositeConfigs, final IToolConfigurator child,
			final IToolConfigurationFactory factory) {
		compositeConfiguration = compositeConfigs;
		childConfigurator = child;
		toolConfigFactory = factory;
	}

	/**
	 * @see edu.ksu.cis.indus.tools.AbstractToolConfigurator#checkConfiguration(edu.ksu.cis.indus.tools.IToolConfiguration)
	 */
	@Override protected void checkConfiguration(@SuppressWarnings("unused") final IToolConfiguration t) {
		// does nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override protected void setup() {
		final GridLayout _gridLayout = new GridLayout();
		_gridLayout.numColumns = 3;
		parent.setLayout(_gridLayout);

		final Label _label = new Label(parent, SWT.NONE);
		GridData _gridData = new GridData();
		_gridData.horizontalSpan = 1;
		_label.setLayoutData(_gridData);
		_label.setText("Configurations:");

		configCombo = new Combo(parent, SWT.DROP_DOWN);
		configCombo.setItems(new String[0]);
		configCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		configCombo.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(final SelectionEvent evt) {
				widgetSelected(evt);
			}

			public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent evt) {
				recordSelection();
				displayChild();
				parent.layout();
				parent.pack();
			}
		});

		configCombo.addFocusListener(new FocusAdapter() {

			@Override public void focusGained(@SuppressWarnings("unused") final FocusEvent evt) {
				recordSelection();
			}

			@Override public void focusLost(@SuppressWarnings("unused") final FocusEvent evt) {
				updateConfigName();
			}
		});

		configCombo.setVisible(true);

		final Button _newConfig = new Button(parent, SWT.PUSH);
		_newConfig.setText("Create");
		_gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		_gridData.horizontalIndent = _newConfig.getText().length();
		_newConfig.setLayoutData(_gridData);
		_newConfig.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(final SelectionEvent evt) {
				widgetSelected(evt);
			}

			public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent evt) {
				createNewConfiguration();
				displayChild();
				parent.layout();
				parent.pack();
			}
		});

		if (compositeConfiguration.configurations.isEmpty()) {
			compositeConfiguration.configurations.add(toolConfigFactory.createToolConfiguration());
		}

		for (final Iterator _i = compositeConfiguration.configurations.iterator(); _i.hasNext();) {
			final IToolConfiguration _config = (IToolConfiguration) _i.next();
			configCombo.add(_config.getConfigName());
		}

		final IToolConfiguration _c = compositeConfiguration.getActiveToolConfiguration();
		configCombo.select(compositeConfiguration.configurations.indexOf(_c));
		displayChild();
	}

	/**
	 * Creates a new configuration.
	 */
	void createNewConfiguration() {
		final IToolConfiguration _temp = toolConfigFactory.createToolConfiguration();
		_temp.setConfigName("tool_configuration_" + compositeConfiguration.configurations.size());
		compositeConfiguration.addToolConfiguration(_temp);
		configCombo.add(_temp.getConfigName());
		configCombo.select(compositeConfiguration.configurations.indexOf(_temp));
	}

	/**
	 * Displays the child configurator.
	 */
	void displayChild() {
		if (childComposite != null) {
			childComposite.dispose();
		}
		childComposite = new Composite(parent, SWT.NONE);

		final GridData _gridData = new GridData(GridData.FILL_HORIZONTAL);
		_gridData.horizontalSpan = 3;
		childComposite.setLayoutData(_gridData);
		childComposite.setVisible(true);

		final int _index = configCombo.getSelectionIndex();

		if (_index != -1) {
			final IToolConfiguration _tc = compositeConfiguration.configurations.get(_index);
			compositeConfiguration.setActiveToolConfiguration(_tc);
			childConfigurator.setConfiguration(_tc);
			childConfigurator.initialize(childComposite);
		}
	}

	/**
	 * Records the index of the current selection in the Combo.
	 */
	void recordSelection() {
		selectedIndex = configCombo.getSelectionIndex();
	}

	/**
	 * Updates the selected configuration's name if it was edited in the text box.
	 */
	void updateConfigName() {
		final int _selIndex = configCombo.getSelectionIndex();

		// if text was changed, selection index is -1 when the name is changed.
		if (_selIndex < 0) {
			// retrive the text at previously selected index and the corresponding configuration
			final List _configurations = compositeConfiguration.configurations;
			final IToolConfiguration _selConfig = (IToolConfiguration) _configurations.get(selectedIndex);

			// retrieve the new Text in the text box.
			final String _newText = configCombo.getText();
			boolean _noDuplicate = true;

			// check if the new name will lead to duplicate entries.
			for (int _i = configCombo.getItemCount() - 1; _i >= 0 && _noDuplicate; _i--) {
				if (((IToolConfiguration) _configurations.get(_i)).getConfigName().equals(_newText)) {
					final MessageBox _msgBox = new MessageBox(parent.getShell(), SWT.OK | SWT.ICON_INFORMATION);
					_msgBox.setMessage("A configuration with the name of \"" + _newText
							+ "\" exists.  \nNo changes will be made.");
					_msgBox.open();
					_noDuplicate = false;
				}
			}

			// if there will be no duplicate entries, then...
			if (_noDuplicate) {
				_selConfig.setConfigName(_newText);
				configCombo.remove(selectedIndex);
				configCombo.add(_newText, selectedIndex);
			}
		}
	}
}

// End of File
