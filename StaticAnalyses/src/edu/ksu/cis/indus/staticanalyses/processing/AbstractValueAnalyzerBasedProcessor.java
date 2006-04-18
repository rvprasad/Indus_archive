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
