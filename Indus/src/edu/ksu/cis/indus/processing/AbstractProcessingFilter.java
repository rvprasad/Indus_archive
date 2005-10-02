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

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.ValueBox;
import soot.jimple.Stmt;

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
	 * {@inheritDoc} Default implementation returns the given classes as is.
	 */
	public final Collection<SootClass> filterClasses(final Collection<SootClass> classes) {
		Collection<SootClass> _result = localFilterClasses(classes);

		if (successor != null) {
			_result = successor.filterClasses(_result);
		}
		return _result;
	}

	/**
	 * {@inheritDoc} Default implementation returns the given fields as is.
	 */
	public final Collection<SootField> filterFields(final Collection<SootField> fields) {
		Collection<SootField> _result = localFilterFields(fields);

		if (successor != null) {
			_result = successor.filterFields(_result);
		}
		return _result;
	}

	/**
	 * {@inheritDoc} Default implementation returns the given methods as is.
	 */
	public final Collection<SootMethod> filterMethods(final Collection<SootMethod> methods) {
		Collection<SootMethod> _result = localFilterMethods(methods);

		if (successor != null) {
			_result = successor.filterMethods(_result);
		}
		return _result;
	}

	/**
	 * {@inheritDoc} Default implementation returns the given stmts as is.
	 */
	public final Collection<Stmt> filterStmts(final Collection<Stmt> stmts) {
		Collection<Stmt> _result = localFilterStmts(stmts);

		if (successor != null) {
			_result = successor.filterStmts(_result);
		}
		return _result;
	}

	/**
	 * {@inheritDoc} Default implementation returns the given value boxes as is.
	 */
	public final Collection<ValueBox> filterValueBoxes(final Collection<ValueBox> boxes) {
		Collection<ValueBox> _result = localFilterValueBoxes(boxes);

		if (successor != null) {
			_result = successor.filterValueBoxes(_result);
		}
		return _result;
	}

	/**
	 * Filter the given classes.
	 * 
	 * @param classes to be filtered.
	 * @return the collection of filtrate classes.
	 * @post classes.containsAll(result)
	 */
	protected Collection<SootClass> localFilterClasses(final Collection<SootClass> classes) {
		return classes;
	}

	/**
	 * Filter the given fields.
	 * 
	 * @param fields to be filtered.
	 * @return the collection of filtrate fields.
	 * @post fields.containsAll(result)
	 */
	protected Collection<SootField> localFilterFields(final Collection<SootField> fields) {
		return fields;
	}

	/**
	 * Filter the given methods.
	 * 
	 * @param methods to be filtered.
	 * @return the collection of filtrate methods.
	 * @post methods.containsAll(result)
	 */
	protected Collection<SootMethod> localFilterMethods(final Collection<SootMethod> methods) {
		return methods;
	}

	/**
	 * Filter the given statements.
	 * 
	 * @param stmts to be filtered.
	 * @return the collection of filtrate statements.
	 * @post stmts.containsAll(result)
	 */
	protected Collection<Stmt> localFilterStmts(final Collection<Stmt> stmts) {
		return stmts;
	}

	/**
	 * Filter the given value boxes.
	 * 
	 * @param boxes to be filtered.
	 * @return the collection of filtrate value boxes.
	 * @post boxes.containsAll(result)
	 */
	protected Collection<ValueBox> localFilterValueBoxes(final Collection<ValueBox> boxes) {
		return boxes;
	}
}

// End of File
