
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

package edu.ksu.cis.indus.tools;

import java.util.Iterator;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;


/**
 * This class provides a graphical interface to configure composite tool configurations.  It typically provides the support
 * to pick each configuration and delegates the configuration of each set of configuration information to specific
 * configurators.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CompositeToolConfigurator
  extends AbstractToolConfigurator {
	/**
	 * This is the child configurator to be used to configure each instance of configuration.
	 *
	 * @invariant childConfigurator != null
	 */
	AbstractToolConfigurator childConfigurator;

	/**
	 * This combo presents the available configurations.
	 */
	Combo configCombo;

	/**
	 * This is composite on which the child configurator will be displayed.
	 */
	Composite childComposite;

	/**
	 * This is the composite configuration being configured.
	 *
	 * @invariant compositeConfiguration != null
	 */
	CompositeToolConfiguration compositeConfiguration;

	/**
	 * The factory used to create a new configuration instance.
	 *
	 * @invariant toolConfigFactory != null
	 */
	IToolConfigurationFactory toolConfigFactory;

	/**
	 * Creates a new CompositeToolConfigurator object.
	 *
	 * @param compositeConfigs is the composite configuration.
	 * @param child is the configurator to be used for each configuration instance.
	 * @param factory is used to create tool configuration of a specific type when there exists none.
	 *
	 * @pre compositeConfigs != null and child != null and factory != null
	 */
	public CompositeToolConfigurator(final CompositeToolConfiguration compositeConfigs, final AbstractToolConfigurator child,
		final IToolConfigurationFactory factory) {
		compositeConfiguration = compositeConfigs;
		childConfigurator = child;
		toolConfigFactory = factory;
	}

	/**
	 * {@inheritDoc}
	 */
	protected void setup() {
		final GridLayout _gridLayout = new GridLayout();
		_gridLayout.numColumns = 3;
		parent.setLayout(_gridLayout);

		final Label _label = new Label(parent, SWT.NONE);
		_label.setText("Configurations:");

		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		_label.setLayoutData(gridData);
		configCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		configCombo.setItems(new String[0]);
		configCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		configCombo.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					displayChild();
					parent.pack();
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});
		configCombo.setVisible(true);

		final Button _ok = new Button(parent, SWT.PUSH);
		_ok.setText("Ok");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		gridData.horizontalIndent = _ok.getText().length();
		_ok.setLayoutData(gridData);
		_ok.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					parent.dispose();
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});

		final Button _newConfig = new Button(parent, SWT.PUSH);
		_newConfig.setText("Create");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.horizontalIndent = _newConfig.getText().length();
		_newConfig.setLayoutData(gridData);
		_newConfig.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					final AbstractToolConfiguration _atc = toolConfigFactory.createToolConfiguration();
					_atc.configName = "slicer_configuration_" + compositeConfiguration.configurations.size();
					compositeConfiguration.addToolConfiguration(_atc);
					configCombo.add(_atc.configName);
					configCombo.select(compositeConfiguration.configurations.indexOf(_atc));
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});

		if (compositeConfiguration.configurations.isEmpty()) {
			compositeConfiguration.configurations.add(toolConfigFactory.createToolConfiguration());
		}

		for (final Iterator _i = compositeConfiguration.configurations.iterator(); _i.hasNext();) {
			final AbstractToolConfiguration _config = (AbstractToolConfiguration) _i.next();
			configCombo.add(_config.configName);
		}

		final AbstractToolConfiguration _c = compositeConfiguration.getActiveToolConfiguration();
		configCombo.select(compositeConfiguration.configurations.indexOf(_c));
		displayChild();
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
		final AbstractToolConfiguration _tc = (AbstractToolConfiguration) compositeConfiguration.configurations.get(_index);
		compositeConfiguration.setActiveToolConfiguration(_tc);
		childConfigurator.setConfiguration(_tc);
		childConfigurator.initialize(childComposite);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/12/02 01:30:59  venku
   - coding conventions and formatting.
   Revision 1.8  2003/11/03 07:59:26  venku
   - documentation.
   Revision 1.7  2003/10/20 13:55:25  venku
   - Added a factory to create new configurations.
   - Simplified AbstractToolConfigurator methods.
   - The driver manages the shell.
   - Got all the gui parts running EXCEPT for changing
     the name of the configuration.
   Revision 1.6  2003/10/14 02:57:10  venku
   - ripple effect of changes to AbstractToolConfigurator.
   Revision 1.5  2003/09/27 01:27:47  venku
   - documentation.
   Revision 1.4  2003/09/27 01:09:36  venku
   - changed AbstractToolConfigurator and CompositeToolConfigurator
     such that the composite to display the interface on is provided by the application.
   - documentation.
   Revision 1.3  2003/09/26 23:03:13  venku
   - Added OK button.
   Revision 1.2  2003/09/26 15:00:01  venku
   - The configuration of tools in Indus has been placed in this package.
   - Formatting.
   Revision 1.1  2003/09/26 13:58:43  venku
   - checkpoint commit.
   - Renamed ToolConfigurationCollection to CompositeToolConfiguration
   - Renamed CollectiveToolConfigurator to CompositeToolConfigurator
 */
