
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

import edu.ksu.cis.indus.IndusTestCase;
import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.Iterator;

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
public abstract class AbstractDependencyAnalysisTest
  extends IndusTestCase
  implements IDependencyAnalysisTest {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IDependencyAnalysis da;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IEnvironment env;

	/**
	 * DOCUMENT ME!
	 */
	protected AbstractDependencyAnalysisTest() {
		super();
		setName("testDependencyAnalysis");
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param analysis DOCUMENT ME!
	 */
	public final void setDA(final IDependencyAnalysis analysis) {
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
						verifyDAFor(_unit, _sm);
					}
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysisTest#getDA()
	 */
	public IDependencyAnalysis getDA() {
		return da;
	}

	/**
	 * @see IDependencyAnalysisTest#setEnvironment(IEnvironment)
	 */
	public void setEnvironment(final IEnvironment environment) {
		env = environment;
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		da = null;
		env = null;
		super.tearDown();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param unit DOCUMENT ME!
	 * @param sm DOCUMENT ME!
	 *
	 * @throws IllegalStateException DOCUMENT ME!
	 */
	protected abstract void verifyDAFor(final Stmt unit, final SootMethod sm);

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	protected final IDependencyAnalysis getDa() {
		return da;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/05/14 06:28:25  venku
   - added a new class AbstractDependencyAnalysisTest to perform unit
     tests on dependency analysis.
   - refactored IDependencyAnalysisTest.
 */
