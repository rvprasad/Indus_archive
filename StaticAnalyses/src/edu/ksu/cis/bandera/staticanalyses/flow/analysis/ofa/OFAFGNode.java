package edu.ksu.cis.bandera.bfa.analysis.ofa;


import edu.ksu.cis.bandera.bfa.AbstractFGNode;
import edu.ksu.cis.bandera.bfa.AbstractWork;
import edu.ksu.cis.bandera.bfa.FGNode;
import edu.ksu.cis.bandera.bfa.WorkList;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * OFAFGNode.java
 *
 *
 * Created: Thu Jan 31 00:42:34 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class OFAFGNode extends AbstractFGNode {

	public OFAFGNode (WorkList wl){
		super(wl);
	}

	public void onNewValue(Object value) {
		for (Iterator i = succs.iterator(); i.hasNext();) {
			 FGNode succ = (FGNode)i.next();
			 if (!succ.getValues().contains(value)) {
				 worklist.addWork(new SendValuesWork(succ, value));
			 } // end of if (!succ.contains(value))
		} // end of for (Iterator i = succs.iterator(); i.hasNext();)
	}

	public void onNewValues(Collection values) {
		for (Iterator i = succs.iterator(); i.hasNext();) {
			 FGNode succ = (FGNode)i.next();
			 if (!diffValues(succ).isEmpty()) {
				 worklist.addWork(new SendValuesWork(succ, values));
			 } // end of if (!succ.diffValues(values).empty())
		} // end of for (Iterator i = succs.iterator(); i.hasNext();)
	}

	public void onNewSucc(FGNode succ) {
		Collection temp = diffValues(succ);
		if (!temp.isEmpty()) {
			worklist.addWork(new SendValuesWork(succ, temp));
		}
	}

	public Object prototype(Object o) {
		return new OFAFGNode((WorkList)o);
	}

	class SendValuesWork extends AbstractWork {

		SendValuesWork(FGNode node, Collection values) {
			super(node, values);
		}

		SendValuesWork(FGNode node, Object value) {
			super(node, new HashSet());
			addValue(value);
		}

		public final void execute() {
			node.addValues(this.values);
		}
	}

}// FGNode
