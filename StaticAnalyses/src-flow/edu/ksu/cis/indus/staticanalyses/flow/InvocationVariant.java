
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import soot.SootClass;


/**
 * This class represents a variant for a method invocation expression.  It captures node information regarding return value
 * of the expression and the exceptions thrown by the expression.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class InvocationVariant
  extends ValuedVariant {
	/**
	 * This maps exception classes to nodes.
	 *
	 * @invariant exception2Node != null
	 */
	private final Map exception2node;

	/**
	 * Creates a new InvocationVariant object.
	 *
	 * @param returnNode is the node associated with the return value of <code>e</code>.
	 * @param thrownExceptions2node is the map from class of exceptions thrown by <code>e</code>to nodes.
	 *
	 * @pre returnNode != null and thrownExceptions2node != null
	 */
	protected InvocationVariant(final IFGNode returnNode, final Map thrownExceptions2node) {
		super(returnNode);

		if (thrownExceptions2node.isEmpty()) {
			this.exception2node = Collections.EMPTY_MAP;
		} else {
			this.exception2node = new HashMap(thrownExceptions2node);
		}
	}

	/**
	 * Returns the node associated with given exception.
	 *
	 * @param exception is the class of the exception thrown by the expression associated with this variant.
	 *
	 * @return the node associated the exception thrown by the expression associated with this variant.
	 *
	 * @pre exception != null
	 */
	public IFGNode queryThrowNode(final SootClass exception) {
		return (IFGNode) exception2node.get(exception);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/16 21:50:51  venku
   Removed ASTVariant as it did not contain any data that was used.
   Concretized AbstractValuedVariant and renamed it to ValuedVariant.
   Ripple effect of the above change in some.
   Spruced up documentation and specification.
   Revision 1.2  2003/08/16 02:50:22  venku
   Spruced up documentation and specification.
   Moved onNewXXX() methods from IFGNode to AbstractFGNode.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.3  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
