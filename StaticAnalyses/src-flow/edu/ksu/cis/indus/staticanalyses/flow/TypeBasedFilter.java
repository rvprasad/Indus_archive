
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

package edu.ksu.cis.indus.staticanalyses.flow;

import soot.RefType;
import soot.SootClass;
import soot.Type;
import soot.Value;

import edu.ksu.cis.indus.staticanalyses.interfaces.IEnvironment;
import edu.ksu.cis.indus.staticanalyses.support.Util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
  implements IValueFilter {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(TypeBasedFilter.class);

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
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IValueFilter#filter(java.util.Collection)
	 */
	public Collection filter(final Collection values) {
		Collection result = new ArrayList();

		for (Iterator i = values.iterator(); i.hasNext();) {
			Value o = (Value) i.next();

			if (!filter(o)) {
				result.add(o);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Filtered " + result + " from " + values + " when type was " + type);
		}
		return result;
	}

	/**
	 * Checks if value should be filtered out.  It is filtered out if it is not of the type being monitored by this object.
	 *
	 * @param value to be filtered out.
	 *
	 * @return <code>true</code> if <code>value</code> should be filtered out; <code>false</code>, otherwise.
	 *
	 * @pre value != null and value.oclIsKindOf(Value)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IValueFilter#filter(java.lang.Object)
	 */
	public boolean filter(final Object value) {
		return !Util.isSameOrSubType(((Value) value).getType(), type, env);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/08/25 11:25:23  venku
   Formatting.
   Revision 1.5  2003/08/25 11:24:53  venku
   Concretized the definition of filter() methods.
   Revision 1.4  2003/08/16 02:50:22  venku
   Spruced up documentation and specification.
   Moved onNewXXX() methods from IFGNode to AbstractFGNode.
   Revision 1.3  2003/08/15 04:15:45  venku
   Got the documentation right.
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
