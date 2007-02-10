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

package edu.ksu.cis.indus.common.fa;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;

/**
 * This is an implementation of deterministic finite automaton.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$ *
 * @param <S> the type of the implementation of this interface.
 * @param <L> the type of the implementation of this interface.
 */
public class DFA<S extends IState<S>, L extends ITransitionLabel<L>>
		extends NFA<S, L> {

	/**
	 * Creates an instance of this class.
	 * 
	 * @param eFactory <i>refer to documentation in super class constructor</i>
	 */
	public DFA(@NonNull @Immutable final ITransitionLabel.IEpsilonLabelFactory<L> eFactory) {
		super(eFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void addLabelledTransitionFromTo(@NonNull @Immutable final S src, @NonNull @Immutable final L label,
			@NonNull @Immutable final S dest) {
		final Collection<S> _states = getResultingStates(src, label);

		if (!_states.isEmpty()) {
			final String _msg = "A transition labelled '" + label + "' already exists from the given source (" + src + ")";
			throw new IllegalStateException(_msg);
		} else if (label.equals(epsilonFactory.getEpsilonTransitionLabel())) {
			final String _msg = "Epsilon transitions are not allowed in Deterministic automata.";
			throw new IllegalArgumentException(_msg);
		} else {
			super.addLabelledTransitionFromTo(src, label, dest);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override @Functional public boolean isDeterministic() {
		return true;
	}
}

// End of File
