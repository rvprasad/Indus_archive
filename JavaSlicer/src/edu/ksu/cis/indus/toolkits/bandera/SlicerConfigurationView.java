
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
