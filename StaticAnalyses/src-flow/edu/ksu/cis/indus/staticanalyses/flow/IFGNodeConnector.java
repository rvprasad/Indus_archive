
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

package edu.ksu.cis.indus.staticanalyses.flow;

/**
 * The super interface to be implemented by classes which connect AST nodes to Non-AST nodes.  An implementation of this
 * interface separates the logic of connecting AST and Non-AST nodes depending on whether the AST node corresponds to  a
 * r-value or l-value expression when constructing the flow graph.  This helps realize something similar to the
 * <i>Strategy</i> pattern as given in "Gang of Four" book.
 * 
 * <p>
 * Created: Wed Jan 30 15:18:24 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public interface IFGNodeConnector {
	/**
	 * Connects the given AST node to the Non-AST node.
	 *
	 * @param ast the AST node to be connected to the Non-AST node.
	 * @param nonast the Non-AST node to be connected to the AST node.
	 *
	 * @pre ast != null and nonast != null
	 */
	void connect(IFGNode ast, IFGNode nonast);
}

/*
   ChangeLog:

   $Log$
   Revision 1.2  2003/08/16 02:50:22  venku
   Spruced up documentation and specification.
   Moved onNewXXX() methods from IFGNode to AbstractFGNode.

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.

   Revision 1.1  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
