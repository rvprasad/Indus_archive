
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNode;
import edu.ksu.cis.indus.staticanalyses.flow.IFGNodeConnector;
import edu.ksu.cis.indus.staticanalyses.flow.MethodVariant;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import soot.ArrayType;

import soot.jimple.ArrayRef;


/**
 * This class is the counter part of <code>FieldAccessExprWork</code>.  It encapsulates the logic to instrument the flow
 * values through array components.
 * 
 * <p>
 * Created: Wed Mar  6 12:31:07 2002.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
class ArrayAccessExprWork
  extends AbstractMemberDataAccessExprWork {
	/**
	 * Creates a new <code>ArrayAccessExprWork</code> instance.
	 *
	 * @param callerMethod the method in which the access occurs.
	 * @param accessContext the context in which the access occurs.
	 * @param accessNode the flow graph node associated with the access expression.
	 * @param connectorToUse the connector to use to connect the ast node to the non-ast node.
	 * @param tokenSet used to store the tokens that trigger the execution of this work peice.
	 *
	 * @pre callerMethod != null and accessProgramPoint != null and accessContext != null and accessNode != null and
	 * 		connectorToUse != null and tokenSet != null
	 */
	public ArrayAccessExprWork(final MethodVariant callerMethod, final Context accessContext, final IFGNode accessNode,
		final IFGNodeConnector connectorToUse, final ITokens tokenSet) {
		super(callerMethod, accessContext, accessNode, connectorToUse, tokenSet);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.AbstractMemberDataAccessExprWork#getFGNodeForMemberData()
	 */
	protected IFGNode getFGNodeForMemberData() {
		final ArrayType _atype = (ArrayType) ((ArrayRef) accessExprBox.getValue()).getBase().getType();
		final FA _fa = caller.getFA();
		return _fa.getArrayVariant(_atype, context).getFGNode();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.11  2004/04/02 21:59:54  venku
   - refactoring.
     - all classes except OFAnalyzer is package private.
     - refactored work class hierarchy.
   Revision 1.10  2004/04/02 09:58:28  venku
   - refactoring.
     - collapsed flow insensitive and sensitive parts into common classes.
     - coding convention
     - documentation.
   Revision 1.9  2003/12/05 02:27:20  venku
   - unnecessary methods and fields were removed. Like
       getCurrentProgramPoint()
       getCurrentStmt()
   - context holds current information and only it must be used
     to retrieve this information.  No auxiliary arguments. FIXED.
   Revision 1.8  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.7  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.6  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.4  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.3  2003/08/16 21:55:14  venku
   Ripple effect of changing FA._FA to FA._fa
   Revision 1.2  2003/08/15 03:39:53  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosened later on in implementaions.
   Renamed a few fields/parameter variables to avoid name confusion.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.6  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
