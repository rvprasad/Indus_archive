
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow.instances;

import edu.ksu.cis.indus.AbstractXMLBasedTest;

import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IFATest;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import edu.ksu.cis.indus.xmlizer.AbstractXMLizer;


/**
 * This is a XML based test for value flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractXMLBasedValueAnalysisTest
  extends AbstractXMLBasedTest
  implements IFATest {
	/** 
	 * The name of the tag used to mark the parts of the system visited during FA.
	 */
	private String nameOfTheTag;

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFATest#setFA(edu.ksu.cis.indus.staticanalyses.flow.FA)
	 */
	public void setFA(final FA flowAnalysis) {
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFATest#setFATagName(java.lang.String)
	 */
	public void setFATagName(final String tagName) {
		nameOfTheTag = tagName;
	}

	/**
	 * @see AbstractXMLBasedTest#localSetup()
	 */
	protected final void localSetup()
	  throws Exception {
		super.localSetup();
		info.put(AbstractXMLizer.FILE_NAME_ID, getName());
		info.put(IValueAnalyzer.TAG_ID, nameOfTheTag);
	}
}

// End of File
