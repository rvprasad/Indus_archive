
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
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IXMLBasedTest
  extends Test {
	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param xmlInputDir DOCUMENT ME!
	 */
	void setXmlInputDir(String xmlInputDir);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param xmlOutputDir DOCUMENT ME!
	 */
	void setXmlOutputDir(String xmlOutputDir);
}

/*
   ChangeLog:
   $Log$
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
