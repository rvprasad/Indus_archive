
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

import edu.ksu.cis.indus.interfaces.IStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is an abstract implementation of ITool which the concrete implementations are encouraged to extend.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractTool
  implements IStatus,
	  ITool {
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
	protected IToolConfiguration configurationInfo;

	/**
	 * This is the configurator associated with this tool instance.  Subclasses should provide a valid reference.
	 *
	 * @invariant configurator != null
	 */
	protected IToolConfigurator configurator;

	/**
	 * This indicates if the tool should pause execution.
	 */
	boolean pause;

	/**
	 * The thread in which the tools is running or ran previously.
	 */
	private Thread thread;

	/**
	 * Retrieves an object that represents the active configuration of the tool.
	 *
	 * @return the active configuration of the tool.
	 *
	 * @post result != null
	 */
	public final IToolConfiguration getActiveConfiguration() {
		IToolConfiguration result;

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
	public final IToolConfigurator getConfigurator() {
		return configurator;
	}

	/**
	 * Checks if the tool is in a stable state.  Tools are in an unstable state when they are running. {@inheritDoc}
	 *
	 * @return <code>true</code> if the tool is not active; <code>false</code>, otherwise.
	 */
	public final synchronized boolean isStable() {
		return thread == null || !thread.isAlive();
	}

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
	 * Executes the tool.
	 *
	 * @param phase is the suggestive phase to start execution in.
	 * @param synchronous <code>true</code> indicates that this method should behave synchronously and return only after the
	 * 		  tool's run has completed; <code>false</code> indicates that this method can return once the tool has started
	 * 		  it's run.
	 *
	 * @throws IllegalStateException when this method called on a paused tool.
	 */
	public final synchronized void run(final Object phase, final boolean synchronous) {
		if (!pause || isStable()) {
			thread =
				new Thread() {
						public final void run() {
							try {
								execute(phase);
							} catch (InterruptedException e) {
								LOGGER.error("InterruptedException occurred.  Resetting the execution pipeline.", e);
								pause = false;
							}
						}
					};
			thread.start();

			if (synchronous) {
				while (!isStable()) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						LOGGER.error("Interrupted while waiting on the run to complete.", e);
						pause = false;
					}
				}
			}
		} else {
			throw new IllegalStateException("run() should be called when the tool is paused or running.");
		}
	}

	/**
	 * This is the template method in which the actual processing of the tool happens.
	 *
	 * @param phase is the suggestive phase to start execution in.
	 *
	 * @throws InterruptedException when the execution of the tool is interrupted.
	 */
	protected abstract void execute(final Object phase)
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
   Revision 1.11  2003/12/02 11:47:19  venku
   - raised the tool to an interface ITool.
   Revision 1.10  2003/12/02 11:31:57  venku
   - Added Interfaces for ToolConfiguration and ToolConfigurator.
   - coding convention and formatting.
   Revision 1.9  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.8  2003/12/02 01:30:59  venku
   - coding conventions and formatting.
   Revision 1.7  2003/11/17 17:56:25  venku
   - reinstated initialize() method in AbstractTool and SlicerTool.  It provides a neat
     way to intialize the tool independent of how it's dependent
     parts (such as configuration) were instantiated and intialized.
   Revision 1.6  2003/11/17 01:46:38  venku
   - documented the support to query stability information.
   Revision 1.5  2003/11/15 21:54:24  venku
   - added support to query status of tool.
   Revision 1.4  2003/11/15 21:26:08  venku
   - removed initialize() method as it was not used.
   Revision 1.3  2003/11/09 05:18:16  venku
   - changed destringizeConfiguraiton() method to inform
     the caller if the given information was used to construct the
     configuration or not.
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
