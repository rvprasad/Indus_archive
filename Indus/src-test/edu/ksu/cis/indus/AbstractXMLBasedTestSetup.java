
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
 * This class is a test setup class that is used in conjunction with xml data-based testing.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class AbstractXMLBasedTestSetup
  extends TestSetup
  implements IXMLBasedTest {
	/**
	 * The directory in which one of the xml-based testing input is read from.
	 */
	private String xmlFirstInputDir;

	/**
	 * The directory in which the other xml-based testing input is read from.
	 */
	private String xmlSecondInputDir;

	/**
	 * @see TestSetup#TestSetup(TestSuite)
	 */
	public AbstractXMLBasedTestSetup(final TestSuite test) {
		super(test);
	}

	/**
	 * @see IXMLBasedTest#setFirstXmlInputDir(String)
	 */
	public void setFirstXmlInputDir(final String xmlInDir) {
		xmlFirstInputDir = xmlInDir;
	}

	/**
	 * Retrieves the directory from which one of the xml-based testing input is read from.
	 *
	 * @return directory in which one of the xml-based testing input is read from.
	 */
	public String getFirstXmlInputDir() {
		return xmlFirstInputDir;
	}

	/**
	 * @see IXMLBasedTest#setSecondXmlInputDir(String)
	 */
	public void setSecondXmlInputDir(final String xmlInDir) {
		xmlSecondInputDir = xmlInDir;
	}

	/**
	 * Retrieves the directory from which the other xml-based testing input is read from.
	 *
	 * @return directory in which the other xml-based testing input is read from.
	 */
	public String getSecondXmlInputDir() {
		return xmlSecondInputDir;
	}

	/**
	 * @see junit.extensions.TestSetup#setUp()
	 */
	protected void setUp()
	  throws Exception {
		final Collection _temp = TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), IXMLBasedTest.class);

		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			final IXMLBasedTest _tester = (IXMLBasedTest) _i.next();
			_tester.setSecondXmlInputDir(xmlSecondInputDir);
			_tester.setFirstXmlInputDir(xmlFirstInputDir);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2004/02/14 23:16:49  venku
   - coding convention.
   Revision 1.3  2004/02/09 04:39:40  venku
   - refactoring test classes still..
   - need to make xmlizer classes independent of their purpose.
     Hence, they need to be highly configurable.
   - For each concept, test setup should be in TestSetup
     rather than in the XMLizer.
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
