package edu.ksu.cis.bandera.bfa;

/**
 * FGNodeConnector.java
 *
 *
 * Created: Wed Jan 30 15:18:24 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public interface FGNodeConnector {

	void connect(AbstractFGNode ast, AbstractFGNode nonast);

}// FGNodeConnector
