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

public class WorkList extends WorkBag {

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
		super(WorkBag.DFS);
	}

	/**
	 * <p>Adds a work piece into the worklist.  If <code>w</code> exists in the worklist, it will not be
	 * added.  The existence check is done using <code>java.lang.Object.equals()</code> method.</p>
	 *
	 * @param w the work to be added into the worklist.
	 */
	public final void addWork(AbstractWork w) {
		logger.debug("Added new work:" + w);
		addWorkNoDuplicates(w);
	}

	/**
	 * <p>Executes the work pieces in the worklist.  This method returns when the worklist is empty, i.e., all the work peices
	 * have been executed.</p>
	 *
	 */
	void process() {
		while (!isEmpty()) {
			AbstractWork w = (AbstractWork)getWork();
			logger.debug("Processing work:" + w);
			w.execute();
		} // end of while (!list.isEmpty())
	}

}// WorkList
