package edu.ksu.cis.bandera.staticanalyses.flow.modes.sensitive;


import edu.ksu.cis.bandera.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.Index;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

// FlowSensitiveASTIndexManager.java
/**
 * <p>This class manages indices associated with AST node in flow sensitive mode.  In reality, it provides the implementation
 * to create new indices.</p>
 *
 * Created: Tue Mar  5 14:08:18 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class FlowSensitiveASTIndexManager extends AbstractIndexManager {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(FlowSensitiveASTIndexManager.class);

	/**
	 * <p>Returns an index corresponding to the given entity and context.</p>
	 *
	 * @param o the entity for which the index in required.  Although it is not enforced, this should be of type
	 * <code>Value</code>.
	 * @param c the context in which information pertaining to <code>o</code> needs to be captured.
	 * @return the index that uniquely identifies <code>o</code> in context, <code>c</code>.
	 */
	protected Index getIndex(Object o, Context c) {
		logger.debug("Getting index for " + o + " in " + c);
		return new OneContextInfoIndex(o, c.getProgramPoint());
	}

	/**
	 * <p>Returns a new instance of this class.</p>
	 *
	 * @return a new instance of this class.
	 */
	public Object prototype() {
		return new FlowSensitiveASTIndexManager();
	}

}// FlowSensitiveASTIndexManager
