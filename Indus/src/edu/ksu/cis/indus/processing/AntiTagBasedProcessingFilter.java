/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.processing;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.tagkit.Host;
import soot.tagkit.Tag;

/**
 * This class filters out classes and methods that do have a tag of the given name.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class AntiTagBasedProcessingFilter
		extends TagBasedProcessingFilter {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(AntiTagBasedProcessingFilter.class);

	/**
	 * Creates a new TagBasedProcessingFilter object.
	 * 
	 * @param theTagName is the name of the tag used during filtering.
	 * @pre theTagName != null
	 */
	public AntiTagBasedProcessingFilter(final String theTagName) {
		super(theTagName);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.TagBasedProcessingFilter#isFilterate(soot.tagkit.Host)
	 */
	@Override protected boolean isFilterate(final Host host) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Host " + host + " is tagged:" + host.hasTag(tagName) + " [" + tagName + "]");

			if (!host.getTags().isEmpty()) {
				LOGGER.debug("Host " + host + " has the following tags: ");

				for (@SuppressWarnings("unchecked") final Iterator<Tag> _i = host.getTags().iterator(); _i.hasNext();) {
					final Tag _tag = _i.next();

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(_tag.getName());
					}
				}
			}
		}
		return !host.hasTag(tagName);
	}
}

// End of File
