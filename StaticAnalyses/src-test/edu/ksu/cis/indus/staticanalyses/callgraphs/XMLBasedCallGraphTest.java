
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

package edu.ksu.cis.indus.staticanalyses.callgraphs;

import edu.ksu.cis.indus.AbstractXMLBasedTest;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.xmlizer.IXMLizer;


/**
 * This class tests call graphs based on their xmlized representation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class XMLBasedCallGraphTest
  extends AbstractXMLBasedTest
  implements ICallGraphTest {
	/** 
     * @see edu.ksu.cis.indus.staticanalyses.callgraphs.ICallGraphTest#setCallGraph(CallGraphInfo)
     */
    public void setCallGraph(final CallGraphInfo callgraph) {
        info.put(ICallGraphInfo.ID, callgraph);        
    }

    /**
	 * @see edu.ksu.cis.indus.AbstractXMLBasedTest#getXMLizer()
	 */
	protected IXMLizer getXMLizer() {
		return new CallGraphXMLizer();
	}
}

// End of File
