
package edu.ksu.cis.bandera.staticanalyses.flow.modes.sensitive;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.bandera.staticanalyses.flow.Context;
import edu.ksu.cis.bandera.staticanalyses.flow.Index;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


// AllocationSiteSensitiveIndexManager.java

/**
 * <p>This class manages indices associated with fields and array components  in allocation-site sensitive mode.  In reality,
 * it provides the implementation to create new indices.</p>
 *
 * Created: Tue Mar  5 14:08:18 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class AllocationSiteSensitiveIndexManager
  extends AbstractIndexManager {
	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	private static final Logger logger = LogManager.getLogger(AllocationSiteSensitiveIndexManager.class);

	/**
	 * <p>Returns a new instance of this class.</p>
	 *
	 * @return a new instance of this class.
	 */
	public Object prototype() {
		return new AllocationSiteSensitiveIndexManager();
	}

	/**
	 * <p>Returns an index corresponding to the given entity and context.</p>
	 *
	 * @param o the entity for which the index in required.  Although it is not enforced, this should be of type
	 * <code>FielRef</code> or <code>ArrayRef</code>.
	 * @param c the context in which information pertaining to <code>o</code> needs to be captured.
	 * @return the index that uniquely identifies <code>o</code> in context, <code>c</code>.
	 */
	protected Index getIndex(Object o, Context c) {
		logger.debug("Getting index for " + o + " in " + c);

		return new OneContextInfoIndex(o, c.getAllocationSite());
	}
} // AllocationSiteBasedIndexManager
