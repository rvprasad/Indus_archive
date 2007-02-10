/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.tools;

import edu.ksu.cis.indus.annotations.Empty;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * This class provides abstract implementation of <code>ITooConfigurator</code> interface which the concrete implementations
 * should extend.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractToolConfigurator
		implements DisposeListener, IToolConfigurator {

	/**
	 * This class handles the changing of boolean property as per to the selection of the associated button widget.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	protected static class BooleanPropertySelectionListener
			implements SelectionListener {

		/**
		 * The button widget that triggers property changes.
		 */
		protected final Button button;

		/**
		 * The configuration that houses the associated property.
		 */
		protected final IToolConfiguration containingConfiguration;

		/**
		 * The id of the property which can be changed via <code>button</code>.
		 */
		protected final Comparable<? extends Object> id;

		/**
		 * Creates a new BooleanSelectionListener object.
		 * 
		 * @param propID is the property id that can be changed via <code>sender</code>.
		 * @param sender is the button widget that is tied to the property.
		 * @param config is the confifugration that houses the given property.
		 * @pre propID != null and sender != null and config != null
		 */
		public BooleanPropertySelectionListener(final Comparable<? extends Object> propID, final Button sender, final IToolConfiguration config) {
			id = propID;
			button = sender;
			containingConfiguration = config;
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(final SelectionEvent evt) {
			widgetSelected(evt);
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(@SuppressWarnings("unused") final SelectionEvent evt) {
			containingConfiguration.setProperty(id, Boolean.valueOf(button.getSelection()));
		}
	}

	/**
	 * This is the configuration to be handled by this object.
	 */
	protected IToolConfiguration configuration;

	/**
	 * The parent composite on which the provided interface will be displayed.
	 */
	protected Composite parent;

	/**
	 * @see IToolConfigurator#initialize(Composite)
	 */
	public final void initialize(final Composite composite) {
		composite.removeDisposeListener(this);
		parent = composite;
		parent.addDisposeListener(this);
		setup();
	}

	/**
	 * @see IToolConfigurator#setConfiguration(IToolConfiguration)
	 */
	public final void setConfiguration(final IToolConfiguration toolConfiguration) {
		checkConfiguration(toolConfiguration);
		configuration = toolConfiguration;
	}

	/**
	 * @see IToolConfigurator#widgetDisposed(DisposeEvent)
	 */
	@Empty public void widgetDisposed(@SuppressWarnings("unused") final DisposeEvent evt) {
		// does nothing
	}

	/**
	 * Checks the given configuration. This is an empty implementation. Subclasses can check the configuration in this method.
	 * 
	 * @param toolConfiguration to be checked.
	 * @pre toolConfiguration != null
	 */
	protected abstract void checkConfiguration(final IToolConfiguration toolConfiguration);

	/**
	 * Setup the graphical parts of the configurator. This will be called before the configurator is displayed.
	 */
	protected abstract void setup();
}

// End of File
