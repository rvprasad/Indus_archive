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

package edu.ksu.cis.indus.staticanalyses.callgraphs;

import edu.ksu.cis.indus.common.collections.SetUtils;
import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.flow.FATestSetup;
import edu.ksu.cis.indus.staticanalyses.flow.IFATest;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import soot.SootClass;
import soot.SootMethod;

/**
 * This class tests information calculated by
 * <code>edu.ksu.cis.indus.staticanalyses.flow.instances.valueAnalyzer.processors.CallGraph</code>.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class OFABasedCallGraphTest
		extends CallGraphTest
		implements IFATest {

	/**
	 * The object flow analysis used to construct the call graph.
	 */
	private OFAnalyzer ofa;

	/**
	 * Tests <code>isReachable</code>.
	 */
	public void localtestIsReachable() {
		final Collection _reachables = cgi.getReachableMethods();
		final Collection _heads = cgi.getEntryMethods();

		for (final Iterator _i = ofa.getEnvironment().getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();
				assertEquals(cgi.isReachable(_sm), _reachables.contains(_sm));

				if (cgi.isReachable(_sm)) {
					boolean _t = false;

					for (final Iterator _k = _heads.iterator(); _k.hasNext();) {
						_t |= cg.isReachable(cg.getNode(_k.next()), cg.getNode(_sm), true);
					}
					assertTrue(_t || _heads.contains(_sm));
				}
			}
		}
	}

	/**
	 * Sets the instance of OFAnalyzer to be used during testing.
	 * 
	 * @param valueAnalyzer to be used by the test.
	 * @pre valueAnalyzer != null
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFATest#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer valueAnalyzer) {
		ofa = (OFAnalyzer) valueAnalyzer;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFATest#setFATagName(java.lang.String)
	 */
	public void setFATagName(final String tagName) {
		// does nothing
	}

	/**
	 * Tests the tags on the reachable methods based on tags used during object flow analysis.
	 */
	public void testTagsOnReachableMethods() {
		final Context _ctxt = new Context();
		final Collection _reachables = cgi.getReachableMethods();
		assertNotNull(_reachables);

		for (final Iterator _i = _reachables.iterator(); _i.hasNext();) {
			final SootMethod _o = (SootMethod) _i.next();
			assertTrue(_o.hasTag(FATestSetup.TAG_NAME));
			assertTrue(_o.getDeclaringClass().hasTag(FATestSetup.TAG_NAME));

			if (!_o.isStatic()) {
				_ctxt.setRootMethod(_o);
				assertNotNull(ofa.getValuesForThis(_ctxt));
			}
		}

		Collection _methods = new HashSet();

		for (final Iterator _i = ofa.getEnvironment().getClasses().iterator(); _i.hasNext();) {
			_methods.addAll(((SootClass) _i.next()).getMethods());
		}
		_methods = SetUtils.difference(_methods, _reachables);

		for (final Iterator _i = _methods.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();

			if (!_sm.isAbstract()) {
				assertFalse(_sm.hasTag(FATestSetup.TAG_NAME));
			}
		}
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		ofa = null;
		super.tearDown();
	}
}

// End of File
