
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

package edu.ksu.cis.indus.interfaces;

import soot.SootMethod;

import soot.toolkits.graph.UnitGraph;


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
	 * Retrieves the statement graph of the given method.
	 *
	 * @param method for which the statement graph is requested. 
	 *
	 * @return the requested statement graph.
     * @pre method != null
	 */
	UnitGraph getStmtGraph(final SootMethod method);
}

/*
   ChangeLog:
   $Log$
 */
