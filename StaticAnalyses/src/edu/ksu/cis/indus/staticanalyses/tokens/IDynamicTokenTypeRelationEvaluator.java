
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

package edu.ksu.cis.indus.staticanalyses.tokens;

import java.util.Collection;
import java.util.Map;


/**
 * This interface can be used to retrieve information about dynamic updates to value to type relationship.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IDynamicTokenTypeRelationEvaluator {
	/**
	 * Retrieves new value to type relations for the given values and types.
	 *
	 * @param values for which new relations need to be discovered.
	 * @param types based on which new relations need to be discovered.
	 *
	 * @return a map from values to types that form the new relations.
	 *
	 * @pre values != null and types != null
	 * @post result != null and result.oclIsKindOf(Map(Object, Collection(Object)))
	 * @post values.containsAll(result.keySet())
	 * @post result.values->forall(o | types.containsAll(o))
	 */
	Map getValue2TypesToUpdate(Collection values, Collection types);

	/**
	 * Reset the token manager.
	 */
	void reset();
}

// End of File
