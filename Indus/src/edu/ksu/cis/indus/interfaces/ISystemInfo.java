
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

package edu.ksu.cis.indus.interfaces;

import java.util.Collection;

import soot.SootMethod;

/**
 * This will be generic interface that provides information about the system.  The difference between this class and
 * <code>edu.ksu.cis.indus.staticanalyses.interfaces.IEnvironment</code> is  that the former provides information about the
 * system which is commonly required used by program transformation while the latter provides information about the system
 * as required by the static analyses such as those described in it's parent package and any other similar sort of analyses.
 * A more compelling reason for these interfaces is to collect and provide information as required by analyses and
 * transformation designed to  fit into this framework.
 *
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ISystemInfo {
	/**
	 * Retrieves the statements of the given method.
	 *
	 * @param method for which the statements are requested.
	 *
	 * @return the statements of the method.
	 *
	 * @pre method != null
	 */
	Collection getUnits(final SootMethod method);
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/09/27 23:21:42  venku
   *** empty log message ***

   Revision 1.1  2003/08/18 04:44:35  venku
   Established an interface which will provide the information about the underlying system as required by transformations.
   It is called ISystemInfo.
   Ripple effect of the above change.
 */
