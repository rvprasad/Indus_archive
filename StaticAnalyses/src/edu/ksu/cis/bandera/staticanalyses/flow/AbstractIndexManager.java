package edu.ksu.cis.bandera.bfa;

import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * AbstractIndexManager.java
 *
 *
 * Created: Tue Jan 22 04:54:38 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public abstract class AbstractIndexManager implements Prototype {

	private static final Logger logger = LogManager.getLogger(AbstractIndexManager.class);

	protected Set indices = new HashSet();

	final Index queryIndex(Object o, Context c) {
		Index temp = getIndex(o, c);
		if (!indices.contains(temp)) {
			indices.add(temp);
		} // end of if (sm2indices.containsKey(sm)) else
		return temp;
	}

	protected abstract Index getIndex(Object o, Context c);

	void reset() {
		indices.clear();
	}

	public Object prototype(Object o) {
		throw new UnsupportedOperationException("Single parameter prototype() is not supported.");
	}

}// AbstractIndexManager
