
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

/*
 * Created on Aug 2, 2004
 *
 *
 */
package edu.ksu.cis.indus.kaveri.views;

/**
 * This interface is implemented by any viewers which want  to listen to changes in PartialStmtData model.
 *
 * @author ganeshan
 */
public interface IDeltaListener {
	/**
	 * The property has changed.  Update the stuff.
	 */
	void propertyChanged();
}
