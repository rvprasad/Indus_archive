
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.Iterator;


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
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		parent.setLayout(gridLayout);

		Label label = new Label(parent, SWT.NONE);
		label.setText("Configurations:");

		GridData gridData = new GridData();
		gridData.horizontalSpan = 3;
		label.setLayoutData(gridData);
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

		Button ok = new Button(parent, SWT.PUSH);
		ok.setText("Ok");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		gridData.horizontalIndent = ok.getText().length() * 5;
		ok.setLayoutData(gridData);
		ok.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent evt) {
					parent.dispose();
				}

				public void widgetDefaultSelected(SelectionEvent evt) {
					widgetSelected(evt);
				}
			});

		Button newConfig = new Button(parent, SWT.PUSH);
		newConfig.setText("Create");
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		gridData.horizontalIndent = newConfig.getText().length() * 5;
		newConfig.setLayoutData(gridData);
		newConfig.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent evt) {
					AbstractToolConfiguration atc = toolConfigFactory.createToolConfiguration();
					atc.NAME = "slicer_configuration_" + compositeConfiguration.configurations.size();
					compositeConfiguration.addToolConfiguration(atc);
					configCombo.add(atc.NAME);
					configCombo.select(compositeConfiguration.configurations.indexOf(atc));
				}

				public void widgetDefaultSelected(SelectionEvent evt) {
					widgetSelected(evt);
				}
			});

		if (compositeConfiguration.configurations.isEmpty()) {
			compositeConfiguration.configurations.add(toolConfigFactory.createToolConfiguration());
		}

		for (Iterator i = compositeConfiguration.configurations.iterator(); i.hasNext();) {
			AbstractToolConfiguration config = (AbstractToolConfiguration) i.next();
			configCombo.add(config.NAME);
		}

		AbstractToolConfiguration c = compositeConfiguration.getActiveToolConfiguration();
		configCombo.select(compositeConfiguration.configurations.indexOf(c));
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

		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		childComposite.setLayoutData(gridData);
		childComposite.setVisible(true);

		int index = configCombo.getSelectionIndex();
		AbstractToolConfiguration tc = (AbstractToolConfiguration) compositeConfiguration.configurations.get(index);
		compositeConfiguration.setActiveToolConfiguration(tc);
		childConfigurator.setConfiguration(tc);
		childConfigurator.initialize(childComposite);
	}
}

/*
   ChangeLog:
   $Log$
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
