
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus;

import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

import edu.ksu.cis.indus.xmlizer.IJimpleIDGenerator;

import junit.framework.Test;


/**
 * This is a common interface implemented by tests that are based on xml data.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IXMLBasedTest
  extends Test {
	/** 
	 * The property suffix that can be used in property file to specify the control input directory.
	 */
	String XML_CONTROL_DIR_PROP_SUFFIX = ".xmlControlDir";

	/** 
	 * The property suffix that can be used in property file to specify the test input directory.
	 */
	String XML_TEST_DIR_PROP_SUFFIX = ".xmlTestDir";

	/**
	 * Set the id generator to be used to xmlize the data.  In general, you want to xmlize the data and use it in subsequent
	 * testing.
	 *
	 * @param generator to be used for xmlizing data.
	 */
	void setIdGenerator(IJimpleIDGenerator generator);

	/**
	 * Sets the factory to be used to obtain statement graphs (CFGs) during testing.
	 *
	 * @param cfgFactory is the factory to be used.
	 */
	void setStmtGraphFactory(IStmtGraphFactory cfgFactory);

	/**
	 * Sets the directory from which to read the test control input.
	 *
	 * @param xmlControlDir is the directory to read the test control input.
	 *
	 * @pre xmlControlDir != null
	 */
	void setXMLControlDir(String xmlControlDir);

	/**
	 * Sets the directory from which to read the test input.
	 *
	 * @param xmlTestDir is the directory to read the test input from.
	 *
	 * @pre xmlTestDir != null
	 */
	void setXMLTestDir(String xmlTestDir);
}

// End of File
