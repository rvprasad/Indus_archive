
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

import edu.ksu.cis.indus.processing.IProcessingFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import soot.SootClass;
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
  implements IProcessingFilter {
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
		 * DOCUMENT ME!
		 *
		 * @param o1 DOCUMENT ME!
		 * @param o2 DOCUMENT ME!
		 *
		 * @return DOCUMENT ME!
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
	 * This compares <code>SootMethod</code> objects lexographically based on their java signature.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class LexographicalMethodComparator
	  implements Comparator {
		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final Object o1, final Object o2) {
			final String _sig1 = ((SootMethod) o1).getSubSignature();
			final String _sig2 = ((SootMethod) o2).getSubSignature();
			return _sig1.substring(_sig1.indexOf(' ')).compareTo(_sig2.substring(_sig2.indexOf(' ')));
		}
	}

	/**
	 * This implementation returns the classes in alphabetical order as required to assing unique id to entities while
	 * XMLizing.
	 *
	 * @see edu.ksu.cis.indus.interfaces.IFilter#filter(java.lang.Object)
	 */
	public Collection filterClasses(final Collection classes) {
		final List _result = new ArrayList(classes);
		Collections.sort(_result, new LexographicalClassComparator());
		return _result;
	}

	/**
	 * This implementation returns the methods in alphabetical order as required to assing unique id to entities while
	 * XMLizing.
	 *
	 * @see edu.ksu.cis.indus.processing.ProcessingController#filterMethods(java.util.Collection)
	 */
	public Collection filterMethods(final Collection methods) {
		final List _result = new ArrayList(methods);
		Collections.sort(_result, new LexographicalMethodComparator());
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
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
