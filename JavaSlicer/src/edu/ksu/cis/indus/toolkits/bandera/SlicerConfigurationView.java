
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

package edu.ksu.cis.indus.toolkits.bandera;

import edu.ksu.cis.bandera.tool.ToolConfigurationView;
import edu.ksu.cis.bandera.tool.ToolIconView;

import edu.ksu.cis.indus.tools.IToolConfigurator;

import org.eclipse.swt.widgets.Shell;


/**
 * This class exposes {@link edu.ksu.cis.indus.tools.AbstractToolConfigurator AbstractToolConfigurator} and  {@link
 * edu.ksu.cis.indus.tools.slicer.SlicerConfigurator SlicerConfigurator} in a way amenable to the  tool configuration API in
 * bandera toolkit.  One can say it adapts <code>AbstractToolConfigurator</code> to Bandera tool  configuration API.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SlicerConfigurationView
  implements ToolConfigurationView,
	  ToolIconView {
	/** 
	 * The configurator used to configure <code>configuration</code>.
	 *
	 * @invariant configurator != null
	 */
	private final IToolConfigurator configurator;

	/**
	 * Creates a new SlicerConfigurationView object.
	 *
	 * @param theConfigurator is used to configure the slicer.
	 *
	 * @pre theConfigurator != null
	 */
	SlicerConfigurationView(final IToolConfigurator theConfigurator) {
		configurator = theConfigurator;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolIconView#getActiveIcon()
	 */
	public Object getActiveIcon() {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolIconView#getDisabledIcon()
	 */
	public Object getDisabledIcon() {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolIconView#getIcon()
	 */
	public Object getIcon() {
		return null;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolIconView#getText()
	 */
	public String getText() {
		return "Configure Slicer";
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolIconView#getToolTipText()
	 */
	public String getToolTipText() {
		return "Click here to configure the slicer";
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolConfigurationView#configure()
	 */
	public void configure() {
		final Shell _shell = new Shell();
		_shell.setText("Configure Slicer");
		configurator.initialize(_shell);
		_shell.pack();
		_shell.open();
	}
}

// End of File
