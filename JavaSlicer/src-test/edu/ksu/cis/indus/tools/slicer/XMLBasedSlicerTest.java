
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

package edu.ksu.cis.indus.tools.slicer;

import edu.ksu.cis.indus.AbstractXMLBasedTest;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;


/**
 * This provides junit-style regression test support to test dependency.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class XMLBasedSlicerTest
  extends AbstractXMLBasedTest {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private SlicerTestSetup setup;

	/**
	 * DOCUMENT ME!
	 *
	 * @param testSetup DOCUMENT ME!
	 */
	public XMLBasedSlicerTest(final SlicerTestSetup testSetup) {
		setup = testSetup;
		setName("testXMLSimilarity");
	}

	/**
	 * @see edu.ksu.cis.indus.AbstractXMLBasedTest#getXMLizer()
	 */
	protected AbstractXMLizer getXMLizer() {
		return setup.driver.getXMLizer();
	}

	/**
	 * @see edu.ksu.cis.indus.AbstractXMLBasedTest#localSetup()
	 */
	protected void localSetup()
	  throws Exception {
		super.localSetup();
		info.put(IEnvironment.ID, setup.driver.slicer.getEnvironment());
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.8  2004/03/21 20:13:17  venku
   - many file handles are left open. FIXED.
   Revision 1.7  2004/03/03 08:06:18  venku
   - renamed SliceXMLizer to SliceXMLizerCLI.
   Revision 1.6  2004/02/09 06:49:27  venku
 *** empty log message ***
             Revision 1.5  2004/01/09 07:02:12  venku
             - Made -o mandatory in SliceDriver.
             - all information is dumped into directory specified via -o.
             - Renamed SliceDriver to SliceXMLizerCLI.
             Revision 1.4  2003/12/13 02:29:16  venku
             - Refactoring, documentation, coding convention, and
               formatting.
             Revision 1.3  2003/12/02 09:42:18  venku
             - well well well. coding convention and formatting changed
               as a result of embracing checkstyle 3.2
             Revision 1.2  2003/11/24 10:14:39  venku
             - there are no residualizers now.  There is a very precise
               slice collector which will collect the slice via tags.
             - architectural change. The slicer is hard-wired wrt to
               slice collection.  Residualization is outside the slicer.
             Revision 1.1  2003/11/17 03:22:55  venku
             - added junit test support for Slicing.
             - refactored code in test for dependency to make it more
               simple.
             Revision 1.6  2003/11/16 19:01:33  venku
             - documentation.
             Revision 1.5  2003/11/16 18:41:18  venku
             - renamed UniqueIDGenerator to UniqueJimpleIDGenerator.
             Revision 1.4  2003/11/12 10:45:36  venku
             - soot class path can be set in SootBasedDriver.
             - dependency tests are xmlunit based.
             Revision 1.3  2003/11/12 05:18:54  venku
             - moved xmlizing classes to a different class.
             Revision 1.2  2003/11/12 05:05:45  venku
             - Renamed SootDependentTest to SootBasedDriver.
             - Switched the contents of DependencyXMLizerDriver and XMLBasedSlicerTest.
             - Corrected errors which emitting xml tags.
             - added a scrapbook.
             Revision 1.1  2003/11/11 10:11:27  venku
             - in the process of making XMLization a user
               application and at the same time a tester application.
 */
