package edu.ksu.cis.bandera.bfa;


import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;

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

	private static final Category cat = Category.getInstance(WorkList.class.getName());

	WorkList() {
		list = new ArrayList();
	}

	public final void addWork(AbstractWork w) {
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
			w.execute();
		} // end of while (!list.isEmpty())
	}

}// WorkList
