
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
 * This is a xmlizing controller.  Two different instances of this object will process a set of classes and their methods in
 * the same order.
 * 
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
	 * Singleton instance of class comparator.
	 */
	private static final Comparator CLASS_COMPARATOR = new LexographicalClassComparator();

	/** 
	 * Singleton instance of field comparator.
	 */
	private static final Comparator FIELD_COMPARATOR = new LexographicalFieldComparator();

	/** 
	 * Singleton instance of method comparator.
	 */
	private static final Comparator METHOD_COMPARATOR = new LexographicalMethodComparator();

	/**
	 * This compares <code>SootClass</code> objects lexographically based on their fully qualified java names.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static final class LexographicalClassComparator
	  implements Comparator {
		/**
		 * Compares the given classes based on their name.
		 *
		 * @param o1 is one of the class to be compared.
		 * @param o2 is the other class to be compared.
		 *
		 * @return -1,0,1 if the name of <code>o1</code> lexically precedes, is the same, or lexically succeeds the name of
		 * 		   <code>o2</code>.
		 *
		 * @pre o1.oclIsKindOf(SootClass) and o2.oclIsKindOf(SootClass)
		 */
		public int compare(final Object o1, final Object o2) {
			final SootClass _sc1 = (SootClass) o1;
			final SootClass _sc2 = (SootClass) o2;
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
	  implements Comparator {
		/**
		 * Compares the given fields based on their name.
		 *
		 * @param o1 is one of the method to be compared.
		 * @param o2 is the other method to be compared.
		 *
		 * @return -1,0,1 if the name of <code>o1</code> lexically precedes, is the same, or lexically succeeds the name of
		 * 		   <code>o2</code>.
		 *
		 * @pre o1.oclIsKindOf(SootMethod) and o2.oclIsKindOf(SootMethod)
		 */
		public int compare(final Object o1, final Object o2) {
			final String _sig1 = ((SootField) o1).getSignature();
			final String _sig2 = ((SootField) o2).getSignature();
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
	  implements Comparator {
		/**
		 * Compares the given methods based on their name.
		 *
		 * @param o1 is one of the method to be compared.
		 * @param o2 is the other method to be compared.
		 *
		 * @return -1,0,1 if the name of <code>o1</code> lexically precedes, is the same, or lexically succeeds the name of
		 * 		   <code>o2</code>.
		 *
		 * @pre o1.oclIsKindOf(SootMethod) and o2.oclIsKindOf(SootMethod)
		 */
		public int compare(final Object o1, final Object o2) {
			final String _sig1 = ((SootMethod) o1).getSignature();
			final String _sig2 = ((SootMethod) o2).getSignature();
			return _sig1.compareTo(_sig2);
		}
	}

	/**
	 * This implementation returns the classes in alphabetical order as required to assing unique id to entities while
	 * XMLizing.
	 *
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterClasses(java.util.Collection)
	 */
	protected Collection localFilterClasses(final Collection classes) {
		final List _result = new ArrayList(classes);
		Collections.sort(_result, CLASS_COMPARATOR);
		return _result;
	}

	/**
	 * This implementation returns the fields in alphabetical order as required to assing unique id to entities while
	 * XMLizing.
	 *
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterFields(java.util.Collection)
	 */
	protected Collection localFilterFields(final Collection fields) {
		final List _result = new ArrayList(fields);
		Collections.sort(_result, FIELD_COMPARATOR);
		return _result;
	}

	/**
	 * This implementation returns the methods in alphabetical order as required to assing unique id to entities while
	 * XMLizing.
	 *
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterMethods(java.util.Collection)
	 */
	protected Collection localFilterMethods(final Collection methods) {
		final List _result = new ArrayList(methods);
		Collections.sort(_result, METHOD_COMPARATOR);
		return _result;
	}
}

// End of File
