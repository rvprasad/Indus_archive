/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.flow;

import edu.ksu.cis.indus.interfaces.IPrototype;
import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.OneContextInfoIndex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ValueBox;

/**
 * This class manages indices associated with entities in flow sensitive mode. In reality, it provides the implementation to
 * create new indices.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <E> is the type of the entity that has been indexed.

 */
public class FlowSensitiveIndexManager<E>
		extends AbstractIndexManager<OneContextInfoIndex<E, ValueBox>, E>
		implements IPrototype<FlowSensitiveIndexManager<E>> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(FlowSensitiveIndexManager.class);

	/**
	 * Returns an index corresponding to the given entity and context. The index is dependent on the program point stored in
	 * the context.
	 *
	 * @param o the entity for which the index in required.
	 * @param c the context which captures program point needed to generate the index.
	 * @return the index that uniquely identifies <code>o</code> at the program point captured in <code>c</code>.
	 * @pre o != null and c != null
	 */
	@Override protected OneContextInfoIndex<E, ValueBox> createIndex(final E o, final Context c) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Getting index for " + o + " in " + c);
		}

		return new OneContextInfoIndex<E, ValueBox>(o, c.getProgramPoint());
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IPrototype#getClone(java.lang.Object[])
	 */
	public FlowSensitiveIndexManager<E> getClone(Object... o) {
		return new FlowSensitiveIndexManager<E>();
	}
}

// End of File
