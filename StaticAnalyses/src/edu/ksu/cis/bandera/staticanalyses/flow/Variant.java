package edu.ksu.cis.bandera.staticanalyses.flow;

//Variant.java

/**
 * <p>A marker interface to be implemented by all variants used in BFA framework.</p>
 *
 * <p>Created: Tue Jan 22 13:05:25 2002</p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */

interface Variant {
	/**
	 * Performs any required post processing after the variant has been instantiated.  This method will be called by the
	 * framework.
	 */
	void process();
}// Variant
