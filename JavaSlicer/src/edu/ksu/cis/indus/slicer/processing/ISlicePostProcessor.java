
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

package edu.ksu.cis.indus.slicer.processing;

import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;
import edu.ksu.cis.indus.slicer.SliceCollector;

import java.util.Collection;


/**
 * DOCUMENT ME!
 * <p></p>
 * 
 * @version $Revision$ 
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 */
interface ISlicePostProcessor {
	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param methods DOCUMENT ME!
	 * @param basicBlockMgr DOCUMENT ME!
	 * @param theCollector DOCUMENT ME!
	 */
	void process(final Collection methods, final BasicBlockGraphMgr basicBlockMgr, final SliceCollector theCollector);
}

/*
   ChangeLog:
   $Log$
 */
