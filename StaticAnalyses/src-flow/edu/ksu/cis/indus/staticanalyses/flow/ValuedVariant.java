
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.annotations.Empty;

/**
 * This class represents the variants of entities associated with AST nodes and fields.  This class should be extended as
 * required for such entities.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <N> is the type of the summary node in the flow analysis.
 */
public class ValuedVariant <N extends IFGNode<?, ?, N>>
  implements IVariant {
	/** 
	 * <p>
	 * The flow graph node associated with this variant.
	 * </p>
	 */
	protected final N node;

	/**
	 * Creates a new <code>ValuedVariant</code> instance.
	 *
	 * @param flowNode the flow graph node associated with this variant.
	 *
	 * @pre flowNode != null
	 */
	ValuedVariant(final N flowNode) {
		this.node = flowNode;
	}

	/**
	 * Returns the flow graph node associated with this node.
	 *
	 * @return the flow graph node associated with this node.
	 *
	 * @post result != null
	 */
	public N getFGNode() {
		return node;
	}

	/**
	 * Performs nothing.  This will be called after a variant is created and should be implemented by subclasses.
	 */
	@Empty public void process() {
		// does nothing
	}
}

// End of File
