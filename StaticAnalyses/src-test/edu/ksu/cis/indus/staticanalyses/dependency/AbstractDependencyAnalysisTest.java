
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

import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.Iterator;

import junit.framework.TestCase;

import soot.SootClass;
import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
abstract class AbstractDependencyAnalysisTest
  extends TestCase
  implements IDependencyAnalysisTest {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private AbstractDependencyAnalysis da;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IEnvironment env;

	/**
	 * Creates a new AbstractDependencyAnalysisTest object.
	 *
	 * @param analysis DOCUMENT ME!
	 */
	public AbstractDependencyAnalysisTest(final AbstractDependencyAnalysis analysis) {
		da = analysis;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testDependencyAnalysis() {
		for (final Iterator _i = env.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();

				if (_sm.isConcrete()) {
					for (final Iterator _k = _sm.retrieveActiveBody().getUnits().iterator(); _k.hasNext();) {
						final Stmt _unit = (Stmt) _k.next();
						verifyDAFor(_unit, _sm, _sc);
					}
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysisTest#getDA()
	 */
	public AbstractDependencyAnalysis getDA() {
		return da;
	}

	/**
	 * @see IDependencyAnalysisTest#setEnvironment(IEnvironment)
	 */
	public void setEnvironment(final IEnvironment environment) {
		env = environment;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param _unit DOCUMENT ME!
	 * @param _sm DOCUMENT ME!
	 * @param _sc DOCUMENT ME!
	 */
	protected abstract void verifyDAFor(Stmt _unit, SootMethod _sm, SootClass _sc);

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		da = null;
		env = null;
		super.tearDown();
	}
}

/*
   ChangeLog:
   $Log$
 */
