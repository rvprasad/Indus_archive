
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

import org.eclipse.swt.widgets.Composite;


/**
 * This is the API exposed by the tool for configuring it via GUI.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class ToolConfigurator {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean disposed;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private boolean initialized = false;

	/**
	 * Displays the editor widget.  The widget can be hidden by calling <code>hide()</code>.
	 *
	 * @param composite DOCUMENT ME!
	 * @param configuration DOCUMENT ME!
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	public final void display(Composite composite, ToolConfiguration configuration) {
		if (!initialized) {
			initialize(composite);
			initialized = true;
		}

		if (!disposed) {
			displayTemplateMethod(configuration);
		} else {
			throw new RuntimeException("Disposed configurators cannot be displayed.");
		}
	}

	/**
	 * Disposes the editor widget. If the widget is displayed, it will be hidden and the widget will not respond to any
	 * subsequent method calls.
	 */
	public final void dispose() {
		disposed = true;
		disposeTemplateMethod();
	}

	/**
	 * Hides the editor widget.  The widget can be redisplayed by calling <code>display()</code>.
	 */
	public abstract void hide();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	protected final boolean isDisposed() {
		return disposed;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	protected final boolean isInitialized() {
		return initialized;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param configuration DOCUMENT ME!
	 */
	protected abstract void displayTemplateMethod(final ToolConfiguration configuration);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	protected abstract void disposeTemplateMethod();

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param composite DOCUMENT ME!
	 */
	protected abstract void initialize(final Composite composite);
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/09/26 13:58:43  venku
   - checkpoint commit.
   - Renamed ToolConfigurationCollection to CompositeToolConfiguration
   - Renamed CollectiveToolConfigurator to CompositeToolConfigurator
   Revision 1.2  2003/09/26 05:56:10  venku
   - a checkpoint commit.
   Revision 1.1  2003/09/24 07:03:02  venku
   - Renamed ToolConfigurationEditor to ToolConfigurator.
   - Added property id creation support, via factory method, to ToolConfiguration.
   - Changed the interface in Tool.
   Revision 1.1  2003/09/24 02:38:55  venku
   - Added Interfaces to expose the components of Indus as a
     tool and configure it.
 */
