
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.AbstractXMLBasedTest;
import edu.ksu.cis.indus.IXMLBasedTest;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.xmlizer.IXMLizer;

import java.util.Collections;


/**
 * This is a XML based test for dependency analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class XMLBasedDependencyAnalysisTest
  extends AbstractXMLBasedTest
  implements IDependencyAnalysisTest, IXMLBasedTest {
	/**
	 * The instance of the analysis being tested.
	 */
	private AbstractDependencyAnalysis da;

	/**
	 * The instance of the xmlizer used to generate the test data.
	 */
	private DependencyXMLizer xmlizer;

	/**
	 * The call graph of the test input system.
	 */
	private ICallGraphInfo cgi;

	/**
	 * The environment which the analysis analyzed.
	 */
	private IEnvironment env;

	/**
	 * Creates an instance of this class.
	 *
	 * @param theDA is the instance to be tested.
	 * @param depXMLizer is the xmlizer used to generate the test data.
	 *
	 * @pre depXMLizer != null and theDA != null
	 */
	public XMLBasedDependencyAnalysisTest(final AbstractDependencyAnalysis theDA, final DependencyXMLizer depXMLizer) {
		da = theDA;
		xmlizer = depXMLizer;
		setName("testXMLSimilarity");
		setTestName(getClass().getName() + ":" + xmlizer.getDAPartOfFileName(theDA));
	}

	/**
	 * Sets the call graph to be used.
	 *
	 * @param callGraph to be used.
	 */
	public void setCallGraph(final ICallGraphInfo callGraph) {
		cgi = callGraph;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysisTest#getDA()
	 */
	public AbstractDependencyAnalysis getDA() {
		return da;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysisTest#setEnvironment(edu.ksu.cis.indus.interfaces.IEnvironment)
	 */
	public void setEnvironment(IEnvironment environment) {
		env = environment;
	}

	/**
	 * @see edu.ksu.cis.indus.AbstractXMLBasedTest#getXMLizer()
	 */
	protected IXMLizer getXMLizer() {
		return xmlizer;
	}

	/**
	 * @see edu.ksu.cis.indus.AbstractXMLBasedTest#localSetup()
	 */
	protected void localSetup()
	  throws Exception {
		super.localSetup();
		info.put(da.getId(), Collections.singleton(da));
		info.put(IEnvironment.ID, env);
		info.put(ICallGraphInfo.ID, cgi);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		cgi = null;
		da = null;
		env = null;
		xmlizer = null;
		super.tearDown();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2004/05/13 03:30:03  venku
   - coding convention.
   - documentation.
   - refactoring: added a new method getFileName() to IXMLizer instead of AbstractXMLizer.

   Revision 1.4  2004/04/19 05:10:26  venku
   - NPE's in test setup caused by unchecked reseting.
   Revision 1.3  2004/04/18 02:05:18  venku
   - memory leak fixes.
   Revision 1.2  2004/03/29 09:44:41  venku
   - finished the xml-based testing framework for dependence.
   Revision 1.1  2004/03/09 19:10:40  venku
   - preliminary commit of test setup for dependency analyses.
 */
