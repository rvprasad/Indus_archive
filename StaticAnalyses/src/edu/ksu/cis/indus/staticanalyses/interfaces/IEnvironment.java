
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

import soot.SootClass;

import java.util.Collection;


/**
 * This interface exposes the information pertaining to the environment in which the analyses function. It provides the
 * non-functional information about the system.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IEnvironment {
	/**
	 * The id of this interface.
	 */
	String ID = "IEnvironment";

	/**
	 * Returns the Jimple representation of the given class.
	 *
	 * @param className is the name of the class whose Jimple representation is to be returned.
	 *
	 * @return the requested class.
	 *
	 * @post result.oclType = SootClass
	 */
	SootClass getClass(String className);

	/**
	 * Returns the classes that form the system.
	 *
	 * @return the classes the form the system.
	 *
	 * @post result->forall(o | o.oclType = SoptClass)
	 */
	Collection getClasses();

	/**
	 * Retrieves the methods that serve as the entry points or as "roots" of the system being analyzed.
	 *
	 * @return a collection of methods that are the "roots".
	 *
	 * @post result != null and result.oclIsKindOf(Collection(soot.SootMethod))
	 */
	Collection getRoots();
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 1.1  2003/05/22 22:16:45  venku
   All the interfaces were renamed to start with an "I".
 */
