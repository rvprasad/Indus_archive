
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

package edu.ksu.cis.indus.common.datastructures;

/**
 * This class serves as a marker in sequences of data.  The stringized representation of this object is dependent on  the
 * stringized representation of it's constituents.  Hence, the stringized representation of this object will change if  that
 * of the constituents change.  A similar dependency exists for hashCode too.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class Marker {
	/** 
	 * Any content to be stored in the marker.
	 */
	private final Object content;

	/**
	 * Creates a new AMarker object.
	 *
	 * @param o is any content to be stored in the marker.
	 */
	public Marker(final Object o) {
		content = o;
	}

	/**
	 * Retrieves the contents of this marker.
	 *
	 * @return the content of this marker.
	 */
	public final Object getContent() {
		return this.content;
	}
}

// End of File
