
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

package edu.ksu.cis.indus;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;

import org.custommonkey.xmlunit.XMLTestCase;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class AbstractXMLBasedTest
  extends XMLTestCase
  implements IXMLBasedTest {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private AbstractXMLizer xmlizer;

	/**
	 * The directory in which xml-based testing input is read from.
	 */
	private String xmlInputDir;

	/**
	 * DOCUMENT ME!
	 *
	 * @param xmlInDir ME!
	 */
	public void setXmlInputDir(final String xmlInDir) {
		xmlInputDir = xmlInDir;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public String getXmlInputDir() {
		return this.xmlInputDir;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param xmlOutputDir
	 */
	public void setXmlOutputDir(String xmlOutputDir) {
		xmlizer.setXmlOutputDir(xmlOutputDir);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public String getXmlOutputDir() {
		return xmlizer.getXmlOutputDir();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/02/09 01:20:06  venku
   - coding convention.
   - added a new abstract class contain the logic required for xml-based
     testing.  (AbstractXMLBasedTest)
   - added a new xml-based call graph testing class.
 */
