package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa;


import edu.ksu.cis.bandera.staticanalyses.flow.AbstractFGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.bandera.staticanalyses.flow.FGNode;
import edu.ksu.cis.bandera.staticanalyses.flow.WorkList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

// OFAFGNode.java
/**
 * <p>This class represents the flow graph node that accumulates objects as their entities would refer to objects at
 * run-time.  This is an Object-flow analysis specific implementation.</p>
 *
 * Created: Thu Jan 31 00:42:34 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public class OFAFGNode extends AbstractFGNode {

	/**
	 * <p>Creates a new <code>OFAFGNode</code> instance.</p>
	 *
	 * @param wl the worklist associated with the instance of the framework within which this node exists.
	 */
	public OFAFGNode (WorkList wl){
		super(wl);
	}

	/**
	 * <p>Adds a new work to the worklist to propogate <code>value</code> in this node to it's successor nodes.</p>
	 *
	 * @param value the value to be propogated to the successor node.
	 */
	public void onNewValue(Object value) {
		for (Iterator i = succs.iterator(); i.hasNext();) {
			 FGNode succ = (FGNode)i.next();
			 if (!succ.getValues().contains(value)) {
				 worklist.addWork(new SendValuesWork(succ, value));
			 } // end of if (!succ.contains(value))
		} // end of for (Iterator i = succs.iterator(); i.hasNext();)
	}

	/**
	 * <p>Adds a new work to the worklist to propogate <code>values</code> in this node to it's successor nodes.</p>
	 *
	 * @param values the values to be propogated to the successor node.  The collection contains object of type
	 * <code>Object</code>.
	 */
	public void onNewValues(Collection values) {
		for (Iterator i = succs.iterator(); i.hasNext();) {
			 FGNode succ = (FGNode)i.next();
			 if (!diffValues(succ).isEmpty()) {
				 worklist.addWork(new SendValuesWork(succ, values));
			 } // end of if (!succ.diffValues(values).empty())
		} // end of for (Iterator i = succs.iterator(); i.hasNext();)
	}

	/**
	 * <p>Adds a new work to the worklist to propogate the values in this node to <code>succ</code>.  Only the difference
	 * values are propogated.</p>
	 *
	 * @param succ the successor node that was added to this node.
	 */
	public void onNewSucc(FGNode succ) {
		Collection temp = diffValues(succ);
		if (!temp.isEmpty()) {
			worklist.addWork(new SendValuesWork(succ, temp));
		}
	}

	/**
	 * <p>Returns a new instance of this class.</p>
	 *
	 * @param o the <code>WorkList</code> to be passed to the constructor of this class.
	 * @return a new instance of this class parameterized by <code>o</code>.
	 */
	public Object prototype(Object o) {
		return new OFAFGNode((WorkList)o);
	}

	/**
	 * <p>This class represents a peice of work to inject a set of values into a flow graph node.</p>
	 *
	 */
	class SendValuesWork extends AbstractWork {

		/**
		 * <p>Creates a new <code>SendValuesWork</code> instance.</p>
		 *
		 * @param node the node into which the values need to be injected.
		 * @param values a collection containing the values to be injected.
		 */
		SendValuesWork(FGNode node, Collection values) {
			super(node, values);
		}

		/**
		 * <p>Creates a new <code>SendValuesWork</code> instance.</p>
		 *
		 * @param node the node into which the values need to be injected.
		 * @param value the value to be injected.
		 */
		SendValuesWork(FGNode node, Object value) {
			super(node, new HashSet());
			addValue(value);
		}

		/**
		 * <p>Injects the values into the associated node.</p>
		 *
		 */
		public final void execute() {
			node.addValues(this.values);
		}
	}

}// FGNode
