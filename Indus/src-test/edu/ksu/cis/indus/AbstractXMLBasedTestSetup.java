
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
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final AbstractXMLBasedTest xmlBasedTest;

	/**
	 * @see TestSetup#TestSetup(TestSuite)
	 */
	public AbstractXMLBasedTestSetup(TestSuite test) {
		super(test);
		xmlBasedTest = new AbstractXMLBasedTest() {
					;
				};
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param xmlInDir
	 */
	public void setXmlInputDir(String xmlInDir) {
		xmlBasedTest.setXmlInputDir(xmlInDir);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public String getXmlInputDir() {
		return xmlBasedTest.getXmlInputDir();
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param xmlOutDir
	 */
	public void setXmlOutputDir(String xmlOutDir) {
		xmlBasedTest.setXmlOutputDir(xmlOutDir);
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return
	 */
	public String getXmlOutputDir() {
		return xmlBasedTest.getXmlOutputDir();
	}

	/**
	 * @see junit.extensions.TestSetup#setUp()
	 */
	protected void setUp()
	  throws Exception {
		final Collection _temp = TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), IXMLBasedTest.class);

		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			final IXMLBasedTest _tester = (IXMLBasedTest) _i.next();
			_tester.setXmlOutputDir(xmlBasedTest.getXmlOutputDir());
			_tester.setXmlInputDir(xmlBasedTest.getXmlInputDir());
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/02/08 04:53:14  venku
   - refactoring!!!
   - All regression tests implement IXMLBasedTest.
   - All test setups extends AbstractXMLBasedTestSetup.
   - coding convention.
   - all tests occur at the same package as the classes
     being tested.
 */
