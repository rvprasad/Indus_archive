
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.interfaces;

import soot.Scene;
import soot.SootMethod;

import edu.ksu.cis.indus.staticanalyses.Context;

import java.util.Collection;


/**
 * This is the interface to be provided by an analysis that operates on values (which may be symbolic).  The analysis that
 * implement this interface are behavioral analysis rather than structural analysis.
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IValueAnalyzer {
	/**
	 * Retrieves the enviroment in which the analysis operates.
	 *
	 * @return the enviroment.
	 */
	IEnvironment getEnvironment();

	/**
	 * Retrieves the values associated with the given entity in the given context.
	 *
	 * @param entity for which values are requested.
	 * @param context in which the returned values will be associated with the entity.
	 *
	 * @return the collection of values.
	 *
	 * @pre context != null
	 * @pre entity != null
	 * @post result != null
	 */
	Collection getValues(Object entity, Context context);

	/**
	 * Retrieves the values associated with <code>this</code> variable in the given context.
	 *
	 * @param context in which the returned values will be associatd with <code>this</code> variable.
	 *
	 * @return the collection of values
	 *
	 * @pre context != null
	 * @pre context.getCurrentMethod() != null
	 * @post result != null
	 */
	Collection getValuesForThis(Context context);

	/**
	 * Analyzes the system represented by the given classes and and scene.
	 *
	 * @param scm manages the classes that constitute the system being analyzed.
	 * @param classes which were mentioned by the user as being part of the system.  This generally serves as the starting
	 * 		  point to discover other constituents of the system.  These may serve as the classes in which to explore for
	 * 		  the  entry point of the system.
	 *
	 * @pre scm != null and classes != null and classes.size() != 0
	 */
	void analyze(Scene scm, Collection classes);

	/**
	 * Analyzes the system represented by the given classes starting at the given entry point.
	 *
	 * @param scm manages the classes that constitute the system being analyzed.
	 * @param entry point into the system being analyzed.
	 *
	 * @pre scm != null and entry != null
	 */
	void analyze(Scene scm, SootMethod entry);

	/**
	 * Resets the analyzer.
	 */
	void reset();
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/08/11 07:11:47  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Moved getRoots() into the environment.
   Added support to inject new roots in BFA.

   Revision 1.2  2003/08/09 23:26:20  venku
   - Added an interface to provide use-def information.
   - Added an implementation to the above interface.
   - Extended call graph processor to retrieve call tree information rooted at arbitrary node.
   - Modified IValueAnalyzer interface such that only generic queries are possible.
     If required, this can be extended in the future.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
 */
