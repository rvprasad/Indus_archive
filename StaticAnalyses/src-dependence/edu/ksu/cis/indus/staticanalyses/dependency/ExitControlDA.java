
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

import edu.ksu.cis.indus.staticanalyses.support.DirectedGraph;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.BitSet;


/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ExitControlDA
  extends EntryControlDA {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ExitControlDA.class);

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#analyze()
	 */
	public void analyze() {
		stable = false;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Control Dependence processing");
		}

		localAnalyze();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Control Dependence processing");
		}
		stable = true;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.EntryControlDA#computeControlDependency(DirectedGraph)
	 */
	protected BitSet[] computeControlDependency(final DirectedGraph graph) {
		// TODO: Auto-generated method stub
		return super.computeControlDependency(graph);
	}
}

/*
   ChangeLog:
   $Log$
 */
