
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

import org.eclipse.swt.events.DisposeEvent;

import org.eclipse.swt.widgets.Composite;


/**
 * This is the interface exposed by the tool for configuring it via GUI.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IToolConfigurator {
	/**
	 * Sets the configuration to be configured.
	 *
	 * @param toolConfiguration is the configuration to be edited.
	 *
	 * @pre toolConfiguration != null
	 */
	void setConfiguration(final IToolConfiguration toolConfiguration);

	/**
	 * Initializes the configurator with the given composite on which it should provide the UI.
	 *
	 * @param composite on which the UI is provided.
	 *
	 * @pre composite != null
	 */
	void initialize(final Composite composite);

	/**
	 * Called when the parent widget is disposed.  Subclasses should override this method appropriately.
	 *
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	void widgetDisposed(final DisposeEvent evt);
}

/*
   ChangeLog:
   $Log$
 */
