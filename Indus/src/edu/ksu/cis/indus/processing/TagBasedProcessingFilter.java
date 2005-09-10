
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

package edu.ksu.cis.indus.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger LOGGER = LoggerFactory.getLogger(TagBasedProcessingFilter.class);

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
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("tagName", this.tagName).toString();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterClasses(java.util.Collection)
	 */
	protected final Collection localFilterClasses(final Collection classes) {
		return filter(classes);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterFields(java.util.Collection)
	 */
	protected final Collection localFilterFields(final Collection fields) {
		return filter(fields);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterMethods(java.util.Collection)
	 */
	protected final Collection localFilterMethods(final Collection methods) {
		return filter(methods);
	}

	/**
	 * Checks if the given host is the filtrate or not.
	 *
	 * @param host to be filtered.
	 *
	 * @return <code>true</code>if <code>host</code> should be filtered; <code>false</code>, otherwise.
	 *
	 * @pre host != null
	 */
	protected boolean isFilterate(final Host host) {
		return host.hasTag(tagName);
	}

	/**
	 * Filters the given hosts based on the return value of <code>isFilterate</code> method.
	 *
	 * @param hosts is the collection of hosts to be filterd.
	 *
	 * @return a collection of hosts that passed through the filter.
	 *
	 * @pre hosts != null and hosts.oclIsKindOf(Collection(Host))
	 * @post result != null and hosts.containsAll(result)
	 * @post result->foreach(o | isFilterate(o))
	 */
	private Collection filter(final Collection hosts) {
		final List _result = new ArrayList();

		for (final Iterator _i = hosts.iterator(); _i.hasNext();) {
			final Host _sc = (Host) _i.next();

			if (isFilterate(_sc)) {
				_result.add(_sc);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Filtered out: " + CollectionUtils.subtract(hosts, _result));
				LOGGER.debug("Filtrate : " + _result);
			}
		}
		return _result;
	}
}

// End of File
