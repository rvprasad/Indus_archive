
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

package edu.ksu.cis.indus.tools;

import sun.util.logging.resources.logging;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
public abstract class ToolConfigurationCollection {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ToolConfigurationCollection.class);

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final List configurations = new ArrayList();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private ToolConfiguration active;

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param tc DOCUMENT ME!
	 */
	public final void setActiveToolConfiguration(final ToolConfiguration tc) {
		if (configurations.contains(tc)) {
			active = tc;
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	public final ToolConfiguration getActiveToolConfiguration() {
		if (active == null) {
			active = (ToolConfiguration) configurations.get(0);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Selecting the first configuration as active configuration.");
			}

			if (active == null) {
				throw new RuntimeException("There are no configurations.");
			}
		}
		return active;
	}

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param tc DOCUMENT ME!
	 */
	public final void addToolConfiguration(final ToolConfiguration tc) {
		if (tc == null) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Null configurations are not supported.");
			}
		} else {
			if (configurations.contains(tc)) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("The given configuration exists.");
				}
			} else {
				configurations.add(tc);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param name DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	final ToolConfiguration getToolConfiguration(String name) {
		ToolConfiguration result = null;

		for (Iterator i = configurations.iterator(); i.hasNext();) {
			result = (ToolConfiguration) i.next();

			if (result.NAME.equals(name)) {
				break;
			}
			result = null;
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
 */
