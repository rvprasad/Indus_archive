
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

package edu.ksu.cis.indus.staticanalyses.processing;

import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.interfaces.*;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;


/**
 * Abstract implementation of processor.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractProcessor
  implements IProcessor {
	/**
	 * Does nothing.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#setAnalyzer(
	 * 		edu.ksu.cis.indus.staticanalyses.flow.AbstractAnalyzer)
	 */
	public void setAnalyzer(IValueAnalyzer analyzer) {
	}

	/**
	 * Does nothing.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(soot.jimple.Value,
	 * 		edu.ksu.cis.indus.staticanalyses.flow.Context)
	 */
	public void callback(Value value, Context context) {
	}

	/**
	 * Does nothing.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(soot.jimple.Stmt,
	 * 		edu.ksu.cis.indus.staticanalyses.flow.Context)
	 */
	public void callback(Stmt stmt, Context context) {
	}

	/**
	 * Does nothing.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(SootMethod method) {
	}

	/**
	 * Does nothing.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(soot.SootClass)
	 */
	public void callback(SootClass clazz) {
	}

	/**
	 * Does nothing.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(SootField)
	 */
	public void callback(SootField field) {
	}

	/**
	 * Does nothing.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#consolidate()
	 */
	public void consolidate() {
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 1.3  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
