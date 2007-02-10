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
