
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

import edu.ksu.cis.indus.staticanalyses.flow.AbstractTokenProcessingWork;
import edu.ksu.cis.indus.staticanalyses.flow.IWorkBagProvider;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class extends the flow graph node by associating a work peice with it.
 * 
 * <p>
 * Created: Tue Jan 22 04:30:32 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
class FGAccessNode
  extends OFAFGNode {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FGAccessNode.class);

	/**
	 * The work associated with this node.
	 *
	 * @invariant work != null
	 */
	private final AbstractTokenProcessingWork work;

	/**
	 * Creates a new <code>FGAccessNode</code> instance.
	 *
	 * @param workPeice the work peice associated with this node.
	 * @param provider provides the workbag into which <code>work</code> will be added.
	 * @param tokenManager that manages the tokens used in the enclosing flow analysis.
	 *
	 * @pre workPeice != null and provider != null and tokenManager != null
	 */
	public FGAccessNode(final AbstractTokenProcessingWork workPeice, final IWorkBagProvider provider,
		final ITokenManager tokenManager) {
		super(provider, tokenManager);
		this.work = workPeice;
	}

	/**
	 * Adds the given tokens to the work peice for processing.
	 *
	 * @param newTokens the collection of values that need to be processed at the given node.
	 *
	 * @pre newTokens != null
	 */
	protected void onNewTokens(final ITokens newTokens) {
		super.onNewTokens(newTokens);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Values: " + newTokens + "\nSuccessors: " + succs);
		}
		work.addTokens(newTokens);
		workbagProvider.getWorkBag().addWork(work);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2004/05/19 05:12:35  venku
   - onNewTokens() is unnecessary accessible to public.  FIXED.
   Revision 1.8  2004/04/16 20:10:38  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
   Revision 1.7  2004/04/02 21:59:54  venku
   - refactoring.
     - all classes except OFAnalyzer is package private.
     - refactored work class hierarchy.
   Revision 1.6  2004/02/26 09:25:59  venku
   - documenation.
   Revision 1.5  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.4  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
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
