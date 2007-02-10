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

import edu.ksu.cis.indus.processing.IProcessor;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class controls the post processing for an analysis. The analysis as realised by FA is very low-level. The information
 * is raw. This needs to be massaged via post processing. Each post processor can registered interest in particular types of
 * AST chunks. The controller will walk over the analyzed system and call the registered post processors. The post processors
 * then collect information from the analysis in form which is more accessible to the other applications. This visitor will
 * notify the interested post processors with the given AST node and then visit it's children.
 * <p>
 * Please note that the processor should be registered/unregistered separately for interface-level (class/method) processing
 * and functional (method-body) processing.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class ValueAnalyzerBasedProcessingController
		extends ProcessingController {

	/**
	 * The analyzer instance that provides the low-level analysis information to be be further processed.
	 * 
	 * @invariant analyzer != null
	 */
	protected IValueAnalyzer<?> analyzer;

	/**
	 * Sets the analyzer which provides the information to be processed. This implementation retrieves the environment from
	 * the provided analyzer.
	 * 
	 * @param analyzerParam an instance of the FA.
	 */
	public void setAnalyzer(final IValueAnalyzer<?> analyzerParam) {
		analyzer = analyzerParam;
		setEnvironment(analyzer.getEnvironment());
	}

	/**
	 * Sets the analyzer on all the processors which require the analyzer.
	 */
	@Override protected void initializeProcessors() {
		final Collection<IProcessor> _processors = new HashSet<IProcessor>();
		_processors.addAll(interfaceProcessors);

		for (final Iterator<Set<IProcessor>> _i = class2processors.values().iterator(); _i.hasNext();) {
			_processors.addAll(_i.next());
		}

		for (final Iterator _i = _processors.iterator(); _i.hasNext();) {
			final Object _o = _i.next();

			if (_o instanceof IValueAnalyzerBasedProcessor) {
				((IValueAnalyzerBasedProcessor) _o).setAnalyzer(analyzer);
			}
		}
	}
}

// End of File
