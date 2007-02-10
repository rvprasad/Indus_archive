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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;

import org.eclipse.swt.widgets.Composite;

/**
 * This is the interface exposed by the tool for configuring it via GUI.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IToolConfigurator
		extends DisposeListener {

	/**
	 * Initializes the configurator with the given composite on which it should provide the UI.
	 * 
	 * @param composite on which the UI is provided.
	 * @pre composite != null
	 */
	void initialize(final Composite composite);

	/**
	 * Sets the configuration to be configured.
	 * 
	 * @param toolConfiguration is the configuration to be edited.
	 * @pre toolConfiguration != null
	 */
	void setConfiguration(final IToolConfiguration toolConfiguration);

	/**
	 * Called when the parent widget is disposed. Subclasses should override this method appropriately.
	 * 
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	void widgetDisposed(final DisposeEvent evt);
}

// End of File
