package edu.ksu.cis.bandera.bfa.analysis.ofa;

import edu.ksu.cis.bandera.bfa.FGNodeConnector;
import edu.ksu.cis.bandera.bfa.AbstractFGNode;

import org.apache.log4j.Category;

/**
 * RHSConnector.java
 *
 *
 * Created: Wed Jan 30 15:19:44 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class RHSConnector implements FGNodeConnector {

	private static final Category cat = Category.getInstance(RHSConnector.class.getName());

	public void connect(AbstractFGNode ast, AbstractFGNode nonast) {
		nonast.addSucc(ast);
	}

}// RHSConnector
