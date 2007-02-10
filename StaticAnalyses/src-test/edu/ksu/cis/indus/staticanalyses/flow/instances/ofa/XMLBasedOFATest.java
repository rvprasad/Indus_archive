
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.instances.AbstractXMLBasedValueAnalysisTest;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import edu.ksu.cis.indus.xmlizer.IXMLizer;


/**
 * This is a XML based test for object flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class XMLBasedOFATest
  extends AbstractXMLBasedValueAnalysisTest {    
	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFATest#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer valueAnalyzer) {
		if (valueAnalyzer instanceof OFAnalyzer) {
			info.put(IValueAnalyzer.ID, valueAnalyzer);
		}
	}

	/**
	 * Retrieve the xmlizer to be used to generate the xml data for testing purpose.
	 *
	 * @return the xmlizer.
	 *
	 * @post result != null
	 */
	protected IXMLizer getXMLizer() {
		return new OFAXMLizer();
	}
}

// End of File
