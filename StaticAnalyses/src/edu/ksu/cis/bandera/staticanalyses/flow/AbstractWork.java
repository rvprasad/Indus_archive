package edu.ksu.cis.bandera.bfa;

import java.util.Collection;
/**
 * AbstractWork.java
 *
 *
 * Created: Tue Jan 22 02:54:57 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public abstract class AbstractWork {

	protected Collection values;

	protected AbstractFGNode node;

	protected AbstractWork(AbstractFGNode node, Collection values) {
		this.node = node;
		this.values = values;
	}

	public final void addValue(Object o) {
		values.add(o);
	}

	public final void addValues(Collection values) {
		this.values.addAll(values);
	}

	public abstract void execute();

	public final void setFGNode(AbstractFGNode node) {
		this.node = node;
	}

}// AbstractWork
