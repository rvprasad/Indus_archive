package edu.ksu.cis.bandera.bfa;

import java.util.HashSet;
import java.util.Set;
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

	private static final Logger logger = Logger.getLogger(AbstractIndexManager.class.getName());

	protected Set indices = new HashSet();

	final Index queryIndex(Object o, Context c) {
		Index temp = getIndex(o, c);
		if (!indices.contains(temp)) {
			indices.add(temp);
		} // end of if (sm2indices.containsKey(sm)) else
		return temp;
	}

	protected abstract Index getIndex(Object o, Context c);

	public Object prototype(Object o) {
		throw new UnsupportedOperationException("clone() with parameter is not supported.");
	}

	public Object prototype() {
		throw new UnsupportedOperationException("Parameterless clone() is not supported.");
	}

	void reset() {
		indices.clear();
	}

}// AbstractIndexManager
