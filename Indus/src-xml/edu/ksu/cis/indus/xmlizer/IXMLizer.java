
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

package edu.ksu.cis.indus.xmlizer;

import java.util.Map;


/**
 * This is the interface for xmlizing jimple-based systems.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IXMLizer {
	/**
	 * Retrieves the a name based on the given name for the file into which the xml data will be written into.
	 *
	 * @param basename to be considered when coming up with the file name.
	 *
	 * @return the name of the file.
	 *
	 * @pre basename != null
	 * @post result != null
	 */
	String getFileName(final String basename);

	/**
	 * Set the xml id generator to be used in xml data generation.
	 *
	 * @param generator generates the id used in xml data.
	 *
	 * @pre generator != null
	 */
	void setGenerator(final IJimpleIDGenerator generator);

	/**
	 * Retrieves the xml id generator used by this object.
	 *
	 * @return the xml id generator.
	 */
	IJimpleIDGenerator getIdGenerator();

	/**
	 * Set the directory into which xml data should be dumped.
	 *
	 * @param xmlOutputDir is the directory into which xml data should be dumped.
	 *
	 * @pre xmlOutputDir != null
	 */
	void setXmlOutputDir(final String xmlOutputDir);

	/**
	 * Retrieves the directory into which xml data will be dumped into.
	 *
	 * @return the directory into which xml data will be dumped.
	 */
	String getXmlOutputDir();

	/**
	 * Writes information in XML form.
	 *
	 * @param info is a map in which information required for xmlization will be provided.
	 *
	 * @pre info != null
	 */
	void writeXML(final Map info);
}

// End of File
