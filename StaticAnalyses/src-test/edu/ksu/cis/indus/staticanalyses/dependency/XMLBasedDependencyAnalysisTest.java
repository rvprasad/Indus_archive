
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

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;

import java.util.Collections;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class XMLBasedDependencyAnalysisTest
  extends AbstractXMLBasedTest
  implements IDependencyAnalysisTest {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final DependencyAnalysis da;

	/**
	 * DOCUMENT ME!
	 *
	 * @param theDA
	 */
	public XMLBasedDependencyAnalysisTest(final DependencyAnalysis theDA) {
		da = theDA;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysisTest#getDA()
	 */
	public DependencyAnalysis getDA() {
		return da;
	}

	/**
	 * @see edu.ksu.cis.indus.AbstractXMLBasedTest#getXMLizer()
	 */
	protected AbstractXMLizer getXMLizer() {
		return new DependencyXMLizer();
	}

	/**
	 * @see edu.ksu.cis.indus.AbstractXMLBasedTest#localSetup()
	 */
	protected void localSetup()
	  throws Exception {
		super.localSetup();
		info.put(da.getId(), Collections.singleton(da));
	}
}

/*
   ChangeLog:
   $Log$
 */
