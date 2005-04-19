
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

package edu.ksu.cis.indus.interfaces;

import edu.ksu.cis.indus.processing.Context;

import java.util.Collection;
import java.util.Collections;


/**
 * This is the interface to retrieve calling contexts based on program points.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ICallingContextRetriever
  extends IIDBasedInfoManagement {
	/** 
	 * This is a collection of calling contexts that imply all call chains should be followed.
	 */
	Collection NULL_CONTEXTS = Collections.singleton(null);

	/** 
	 * This retrives null contexts.
	 */
	ICallingContextRetriever NULL_CONTEXT_RETRIEVER =
		new ICallingContextRetriever() {
			public Collection getCallingContextsForProgramPoint(final Context programPointContext) {
				return NULL_CONTEXTS;
			}

			public Collection getCallingContextsForThis(final Context methodContext) {
				return NULL_CONTEXTS;
			}

			public Object setInfoFor(final Object infoID, final Object info) {
				return null;
			}

			public void clearInfo() {
			}

			public Object removeInfo(final Object infoID) {
				return null;
			}
		};

	/** 
	 * This identifies the entity at the program point.
	 */
	Object SRC_ENTITY = "Entity at the program point";

	/** 
	 * This identifies the method enclosing the program point.
	 */
	Object SRC_METHOD = "Method enclosing the program point";

	/**
	 * Retrieves the calling contexts for the program point specified in the given context
	 *
	 * @param programPointContext of interest.
	 *
	 * @return a collection of calling contexts for the given program point.
	 *
	 * @pre programPointContext != null
	 * @post result != null and result.oclIsKindOf(Collection(Stack(CallTriple)))
	 */
	Collection getCallingContextsForProgramPoint(Context programPointContext);

	/**
	 * Retrieves the calling contexts for the method specified in the given context based on it's "this" variable.
	 *
	 * @param methodContext of interest.
	 *
	 * @return a collection of calling contexts for the given method based on it's "this" variable.
	 *
	 * @pre methodContext != null
	 * @post result != null and result.oclIsKindOf(Collection(Stack(CallTriple)))
	 */
	Collection getCallingContextsForThis(Context methodContext);
}

// End of File
