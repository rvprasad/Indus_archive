
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import java.util.Iterator;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CompositeToolConfigurator
  extends ToolConfigurator {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	Combo configCombo;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	Composite composite;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	CompositeToolConfiguration configurationCollection;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	ToolConfigurator childConfigurator;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private Shell shell;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private String title;

	/**
	 * Creates a new CompositeToolConfigurator object.
	 *
	 * @param configs DOCUMENT ME!
	 * @param child DOCUMENT ME!
	 */
	protected CompositeToolConfigurator(final CompositeToolConfiguration configs, final ToolConfigurator child) {
		configurationCollection = configs;
		childConfigurator = child;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param text DOCUMENT ME!
	 */
	public void setTitle(final String text) {
		title = text;
	}

	/**
	 * Disposes the editor widget. If the widget is displayed, it will be hidden and the widget will not respond to any
	 * subsequent method calls.
	 */
	public void disposeTemplateMethod() {
		composite = null;
		configCombo = null;
		shell.dispose();
		shell = null;
		configurationCollection = null;
		childConfigurator.dispose();
		childConfigurator = null;
	}

	/**
	 * Hides the editor widget.  The widget can be redisplayed by calling <code>display()</code>.
	 */
	public void hide() {
		shell.close();
	}

	/**
	 * Displays the editor widget.  The widget can be hidden by calling <code>hide()</code>.
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	public void open() {
		if (!isInitialized()) {
			initialize();
		}

		if (!isDisposed()) {
			configCombo.removeAll();

			for (Iterator i = configurationCollection.configurations.iterator(); i.hasNext();) {
				ToolConfiguration config = (ToolConfiguration) i.next();
				configCombo.add(config.NAME);
			}
			configCombo.select(configurationCollection.configurations.indexOf(
					configurationCollection.getActiveToolConfiguration()));
		} else {
			throw new RuntimeException("Disposed configurators cannot be displayed.");
		}
	}

	/**
	 * {@inheritDoc}<i>This method implementation is empty.</i>
	 *
	 * @see edu.ksu.cis.indus.tools.ToolConfigurator#display(java.lang.Object)
	 */
	protected void displayTemplateMethod(final ToolConfiguration configuration) {
		// does nothing
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param composite DOCUMENT ME!
	 */
	protected void initialize(final Composite composite) {
        // does nothing
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	private void initialize() {
		shell = new Shell(SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(title);

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		shell.setLayout(gridLayout);

		new Label(shell, SWT.NONE).setText("Configurations:");
		configCombo = new Combo(shell, SWT.DROP_DOWN | SWT.READ_ONLY);
		configCombo.setItems(new String[0]);
		configCombo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		configCombo.addSelectionListener(new SelectionListener() {
				public void widgetSelected(final SelectionEvent evt) {
					int index = configCombo.getSelectionIndex();
					ToolConfiguration tc = (ToolConfiguration) configurationCollection.configurations.get(index);
					configurationCollection.setActiveToolConfiguration(tc);
					childConfigurator.display(composite, tc);
				}

				public void widgetDefaultSelected(final SelectionEvent evt) {
					widgetSelected(evt);
				}
			});
		composite = new Composite(shell, SWT.NONE);
	}
}

/*
   ChangeLog:
   $Log$
 */
