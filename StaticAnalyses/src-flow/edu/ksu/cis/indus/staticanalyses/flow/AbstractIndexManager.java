
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

import edu.ksu.cis.indus.interfaces.IPrototype;
import edu.ksu.cis.indus.processing.Context;

import java.util.HashSet;
import java.util.Set;


/**
 * This class encapsulates the index creation logic.  It is abstract and it provides an interface through which new indices
 * can be obtained.  The sub classes should provide the logic for the actual creation of the indices.
 * 
 * <p>
 * Created: Tue Jan 22 04:54:38 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractIndexManager
  implements IPrototype {
	/**
	 * The collection of indices managed by this object.
	 *
	 * @invariant indices != null
	 */
	protected final Set indices = new HashSet();

	/**
	 * This operation is unsupported.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException if the operation is not supported.
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("prototype() is not supported.");
	}

	/**
	 * This operation is unsupported.
	 *
	 * @param o is ignored.
	 *
	 * @return (This method will raise an exception.)
	 *
	 * @throws UnsupportedOperationException if the operation is not supported.
	 */
	public Object getClone(final Object o) {
		throw new UnsupportedOperationException("prototype(Object) is not supported.");
	}

	/**
	 * Returns the index corresponding to the given entity in the given context, if one exists.  If none exist, a new index
	 * is created and returned.
	 *
	 * @param o the entity whose index is to be returned.
	 * @param c the context in which the entity's index is requested.
	 *
	 * @return the index corresponding to the entity in the given context.
	 *
	 * @pre o != null and c != null
	 * @post result != null
	 */
	protected abstract IIndex getIndex(final Object o, final Context c);

	/**
	 * Returns the index corresponding to the given entity in the given context, if one exists.  If none exist, it returns
	 * <code>null</code>.
	 *
	 * @param o the entity whose index is to be returned.
	 * @param c the context in which the entity's index is requested.
	 *
	 * @return the index corresponding to the entity in the given context, if one exists; <code>null</code> otherwise.
	 *
	 * @pre o != null and c != null
	 */
	final IIndex queryIndex(final Object o, final Context c) {
		IIndex temp = getIndex(o, c);

		if (!indices.contains(temp)) {
			indices.add(temp);
		}

		return temp;
	}

	/**
	 * Reset the manager.  Flush all the internal data structures to enable a new session.
	 */
	void reset() {
		indices.clear();
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.

   Revision 1.4  2003/08/20 18:14:38  venku
   Log4j was used instead of logging.  That is fixed.
   Revision 1.3  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.
   Revision 1.2  2003/08/12 18:39:56  venku
   Ripple effect of moving IPrototype to Indus.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.10  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
