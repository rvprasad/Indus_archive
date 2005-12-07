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

import edu.ksu.cis.indus.staticanalyses.flow.AbstractMethodVariant;
import edu.ksu.cis.indus.staticanalyses.flow.FA;
import edu.ksu.cis.indus.staticanalyses.flow.IVariantManager;
import edu.ksu.cis.indus.staticanalyses.flow.ValuedVariant;
import edu.ksu.cis.indus.staticanalyses.tokens.ITokens;

import soot.SootMethod;
import soot.Type;
import soot.Value;

/**
 * This variant implementation acts as a stub that does not capture all of the flow within the body of the method.
 * Dependending on the configuration, it may capture some flow within the body of the method.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> DOCUMENT ME!
 */
public class StubMethodVariant<T extends ITokens<T, Value>>
		extends AbstractMethodVariant<Value, T, OFAFGNode<T>> {

	/**
	 * Creates an instance of this class.
	 *
	 * @param sm is the method being represented by this variant.
	 * @param astVariantManager used by this variant.
	 * @param theFA the flow analysis with which this variant is associated.
	 * @pre sm != null and astVariantManager != null and theFA != null
	 */
	public StubMethodVariant(final SootMethod sm, final IVariantManager<ValuedVariant<OFAFGNode<T>>, Value> astVariantManager,
			final FA<Value, T, OFAFGNode<T>> theFA) {
		super(sm, astVariantManager, theFA);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.MethodVariant#process()
	 */
	public void process() {
		// TODO: add code that can plug in flow summary depending on the configuration.
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.AbstractMethodVariant#shouldConsider(soot.Type)
	 */
	@Override protected boolean shouldConsider(final Type type) {
		// TODO: Auto-generated method stub
		return false;
	}
}

// End of File
