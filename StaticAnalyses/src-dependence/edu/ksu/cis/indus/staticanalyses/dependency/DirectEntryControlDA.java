
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

import java.util.BitSet;
import java.util.Collection;
import java.util.List;

/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DirectEntryControlDA
  extends EntryControlDA {
	/**
	 * @see EntryControlDA#propagateInfoToSuccs(BitSet, Collection, int, BitSet[][], List)
	 */
	protected Collection propagateInfoToSuccs(final BitSet parentNodeTokenSet, final Collection succsOf, final int nodeIndex,
		final BitSet[][] tokenSets, final List nodes) {
		// TODO: Auto-generated method stub
		return super.propagateInfoToSuccs(parentNodeTokenSet, succsOf, nodeIndex, tokenSets, nodes);
	}
}

/*
   ChangeLog:
   $Log$
 */
