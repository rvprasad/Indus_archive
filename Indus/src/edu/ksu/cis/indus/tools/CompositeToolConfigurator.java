
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
import org.eclipse.swt.widgets.Shell;

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
	 */
	AbstractToolConfigurator childConfigurator;

	/**
	 * This combo presents the available configurations.
	 */
	Combo configCombo;

	/**
	 * This is composite on which the child configurator will be displayed.
	 */
	Composite composite;

	/**
	 * The shell on which the provided interface will be displayed.
	 */
	Composite parent;

	/**
	 * This is the composite configuration being configured.
	 */
	CompositeToolConfiguration configurationCollection;

	/**
	 * Creates a new CompositeToolConfigurator object.
	 *
	 * @param configs is the composite configuration.
	 * @param child is the configurator to be used for each configuration instance.
	 *
	 * @pre configs != null and child != null
	 */
	public CompositeToolConfigurator(final CompositeToolConfiguration configs, final AbstractToolConfigurator child) {
		configurationCollection = configs;
		childConfigurator = child;
	}

	/**
	 * Disposes the editor widget. If the widget is displayed, it will be hidden and the widget will not respond to any
	 * subsequent method calls.
	 */
	public void disposeTemplateMethod() {
		composite = null;
		configCombo = null;
		configurationCollection = null;
		childConfigurator.dispose();
		childConfigurator = null;

		if (!parent.isDisposed()) {
			parent.dispose();
			parent = null;
		}
	}

	/**
	 * {@inheritDoc}<code>configuration</code> is <i>ignored</i>.
	 */
	protected void displayTemplateMethod(final AbstractToolConfiguration configuration) {
		configCombo.removeAll();

		for (Iterator i = configurationCollection.configurations.iterator(); i.hasNext();) {
			AbstractToolConfiguration config = (AbstractToolConfiguration) i.next();
			configCombo.add(config.NAME);
		}
		configCombo.select(configurationCollection.configurations.indexOf(
				configurationCollection.getActiveToolConfiguration()));
	}

	/**
	 * {@inheritDoc}<i>Does nothing.</i>
	 */
	protected void initialize(final Composite theComposite) {
		if (theComposite == null) {
			parent = new Shell(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		} else {
			parent = theComposite;
		}

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		parent.setLayout(gridLayout);

		new Label(parent, SWT.NONE).setText("Configurations:");
		configCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		configCombo.setItems(new String[0]);
		configCombo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		configCombo.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					int index = configCombo.getSelectionIndex();
					AbstractToolConfiguration tc =
						(AbstractToolConfiguration) configurationCollection.configurations.get(index);
					configurationCollection.setActiveToolConfiguration(tc);
					childConfigurator.display(composite, tc);
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});
		composite = new Composite(parent, SWT.NONE);

		Button ok = new Button(parent, SWT.PUSH);
		ok.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent evt) {
					parent.setVisible(false);
				}

				public void widgetDefaultSelected(SelectionEvent evt) {
					widgetSelected(evt);
				}
			});
	}
}

/*
   ChangeLog:
   $Log$
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
