package edu.ksu.cis.bandera.bfa.analysis.ofa;

import edu.ksu.cis.bandera.bfa.AbstractFGNode;
import edu.ksu.cis.bandera.bfa.AbstractWork;
import edu.ksu.cis.bandera.bfa.Prototype;
import edu.ksu.cis.bandera.bfa.WorkList;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Category;

/**
 * FGNode.java
 *
 *
 * Created: Thu Jan 31 00:42:34 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class FGNode extends AbstractFGNode implements Prototype {

	private static final Category cat = Category.getInstance(FGNode.class.getName());

	public FGNode (WorkList wl){
		super(wl);
	}

	protected void doWorkOnNewValue(Object value) {
		for (Iterator i = succs.iterator(); i.hasNext();) {
			 FGNode succ = (FGNode)i.next();
			 if (!succ.values.contains(value)) {
				 worklist.addWork(new SendValuesWork(succ, value));
			 } // end of if (!succ.contains(value))
		} // end of for (Iterator i = succs.iterator(); i.hasNext();)
	}

	protected void doWorkOnNewSucc(AbstractFGNode succ) {
		Set temp = diffValues(succ);
		if (!temp.isEmpty()) {
			worklist.addWork(new SendValuesWork(succ, diffValues(succ)));
		}
	}

	public Object prototype(Object o) {
		return new FGNode((WorkList)o);
	}

	class SendValuesWork extends AbstractWork {

		SendValuesWork(AbstractFGNode node, Set values) {
			super(node, values);
		}

		SendValuesWork(AbstractFGNode node, Object value) {
			super(node, new HashSet());
			addValue(value);
		}

		public final void execute() {
			node.addValues(this.values);
		}
	}

}// FGNode
