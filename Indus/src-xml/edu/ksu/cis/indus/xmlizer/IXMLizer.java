
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
