
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

package edu.ksu.cis.indus;

import edu.ksu.cis.indus.common.graph.FIFOWorkBag;
import edu.ksu.cis.indus.common.graph.IWorkBag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * This is a class with helper methods that can be used in conjunction with JUnit.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class TestHelper {
	///CLOVER:OFF

	/**
	 * Creates a new TestHelper object.
	 */
	private TestHelper() {
	}

	///CLOVER:ON

	/**
	 * Retrieves all instances of <code>TestCase</code> of type <code>type</code> reachable from the given test suite.
	 *
	 * @param suite is the test suite to be drilled.
	 * @param type of instance of the <code>TestCase</code> the user is interested in.
	 *
	 * @return a collection of <code>TestCase</code> objects of type <code>type</code>.
	 *
	 * @pre suite != null and type != null
	 * @post result != null
	 * @post result->forall(o | o.oclIsKindOf(junit.framework.TestCase) and type.isInstance(o))
	 */
	public static Collection getTestCasesReachableFromSuite(final TestSuite suite, final Class type) {
		final Collection _result = new HashSet();
		final Collection _processed = new HashSet();
		final IWorkBag _workList = new FIFOWorkBag();
		_workList.addAllWorkNoDuplicates(Collections.list(suite.tests()));

		while (_workList.hasWork()) {
			final Test _o = (Test) _workList.getWork();

			if (_processed.contains(_o)) {
				continue;
			}
			_processed.add(_o);

			if (type.isInstance(_o)) {
				_result.add(_o);
			} else if (_o instanceof TestSuite) {
				_workList.addAllWorkNoDuplicates(Collections.list(((TestSuite) _o).tests()));
			}
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
 */
