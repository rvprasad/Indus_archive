package edu.ksu.cis.bandera.bfa.modes.sensitive;


import edu.ksu.cis.bandera.bfa.AbstractIndexManager;
import edu.ksu.cis.bandera.bfa.Context;
import edu.ksu.cis.bandera.bfa.Index;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * FlowSensitiveASTIndexManager.java
 *
 *
 * Created: Tue Mar  5 14:08:18 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class FlowSensitiveASTIndexManager extends AbstractIndexManager {

	private static final Logger logger = LogManager.getLogger(FlowSensitiveASTIndexManager.class);

	protected Index getIndex(Object o, Context c) {
		logger.debug("Getting index for " + o + " in " + c);
		return new OneContextInfoIndex(o, c.getProgramPoint());
	}

	public Object prototype() {
		return new FlowSensitiveASTIndexManager();
	}

}// FlowSensitiveASTIndexManager
