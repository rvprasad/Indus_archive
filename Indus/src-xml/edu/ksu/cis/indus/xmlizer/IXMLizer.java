
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
	 * Retrieves the name of the file that contains the test data.  The default implementation constructs a name from the
	 * xmlizer.
	 *
	 * @param basename is the name upon which the file nam will be based upon.
	 *
	 * @return the name of the file.
	 *
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
	 * @throws IllegalArgumentException when the given directory does not exist or cannot be written into.
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

/*
   ChangeLog:
   $Log$
   Revision 1.5  2004/04/22 23:02:49  venku
   - moved writeXML into IXMLizer.
   Revision 1.4  2004/02/09 02:00:11  venku
   - changed AbstractXMLizer.
   - ripple effect.
   Revision 1.3  2004/02/08 19:08:03  venku
   - documentation
   Revision 1.2  2003/12/16 00:29:12  venku
   - documentation.
   Revision 1.1  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
 */
