package edu.ksu.cis.bandera.staticanalyses.flow;

//Prototype.java

/**
 * <p>This interface helps realize the <i>Prototype</i> design pattern as defined in the Gang of Four book. It provides the
 * methods via which concrete object can be created from a prototype object.  The default implementation for these methods
 * should raise <code>UnsupportedOperationException</code>.</p>
 *
 * <p>Created: Sun Jan 27 18:04:58 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

public interface Prototype {

	/**
	 * <p>Creates a concrete object from this prototype object.  Usually, it is a duplicate of this prototype object.</p>
	 *
	 * @return concrete object based on this prototype object.
	 */
	public Object prototype();

	/**
	 * <p>Creates a concrete object from this prototype object.  The concrete object can be parameterized by the information
	 * in <code>o</code>.</p>
	 *
	 * @param o object containing the information to parameterize the concrete object.
	 * @return concrete object based on this prototype object.
	 */
	public Object prototype(Object o);

}// Prototype
