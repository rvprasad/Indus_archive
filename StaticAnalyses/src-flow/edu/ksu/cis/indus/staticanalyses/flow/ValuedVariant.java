
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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
	protected IFGNode node;

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
	 * Sets the given node as the flow graph node of this variant.
	 *
	 * @param flowNode the node to be set as the flow graph node of this variant.
	 *
	 * @pre flowNode != null
	 */
	public void setFGNode(final IFGNode flowNode) {
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
	 * Returns the set of values associated with this variant.
	 *
	 * @return the set of values associated with this variant.
	 *
	 * @post result != null
	 */
	public final Collection getValues() {
		return node.getValues();
	}

	/**
	 * Performs nothing.  This will be called after a variant is created and should be implemented by subclasses.
	 */
	public void process() {
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.2  2003/08/16 21:50:39  venku
   Removed ASTVariant as it did not contain any data that was used.
   Concretized AbstractValuedVariant and renamed it to ValuedVariant.
   Ripple effect of the above change in some.
   Spruced up documentation and specification.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.9  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
