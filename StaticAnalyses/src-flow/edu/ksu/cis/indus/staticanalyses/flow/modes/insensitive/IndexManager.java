
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

package edu.ksu.cis.indus.staticanalyses.flow.modes.insensitive;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;


/**
 * This class implements insensitive index manager.  In simple words, it generates indices such that entities can be
 * differentiated solely on their credentials and not on any other auxiliary information such as program point or call
 * stack.
 * 
 * <p>
 * Created: Fri Jan 25 13:11:19 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class IndexManager
  extends AbstractIndexManager {
	/**
	 * This class represents an index that identifies an entity independent of any context information..
	 */
	private static class DummyIndex
	  implements IIndex {
		/**
		 * The entity that this index identifies.
		 */
		Object object;

		/**
		 * Creates a new <code>DummyIndex</code> instance.
		 *
		 * @param o the entity being identified by this index.
		 */
		DummyIndex(final Object o) {
			this.object = o;
		}

		/**
		 * Compares if the given object is the same as this object.
		 *
		 * @param o the object to be compared with.
		 *
		 * @return <code>true</code> if <code>object</code> is the same as this object; <code>false</code> otherwise.
		 */
		public boolean equals(final Object o) {
			boolean _result = false;

			if (o != null && o instanceof DummyIndex) {
				final DummyIndex _di = (DummyIndex) o;

				if (object != null) {
					_result = object.equals(_di.object);
				} else {
					_result = object == _di.object;
				}
			}
			return _result;
		}

		/**
		 * Returns the hash code for this object.
		 *
		 * @return returns the hash code for this object.
		 */
		public int hashCode() {
			int _result = 17;

			if (object != null) {
				_result = 37 * _result * object.hashCode();
			}
			return _result;
		}

		/**
		 * Returns the stringized representation of this object.
		 *
		 * @return the stringized representation of this object.
		 */
		public String toString() {
			return object.toString();
		}
	}

	/**
	 * Returns a new instance of this class.
	 *
	 * @return a new instance of this class.
	 */
	public Object getClone() {
		return new IndexManager();
	}

	/**
	 * This method throws an <code>UnsupportedOperationException</code> exception.
	 *
	 * @param o <i>ignored</i>.
	 *
	 * @return (This method throws an exception.)
	 *
	 * @throws UnsupportedOperationException as this method is not supported.
	 */
	public Object getClone(final Object o) {
		throw new UnsupportedOperationException("Single parameter prototype() is not supported.");
	}

	/**
	 * Returns an index corresponding to the given entity.
	 *
	 * @param o the entity for which the index in required.
	 * @param c <i>ignored</i>..
	 *
	 * @return the index that uniquely identifies <code>o</code>.
	 */
	protected IIndex getIndex(final Object o, final Context c) {
		return new DummyIndex(o);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/12/02 09:42:39  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.5  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.4  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/13 07:56:25  venku
   Fixed coding style violations for redundant throws and field name.
   Revision 1.2  2003/08/12 19:03:47  venku
   Spruced up documentation and specification.
   Changed equals() and hashCode().
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 0.10  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
