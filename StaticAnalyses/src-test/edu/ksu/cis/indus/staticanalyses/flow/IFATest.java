
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;


/**
 * This is the interface of unit tests that test instances of Flow analysis framework.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IFATest {
	/**
	 * Sets the analyzer to be tested.
	 *
	 * @param valueAnalyzer will be tested.
	 *
	 * @pre valueAnalyzer != null
	 */
	void setAnalyzer(IValueAnalyzer valueAnalyzer);
    
	/**
	 * Sets the name of the tag used by the flow analysis instance.
	 *
	 * @param tagName is the name of the tag.
	 *
	 * @pre tagName != null
	 */
	void setFATagName(String tagName);
}

// End of File
