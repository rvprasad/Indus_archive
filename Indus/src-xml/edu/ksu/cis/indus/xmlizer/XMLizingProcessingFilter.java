
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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
	private final class LexographicalClassComparator
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
	private final class LexographicalFieldComparator
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
	private final class LexographicalMethodComparator
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
		Collections.sort(_result, new LexographicalClassComparator());
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
		Collections.sort(_result, new LexographicalFieldComparator());
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
		Collections.sort(_result, new LexographicalMethodComparator());
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.8  2004/02/11 09:37:21  venku
   - large refactoring of code based  on testing :-)
   - processing filters can now be chained.
   - ofa xmlizer was implemented.
   - xml-based ofa tester was implemented.

   Revision 1.7  2004/02/09 07:31:21  venku
   - in cases where fields and methods from different classes are
     compared, a fixed ordering may not result. FIXED.
   Revision 1.6  2003/12/14 16:43:45  venku
   - extended ProcessingController to filter fields as well.
   - ripple effect.
   Revision 1.5  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.4  2003/12/02 09:42:24  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.3  2003/12/02 01:30:58  venku
   - coding conventions and formatting.
   Revision 1.2  2003/11/30 09:03:58  venku
   - inner classes are visible when they are not used outside.  FIXED.
   Revision 1.1  2003/11/30 01:17:11  venku
   - renamed CGBasedXMLizingFilter to CGBasedXMLizingProcessingFilter.
   - renamed XMLizingController to XMLizingProcessingFilter.
   - ripple effect.
   Revision 1.6  2003/11/30 00:10:17  venku
   - Major refactoring:
     ProcessingController is more based on the sort it controls.
     The filtering of class is another concern with it's own
     branch in the inheritance tree.  So, the user can tune the
     controller with a filter independent of the sort of processors.
   Revision 1.5  2003/11/17 16:58:19  venku
   - populateDAs() needs to be called from outside the constructor.
   - filterClasses() was called in CGBasedXMLizingController instead of filterMethods. FIXED.
   Revision 1.4  2003/11/17 02:24:00  venku
   - documentation.
   - xmlizers require streams/writers to be provided to them
     rather than they constructing them.
   Revision 1.3  2003/11/12 03:59:41  venku
   - exposed inner classes as static classes.
   Revision 1.2  2003/11/07 11:13:06  venku
   - used class comparator instead of method comparator. FIXED.
   Revision 1.1  2003/11/06 10:01:25  venku
   - created support for xmlizing Jimple in a customizable manner.
 */
