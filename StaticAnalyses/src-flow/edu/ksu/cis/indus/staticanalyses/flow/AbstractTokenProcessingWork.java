
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

import edu.ksu.cis.indus.common.datastructures.IWork;

import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;


/**
 * A piece of work that can be processed by <code>WorkList</code>.
 * 
 * <p>
 * Created: Tue Jan 22 02:54:57 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractTokenProcessingWork
  implements IWork {
	/**
	 * The collection of values to be processed.
	 */
	protected ITokens tokens;

	/**
	 * Creates a new AbstractTokenProcessingWork object.
	 *
	 * @param tokenSet to be used by this work object to store the tokens whose flow should be instrumented.
	 *
	 * @pre tokenSet != null
	 */
	protected AbstractTokenProcessingWork(final ITokens tokenSet) {
		tokens = tokenSet;
	}

	/**
	 * Adds a collection of values to the collection of values associated with this work.
	 *
	 * @param tokensToBeProcessed the collection of values to be added for processing.
	 *
	 * @pre valuesToBeProcessed != null
	 */
	public final void addTokens(final ITokens tokensToBeProcessed) {
		tokens.addTokens(tokensToBeProcessed);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.8  2004/04/02 22:01:34  venku
   - node is not integral to the work - deleted.
   Revision 1.7  2003/12/02 09:42:35  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.6  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.5  2003/08/21 10:53:52  venku
   Changed the value collection into a set.
   Revision 1.4  2003/08/18 11:08:00  venku
   Name change for pooling support.
   Revision 1.3  2003/08/17 11:19:13  venku
   Placed the simple SendTokensWork class into a separate file.
   Extended it with work pool support.
   Amended AbstractTokenProcessingWork and WorkList to enable work pool support.
   Revision 1.2  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.9  2003/05/22 22:18:50  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
