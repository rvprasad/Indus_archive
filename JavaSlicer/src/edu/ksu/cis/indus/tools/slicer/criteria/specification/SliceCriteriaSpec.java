
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

package edu.ksu.cis.indus.tools.slicer.criteria.specification;

import edu.ksu.cis.indus.slicer.ISliceCriterion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Scene;


/**
 * This class is the criteria specification.  <i>This is intended for internal use only.  Clients should not depend on or use
 * this class.</i>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class SliceCriteriaSpec {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SliceCriteriaSpec.class);

	/**
	 * The collection of criterion specification.
	 */
	private List<SliceCriterionSpec> criteria;

	/**
	 * Creates an instance of this class.
	 */
	private SliceCriteriaSpec() {
		criteria = createCriteriaContainer();
	}

	/**
	 * Generates criteria relative on the given scene.
	 *
	 * @param scene relative to which the criteria will be generated.
	 *
	 * @return the collection of slice criteria.
	 *
	 * @pre scene != null
	 * @post result != null
	 */
	public Collection<ISliceCriterion> getCriteria(final Scene scene) {
		final Collection<ISliceCriterion> _result = new HashSet<ISliceCriterion>();

		for (final Iterator<SliceCriterionSpec> _i = criteria.iterator(); _i.hasNext();) {
			final SliceCriterionSpec _critSpec = _i.next();

			try {
				_result.addAll(_critSpec.getCriteria(scene));
			} catch (final MissingResourceException _e) {
				LOGGER.error("Discarding criteria - " + _critSpec, _e);
			}
		}
		return _result;
	}

	/**
	 * Retrieves the criteria specification for the given collection of slicing criteria.
	 *
	 * @param criteria is a collection of slice criterion.
	 *
	 * @return the criteria specification.
	 *
	 * @pre criteria != null
	 * @post result != null
	 */
	static SliceCriteriaSpec getSliceCriteriaSpec(final Collection<ISliceCriterion> criteria) {
		final List<SliceCriterionSpec> _temp = new ArrayList<SliceCriterionSpec>();

		for (final Iterator<ISliceCriterion> _i = criteria.iterator(); _i.hasNext();) {
			final ISliceCriterion _criterion = _i.next();
			_temp.addAll(SliceCriterionSpec.getCriterionSpec(_criterion));
		}

		final SliceCriteriaSpec _result = new SliceCriteriaSpec();
		_result.criteria = _temp;
		return _result;
	}

	/**
	 * Creates an instance of the container for criteria.  This is primarily used during deserialization.
	 *
	 * @return the instance of the container.
	 *
	 * @post result != null
	 */
	private static List<SliceCriterionSpec> createCriteriaContainer() {
		return new ArrayList<SliceCriterionSpec>();
	}
}

// End of File
