package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import java.util.HashMap;
import java.util.Map;

import soot.Type;
import soot.Value;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
class Type2CanonicalValueMapper
		extends Type2ValueMapper {

	/**
	 * DOCUMENT ME!
	 */
	final Map<Type, Value> type2canonicalValue = new HashMap<Type, Value>();

	/**
	 * DOCUMENT ME!
	 * 
	 * @param e DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	@Override Value getValue(final Value e) {
		final Type _type = e.getType();
		final Value _result;
		if (type2canonicalValue.containsKey(_type)) {
			_result = type2canonicalValue.get(_type);
		} else {
			_result = e;
			type2canonicalValue.put(_type, e);
		}
		return _result;
	}
}

// End of File
