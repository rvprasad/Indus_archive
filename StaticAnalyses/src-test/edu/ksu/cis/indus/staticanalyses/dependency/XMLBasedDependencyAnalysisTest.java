
/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.AbstractXMLBasedTest;
import edu.ksu.cis.indus.IXMLBasedTest;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.xmlizer.IXMLizer;

import java.util.Collections;
import java.util.Iterator;


/**
 * This is a XML based test for dependency analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class XMLBasedDependencyAnalysisTest
  extends AbstractXMLBasedTest
  implements IDependencyAnalysisTest,
	  IXMLBasedTest {
	/** 
	 * The instance of the xmlizer used to generate the test data.
	 */
	private DependencyXMLizer xmlizer;

	/** 
	 * The call graph of the test input system.
	 */
	private ICallGraphInfo cgi;

	/** 
	 * The instance of the analysis being tested.
	 */
	private IDependencyAnalysis da;

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
	public XMLBasedDependencyAnalysisTest(final IDependencyAnalysis theDA, final DependencyXMLizer depXMLizer) {
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
	public IDependencyAnalysis getDA() {
		return da;
	}

	/**
	 * @see edu.ksu.cis.indus.IEnvironmentBasedTest#setEnvironment(edu.ksu.cis.indus.interfaces.IEnvironment)
	 */
	public void setEnvironment(final IEnvironment environment) {
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
		for (final Iterator _i = da.getIds().iterator(); _i.hasNext();) {
            final Object _id = _i.next();
            info.put(_id, Collections.singleton(da));
        }
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

// End of File
