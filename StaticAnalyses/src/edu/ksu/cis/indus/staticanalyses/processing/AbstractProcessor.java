
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
import edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor;
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
	 * @see IProcessor#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer analyzer) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#callback(Value, Context)
	 */
	public void callback(final Value value, final Context context) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#callback(Stmt, Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#callback(SootMethod)
	 */
	public void callback(final SootMethod method) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#callback(SootClass)
	 */
	public void callback(final SootClass clazz) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#callback(SootField)
	 */
	public void callback(final SootField field) {
	}

	/**
	 * Does nothing.
	 *
	 * @see IProcessor#consolidate()
	 */
	public void consolidate() {
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/08/11 07:15:57  venku
   Finalized the parameters.

   Revision 1.2  2003/08/11 06:38:25  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.

   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 1.3  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
