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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.annotations.Empty;
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
 * @param <T>  is the type of the token set object.
 */
public class StubMethodVariant<T extends ITokens<T, Value>>
		extends AbstractMethodVariant<Value, T, OFAFGNode<T>, Type> {

	/**
	 * Creates an instance of this class.
	 *
	 * @param sm is the method being represented by this variant.
	 * @param astVariantManager used by this variant.
	 * @param theFA the flow analysis with which this variant is associated.
	 * @pre sm != null and astVariantManager != null and theFA != null
	 */
	public StubMethodVariant(final SootMethod sm, final IVariantManager<ValuedVariant<OFAFGNode<T>>, Value> astVariantManager,
			final FA<Value, T, OFAFGNode<T>, Type> theFA) {
		super(sm, astVariantManager, theFA);
	}

	/**
	 * {@inheritDoc}
	 */
	public void process() {
		// TODO: add code that can plug in flow summary depending on the configuration.
	}

	/**
	 * {@inheritDoc}
	 */
	@Empty @Override protected boolean shouldConsider(final Type type) {
		// TODO: Auto-generated method stub
		return false;
	}
}

// End of File
