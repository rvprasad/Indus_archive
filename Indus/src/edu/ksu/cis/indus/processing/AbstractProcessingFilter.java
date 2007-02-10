/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

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
