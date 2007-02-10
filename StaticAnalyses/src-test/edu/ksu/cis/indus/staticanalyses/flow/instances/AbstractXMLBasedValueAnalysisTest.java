
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

package edu.ksu.cis.indus.staticanalyses.flow.instances;

import edu.ksu.cis.indus.AbstractXMLBasedTest;

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
  extends AbstractXMLBasedTest implements IFATest {
	/** 
	 * The name of the tag used to mark the parts of the system visited during FA.
	 */
	private String nameOfTheTag;

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
