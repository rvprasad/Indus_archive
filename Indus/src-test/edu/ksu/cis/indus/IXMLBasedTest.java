
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
	 * Sets the directory from which to read the first xml test input.
	 *
	 * @param xmlInDir is the directory to read the xml test input from.
	 *
	 * @pre xmlInDir != null
	 */
	void setFirstXmlInputDir(String xmlInputDir);

	/**
	 * Sets the directory from which to read the second xml test input.
	 *
	 * @param xmlInDir is the directory to read the xml test input from.
	 *
	 * @pre xmlInDir != null
	 */
	void setSecondXmlInputDir(String xmlInputDir);
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/02/09 01:20:06  venku
   - coding convention.
   - added a new abstract class contain the logic required for xml-based
     testing.  (AbstractXMLBasedTest)
   - added a new xml-based call graph testing class.
   Revision 1.2  2004/02/08 19:08:03  venku
   - documentation
   Revision 1.1  2004/02/08 04:53:14  venku
   - refactoring!!!
   - All regression tests implement IXMLBasedTest.
   - All test setups extends AbstractXMLBasedTestSetup.
   - coding convention.
   - all tests occur at the same package as the classes
     being tested.
 */
