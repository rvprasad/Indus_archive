package edu.ksu.cis.bandera.bfa;


import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * AbstractFGNode.java
 *
 *
 * Created: Tue Jan 22 02:57:07 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public abstract class AbstractFGNode implements FGNode {

	protected final Set succs = new HashSet();

	protected final Set values = new HashSet();

	protected final WorkList worklist;

	private static final Logger logger = Logger.getLogger(AbstractFGNode.class.getName());

	protected AbstractFGNode(WorkList worklist) {
		this.worklist = worklist;
	}

	public void addSucc(FGNode node) {
		logger.debug("Adding " + node + " as successor of " + this);
		succs.add(node);
		onNewSucc(node);
	}

	public void addSuccs(Collection succs) {
		logger.debug("Adding " + succs + " as successors of " + this);
		this.succs.addAll(succs);
		onNewSuccs(succs);
	}

	public void addValue(Object value) {
		logger.debug("Adding " + value + " to " + this);
		values.add(value);
		onNewValue(value);
	}

	public void addValues(Collection values) {
		logger.debug("Adding " + values + " to " + this);
		this.values.addAll(values);
		onNewValues(values);
	}

	public boolean containsValue(Object o) {
		return values.contains(o);
	}

	public final Collection diffValues(edu.ksu.cis.bandera.bfa.FGNode src) {
		Set temp = new HashSet();
		for (Iterator i = values.iterator(); i.hasNext();) {
			Object t = i.next();
			if (!src.getValues().contains(t)) {
				temp.add(t);
			} // end of if (!dest.values.contains(t))
		} // end of for (Iterator i = dest.iterator(); i.hasNext();)
		return temp;
	}

	public void onNewSuccs(Collection succs) {
		for (Iterator i = succs.iterator(); i.hasNext();) {
			 onNewSucc((AbstractFGNode)i.next());
		} // end of for (Iterator i = values.iterator(); i.hasNext();)
	}

	public Collection getValues() {
		Set temp = new HashSet();
		temp.addAll(values);
		return temp;
	}

	public Object prototype(Object param1) {
		throw new UnsupportedOperationException("prototype(param1) method is not supported.");
	}

	public Object prototype() {
		throw new UnsupportedOperationException("Parameterless prototype() method is not supported.");
	}

}// AbstractFGNode
