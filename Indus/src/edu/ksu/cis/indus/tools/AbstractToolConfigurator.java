
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
 * This class provides abstract implementation of <code>ITooConfigurator</code> interface which the concrete implementations
 * should extend.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractToolConfigurator
  implements DisposeListener,
	  IToolConfigurator {
	/**
	 * The parent composite on which the provided interface will be displayed.
	 */
	protected Composite parent;

	/**
	 * This is the configuration to be handled by this object.
	 */
	protected IToolConfiguration configuration;

	/**
	 * This class handles the changing of boolean property as per to the selection of the associated button widget.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	protected static final class BooleanPropertySelectionListener
	  implements SelectionListener {
		/**
		 * The button widget that triggers property changes.
		 */
		private final Button button;

		/**
		 * The configuration that houses the associated property.
		 */
		private final IToolConfiguration containingConfiguration;

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
		public BooleanPropertySelectionListener(final Object propID, final Button sender, final IToolConfiguration config) {
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
		public void widgetSelected(final SelectionEvent evt) {
			containingConfiguration.setProperty(id, Boolean.valueOf(button.getSelection()));
		}
	}

	/**
	 * @see IToolconfigurator#setConfiguration(IToolConfiguration)
	 */
	public final void setConfiguration(final IToolConfiguration toolConfiguration) {
		checkConfiguration(toolConfiguration);
		configuration = toolConfiguration;
	}

	/**
	 * @see IToolconfigurator#initialize(Composite)
	 */
	public final void initialize(final Composite composite) {
		parent = composite;
		parent.addDisposeListener(this);
		setup();
	}

	/**
	 * @see IToolConfigurator#widgetDisposed(DisposeEvent)
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
	protected abstract void checkConfiguration(final IToolConfiguration toolConfiguration);
    
	/**
	 * Setup the graphical parts of the configurator.  This will be called before the configurator is displayed.
	 */
	protected abstract void setup();
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2003/12/02 11:31:57  venku
   - Added Interfaces for ToolConfiguration and ToolConfigurator.
   - coding convention and formatting.

   Revision 1.6  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.5  2003/11/05 08:20:52  venku
   - coding convention.
   Revision 1.4  2003/10/20 13:55:25  venku
   - Added a factory to create new configurations.
   - Simplified AbstractToolConfigurator methods.
   - The driver manages the shell.
   - Got all the gui parts running EXCEPT for changing
     the name of the configuration.
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
