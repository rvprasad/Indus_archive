
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

package edu.ksu.cis.indus.processing;

import java.util.Collection;


/**
 * This is the interface via which class and method filtering will occur in <code>ProcessingController</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IProcessingFilter {
	/**
	 * Chains the given filter to this filter.  The net effect is that the result from the <code>filter</code> methods are
	 * the entities which passed through this filter and the corresponding filter method of <code>successor</code> as well.
	 *
	 * @param successor is another filter.
	 *
	 * @pre successor != null
	 */
	void chain(final IProcessingFilter successor);

	/**
	 * Filter out classes from the given collection of classes.
	 *
	 * @param classes is the collection to be filtered.
	 *
	 * @return a collection of classes that were not filtered.
	 *
	 * @pre classes.oclIsKindOf(Collection(soot.SootClass)
	 * @post result.oclIsKinfOf(Collection(soot.SootClass))
	 * @post result->forall(o | classes.contains(o))
	 */
	Collection filterClasses(Collection classes);

	/**
	 * Filter out fields from the given collection of methods.
	 *
	 * @param fields is the collection to be filtered.
	 *
	 * @return a collection of fields that were not filtered.
	 *
	 * @pre methods.oclIsKindOf(Collection(soot.SootField)
	 * @post result.oclIsKinfOf(Collection(soot.SootField))
	 * @post result->forall(o | fields.contains(o))
	 */
	Collection filterFields(Collection fields);

	/**
	 * Filter out methods from the given collection of methods.
	 *
	 * @param methods is the collection to be filtered.
	 *
	 * @return a collection of methods that were not filtered.
	 *
	 * @pre methods.oclIsKindOf(Collection(soot.SootMethod)
	 * @post result.oclIsKinfOf(Collection(soot.SootMethod))
	 * @post result->forall(o | methods.contains(o))
	 */
	Collection filterMethods(Collection methods);
}

// End of File
