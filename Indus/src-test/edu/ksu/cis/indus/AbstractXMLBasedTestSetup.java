
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

import java.util.Collection;
import java.util.Iterator;

import junit.extensions.TestSetup;

import junit.framework.TestSuite;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class AbstractXMLBasedTestSetup
  extends TestSetup
  implements IXMLBasedTest {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private String xmlInputDir;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	private String xmlOutputDir;

	/**
	 * @see TestSetup#TestSetup(TestSuite)
	 */
	public AbstractXMLBasedTestSetup(TestSuite test) {
		super(test);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param xmlInDir
	 */
	public void setXmlInputDir(String xmlInDir) {
		xmlInputDir = xmlInDir;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public String getXmlInputDir() {
		return xmlInputDir;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param xmlOutDir
	 */
	public void setXmlOutputDir(String xmlOutDir) {
		xmlOutputDir = xmlOutDir;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public String getXmlOutputDir() {
		return xmlOutputDir;
	}

	/**
	 * @see junit.extensions.TestSetup#setUp()
	 */
	protected void setUp()
	  throws Exception {
		final Collection _temp = TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), IXMLBasedTest.class);

		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			final IXMLBasedTest _tester = (IXMLBasedTest) _i.next();
			_tester.setXmlOutputDir(xmlOutputDir);
			_tester.setXmlInputDir(xmlInputDir);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/02/09 01:20:06  venku
   - coding convention.
   - added a new abstract class contain the logic required for xml-based
     testing.  (AbstractXMLBasedTest)
   - added a new xml-based call graph testing class.
   Revision 1.1  2004/02/08 04:53:14  venku
   - refactoring!!!
   - All regression tests implement IXMLBasedTest.
   - All test setups extends AbstractXMLBasedTestSetup.
   - coding convention.
   - all tests occur at the same package as the classes
     being tested.
 */
