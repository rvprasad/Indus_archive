
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2002, 2003, 2004.
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

import soot.ArrayType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Value;

import soot.jimple.InvokeExpr;
import soot.jimple.ParameterRef;

import edu.ksu.cis.indus.staticanalyses.Context;

import java.util.Collection;


/**
 * DOCUMENT ME!
 * <p></p>
 * 
 * @version $Revision$ 
 * @author <a href="$user_web$">$user_name$</a>
 * @author $Author$
 */
public interface IValueAnalyzer {
	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public IEnvironment getEnvironment();

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getRoots();

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param expr DOCUMENT ME!
	 * @param exception DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getThrowValues(final InvokeExpr expr, final SootClass exception);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param arrType DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getValues(final ArrayType arrType);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param param DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getValues(final ParameterRef param);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param field DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getValues(final SootField field);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param value DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getValues(final Value value);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param value DOCUMENT ME!
	 * @param context DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getValues(final Object value, final Context context);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param context DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Collection getValuesForThis(final Context context);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param scm DOCUMENT ME!
	 * @param classes DOCUMENT ME!
	 */
	public void analyze(final Scene scm, final Collection classes);

	/**
	 * DOCUMENT ME! <p></p>
	 *
	 * @param scm DOCUMENT ME!
	 * @param entry DOCUMENT ME!
	 */
	public void analyze(final Scene scm, final SootMethod entry);

	/**
	 * DOCUMENT ME! <p></p>
	 */
	public void reset();
}

/*****
 ChangeLog:

$Log$

*****/
