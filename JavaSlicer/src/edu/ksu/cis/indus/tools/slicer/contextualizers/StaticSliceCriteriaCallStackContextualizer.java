
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

package edu.ksu.cis.indus.tools.slicer.contextualizers;

import edu.ksu.cis.indus.processing.Context;

import java.util.ArrayList;
import java.util.Collection;

import soot.SootMethod;


/**
 * This implementation can be used to combine the given set of contexts with the criteria.  This implementation will always
 * provide the same set of given contexts.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class StaticSliceCriteriaCallStackContextualizer
  extends AbstractSliceCriteriaCallStackContextualizer {
	/** 
	 * The calling contexts to be returned.
	 */
	private final Collection contexts;

	/**
	 * Creates a new StaticSliceCriteriaCallStackContextualizer object.
	 *
	 * @param callingContexts to be used.  The call triples in the calling contexts correspond to the caller side, i.e, they
	 * 		  contain caller and call-site information.
	 *
	 * @pre callingContexts != null and callingContext.oclIsKindOf(Collection(Stack(CallTriple)))
	 */
	public StaticSliceCriteriaCallStackContextualizer(final Collection callingContexts) {
		contexts = new ArrayList(callingContexts);
	}

	/**
	 * @see AbstractSliceCriteriaCallStackContextualizer#getCallingContextsForProgramPoint(Context)
	 */
	protected Collection getCallingContextsForProgramPoint(final Context programPoint) {
		return contexts;
	}

	/**
	 * @see AbstractSliceCriteriaCallStackContextualizer#getCallingContextsForThis(SootMethod)
	 */
	protected Collection getCallingContextsForThis(final SootMethod method) {
		return contexts;
	}
}

// End of File
