
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

package edu.ksu.cis.indus.staticanalyses.flow;

import soot.RefType;
import soot.SootClass;
import soot.Type;
import soot.Value;

import edu.ksu.cis.indus.staticanalyses.interfaces.IEnvironment;
import edu.ksu.cis.indus.staticanalyses.support.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


/**
 * This class filters out values which are not of the same type or a sub type of <code>type</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class TypeBasedFilter
  extends AValueFilter {
	/**
	 * The environment in which the analysis happens.
	 *
	 * @invariant env != null
	 */
	private final IEnvironment env;

	/**
	 * The type which is used to decide filtering.
	 *
	 * @invariant type != null
	 */
	private final Type type;

	/**
	 * Creates a new TypeBasedFilter object.
	 *
	 * @param filterType to be used to decide filtering.
	 * @param enclosingEnv is the environment in which the analysis happens.
	 *
	 * @pre filterType != null and enclosingEnv != null
	 */
	public TypeBasedFilter(final Type filterType, final IEnvironment enclosingEnv) {
		this.type = filterType;
		this.env = enclosingEnv;
	}

	/**
	 * Creates a new TypeBasedFilter object.
	 *
	 * @param clazz to be used to decide filtering.
	 * @param enclosingEnv is the environment in which the analysis happens.
	 *
	 * @pre class != null and enclosingEnv != null
	 */
	public TypeBasedFilter(final SootClass clazz, final IEnvironment enclosingEnv) {
		this.type = RefType.v(clazz.getName());
		this.env = enclosingEnv;
	}

	/**
	 * Filters out those values from the given collection which are not of the type being monitored by this object.
	 *
	 * @param values to be filtered
	 *
	 * @return a collection of values which are of type being monitored by this object.
	 *
	 * @pre values != null and values.oclIsKindOf(Collection(Value))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.flow.AValueFilter#filter(java.util.Collection)
	 */
	public Collection filter(final Collection values) {
		Collection result = new ArrayList();

		for (Iterator i = values.iterator(); i.hasNext();) {
			Value o = (Value) i.next();

			if (filter(o)) {
				result.add(o);
			}
		}
		return result;
	}

	/**
	 * Checks if value should be let through the fitler.  It is let through if it is of the type being monitored by this
	 * object.
	 *
	 * @param value to be filtered.
	 *
	 * @return <code>true</code> if <code>value</code> should be filtered; <code>false</code>, otherwise.
	 *
	 * @pre value != null and value.oclIsKindOf(Value)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.flow.AValueFilter#filter(java.lang.Object)
	 */
	public boolean filter(final Object value) {
		return Util.isSameOrSubType(((Value) value).getType(), type, env);
	}
}

/*
   ChangeLog:

   $Log$
   Revision 1.2  2003/08/15 04:07:56  venku
   Spruced up documentation and specification.
   - Important change is that previously all types of retype and nullconstant were let through.
     This is incorrect as there is not type filtering happening.  This has been fixed.  We now
     only let those that are not of the monitored type.

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.

   Revision 1.2  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
