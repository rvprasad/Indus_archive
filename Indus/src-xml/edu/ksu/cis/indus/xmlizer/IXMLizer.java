
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

package edu.ksu.cis.indus.xmlizer;

/**
 * DOCUMENT ME!
 * <p></p>
 * 
 * @version $Revision$ 
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 */
public interface IXMLizer {
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
	 * @throws IllegalArgumentException when the given directory does not exist or cannot be written into.
	 *
	 * @pre xmlOutputDir != null
	 */
	void setXMLOutputDir(final String xmlOutputDir);

	/**
	 * Retrieves the directory into which xml data will be dumped into.
	 *
	 * @return the directory into which xml data will be dumped.
	 */
	String getXmlOutDir();
}

/*
   ChangeLog:
   $Log$
 */
