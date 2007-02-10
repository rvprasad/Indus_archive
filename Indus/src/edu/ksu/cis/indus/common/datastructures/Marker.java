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

package edu.ksu.cis.indus.common.datastructures;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;

/**
 * This class serves as a marker in sequences of data. The stringized representation of this object is dependent on the
 * stringized representation of it's constituents. Hence, the stringized representation of this object will change if that of
 * the constituents change. A similar dependency exists for hashCode too.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class Marker {

	/**
	 * Any content to be stored in the marker.
	 */
	@Immutable private final Object content;

	/**
	 * Creates a new Marker object.
	 * 
	 * @param o is any content to be stored in the marker.
	 */
	public Marker(@Immutable final Object o) {
		content = o;
	}

	/**
	 * Retrieves the contents of this marker.
	 * 
	 * @return the content of this marker.
	 */
	@Functional public final Object getContent() {
		return this.content;
	}
}

// End of File
