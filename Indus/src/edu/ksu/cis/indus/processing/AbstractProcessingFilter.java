
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
 * This is an abstract implementation of <code>IProcessingFilter</code> via which class and method filtering will occur in
 * <code>ProcessingController</code>. Default implementation returns the given methods/classes as is.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractProcessingFilter
  implements IProcessingFilter {
	/** 
	 * The successor filter, if any.
	 */
	private IProcessingFilter successor;

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessingFilter#chain(edu.ksu.cis.indus.processing.IProcessingFilter)
	 */
	public final void chain(final IProcessingFilter filter) {
		successor = filter;
	}

	/**
	 * {@inheritDoc}  Default implementation returns the given classes as is.
	 */
	public final Collection filterClasses(final Collection classes) {
		Collection _result = localFilterClasses(classes);

		if (successor != null) {
			_result = successor.filterClasses(_result);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}  Default implementation returns the given fields as is.
	 */
	public final Collection filterFields(final Collection fields) {
		Collection _result = localFilterFields(fields);

		if (successor != null) {
			_result = successor.filterFields(_result);
		}
		return _result;
	}

	/**
	 * {@inheritDoc}  Default implementation returns the given methods as is.
	 */
	public final Collection filterMethods(final Collection methods) {
		Collection _result = localFilterMethods(methods);

		if (successor != null) {
			_result = successor.filterMethods(_result);
		}
		return _result;
	}

	/**
	 * Filter the given classes.
	 *
	 * @param classes to be filtered.
	 *
	 * @return the collection of filtrate classes.
	 *
	 * @pre classes != null and classes.oclIsKindOf(Collection(SootClass))
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post classes.containsAll(result)
	 */
	protected Collection localFilterClasses(final Collection classes) {
		return classes;
	}

	/**
	 * Filter the given fields.
	 *
	 * @param fields to be filtered.
	 *
	 * @return the collection of filtrate fields.
	 *
	 * @pre fields != null and fields.oclIsKindOf(Collection(SootClass))
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post fields.containsAll(result)
	 */
	protected Collection localFilterFields(final Collection fields) {
		return fields;
	}

	/**
	 * Filter the given methods.
	 *
	 * @param methods to be filtered.
	 *
	 * @return the collection of filtrate methods.
	 *
	 * @pre methods != null and methods.oclIsKindOf(Collection(SootClass))
	 * @post result != null and result.oclIsKindOf(Collection(SootClass))
	 * @post methods.containsAll(result)
	 */
	protected Collection localFilterMethods(final Collection methods) {
		return methods;
	}
}

// End of File
