
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

import edu.ksu.cis.indus.interfaces.AbstractStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This is an abstract implementation of ITool which the concrete implementations are encouraged to extend.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractTool extends AbstractStatus
  implements ITool {
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
	 * This variable is used by the child thread to communicate exception state to the parent thread.
	 */
	Exception childException;

	/**
	 * This indicates if the tool should pause execution.
	 */
	boolean pause;

	/**
	 * The thread in which the tools is running or ran previously.
	 */
	Thread thread;

	/**
	 * Retrieves an object that represents the active configuration of the tool.
	 *
	 * @return the active configuration of the tool.
	 *
	 * @post result != null
	 */
	public final IToolConfiguration getActiveConfiguration() {
		IToolConfiguration _result;

		if (configurationInfo instanceof CompositeToolConfiguration) {
			_result = ((CompositeToolConfiguration) configurationInfo).getActiveToolConfiguration();
		} else {
			_result = configurationInfo;
		}
		return _result;
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
	 * Executes the tool. The tool is multithreaded.  However, the user can run it in asynchronous mode.  In asynchronous
	 * mode, if tool fails, any subsequent calls to <code>isStable()</code> until a following call to <code>run()</code>
	 * will return  <code>false</code>.
	 *
	 * @param phase is the suggestive phase to start execution in.
	 * @param synchronous <code>true</code> indicates that this method should behave synchronously and return only after the
	 * 		  tool's run has completed; <code>false</code> indicates that this method can return once the tool has started
	 * 		  it's run.
	 *
	 * @throws RuntimeException when the tool fails.
	 * @throws IllegalStateException when this method is called on a paused tool or the tool cannot be configuraed according
	 * 		   to the configuration.
	 */
	public final synchronized void run(final Object phase, final boolean synchronous) {
		if (!pause || isNotAlive()) {
			checkConfiguration();
			childException = null;
			unstable();
			thread =
				new Thread() {
						public final void run() {
							Exception _temp = null;

							try {
								execute(phase);
							} catch (InterruptedException _e) {
								LOGGER.fatal("Interrupted while executing the tool.", _e);
								_temp = _e;
							} catch (Exception _e) {
								LOGGER.fatal("Tool failed.", _e);
								_temp = _e;
							} finally {
								if (_temp != null) {
									childException = _temp;
								}
								pause = false;
							}
						}
					};
			thread.start();

			if (synchronous) {
				try {
					thread.join();

					if (childException != null) {
						throw new RuntimeException(childException);
					}
				} catch (final InterruptedException _e) {
					LOGGER.error("Interrupted while waiting on the run to complete.", _e);
					throw new RuntimeException(_e);
				}
			} else {
			    final Thread _temp = new Thread() {
			        public void run() {
			            try {
                            thread.join();
    			            stable();
                        } catch (final InterruptedException _e) {
        					LOGGER.error("Interrupted while waiting on the helper thread.", _e);
                        }
			        }
			    };
			    _temp.start();
			}
		} else {
			throw new IllegalStateException("run() should be called when the tool is paused or running.");
		}
	}

	/**
	 * Checks if the tool can be configured as per the given configuration.  Subclasses must override this method and throw
	 * an <code>IllegalStateException</code> if the tool cannot be configured.
	 */
	protected void checkConfiguration() {
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

	/**
	 * Checks if the tool's thread is not alive. {@inheritDoc}
	 *
	 * @return <code>true</code> if the tool is not alive; <code>false</code>, otherwise.
	 */
	private boolean isNotAlive() {
		return thread == null || !thread.isAlive();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.23  2004/05/11 19:16:47  venku
   The logic to spawn a thread and wait on it to finish was incorrect.  It led
   to race condition.  FIXED.

   Revision 1.22  2004/02/27 09:40:45  venku
   - documentation.

   Revision 1.21  2004/02/23 03:04:53  venku
   - synchronization issues in the tool.

   Revision 1.20  2004/02/17 05:43:57  venku
   - we do not want to catch errors but only exceptions. FIXED.
   Revision 1.19  2004/01/27 15:19:21  venku
   - coding convention.
   Revision 1.18  2004/01/25 09:07:18  venku
   - coding convention.
   Revision 1.17  2004/01/16 22:11:47  venku
   - join does not relinquish the lock.  Hence, a new solution
     to communicate the death of the child thread has been
     implemented.
   Revision 1.16  2004/01/13 10:01:35  venku
   - added a provision for the tool to check if it can be configured
     according to the given configuration.
   Revision 1.15  2004/01/08 23:55:34  venku
   - documentation.
   Revision 1.14  2004/01/08 23:51:34  venku
   - exceptions in child thread were not being communicated to
     the parent thread.  Now, the parent thread will know about
     such exceptions while doing synchronous runs.  However,
     on asynchronous runs, if the child thread fails, subsequent
     calls to isStable() until a following call to run() will return false.
   Revision 1.13  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.12  2003/12/09 12:18:18  venku
   - added support to control synchronicity of method runs.
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
