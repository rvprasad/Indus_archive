
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is the facade interface exposed by a tool in Indus.  The tool will expose the configurationCollection via
 * <code>AbstractToolConfiguration</code>, hence, this api forces the tool implementation to handle the interaction with the
 * environment for issues such as persistence of the configurationCollection.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractTool {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(AbstractTool.class);

	/**
	 * This an object used to control the execution of the tool.
	 */
	protected final Object control = new Object();

	/**
	 * This is the configuration information associated with this tool instance.  Subclasses should provide a valid
	 * reference.
	 *
	 * @invariant configurationInfo != null
	 */
	protected AbstractToolConfiguration configurationInfo;

	/**
	 * This is the configurator associated with this tool instance.  Subclasses should provide a valid reference.
	 *
	 * @invariant configurator != null
	 */
	protected AbstractToolConfigurator configurator;

	/**
	 * This indicates if the tool should pause execution.
	 */
	boolean pause = false;

	/**
	 * Populate this object with the information in given in string form.
	 *
	 * @param stringizedForm contains the information to be loaded into this object.
	 *
	 * @return <code>true</code> if the configuration could be constructed from the given stringized form. <code>false</code>
	 * 		   if the configuration could not be constructed from the given stringized form.  In the latter case, the
	 * 		   implementation  can load a default configuration.
	 *
	 * @pre stringizedForm != null
	 */
	public abstract boolean destringizeConfiguration(final String stringizedForm);

	/**
	 * Returns a stringized from of the information in the object suitable for serialization.
	 *
	 * @return a stringized representation of the infornmation in the collection.
	 *
	 * @post result != null
	 */
	public abstract String stringizeConfiguration();

	/**
	 * Retrieves an object that represents the active configuration of the tool.
	 *
	 * @return the active configuration of the tool.
	 *
	 * @post result != null
	 */
	public final AbstractToolConfiguration getActiveConfiguration() {
		AbstractToolConfiguration result;

		if (configurationInfo instanceof CompositeToolConfiguration) {
			result = ((CompositeToolConfiguration) configurationInfo).getActiveToolConfiguration();
		} else {
			result = configurationInfo;
		}
		return result;
	}

	/**
	 * Retrieves an editor which enables the user to edit the configuration of the tool.  This can return <code>null</code>,
	 * if the tool does not have a configurationCollection to edit which is seldom the case.
	 *
	 * @return a configurationCollection editor.
	 */
	public final AbstractToolConfigurator getConfigurator() {
		return configurator;
	}

	/**
	 * Executes the tool.
	 *
	 * @param phase is the suggestive phase to start execution in.
	 *
	 * @throws RuntimeException when this method called on a paused tool.
	 */
	public final void run(final Object phase) {
		if (!pause) {
			Thread i =
				new Thread() {
					public void run() {
						try {
							execute(phase);
						} catch (InterruptedException e) {
							LOGGER.error("InterruptedException occurred.  Resetting the execution pipeline.", e);
							pause = false;
						}
					}
				};
			i.start();
		} else {
			throw new RuntimeException("run() should be called when the tool is paused.");
		}
	}

	/**
	 * Retursn the current phase in which the tool was executing.
	 *
	 * @return the current phase.
	 */
	public abstract Object getPhase();

	/**
	 * Aborts the execution of the tool.
	 */
	public final void abort() {
		// TODO: this needs more support from the underlying framework.
	}

	/**
	 * Pauses the execution of the tool.
	 */
	public final void pause() {
		pause = true;
	}

	/**
	 * Resumes the execution of the tool.
	 */
	public final void resume() {
		pause = false;
		control.notify();
	}

	/**
	 * Initialize the tool.  This is usually called once on the tool.
	 */
	public abstract void initialize();

	/**
	 * Reset the tool.  All state related information should be erased.
	 */
	public abstract void reset();

	/**
	 * This is the template method in which the actual processing of the tool happens.
	 *
	 * @param phase is the suggestive phase to start execution in.
	 */
	protected abstract void execute(Object phase)
	  throws InterruptedException;

	/**
	 * Used to suspend the tool execution. This indicates that the tool implementation is moving onto a new phase, hence, it
	 * is at a point where it is safe to  pause/suspend execution.  If the application had requested the tool to pause via
	 * <code>pause()</code>, this method will suspend the execution of the tool.
	 *
	 * @throws InterruptedException when the thread in which the tool has paused is interrupted.
	 */
	protected final void movingToNextPhase()
	  throws InterruptedException {
		if (pause) {
			control.wait();
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/10/19 20:29:03  venku
   - access specifier on pause.
   Revision 1.1  2003/09/26 23:46:58  venku
   - Renamed Tool to AbstractTool
   - Renamed ToolConfiguration to AbstractToolConfiguration
   - Renamed ToolConfigurator to AbstractToolConfigurator
   Revision 1.5  2003/09/26 15:00:01  venku
   - The configuration of tools in Indus has been placed in this package.
   - Formatting.
   Revision 1.4  2003/09/26 13:58:43  venku
   - checkpoint commit.
   - Renamed ToolConfigurationCollection to CompositeToolConfiguration
   - Renamed CollectiveToolConfigurator to CompositeToolConfigurator
   Revision 1.3  2003/09/26 05:56:10  venku
   - a checkpoint commit.
   Revision 1.2  2003/09/24 07:03:02  venku
   - Renamed ToolConfigurationEditor to AbstractToolConfigurator.
   - Added property id creation support, via factory method, to AbstractToolConfiguration.
   - Changed the interface in AbstractTool.
   Revision 1.1  2003/09/24 02:38:55  venku
   - Added Interfaces to expose the components of Indus as a
     tool and configure it.
 */
