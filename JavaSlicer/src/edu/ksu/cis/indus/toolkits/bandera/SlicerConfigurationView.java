
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

package edu.ksu.cis.indus.toolkits.bandera;

import edu.ksu.cis.bandera.tool.ToolConfigurationView;
import edu.ksu.cis.bandera.tool.ToolIconView;
import edu.ksu.cis.indus.tools.AbstractToolConfigurator;

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
public class SlicerConfigurationView
  implements ToolConfigurationView,
	  ToolIconView {
	/**
	 * The configurator used to configure <code>configuration</code>.
	 *
	 * @invariant configurator != null
	 */
	private final AbstractToolConfigurator configurator;

	/**
	 * The window in which the configuration interface of the slicer will be displayed.
	 */
	private Shell shell;

	/**
	 * Creates a new SlicerConfigurationView object.
	 *
	 * @param theConfigurator is used to configure the slicer.
	 *
	 * @pre theConfigurator != null
	 */
	SlicerConfigurationView(final AbstractToolConfigurator theConfigurator) {
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
		if (shell == null) {
			shell = new Shell();
			shell.setText("Configure Slicer");
		}
		configurator.initialize(shell);
		shell.pack();
		shell.open();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2003/10/14 03:00:22  venku
   - setConfiguration() was not called. This makes sense as the
     CompositeToolConfigurator() will provide the configuration.
   Revision 1.6  2003/09/28 23:16:18  venku
   - documentation
   Revision 1.5  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.4  2003/09/27 01:09:35  venku
   - changed AbstractToolConfigurator and CompositeToolConfigurator
     such that the composite to display the interface on is provided by the application.
   - documentation.
   Revision 1.3  2003/09/26 15:07:51  venku
   - completed support for exposing slicer as a tool
     and configuring it both in Bandera and outside it.
   Revision 1.2  2003/09/24 07:33:24  venku
   - Nightly commit.
   - Need to wrap the indus tool api in ways specific to bandera
     tool api.
   Revision 1.1  2003/09/24 01:43:45  venku
   - Renamed edu.ksu.cis.indus.tools to edu.ksu.cis.indus.toolkits.
     This package is to house adaptation of each tools for each toolkits.
   - Retained edu.ksu.cis.indus.tools to contain API/interface to expose
     the implementation as a tool.
   Revision 1.2  2003/09/16 07:52:25  venku
   - coding convention.
   Revision 1.1  2003/09/15 08:55:23  venku
   - Well, the SlicerTool is still a mess in my opinion as it needs
     to be implemented as required by Bandera.  It needs to be
     much richer than it is to drive the slicer.
   - SlicerConfigurator is supposed to bridge the above gap.
     I doubt it.
 */
