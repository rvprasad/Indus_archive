
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

/**
 * This is the facade interface exposed by a tool in Indus.  The tool will expose the configurationCollection via
 * <code>IToolConfiguration</code>, hence, this api forces the tool implementation to handle the interaction with the
 * environment for issues such as persistence of the configuration.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ITool {
	/**
	 * Retrieves an object that represents the active configuration of the tool.
	 *
	 * @return the active configuration of the tool.
	 *
	 * @post result != null
	 */
	IToolConfiguration getActiveConfiguration();

	/**
	 * Retrieves an editor which enables the user to edit the configuration of the tool.  This can return <code>null</code>,
	 * if the tool does not have a configurationCollection to edit which is seldom the case.
	 *
	 * @return a configurationCollection editor.
	 */
	IToolConfigurator getConfigurator();

	/**
	 * Retursn the current phase in which the tool was executing.
	 *
	 * @return the current phase.
	 */
	Object getPhase();

	/**
	 * Aborts the execution of the tool.
	 */
	void abort();

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
	boolean destringizeConfiguration(final String stringizedForm);

	/**
	 * Initialize the tool.  This is called once on the tool.
	 */
	void initialize();

	/**
	 * Pauses the execution of the tool.
	 */
	void pause();

	/**
	 * Reset the tool.  All state related information should be erased.
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
	 * @param synchronous <code>true</code> indicates that this method should behave synchronously and return only after the
	 * 		  tool's run has completed; <code>false</code> indicates that this method can return once the tool has started
	 * 		  it's run.
	 */
	void run(final Object phase, final boolean synchronous);

	/**
	 * Returns a stringized from of the information in the object suitable for serialization.
	 *
	 * @return a stringized representation of the infornmation in the collection.
	 *
	 * @post result != null
	 */
	String stringizeConfiguration();
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/12/09 12:23:52  venku
   - added support to control synchronicity of method runs.
   - ripple effect.
   Revision 1.1  2003/12/02 11:47:19  venku
   - raised the tool to an interface ITool.
 */
