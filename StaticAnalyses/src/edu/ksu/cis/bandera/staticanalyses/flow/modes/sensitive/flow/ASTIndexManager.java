package edu.ksu.cis.bandera.bfa.modes.sensitive.flow;

import edu.ksu.cis.bandera.bfa.AbstractIndexManager;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.Index;

import org.apache.log4j.Category;

/**
 * ASTIndexManager.java
 *
 *
 * Created: Fri Jan 25 13:11:19 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class ASTIndexManager extends AbstractIndexManager {

	private static final Category cat = Category.getInstance(ASTIndexManager.class.getName());

	protected Index getIndex(Object o, Context c) {
		return null;
	}

}// ASTIndexManager
