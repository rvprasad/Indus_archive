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

package edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.staticanalyses.Constants;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.OneContextInfoIndex;

import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.RefType;
import soot.Type;
import soot.Value;

/**
 * This class manages indices associated with fields and array components in allocation-site sensitive mode. In reality, it
 * provides the implementation to create new indices. Created: Tue Mar 5 14:08:18 2002.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <E> is the type of the entity that has been indexed.
 */
public class AllocationSiteSensitiveIndexManager<E>
		extends AbstractIndexManager<OneContextInfoIndex<E, Object>, E> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AllocationSiteSensitiveIndexManager.class);

	/**
	 * The pattern that defines an object-based scope in which value flow through fields is object sensitive.
	 */
	private final Pattern pattern;

	/**
	 * This indicates if value flow through arrays should be object sensitive.
	 */
	private final boolean objectSensitiveArrayTracking;

	/**
	 * Creates an instance of this class.
	 */
	public AllocationSiteSensitiveIndexManager() {
		final String _p = Constants.getObjectSensitivityScopePattern();

		if (_p != null) {
			pattern = Pattern.compile(_p);
		} else {
			pattern = null;
		}

		objectSensitiveArrayTracking = Constants.getObjectSensitiveArrayTracking();
	}

	/**
	 * Returns an index corresponding to the given entity and context.
	 * 
	 * @param o the entity for which the index in required. Although it is not enforced, this should be of type
	 *            <code>FielRef</code> or <code>ArrayRef</code>.
	 * @param c the context in which information pertaining to <code>o</code> needs to be captured.
	 * @return the index that uniquely identifies <code>o</code> in context, <code>c</code>.
	 * @pre o != null and c != null and
	 *      c.oclIsTypeOf(edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.allocation.AllocationContext)
	 */
	@Override protected OneContextInfoIndex<E, Object> createIndex(final E o, final Context c) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Getting index for " + o + " in " + c);
		}

		final AllocationContext _ctxt = (AllocationContext) c;
		final Type _type = ((Value) _ctxt.allocationSite).getType();
		if ((_type instanceof RefType && pattern != null && pattern.matcher(((RefType) _type).getClassName()).matches())
				|| (objectSensitiveArrayTracking && _type instanceof ArrayType)) {
			return new OneContextInfoIndex<E, Object>(o, _ctxt.getAllocationSite());
		}
		return new OneContextInfoIndex<E, Object>(o, null);
	}
}

// End of File
