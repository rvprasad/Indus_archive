package edu.ksu.cis.bandera.bfa.modes.sensitive.allocationsite;


import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.Index;
import edu.ksu.cis.bandera.bfa.AbstractIndexManager;

import org.apache.log4j.Category;

/**
 * InstanceFieldIndexManager.java
 *
 *
 * Created: Fri Jan 25 13:26:37 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class InstanceFieldIndexManager extends AbstractIndexManager {

	private static final Category cat =
		Category.getInstance(InstanceFieldIndexManager.class.getName());

	protected Index getIndex(Object o, Context c) {
		return null;
	}

}// InstanceFieldIndexManager
