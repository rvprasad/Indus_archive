package edu.ksu.cis.bandera.bfa;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

// WorkList.java

/**
 * <p>A worklist implementation.</p>
 *
 * <p>Created: Tue Jan 22 02:43:16 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class WorkList {

	/**
	 * <p>The list containing the work pieces in the work list. </p>
	 *
	 */
	private final List list;

	/**
	 * <p>An instance of <code>Logger</code> used for logging purposes.</p>
	 *
	 */
	private static final Logger logger = Logger.getLogger(WorkList.class);

	/**
	 * <p>Creates a new <code>WorkList</code> instance.</p>
	 *
	 */
	WorkList() {
		list = new ArrayList();
	}

	/**
	 * <p>Adds a work piece into the worklist.  If <code>w</code> exists in the worklist, it will not be
	 * added.  The existence check is done using <code>java.lang.Object.equals()</code> method.</p>
	 *
	 * @param w the work to be added into the worklist.
	 */
	public final void addWork(AbstractWork w) {
		if (!list.contains(w)) {
			logger.debug("Added new work:" + w);
			list.add(w);
		} // end of if (!list.contains(w))
	}

	/**
	 * <p>Removes any work left in the worklist.  These work pieces are not "executed".</p>
	 *
	 */
	void clear() {
		list.clear();
	}

	/**
	 * <p>Executes the work pieces in the worklist.  This method returns when the worklist is empty, i.e., all the work peices
	 * have been executed.</p>
	 *
	 */
	void process() {
		while (!list.isEmpty()) {
			AbstractWork w = (AbstractWork)list.remove(0);
			logger.debug("Processing work:" + w);
			w.execute();
		} // end of while (!list.isEmpty())
	}

}// WorkList
