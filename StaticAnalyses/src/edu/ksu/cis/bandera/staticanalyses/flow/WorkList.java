package edu.ksu.cis.bandera.bfa;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * WorkList.java
 *
 *
 * Created: Tue Jan 22 02:43:16 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class WorkList {

	List list;

	private static final Logger logger = Logger.getLogger(WorkList.class.getName());

	WorkList() {
		list = new ArrayList();
	}

	public final void addWork(AbstractWork w) {
		logger.debug(w.values + "--" + w.node + ": new work");
		if (!list.contains(w)) {
			list.add(w);
		} // end of if (!list.contains(w))
	}

	void clear() {
		list.clear();
	}

	void process() {
		while (!list.isEmpty()) {
			AbstractWork w = (AbstractWork)list.remove(0);
			logger.debug(w.values + "--" + w.node + ": processing");
			w.execute();
		} // end of while (!list.isEmpty())
	}

}// WorkList
