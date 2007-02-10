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

import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.lang.builder.ToStringBuilder;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

/**
 * This class can be used to filter classes, methods, and fields guided by a specification-based matcher.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SpecificationBasedFilter
		extends AbstractProcessingFilter {

	/**
	 * The matcher to be used to filter.
	 */
	private SpecificationBasedScopeDefinition matcher;

	/**
	 * The entities being filtered belong to this environment.
	 */
	private IEnvironment system;

	/**
	 * Creates an instance of this class.
	 */
	public SpecificationBasedFilter() {
		super();
	}

	/**
	 * Sets the value of <code>matcher</code>. This method should be called before using this filter.
	 * 
	 * @param theMatcher the new value of <code>matcher</code>.
	 * @pre theMatcher != null
	 */
	public void setMatcher(final SpecificationBasedScopeDefinition theMatcher) {
		this.matcher = theMatcher;
	}

	/**
	 * Sets the value of <code>system</code>. This method should be called before using this filter.
	 * 
	 * @param theSystem the new value of <code>system</code>.
	 * @pre theSystem != null
	 */
	public void setSystem(final IEnvironment theSystem) {
		this.system = theSystem;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("system", this.system).append("matcher",
				this.matcher).toString();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterClasses(java.util.Collection)
	 */
	@Override protected Collection<SootClass> localFilterClasses(final Collection<SootClass> classes) {
		final Collection<SootClass> _result = new ArrayList<SootClass>();
		final Iterator<SootClass> _i = classes.iterator();
		final int _iEnd = classes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _clazz = _i.next();

			if (matcher.isInScope(_clazz, system)) {
				_result.add(_clazz);
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterFields(java.util.Collection)
	 */
	@Override protected Collection<SootField> localFilterFields(final Collection<SootField> fields) {
		final Collection<SootField> _result = new ArrayList<SootField>();
		final Iterator<SootField> _i = fields.iterator();
		final int _iEnd = fields.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootField _field = _i.next();

			if (matcher.isInScope(_field, system)) {
				_result.add(_field);
			}
		}
		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterMethods(java.util.Collection)
	 */
	@Override protected Collection<SootMethod> localFilterMethods(final Collection<SootMethod> methods) {
		final Collection<SootMethod> _result = new ArrayList<SootMethod>();
		final Iterator<SootMethod> _i = methods.iterator();
		final int _iEnd = methods.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootMethod _method = _i.next();

			if (matcher.isInScope(_method, system)) {
				_result.add(_method);
			}
		}
		return _result;
	}
}

// End of File
