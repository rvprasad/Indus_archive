
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */

package edu.ksu.cis.indus.staticanalyses.flow;

import java.util.Collection;


/**
 * This class represents the variants of entities associated with AST nodes and fields.  This class should be extended as
 * required for such entities.
 * 
 * <p>
 * Created: Tue Jan 22 15:44:48 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ValuedVariant
  implements IVariant {
	/** 
	 * <p>
	 * The flow graph node associated with this variant.
	 * </p>
	 */
	protected final IFGNode node;

	/**
	 * Creates a new <code>ValuedVariant</code> instance.
	 *
	 * @param flowNode the flow graph node associated with this variant.
	 *
	 * @pre flowNode != null
	 */
	ValuedVariant(final IFGNode flowNode) {
		this.node = flowNode;
	}

	/**
	 * Returns the flow graph node associated with this node.
	 *
	 * @return the flow graph node associated with this node.
	 *
	 * @post result != null
	 */
	public IFGNode getFGNode() {
		return node;
	}

	/**
	 * Performs nothing.  This will be called after a variant is created and should be implemented by subclasses.
	 */
	public void process() {
	}
}

// End of File
