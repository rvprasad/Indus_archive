
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

import edu.ksu.cis.indus.common.graph.IDirectedGraph;

import java.util.BitSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class contains the logic to calculate control dependences in the reverse direction of control flow considering the
 * exit points as the entry points.
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
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.EntryControlDA#computeControlDependency(IDirectedGraph)
	 */
	protected BitSet[] computeControlDependency(final IDirectedGraph graph) {
		// TODO: Implement this.
		return super.computeControlDependency(graph);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.7  2004/01/30 23:55:18  venku
   - added a new analyze method to analyze only the given
     collection of methods.

   Revision 1.6  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.

   Revision 1.5  2003/12/09 04:22:09  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.4  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.3  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.2  2003/11/25 19:12:59  venku
   - documentation.
   Revision 1.1  2003/11/25 17:51:23  venku
   - split control dependence into 2 classes.
     EntryControlDA handled control DA as required for backward slicing.
     ExitControlDA handles control DA as required for forward slicing.
   - ripple effect.
 */
