
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
 * This is the facade interface exposed by a tool in Indus.  The tool will expose the configuration via
 * <code>ToolConfiguration</code>, hence, this api forces the tool implementation to handle the interaction with the
 * environment for issues such as persistence of the configuration.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface Tool {
	/**
	 * Retrieves an object that represents the configuration of the tool.
	 *
	 * @return the configuration of the tool.
	 *
	 * @post result != null
	 */
	public ToolConfiguration getConfiguration();

	/**
	 * Retrieves an editor which enables the user to edit the configuration of the tool.  This can return <code>null</code>,
	 * if the tool does not have a configuration to edit which is seldom the case.
	 *
	 * @return a configuration editor.
	 */
	public ToolConfigurationEditor getConfigurationEditor();
}

/*
   ChangeLog:
   $Log$
 */
