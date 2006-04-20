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
package edu.ksu.cis.indus.common.fa;

import edu.ksu.cis.indus.annotations.Marker;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.common.graph.IEdgeLabel;

/**
 * This is a marker interface that represents a transition.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <L> the type of the implementation of this interface.
 */
@Marker public interface ITransitionLabel<L extends ITransitionLabel<L>>
		extends IEdgeLabel {

	/**
	 * The factory that creates epsilon transition labels.
	 * 
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 * @param <L> is the type of the labels.
	 */
	interface IEpsilonLabelFactory<L> {

		/**
		 * Retrieves an epsilon label.
		 * 
		 * @return the epsilon label.
		 */
		@NonNull L getEpsilonTransitionLabel();
	}
	// empty
}
