
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

import soot.SootClass;
import soot.SootMethod;

import edu.ksu.cis.indus.processing.ProcessingController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class XMLizingController
  extends ProcessingController {
	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static final class LexographicalClassComparator
	  implements Comparator {
		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			SootClass sc1 = (SootClass) o1;
			SootClass sc2 = (SootClass) o2;
			return sc1.getName().compareTo(sc2.getName());
		}
	}


	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static final class LexographicalMethodComparator
	  implements Comparator {
		/**
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			String sig1 = ((SootMethod) o1).getSubSignature();
			String sig2 = ((SootMethod) o2).getSubSignature();
			return sig1.substring(sig1.indexOf(' ')).compareTo(sig2.substring(sig2.indexOf(' ')));
		}
	}

	/**
	 * This implementation returns the classes in alphabetical order as required to assing unique id to entities while
	 * XMLizing.
	 *
	 * @see edu.ksu.cis.indus.processing.ProcessingController#filterClasses(Collection)
	 */
	public Collection filterClasses(final Collection classes) {
		List result = new ArrayList(classes);
		Collections.sort(result, new LexographicalClassComparator());
		return result;
	}

	/**
	 * This implementation returns the methods in alphabetical order as required to assing unique id to entities while
	 * XMLizing.
	 *
	 * @see edu.ksu.cis.indus.processing.ProcessingController#filterMethods(java.util.Collection)
	 */
	protected Collection filterMethods(Collection methods) {
		List result = new ArrayList(methods);
		Collections.sort(result, new LexographicalMethodComparator());
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/11/07 11:13:06  venku
   - used class comparator instead of method comparator. FIXED.
   Revision 1.1  2003/11/06 10:01:25  venku
   - created support for xmlizing Jimple in a customizable manner.
 */
