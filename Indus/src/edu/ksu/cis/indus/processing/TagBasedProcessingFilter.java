
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootClass;
import soot.SootMethod;
import soot.tagkit.Host;


/**
 * This class filters out classes and methods that do not have a tag of the given name.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class TagBasedProcessingFilter
  extends AbstractProcessingFilter {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(TagBasedProcessingFilter.class);

	/**
	 * The name of the tag used to filter out classes and methods.
	 */
	protected final String tagName;

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
	public final Collection filterClasses(final Collection classes) {
		final List _result = new ArrayList();

		for (final Iterator _i = classes.iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (filter(_sc)) {
				_result.add(_sc);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Filtered out: " + CollectionUtils.subtract(classes, _result));
				LOGGER.debug("Retained : " + _result);
			}
		}

		return _result;
	}

	/**
     * Checks if the given host can should be filtered or not.
     * 
     * @param host to be filtered.
     * @return <code>true</code>if <code>host</code> should be filtered; <code>false</code>, otherwise.
     * @pre host != null
     */
    protected boolean filter(final Host host) {
        return !host.hasTag(tagName);
    }

    /**
	 * @see edu.ksu.cis.indus.processing.IProcessingFilter#filterMethods(java.util.Collection)
	 */
	public final Collection filterMethods(final Collection methods) {
		final List _result = new ArrayList();

		for (final Iterator _i = methods.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();

			if (filter(_sm)) {
				_result.add(_sm);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Filtered out: " + CollectionUtils.subtract(methods, _result));
				LOGGER.debug("Retained : " + _result);
			}
		}

		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.

   Revision 1.5  2003/12/05 12:43:22  venku
   - logging.
   Revision 1.4  2003/12/02 11:31:57  venku
   - Added Interfaces for ToolConfiguration and ToolConfigurator.
   - coding convention and formatting.
   Revision 1.3  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.2  2003/12/02 01:30:59  venku
   - coding conventions and formatting.
   Revision 1.1  2003/11/30 01:20:37  venku
   - added a new tag based processing filter.
 */
