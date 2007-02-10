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

package edu.ksu.cis.indus.staticanalyses.interfaces;

import edu.ksu.cis.indus.processing.IProcessor;

/**
 * This is the interface to be provided by higher level analyses to a controlling agent. The controlling agent walks over the
 * analyzed system and calls the interested processors at each value and statement in the analyzed system. Upon callback, the
 * processors suitably collect/process the information available from the low-level analyzer.
 * <p>
 * Objects that provide this interface can be used with <code>ValueAnalyzerBasedProcessingController</code>.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 * @param <V> is the type of the value object.
 */
public interface IValueAnalyzerBasedProcessor<V>
		extends IProcessor {

	/**
	 * Sets the analyzer that provides the low-level information to be processed..
	 * 
	 * @param analyzer that provides low-level info.
	 * @pre analyzer != null
	 */
	void setAnalyzer(IValueAnalyzer<V> analyzer);
}

// End of File
