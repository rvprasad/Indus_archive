
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

package edu.ksu.cis.indus.staticanalyses.flow;

import soot.RefType;
import soot.SootClass;
import soot.Type;

import soot.jimple.NewExpr;
import soot.jimple.NullConstant;
import soot.Value;

import edu.ksu.cis.indus.staticanalyses.interfaces.IEnvironment;
import edu.ksu.cis.indus.staticanalyses.support.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * DOCUMENT ME!
 *
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class TypeBasedFilter
  extends ValueFilter {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final IEnvironment env;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Type type;

	/**
	 * Creates a new TypeBasedFilter object.
	 *
	 * @param type DOCUMENT ME!
	 * @param env DOCUMENT ME!
	 */
	public TypeBasedFilter(Type type, IEnvironment env) {
		this.type = type;
		this.env = env;
	}

	/**
	 * Creates a new TypeBasedFilter object.
	 *
	 * @param clazz DOCUMENT ME!
	 * @param env DOCUMENT ME!
	 */
	public TypeBasedFilter(SootClass clazz, IEnvironment env) {
		this.type = RefType.v(clazz.getName());
		this.env = env;
	}

	/**
	 * Filters away values that do not have the required type.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.flow.ValueFilter#filter(java.util.Collection)
	 */
	public Collection filter(Collection values) {
		Collection result = new ArrayList();

		for (Iterator i = values.iterator(); i.hasNext();) {
			Value o = (Value) i.next();

			if ((type instanceof RefType && o instanceof NullConstant) || Util.isSameOrSubType(o.getType(), type, env)) {
				result.add(o);
			}
		}
		return result;
	}

	/**
	 * Checks if the given value can be filtered away(<code>true</code>) or not(<code>false</code>).
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.flow.ValueFilter#filter(java.lang.Object)
	 */
	public boolean filter(Object value) {
		return Util.isSameOrSubType(((NewExpr) value).getType(), type, env);
	}
}

/*****
 ChangeLog:

$Log$
Revision 1.2  2003/05/22 22:18:31  venku
All the interfaces were renamed to start with an "I".
Optimizing changes related Strings were made.


*****/
