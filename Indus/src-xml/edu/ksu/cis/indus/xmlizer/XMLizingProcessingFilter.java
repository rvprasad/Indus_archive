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

package edu.ksu.cis.indus.xmlizer;

import edu.ksu.cis.indus.processing.AbstractProcessingFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;

/**
 * This is a xmlizing controller. Two different instances of this object will process a set of classes and their methods in
 * the same order.
 * <p>
 * In this implementation, the ordering in the returned collection may not respect the ordering of the given collection.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class XMLizingProcessingFilter
		extends AbstractProcessingFilter {

	/**
	 * This compares <code>SootClass</code> objects lexographically based on their fully qualified java names.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static final class LexographicalClassComparator
			implements Comparator<SootClass> {

		/**
		 * Creates an instance of this class.
		 */
		public LexographicalClassComparator() {
			super();
		}

		/**
		 * Compares the given classes based on their name.
		 * 
		 * @param o1 is one of the class to be compared.
		 * @param o2 is the other class to be compared.
		 * @return -1,0,1 if the name of <code>o1</code> lexically precedes, is the same, or lexically succeeds the name of
		 *         <code>o2</code>.
		 * @pre o1.oclIsKindOf(SootClass) and o2.oclIsKindOf(SootClass)
		 */
		public int compare(final SootClass o1, final SootClass o2) {
			final SootClass _sc1 = o1;
			final SootClass _sc2 = o2;
			return _sc1.getName().compareTo(_sc2.getName());
		}
	}

	/**
	 * This compares <code>SootField</code> objects lexographically based on their java signature.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static final class LexographicalFieldComparator
			implements Comparator<SootField> {

		/**
		 * Creates an instance of this class.
		 */
		public LexographicalFieldComparator() {
			super();
		}

		/**
		 * Compares the given fields based on their name.
		 * 
		 * @param o1 is one of the method to be compared.
		 * @param o2 is the other method to be compared.
		 * @return -1,0,1 if the name of <code>o1</code> lexically precedes, is the same, or lexically succeeds the name of
		 *         <code>o2</code>.
		 * @pre o1.oclIsKindOf(SootMethod) and o2.oclIsKindOf(SootMethod)
		 */
		public int compare(final SootField o1, final SootField o2) {
			final String _sig1 = o1.getSignature();
			final String _sig2 = o2.getSignature();
			return _sig1.compareTo(_sig2);
		}
	}

	/**
	 * This compares <code>SootMethod</code> objects lexographically based on their java signature.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static final class LexographicalMethodComparator
			implements Comparator<SootMethod> {

		/**
		 * Creates an instance of this class.
		 */
		public LexographicalMethodComparator() {
			super();
		}

		/**
		 * Compares the given methods based on their name.
		 * 
		 * @param o1 is one of the method to be compared.
		 * @param o2 is the other method to be compared.
		 * @return -1,0,1 if the name of <code>o1</code> lexically precedes, is the same, or lexically succeeds the name of
		 *         <code>o2</code>.
		 * @pre o1.oclIsKindOf(SootMethod) and o2.oclIsKindOf(SootMethod)
		 */
		public int compare(final SootMethod o1, final SootMethod o2) {
			final String _sig1 = o1.getSignature();
			final String _sig2 = o2.getSignature();
			return _sig1.compareTo(_sig2);
		}
	}

	/**
	 * Singleton instance of class comparator.
	 */
	private static final Comparator<SootClass> CLASS_COMPARATOR = new LexographicalClassComparator();

	/**
	 * Singleton instance of field comparator.
	 */
	private static final Comparator<SootField> FIELD_COMPARATOR = new LexographicalFieldComparator();

	/**
	 * Singleton instance of method comparator.
	 */
	private static final Comparator<SootMethod> METHOD_COMPARATOR = new LexographicalMethodComparator();

	/**
	 * Creates an instance of this class.
	 */
	public XMLizingProcessingFilter() {
		super();
	}

	/**
	 * This implementation returns the classes in alphabetical order as required to assing unique id to entities while
	 * XMLizing.
	 * 
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterClasses(java.util.Collection)
	 */
	@Override protected Collection<SootClass> localFilterClasses(final Collection<SootClass> classes) {
		final List<SootClass> _result = new ArrayList<SootClass>(classes);
		Collections.sort(_result, CLASS_COMPARATOR);
		return _result;
	}

	/**
	 * This implementation returns the fields in alphabetical order as required to assing unique id to entities while
	 * XMLizing.
	 * 
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterFields(java.util.Collection)
	 */
	@Override protected Collection<SootField> localFilterFields(final Collection<SootField> fields) {
		final List<SootField> _result = new ArrayList<SootField>(fields);
		Collections.sort(_result, FIELD_COMPARATOR);
		return _result;
	}

	/**
	 * This implementation returns the methods in alphabetical order as required to assing unique id to entities while
	 * XMLizing.
	 * 
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterMethods(java.util.Collection)
	 */
	@Override protected Collection<SootMethod> localFilterMethods(final Collection<SootMethod> methods) {
		final List<SootMethod> _result = new ArrayList<SootMethod>(methods);
		Collections.sort(_result, METHOD_COMPARATOR);
		return _result;
	}
}

// End of File
