
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.interfaces;

import ca.mcgill.sable.soot.SootClass;

import java.util.Collection;


/**
 * This interface exposes the information pertaining to the system being analyzed which is essential for higher level
 * analyses to function. However, it does not expose any information pertaining to the actual implementation of the analysis
 * instance.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface Environment {
	/**
	 * The id of this interface.
	 */
	String ID = "Environment";

	/**
	 * Returns the Jimple representation of the given class.
	 *
	 * @param className the name of the class whose Jimple representation is to be returned.
	 *
	 * @return the requested class.
	 *
	 * @post result.oclType = ca.mcgill.sable.soot.SootClass
	 */
	SootClass getClass(String className);

	/**
	 * Returns the classes accessed/used by the analyzer.
	 *
	 * @return the classes accessed/used by the analyzer.
	 *
	 * @post result->forall(o | o.oclType = ca.mcgill.sable.soot.SootClass)
	 */
	Collection getClasses();
}

/*****
 ChangeLog:

$Log$

*****/
