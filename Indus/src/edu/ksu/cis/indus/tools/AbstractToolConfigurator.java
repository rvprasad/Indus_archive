
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
public abstract class AbstractToolConfigurator {
	/**
	 * This is the configuration to be handled by this object.
	 */
	protected AbstractToolConfiguration configuration;

	/**
	 * The parent composite on which the provided interface will be displayed.
	 */
	protected Composite parent;

	/**
	 * This indicates if the configurator has been disposed.
	 */
	private boolean disposed;

	/**
	 * This indicates if the configurator has been initialized.
	 */
	private boolean initialized = false;

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
	 * Displays the editor widget.  The widget can be hidden by calling <code>hide()</code>.
	 *
	 * @param composite on which to display the configuration interface.
	 *
	 * @throws RuntimeException when this method is invoked on a disposed instance.
	 *
	 * @pre composite != null
	 */
	public final void display(final Composite composite) {
		if (composite != parent || !initialized) {
			initialized = false;
			parent = composite;
			initialize();
			initialized = true;
		}

		if (!disposed) {
			displayTemplateMethod();
		} else {
			throw new RuntimeException("Disposed configurators cannot be displayed.");
		}
	}

	/**
	 * Disposes the configurator widget. If the configurator was displayed, it will be hidden. The configurator will not 
     * respond to any subsequent method calls.
	 */
	public final void dispose() {
		disposed = true;
        if (!parent.isDisposed()) {
            parent.dispose();
            parent = null;
        }
		disposeTemplateMethod();
	}

	/**
	 * Hides the configurator.  The configurator can be displayed again by calling <code>display()</code>.
	 */
	public void hide() {
		parent.setVisible(false);
	}

	/**
	 * Checks the given configuration.  This is an empty implementation.  Subclasses can check the configuration in this method.
	 *
	 * @param toolConfiguration to be checked.
	 *
	 * @pre toolConfiguration != null
	 */
	protected void checkConfiguration(final AbstractToolConfiguration toolConfiguration) {        
    }

	/**
	 * Checks if this instance has been disposed.
	 *
	 * @return <code>true</code> if this object has been disposed; <code>false</code>, otherwise.
	 */
	protected final boolean isDisposed() {
		return disposed;
	}

	/**
	 * Checks if this instance has been initialized.
	 *
	 * @return <code>true</code> if this object has been initialized; <code>false</code>, otherwise.
	 */
	protected final boolean isInitialized() {
		return initialized;
	}

	/**
	 * Called when <code>display()</code> is called on this instance.  The default implement just displays the widget. The
	 * subclasses can override this method to control the display.
	 */
	protected void displayTemplateMethod() {
		parent.setVisible(true);
	}

	/**
	 * Called when <code>dispose()</code> is called on this instance.  The subclass should clean up GUI related resources
	 * here.
	 */
	protected abstract void disposeTemplateMethod();

	/**
	 * Initialize the configurator.  This will be called once on each configurator.  The intention is to create the GUI
	 * resources here and later on use during display.  This will be called before <code>displayTemplateMethod()</code> is
	 * called.
	 */
	protected abstract void initialize();
}

/*
   ChangeLog:
   $Log$
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
