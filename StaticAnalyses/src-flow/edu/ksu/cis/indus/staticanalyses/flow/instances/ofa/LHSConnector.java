
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;


/**
 * This class encapsulates the logic to connect ast flow graph nodes with non-ast flow graph nodes when the ast nodes
 * correspond to l-values.
 * 
 * <p>
 * Created: Wed Jan 30 15:19:44 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class LHSConnector
  implements IFGNodeConnector {
	/**
	 * Connects the given ast flow graph node to the non-ast flow graph node.  This is used to connect flow nodes
	 * corresponding to LHS expressions.
	 *
	 * @param ast the ast flow graph node to be connected.
	 * @param nonast the non-ast flow graph node to be connnected.
	 *
	 * @pre ast != null and nonast != null
	 */
	public void connect(final IFGNode ast, final IFGNode nonast) {
		ast.addSucc(nonast);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.2  2003/08/15 03:39:53  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosened later on in implementaions.
   Renamed a few fields/parameter variables to avoid name confusion.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.5  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
