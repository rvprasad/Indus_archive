
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

import edu.ksu.cis.indus.common.datastructures.IWorkBag;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractFGNode;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import java.util.Collection;
import java.util.Collections;


/**
 * This class represents the flow graph node that accumulates objects as their entities would refer to objects at run-time.
 * This is an Object-flow analysis specific implementation.
 * 
 * <p>
 * Created: Thu Jan 31 00:42:34 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
class OFAFGNode
  extends AbstractFGNode {
	/**
	 * The token manager that manages the tokens whose flow is being instrumented.
	 *
	 * @invariant tokenMgr != null
	 */
	private final ITokenManager tokenMgr;

	/**
	 * Creates a new <code>OFAFGNode</code> instance.
	 *
	 * @param wl the worklist associated with the instance of the framework within which this node exists.
	 * @param tokenManager that manages the tokens whose flow is being instrumented.
	 *
	 * @pre wl != null and tokenManager != null
	 */
	public OFAFGNode(final IWorkBag wl, final ITokenManager tokenManager) {
		super(wl, tokenManager.getTokens(Collections.EMPTY_LIST));
		tokenMgr = tokenManager;
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @param o the <code>WorkList</code> to be passed to the constructor of this class.
	 *
	 * @return a new instance of this class parameterized by <code>o</code>.
	 *
	 * @pre o != null and o.oclIsKindOf(WorkList)
	 * @post result != null and result.oclIsKindOf(OFAFGNode)
	 */
	public Object getClone(final Object o) {
		return new OFAFGNode((IWorkBag) o, tokenMgr);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFGNode#injectValue(java.lang.Object)
	 */
	public void injectValue(final Object value) {
		final ITokens _tokens = tokenMgr.getTokens(Collections.singleton(value));
		injectTokens(_tokens);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.12  2004/04/16 20:10:39  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
   Revision 1.11  2004/04/02 21:59:54  venku
   - refactoring.
     - all classes except OFAnalyzer is package private.
     - refactored work class hierarchy.
   Revision 1.10  2004/04/02 09:58:28  venku
   - refactoring.
     - collapsed flow insensitive and sensitive parts into common classes.
     - coding convention
     - documentation.
   Revision 1.9  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.8  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.7  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.6  2003/08/25 11:26:14  venku
   Major bug: Added the original set of values instead of the filtered values. FIXED.
   Revision 1.5  2003/08/17 11:54:19  venku
   Formatting and documentation.
   Revision 1.4  2003/08/17 11:19:13  venku
   Placed the simple SendTokensWork class into a separate file.
   Extended it with work pool support.
   Amended AbstractTokenProcessingWork and WorkList to enable work pool support.
   Revision 1.3  2003/08/17 10:33:03  venku
   WorkList does not inherit from IWorkBag rather contains an instance of IWorkBag.
   Ripple effect of the above change.
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
