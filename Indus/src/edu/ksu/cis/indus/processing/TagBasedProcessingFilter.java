
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

package edu.ksu.cis.indus.processing;

import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * This class filters out classes and methods that do not have a tag of the given name.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class TagBasedProcessingFilter
  implements IProcessingFilter {
	/**
	 * The name of the tag used to filter out classes and methods.
	 */
	private final String tagName;

	/**
	 * Creates a new TagBasedProcessingFilter object.
	 *
	 * @param theTagName is the name of the tag used during filtering.
	 *
	 * @pre theTagName != null
	 */
	public TagBasedProcessingFilter(final String theTagName) {
		tagName = theTagName;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessingFilter#filterClasses(java.util.Collection)
	 */
	public Collection filterClasses(final Collection classes) {
		List result = new ArrayList();

		for (Iterator i = classes.iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();

			if (sc.hasTag(tagName)) {
				result.add(sc);
			}
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessingFilter#filterMethods(java.util.Collection)
	 */
	public Collection filterMethods(final Collection methods) {
		List result = new ArrayList();

		for (Iterator i = methods.iterator(); i.hasNext();) {
			SootMethod sc = (SootMethod) i.next();

			if (sc.hasTag(tagName)) {
				result.add(sc);
			}
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/11/30 01:20:37  venku
   - added a new tag based processing filter.
 */
