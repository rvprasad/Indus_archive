package edu.ksu.cis.bandera.bfa.analysis.ofa;


import edu.ksu.cis.bandera.bfa.FGNodeConnector;
import edu.ksu.cis.bandera.bfa.FGNode;

import org.apache.log4j.Category;

/**
 * LHSConnector.java
 *
 *
 * Created: Wed Jan 30 15:19:44 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class LHSConnector implements FGNodeConnector {

	private static final Category cat = Category.getInstance(LHSConnector.class.getName());

	public void connect(FGNode ast, FGNode nonast) {
		ast.addSucc(nonast);
	}

}// LHSConnector
