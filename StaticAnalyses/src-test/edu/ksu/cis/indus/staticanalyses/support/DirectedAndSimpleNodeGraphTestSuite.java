
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.support;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * This is the test suite for <code>DirectedGraph</code> and <code>SimpleNodeGraph</code>.  Any new test cases should add to
 * this suite.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class DirectedAndSimpleNodeGraphTestSuite {
	/**
	 * Creates a new DirectedAndSimpleNodeGraphTestSuite object.
	 */
	private DirectedAndSimpleNodeGraphTestSuite() {
	}

	/**
	 * Creates the test suite.
	 *
	 * @return the created test suite.
	 *
	 * @post result != null
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.support");

		//$JUnit-BEGIN$
		suite.addTestSuite(DirectedAndSimpleNodeGraphTest1.class);
		suite.addTestSuite(DirectedAndSimpleNodeGraphTest2.class);
		suite.addTestSuite(DirectedAndSimpleNodeGraphTest3.class);
		//$JUnit-END$
		return suite;
	}
}

/*
   ChangeLog:
   $Log$
 */
