package edu.ksu.cis.bandera.bfa.analysis.ofa;


import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.FGNodeConnector;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

// RHSConnector.java
/**
 * <p>This class encapsulates the logic to connect ast flow graph nodes with non-ast flow graph nodes when the ast nodes
 * correspond to r-values.
 *
 * Created: Wed Jan 30 15:19:44 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class RHSConnector implements FGNodeConnector {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(RHSConnector.class);

	/**
	 * <p>Connects the given non-ast flow graph node to the ast flow graph node.</p>
	 *
	 * @param ast the ast flow graph node to be connected.
	 * @param nonast the non-ast flow graph node to be connnected.
	 */
	public void connect(FGNode ast, FGNode nonast) {
		nonast.addSucc(ast);
	}

}// RHSConnector
