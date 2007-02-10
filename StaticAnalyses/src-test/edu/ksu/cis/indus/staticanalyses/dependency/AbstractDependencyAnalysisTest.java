
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.IndusTestCase;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import java.util.Iterator;

import soot.SootClass;
import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This class provides the basic infrastructure to test dependency analyses.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractDependencyAnalysisTest
  extends IndusTestCase
  implements IDependencyAnalysisTest {
	/** 
	 * The analysis to test.
	 */
	private IDependencyAnalysis da;

	/** 
	 * The environment in which the analysis executed.
	 */
	private IEnvironment env;

	/**
	 * Creates an instance of this class.
	 */
	protected AbstractDependencyAnalysisTest() {
		super();
		setName("testDependencyAnalysis");
	}

	/**
	 * Sets the analysis to test.
	 *
	 * @param analysis to test.
	 *
	 * @pre analysis != null
	 */
	public final void setDA(final IDependencyAnalysis analysis) {
		da = analysis;
	}

	/**
	 * Tests the dependency analysis.
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
	 * @see edu.ksu.cis.indus.IEnvironmentBasedTest#setEnvironment(IEnvironment)
	 */
	public void setEnvironment(final IEnvironment environment) {
		env = environment;
	}

	/**
	 * @see IndusTestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		da = null;
		env = null;
		super.tearDown();
	}

	/**
	 * Verify the results of the analysis for the given statement and method.
	 *
	 * @param unit is the focus of the test.
	 * @param sm is the method in which <code>unit</code> occurs.
	 *
	 * @pre unit != null and sm != null
	 */
	protected abstract void verifyDAFor(final Stmt unit, final SootMethod sm);

	/**
	 * Returns the analysis being tested.
	 *
	 * @return the analysis being tested.
	 */
	protected final IDependencyAnalysis getDa() {
		return da;
	}
}

// End of File
