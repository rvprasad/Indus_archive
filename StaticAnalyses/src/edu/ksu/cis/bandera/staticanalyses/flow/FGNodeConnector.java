package edu.ksu.cis.bandera.bfa;

//FGNodeConnector.java

/**
 * <p>The super interface to be implemented by classes which connect AST nodes to Non-AST nodes.  An implementation of this
 * interface separates the logic of connecting AST and Non-AST nodes depending on whether the AST node corresponds to  a
 * r-value or l-value expression when constructing the flow graph.  This helps realize something similar to the
 * <i>Strategy</i> pattern as given in Gang of Four book.</p>
 *
 * <p>Created: Wed Jan 30 15:18:24 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public interface FGNodeConnector {

	/**
	 * <p>Connects the given AST node to the Non-AST node.</p>
	 *
	 * @param ast the AST node to be connected to the Non-AST node.
	 * @param nonast the Non-AST node to be connected to the AST node.
	 */
	void connect(FGNode ast, FGNode nonast);

}// FGNodeConnector
