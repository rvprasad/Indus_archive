package edu.ksu.cis.bandera.bfa.modes.sensitive;


import edu.ksu.cis.bandera.bfa.Index;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * OneContextInfoIndex.java
 *
 *
 * Created: Fri Jan 25 13:11:19 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class OneContextInfoIndex implements Index {

	private static final Logger logger = LogManager.getLogger(OneContextInfoIndex.class);

	private Object value, contextInfo;

	OneContextInfoIndex(Object value, Object c) {
		this.value = value;
		this.contextInfo = c;
		logger.debug("Value: " + value + "  Context: " + contextInfo);
	}

	public boolean equals(Object index) {
		boolean temp = false;
		logger.debug(index + "\n" + this);
		if (index instanceof OneContextInfoIndex) {
			OneContextInfoIndex d = (OneContextInfoIndex)index;
			temp = d.value.equals(value) && d.contextInfo.equals(contextInfo);
		} // end of if (o instanceof DummyIndex)
		return temp;
	}

	public int hashCode() {
		return (value + " " + contextInfo).hashCode();
	}

	public String toString() {
		return value + " " + contextInfo + " " + hashCode();
	}

}// OneContextInfoIndex
