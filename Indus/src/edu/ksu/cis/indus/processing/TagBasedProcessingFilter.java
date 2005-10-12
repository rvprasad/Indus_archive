
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

import edu.ksu.cis.indus.common.collections.SetUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.SootClass;
import soot.SootField;
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
	@Override public String toString() {
		return new ToStringBuilder(this).appendSuper(super.toString()).append("tagName", this.tagName).toString();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterClasses(java.util.Collection)
	 */
	@Override protected final Collection<SootClass> localFilterClasses(final Collection<SootClass> classes) {
		return filter(classes);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterFields(java.util.Collection)
	 */
	@Override protected final Collection<SootField> localFilterFields(final Collection<SootField> fields) {
		return filter(fields);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.AbstractProcessingFilter#localFilterMethods(java.util.Collection)
	 */
	@Override protected final Collection<SootMethod> localFilterMethods(final Collection<SootMethod> methods) {
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
	 * @param <T> the type of the hosts.
	 *
	 * @param hosts is the collection of hosts to be filterd.
	 *
	 * @return a collection of hosts that passed through the filter.
	 *
	 * @pre hosts != null
	 * @post result != null and hosts.containsAll(result)
	 * @post result->foreach(o | isFilterate(o))
	 */
	private <T extends Host> Collection<T> filter(final Collection<T> hosts) {
		final List<T> _result = new ArrayList<T>();

		for (final Iterator<T> _i = hosts.iterator(); _i.hasNext();) {
			final T _e = _i.next();

			if (isFilterate(_e)) {
				_result.add(_e);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Filtered out: " + SetUtils.difference(hosts, _result));
				LOGGER.debug("Filtrate : " + _result);
			}
		}
		return _result;
	}
}

// End of File
