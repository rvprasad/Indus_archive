
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

package edu.ksu.cis.indus.slicer;

import soot.SootMethod;

import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
interface IGotoProcessor {
	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	void postprocess();

	/**
	 * DOCUMENT ME!
	 *
	 * @param method DOCUMENT ME!
	 */
	void preprocess(SootMethod method);

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param bbg DOCUMENT ME!
	 */
	void process(BasicBlock bbg);
}

/*
   ChangeLog:
   $Log$
 */
