
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

import org.custommonkey.xmlunit.XMLTestCase;

import junit.framework.TestCase;


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
	 * The directory in which xml-based testing input is read from.
	 */
	private String xmlInputDir;

	/**
	 * The directory in which xml-based testing output is dumped.
	 */
	private String xmlOutputDir;

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
	 * @param xmlOutDir ME!
	 */
	public void setXmlOutputDir(final String xmlOutDir) {
		xmlOutputDir = xmlOutDir;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public String getXmlOutputDir() {
		return this.xmlOutputDir;
	}
}

/*
   ChangeLog:
   $Log$
 */
