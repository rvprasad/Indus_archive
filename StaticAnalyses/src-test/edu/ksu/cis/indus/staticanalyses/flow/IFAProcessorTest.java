
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.processing.IProcessor;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import soot.Scene;


/**
 * DOCUMENT ME!
 * <p></p>
 * 
 * @version $Revision$ 
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 */
public interface IFAProcessorTest {
	/**
	 * DOCUMENT ME!
	 *
	 * @param valueAnalyzer
	 */
	void setFA(IValueAnalyzer valueAnalyzer);

	/**
	 * DOCUMENT ME!
	 *
	 * @param processor
	 */
	void setProcessor(IProcessor processor);

	/**
	 * DOCUMENT ME!
	 *
	 * @param scene
	 */
	void setScene(Scene scene);
}

/*
   ChangeLog:
   $Log$
 */
