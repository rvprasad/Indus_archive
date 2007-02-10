/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.tools;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.interfaces.AbstractStatus;
import edu.ksu.cis.indus.interfaces.IActivePart;
import edu.ksu.cis.indus.tools.IToolProgressListener.ToolProgressEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an abstract implementation of ITool which the concrete implementations are encouraged to extend.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath </a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractTool
		extends AbstractStatus
		implements ITool {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Logger LOGGER = LoggerFactory.getLogger(AbstractTool.class);

	/**
	 * This is the configuration information associated with this tool instance. Subclasses should provide a valid reference.
	 * 
	 * @invariant configurationInfo != null
	 */
	protected IToolConfiguration configurationInfo;

	/**
	 * This is the configurator associated with this tool instance. Subclasses should provide a valid reference.
	 * 
	 * @invariant configurator != null
	 */
	protected IToolConfigurator configurator;

	/**
	 * This an object used to control the execution of the tool.
	 */
	protected final Object control = new Object();

	/**
	 * This variable is used by the child thread to communicate exception state to the parent thread.
	 */
	Throwable childException;

	/**
	 * A collection of listeners of tools progress.
	 */
	final Collection<IToolProgressListener> listeners;

	/**
	 * This is the number of messages that have been accepted for delivery.
	 */
	int messageId;

	/**
	 * This indicates if the tool should pause execution.
	 */
	boolean pause;

	/**
	 * The thread in which the tools is running or ran previously.
	 */
	Thread thread;

	/**
	 * This is the number of the message to be delivered next.
	 */
	int token;

	/**
	 * The object used to realize the "active" part of this object.
	 */
	private final IActivePart.ActivePart activePart = new IActivePart.ActivePart();

	/**
	 * This is the collection of active parts.
	 */
	private Collection<IActivePart> activeParts = new HashSet<IActivePart>();

	/**
	 * The current configuration. This is the configuration that is currently being used by the tool.
	 */
	private IToolConfiguration currentConfiguration;

	/**
	 * This executor executes actions corresponding to delivering tool progress events.
	 */
	ExecutorService executor = Executors.newSingleThreadExecutor();

	/**
	 * Creates a new AbstractTool object.
	 */
	public AbstractTool() {
		activeParts.add(activePart);
		listeners = Collections.synchronizedCollection(new HashSet<IToolProgressListener>());
	}

	/**
	 * Aborts the execution of the tool.
	 */
	public final void abort() {
		final Iterator<IActivePart> _i = activeParts.iterator();
		final int _iEnd = activeParts.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IActivePart _executor = _i.next();

			if (_executor != null) {
				fireToolProgressEvent("Aborting " + _executor, null);
				_executor.deactivate();
			}
		}
		resume();
	}

	/**
	 * @see edu.ksu.cis.indus.tools.ITool#addToolProgressListener(edu.ksu.cis.indus.tools.IToolProgressListener)
	 */
	public void addToolProgressListener(final IToolProgressListener listener) {
		listeners.add(listener);
	}

	/**
	 * Retrieves an object that represents the active configuration of the tool. This need not be the configuration currently
	 * being used by this tool. For that please use <code>getCurrentConfiguration()</code>.
	 * 
	 * @return the active configuration of the tool.
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
	 * This implementation will breakdown the topmost composite configuration but any embedded configurations are returned as
	 * is. {@inheritDoc}
	 * 
	 * @see ITool#getConfigurations()
	 */
	public Collection<IToolConfiguration> getConfigurations() {
		final Collection<IToolConfiguration> _result = new HashSet<IToolConfiguration>();

		if (configurationInfo instanceof CompositeToolConfiguration) {
			_result.addAll(((CompositeToolConfiguration) configurationInfo).configurations);
		} else {
			_result.add(configurationInfo);
		}
		return _result;
	}

	/**
	 * Retrieves an editor which enables the user to edit the configuration of the tool. This can return <code>null</code>,
	 * if the tool does not have a configurationCollection to edit which is seldom the case.
	 * 
	 * @return a configurationCollection editor.
	 */
	public final IToolConfigurator getConfigurator() {
		return configurator;
	}

	/**
	 * Retrieves the configuration being used by this tool. This may be the active configuration or another configuration set
	 * the by tool.
	 * 
	 * @return the current configuration.
	 * @post result != null
	 */
	public final IToolConfiguration getCurrentConfiguration() {
		return currentConfiguration != null ? currentConfiguration : getActiveConfiguration();
	}

	/**
	 * Pauses the execution of the tool.
	 */
	public final void pause() {
		synchronized (control) {
			pause = true;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see edu.ksu.cis.indus.tools.ITool#removeToolProgressListener(edu.ksu.cis.indus.tools.IToolProgressListener)
	 */
	public void removeToolProgressListener(final IToolProgressListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Resumes the execution of the tool.
	 */
	public final void resume() {
		synchronized (control) {
			pause = false;
			control.notify();
		}
	}

	/**
	 * Executes the tool. The tool is multithreaded. However, the user can run it in synchronous mode and if tool fails in
	 * this mode, any subsequent calls to <code>isStable()</code> until a following call to <code>run()</code> will return
	 * <code>false</code>.
	 * 
	 * @param phase is the suggestive phase to start execution in.
	 * @param lastPhase is the phase that should be executed last before exiting.
	 * @param synchronous <code>true</code> indicates that this method should behave synchronously and return only after the
	 *            tool's run has completed; <code>false</code> indicates that this method can return once the tool has
	 *            started it's run.
	 * @throws RuntimeException when the tool fails.
	 * @throws IllegalStateException when this method is called on a paused tool.
	 */
	public final synchronized void run(final Phase phase, final Phase lastPhase, final boolean synchronous) {
		if (!pause || isNotAlive()) {
			checkConfiguration();
			childException = null;
			unstable();
			activateActiveParts();
			thread = new Thread() {

				@Override public final void run() {
					Throwable _temp = null;
					try {
						// we do this to respect any pre-run pause calls.
						movingToNextPhase();

						execute(phase, lastPhase);
					} catch (final InterruptedException _e) {
						LOGGER.error("Interrupted while executing the tool.", _e);
						_temp = _e;
					} catch (final Throwable _e) {
						LOGGER.error("Tool failed.", _e);
						_temp = _e;
					} finally {
						if (_temp != null) {
							childException = _temp;
						}
						pause = false;
						executor.shutdown();
						executor = Executors.newSingleThreadExecutor();
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
					stable();
				} catch (final InterruptedException _e) {
					LOGGER.error("Interrupted while waiting on the run to complete.", _e);
					throw new RuntimeException(_e);
				}
			} else {
				final Thread _temp = new Thread() {

					@Override public void run() {
						try {
							thread.join();

							if (childException != null) {
								throw new RuntimeException(childException);
							}
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
	 * Adds the given object to the collection of active part.
	 * 
	 * @param part of in interest
	 */
	protected final void addActivePart(final IActivePart part) {
		activeParts.add(part);
	}

	/**
	 * Checks if the tool can be configured as per the given configuration. Subclasses must override this method and throw an
	 * <code>IllegalStateException</code> if the tool cannot be configured.
	 * 
	 * @throws ToolConfigurationException when the tool cannot be configured according to the configuration.
	 */
	@Empty protected void checkConfiguration() throws ToolConfigurationException {
		// does nothing
	}

	/**
	 * This is the template method in which the actual processing of the tool happens.
	 * 
	 * @param phase is the suggestive phase to start execution in.
	 * @param lastPhase is the phase that should be executed last before exiting.
	 * @throws InterruptedException when the execution of the tool is interrupted.
	 */
	protected abstract void execute(final Phase phase, final Phase lastPhase) throws InterruptedException;

	/**
	 * Reports the given tool progress information to any registered listeners. The listeners will receive the events in the
	 * order they were fired.
	 * 
	 * @param message about the progress of the tool.
	 * @param info anything the tool may want to convey to the listener.
	 * @throws RuntimeException is thrown by the message delivery thread when it is interrupted.
	 * @pre message != null and info != null
	 */
	protected void fireToolProgressEvent(final String message, final Object info) {
		executor.execute(new Runnable() {

			public void run() {
				synchronized (listeners) {
					final ToolProgressEvent _evt = new ToolProgressEvent(this, message, info);

					for (final Iterator<IToolProgressListener> _i = listeners.iterator(); _i.hasNext();) {
						final IToolProgressListener _listener = _i.next();
						_listener.toolProgess(_evt);
					}
				}
			}
		});
	}

	/**
	 * Used to suspend the tool execution. This indicates that the tool implementation is moving onto a new phase, hence, it
	 * is at a point where it is safe to pause/suspend execution. If the application had requested the tool to pause via
	 * <code>pause()</code>, this method will suspend the execution of the tool.
	 * 
	 * @throws InterruptedException when the thread in which the tool has paused is interrupted.
	 */
	protected final void movingToNextPhase() throws InterruptedException {
		synchronized (control) {
			if (pause) {
				control.wait();
			} else if (!activePart.canProceed()) {
				final String _string = "Tool was interrupted.";
				fireToolProgressEvent(_string, null);
			}
		}
	}

	/**
	 * Removes the given object from the collection of active part.
	 * 
	 * @param part of interest.
	 * @return <code>true</code> if <code>part</code> was removed; <code>false</code>, otherwise.
	 */
	protected final boolean removeActivePart(final IActivePart part) {
		return activeParts.remove(part);
	}

	/**
	 * Sets the current configuration. This method should be called by the sub-classes each time after they have selected a
	 * configuration to use.
	 * 
	 * @param config to be considered as the current configuration. This has to be one of the configuration associated with
	 *            this tool.
	 * @pre config != null
	 */
	protected void setCurrentConfiguration(final IToolConfiguration config) {
		currentConfiguration = config;
	}

	/**
	 * Aborts the execution of the tool.
	 */
	private void activateActiveParts() {
		final Iterator<IActivePart> _i = activeParts.iterator();
		final int _iEnd = activeParts.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final IActivePart _executor = _i.next();

			if (_executor != null) {
				fireToolProgressEvent("Activating " + _executor, null);
				_executor.activate();
			}
		}
	}

	/**
	 * Checks if the tool's thread is not alive.
	 * 
	 * @return <code>true</code> if the tool is not alive; <code>false</code>, otherwise.
	 */
	private boolean isNotAlive() {
		return thread == null || !thread.isAlive();
	}
}

// End of File
