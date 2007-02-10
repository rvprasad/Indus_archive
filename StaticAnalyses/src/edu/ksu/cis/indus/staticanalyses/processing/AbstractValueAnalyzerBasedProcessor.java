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

package edu.ksu.cis.indus.staticanalyses.processing;

import edu.ksu.cis.indus.annotations.Empty;
import edu.ksu.cis.indus.processing.AbstractProcessor;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor;

/**
 * Abstract implementation of <code>IValueAnalyzerBasedProcessor</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <V> 
 */
public abstract class AbstractValueAnalyzerBasedProcessor<V>
		extends AbstractProcessor
		implements IValueAnalyzerBasedProcessor<V> {

	/**
	 * @see IValueAnalyzerBasedProcessor#setAnalyzer(IValueAnalyzer)
	 */
	@Empty public void setAnalyzer(@SuppressWarnings("unused") final IValueAnalyzer<V> analyzer) {
		// does nothing
	}
}

// End of File
