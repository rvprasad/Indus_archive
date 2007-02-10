
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
