
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import org.apache.commons.logging.LogFactory;


/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DependencyXMLizer
  extends DependencyTest {
	/**
	 * Creates a new DependencyXMLizer object.
	 *
	 * @param namesOfClasses DOCUMENT ME!
	 * @param xmlOutputDir DOCUMENT ME!
	 */
	public DependencyXMLizer(final String[] namesOfClasses, final String xmlOutputDir) {
		super(namesOfClasses, xmlOutputDir, null);
		logger = LogFactory.getLog(DependencyXMLizer.class);
	}
}

/*
   ChangeLog:
   $Log$
 */
