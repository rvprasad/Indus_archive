
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
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


/**
 * This is the API exposed by the tool for configuring it via GUI.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractToolConfigurator
  implements DisposeListener {
	/**
	 * This is the configuration to be handled by this object.
	 */
	protected AbstractToolConfiguration configuration;

	/**
	 * The parent composite on which the provided interface will be displayed.
	 */
	protected Composite parent;

	/**
	 * This class handles the changing of boolean property as per to the selection of the associated button widget.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	protected class BooleanPropertySelectionListener
	  implements SelectionListener {
		/**
		 * The configuration that houses the associated property.
		 */
		private final AbstractToolConfiguration configuration;

		/**
		 * The button widget that triggers property changes.
		 */
		private final Button button;

		/**
		 * The id of the property which can be changed via <code>button</code>.
		 */
		private final Object id;

		/**
		 * Creates a new BooleanSelectionListener object.
		 *
		 * @param propID is the property id that can be changed via <code>sender</code>.
		 * @param sender is the button widget that is tied to the property.
		 * @param config is the confifugration that houses the given property.
		 *
		 * @pre propID != null and sender != null and config != null
		 */
		public BooleanPropertySelectionListener(final Object propID, final Button sender,
			final AbstractToolConfiguration config) {
			id = propID;
			button = sender;
			configuration = config;
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
		public void widgetSelected(final SelectionEvent evt) {
			configuration.setProperty(id, Boolean.valueOf(button.getSelection()));
		}
	}

	/**
	 * Sets the configuration to be configured.
	 *
	 * @param toolConfiguration is the configuration to be edited.
	 *
	 * @pre toolConfiguration != null
	 */
	public final void setConfiguration(final AbstractToolConfiguration toolConfiguration) {
		checkConfiguration(toolConfiguration);
		configuration = toolConfiguration;
	}

	/**
	 * Initializes the configurator with the given composite on which it should provide the UI.
	 *
	 * @param composite on which the UI is provided.
	 *
	 * @pre composite != null
	 */
	public final void initialize(final Composite composite) {
		parent = composite;
		parent.addDisposeListener(this);
		setup();
	}

	/**
	 * Called when the parent widget is disposed.  Subclasses should override this method appropriately.
	 *
	 * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
	 */
	public void widgetDisposed(final DisposeEvent evt) {
	}

	/**
	 * Checks the given configuration.  This is an empty implementation.  Subclasses can check the configuration in this
	 * method.
	 *
	 * @param toolConfiguration to be checked.
	 *
	 * @pre toolConfiguration != null
	 */
	protected void checkConfiguration(final AbstractToolConfiguration toolConfiguration) {
	}

	/**
	 * Setup the graphical parts of the configurator.  This will be called before the configurator is displayed.
	 */
	protected abstract void setup();
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/10/14 02:56:51  venku
   - exposed parent field to subclasses.
   - added hide() method
   - added setConfiguration() method to change configuration
   - added checkConfiguration() because of setConfiguration()
   Revision 1.2  2003/09/27 01:09:36  venku
   - changed AbstractToolConfigurator and CompositeToolConfigurator
     such that the composite to display the interface on is provided by the application.
   - documentation.
   Revision 1.1  2003/09/26 23:46:59  venku
   - Renamed Tool to AbstractTool
   - Renamed ToolConfiguration to AbstractToolConfiguration
   - Renamed ToolConfigurator to AbstractToolConfigurator
   Revision 1.4  2003/09/26 15:00:01  venku
   - The configuration of tools in Indus has been placed in this package.
   - Formatting.
   Revision 1.3  2003/09/26 13:58:43  venku
   - checkpoint commit.
   - Renamed ToolConfigurationCollection to CompositeToolConfiguration
   - Renamed CollectiveToolConfigurator to CompositeToolConfigurator
   Revision 1.2  2003/09/26 05:56:10  venku
   - a checkpoint commit.
   Revision 1.1  2003/09/24 07:03:02  venku
   - Renamed ToolConfigurationEditor to AbstractToolConfigurator.
   - Added property id creation support, via factory method, to AbstractToolConfiguration.
   - Changed the interface in AbstractTool.
   Revision 1.1  2003/09/24 02:38:55  venku
   - Added Interfaces to expose the components of Indus as a
     tool and configure it.
 */
