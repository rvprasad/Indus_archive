
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
import edu.ksu.cis.indus.tools.ToolConfiguration;
import edu.ksu.cis.indus.tools.ToolConfigurator;

import org.eclipse.swt.widgets.Shell;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicerConfigurationView
  implements ToolConfigurationView,
	  ToolIconView {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private ToolConfiguration configuration;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private ToolConfigurator configurator;

	/**
	 * Creates a new SlicerConfigurationView object.
	 *
	 * @param theConfigurator DOCUMENT ME!
	 */
	SlicerConfigurationView(final ToolConfigurator theConfigurator) {
		configurator = theConfigurator;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.ToolIconView#getActiveIcon()
	 */
	public Object getActiveIcon() {
		return null;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param tc DOCUMENT ME!
	 */
	public void setConfiguration(final ToolConfiguration tc) {
		configuration = tc;
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
	 * @see edu.ksu.cis.bandera.tool.ToolConfigurator#configure()
	 */
	public void configure() {
		configurator.display(new Shell(), configuration);
	}
}

/*
   ChangeLog:
   $Log$
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
