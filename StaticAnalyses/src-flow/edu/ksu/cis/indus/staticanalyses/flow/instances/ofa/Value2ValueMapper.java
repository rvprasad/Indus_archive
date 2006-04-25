package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import soot.Value;

/**
 * This maps values (expression AST nodes) to to other nodes.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class Value2ValueMapper {

	/**
	 * Retrieves the value to which the given value is mapped to.
	 * 
	 * @param e is the value of interest.
	 * @return the mapped value.
	 */
	Value getValue(final Value e) {
		return e;
	}
}

// End of File
