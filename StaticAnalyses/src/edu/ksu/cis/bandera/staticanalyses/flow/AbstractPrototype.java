package edu.ksu.cis.bandera.bfa;


import edu.ksu.cis.bandera.bfa.Prototype;
import org.apache.log4j.Logger;

/**
 * AbstractPrototype.java
 *
 *
 * Created: Sun Feb 24 08:39:04 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$ 
 */

public class AbstractPrototype implements Prototype {

	public Object prototype(Object param1) {
		throw new UnsupportedOperationException("prototype(param1) is not supported.");
	}

	public Object prototype() {
		throw new UnsupportedOperationException("Parameterless prototype() is not supported.");
	}
	
}// AbstractPrototype
