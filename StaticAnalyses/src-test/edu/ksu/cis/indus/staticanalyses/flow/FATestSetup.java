
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import junit.extensions.TestSetup;

import junit.framework.TestSuite;

import soot.ArrayType;
import soot.G;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;


/**
 * This is the setup in which various tests of flow analyses are run.  The classes to be processed during the test can be
 * configured via the command line or via specifying <code>CLASSES_PROPERTY</code>. The syntax for both these options is a
 * space separated list of class names.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FATestSetup
  extends TestSetup {
	/**
	 * The name of the property via which the names of the classes to be used to drive the test is specified.
	 */
	public static final String CLASSES_PROPERTY = "edu.ksu.cis.indus.staticanalyses.flow.FATester.classes";

	/**
	 * The tag used by the flow analysis instance.
	 */
	public static final String TAG_NAME = "FATestSetup:TestTag";

	/** 
	 * The value analyzer used during testing.
	 */
	protected IValueAnalyzer valueAnalyzer;

	/** 
	 * The scene used during testing.
	 */
	protected Scene scene;

	/**
	 * Creates a new FATestSetup object.
	 *
	 * @param test is the test to run in this setup.
	 *
	 * @pre test != null
	 */
	protected FATestSetup(final TestSuite test) {
		super(test);
		valueAnalyzer = OFAnalyzer.getFSOSAnalyzer(FATestSetup.TAG_NAME);
		scene = Scene.v();
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		final String _classes = System.getProperty(CLASSES_PROPERTY);

		if (_classes == null || _classes.length() == 0) {
			throw new RuntimeException(CLASSES_PROPERTY + " property is invalid.  Aborting.");
		}

		final StringBuffer _sb = new StringBuffer(_classes);
		final String[] _j = _sb.toString().split(" ");
		final Collection _rootMethods = new ArrayList();

		for (int _i = _j.length - 1; _i >= 0; _i--) {
			final SootClass _sc = scene.loadClassAndSupport(_j[_i]);

			if (_sc.declaresMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
					  VoidType.v())) {
				final SootMethod _sm =
					_sc.getMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
						VoidType.v());

				if (_sm.isPublic() && _sm.isConcrete()) {
					_rootMethods.add(_sm);
				}
			}
		}

		valueAnalyzer.analyze(scene, _rootMethods);

		final Collection _temp = TestHelper.getTestCasesReachableFromSuite((TestSuite) getTest(), FATester.class);

		for (final Iterator _i = _temp.iterator(); _i.hasNext();) {
			final FATester _tester = (FATester) _i.next();
			_tester.setFA(((AbstractAnalyzer) valueAnalyzer).fa);
			_tester.setFATagName(TAG_NAME);
		}
	}

	/**
	 * @see TestCase#teardown()
	 */
	protected void teardown()
	  throws Exception {
		G.reset();
		valueAnalyzer = null;
		scene = null;
	}
}

/*
   ChangeLog:
   $Log$
 */
