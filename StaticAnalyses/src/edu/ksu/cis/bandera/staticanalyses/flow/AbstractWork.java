package edu.ksu.cis.bandera.staticanalyses.flow;

import java.util.Collection;

//AbstractWork.java
/**
 * A piece of work that can be processed by <code>WorkList</code>.
 *
 * <p>Created: Tue Jan 22 02:54:57 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public abstract class AbstractWork {

	/**
	 * <p>An instance of <code>Logger</code> used for logging purpose.</p>
	 *
	 */
	protected Collection values;

	/**
	 * <p>The flow graph node associated with this work.</p>
	 *
	 */
	protected FGNode node;

	/**
	 * <p>Creates a new <code>AbstractWork</code> instance.</p>
	 *
	 * @param node the flow graph node associated with this work.
	 * @param values the walues associated with this work.
	 */
	protected AbstractWork(FGNode node, Collection values) {
		this.node = node;
		this.values = values;
	}

	/**
	 * <p>Adds a value to the collection of values associated with this work.</p>
	 *
	 * @param o the value to be added.
	 */
	public final synchronized void addValue(Object o) {
		values.add(o);
	}

	/**
	 * <p>Adds a collection of values to the collection of values associated with this work.</p>
	 *
	 * @param values the collection of values to be added.
	 */
	public final synchronized void addValues(Collection values) {
		this.values.addAll(values);
	}

	/**
	 * <p>The actual work that needs to be done when this work is executed should be in this method.</p>
	 *
	 */
	public abstract void execute();

	/**
	 * <p>Associates a flow graph node with this work.</p>
	 *
	 * @param node the flow graph node to be associated.
	 */
	public final void setFGNode(FGNode node) {
		this.node = node;
	}

}// AbstractWork
