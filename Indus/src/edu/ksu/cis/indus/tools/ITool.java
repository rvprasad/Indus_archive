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

import java.util.Collection;

/**
 * This is the facade interface exposed by a tool in Indus. The tool will expose the configurationCollection via
 * <code>IToolConfiguration</code>, hence, this api forces the tool implementation to handle the interaction with the
 * environment for issues such as persistence of the configuration.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ITool {

	/**
	 * Aborts the execution of the tool.
	 */
	void abort();

	/**
	 * Adds the given listener from the listener list.
	 * 
	 * @param listener to be added.
	 */
	void addToolProgressListener(IToolProgressListener listener);

	/**
	 * Populate this object with the information in given in string form.
	 * 
	 * @param stringizedForm contains the information to be loaded into this object.
	 * @return <code>true</code> if the configuration could be constructed from the given stringized form.
	 *         <code>false</code> if the configuration could not be constructed from the given stringized form. In the
	 *         latter case, the implementation can load a default configuration.
	 * @pre stringizedForm != null
	 */
	boolean destringizeConfiguration(final String stringizedForm);

	/**
	 * Retrieves an object that represents the active configuration of the tool.
	 * 
	 * @return the active configuration of the tool.
	 * @post result != null
	 */
	IToolConfiguration getActiveConfiguration();

	/**
	 * Retrieves the configurations available in the tool. The implementations decide if composite configurations are included
	 * in the returned collection or if the non-composite configurations that make up the composite configuration will be
	 * included in the returned collection instead of composite configuration.
	 * 
	 * @return the configurations in the tool.
	 * @post result != null
	 */
	Collection<IToolConfiguration> getConfigurations();

	/**
	 * Retrieves an editor which enables the user to edit the configuration of the tool. This can return <code>null</code>,
	 * if the tool does not have a configurationCollection to edit which is seldom the case.
	 * 
	 * @return a configurationCollection editor.
	 */
	IToolConfigurator getConfigurator();

	/**
	 * Returns the current phase in which the tool was executing.
	 * 
	 * @return the current phase.
	 */
	Object getPhase();

	/**
	 * Initialize the tool. This is called once on the tool.
	 */
	void initialize();

	/**
	 * Pauses the execution of the tool.
	 */
	void pause();

	/**
	 * Removes the given listener from the listener list.
	 * 
	 * @param listener to be removed.
	 */
	void removeToolProgressListener(IToolProgressListener listener);

	/**
	 * Reset the tool. All state related information should be erased.
	 */
	void reset();

	/**
	 * Resumes the execution of the tool.
	 */
	void resume();

	/**
	 * Executes the tool.
	 * 
	 * @param phase is the suggestive phase to start execution in.
	 * @param lastPhase is the phase that should be executed last before exiting.
	 * @param synchronous <code>true</code> indicates that this method should behave synchronously and return only after the
	 *            tool's run has completed; <code>false</code> indicates that this method can return once the tool has
	 *            started it's run.
	 */
	void run(Phase phase, Phase lastPhase, boolean synchronous);

	/**
	 * Returns a stringized from of the information in the object suitable for serialization.
	 * 
	 * @return a stringized representation of the infornmation in the collection.
	 * @post result != null
	 */
	String stringizeConfiguration();
}

// End of File
