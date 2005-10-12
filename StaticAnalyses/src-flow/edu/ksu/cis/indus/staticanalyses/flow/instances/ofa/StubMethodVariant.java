/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IVariantManager;
import edu.ksu.cis.indus.staticanalyses.flow.ValuedVariant;

import soot.SootMethod;
import soot.Value;

/**
 * This variant implementation acts as a stub that does not capture all of the flow within the body of the method.
 * Dependending on the configuration, it may capture some flow within the body of the method.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class StubMethodVariant
		extends MethodVariant {

	/**
	 * Creates an instance of this class.
	 * 
	 * @param sm
	 *            is the method being represented by this variant.
	 * @param astVariantManager
	 *            used by this variant.
	 * @param theFA
	 *            the flow analysis with which this variant is associated.
	 * @pre sm != null and astVariantManager != null and theFA != null
	 */
	public StubMethodVariant(final SootMethod sm, final IVariantManager<ValuedVariant<OFAFGNode>, Value> astVariantManager, final FA theFA) {
		super(sm, astVariantManager, theFA);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.MethodVariant#process()
	 */
	public void process() {
		// TODO: add code that can plug in flow summary depending on the configuration.
	}
}

// End of File
