package edu.ksu.cis.bandera.bfa;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Category;

/**
 * AbstractFGNode.java
 *
 *
 * Created: Tue Jan 22 02:57:07 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public abstract class AbstractFGNode {

	protected final Set succs = new HashSet();

	protected final Set values = new HashSet();

	protected final WorkList worklist;

	private static final Category cat = Category.getInstance(AbstractFGNode.class.getName());

	protected AbstractFGNode(WorkList worklist) {
		this.worklist = worklist;
	}

	public void addSucc(AbstractFGNode node) {
		this.succs.add(node);
		doWorkOnNewSucc(node);
	}

	public void addSuccs(Collection succs) {
		this.succs.addAll(succs);
		doWorkOnNewSuccs(succs);
	}

	public void addValue(Object value) {
		values.add(value);
		doWorkOnNewValue(value);
	}

	public void addValues(Collection values) {
		this.values.addAll(values);
		doWorkOnNewValues(values);
	}

	public boolean containsValue(Object o) {
		return values.contains(o);
	}

	public final Set diffValues(AbstractFGNode src) {
		Set temp = new HashSet();
		for (Iterator i = values.iterator(); i.hasNext();) {
			Object t = i.next();
			if (!src.values.contains(t)) {
				temp.add(t);
			} // end of if (!dest.values.contains(t))
		} // end of for (Iterator i = dest.iterator(); i.hasNext();)
		return temp;
	}

	protected void doWorkOnNewValues(Collection values) {
		for (Iterator i = values.iterator(); i.hasNext();) {
			 doWorkOnNewValue(i.next());
		} // end of for (Iterator i = values.iterator(); i.hasNext();)
	}

	protected void doWorkOnNewSuccs(Collection succs) {
		for (Iterator i = succs.iterator(); i.hasNext();) {
			 doWorkOnNewSucc((AbstractFGNode)i.next());
		} // end of for (Iterator i = values.iterator(); i.hasNext();)
	}

	protected abstract void doWorkOnNewValue(Object value);

	protected abstract void doWorkOnNewSucc(AbstractFGNode succ);

	public Collection getValues() {
		Set temp = new HashSet();
		temp.addAll(values);
		return temp;
	}

	public final Object prototype() {
		throw new UnsupportedOperationException("Parameterless clone() is not supported.");
	}

}// AbstractFGNode
