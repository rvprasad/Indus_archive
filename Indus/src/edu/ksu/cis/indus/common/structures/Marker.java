
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

package edu.ksu.cis.indus.common.structures;

/**
 * This class serves as a marker in sequences of data.  The stringized representation of this object is dependent on  the
 * stringized representation of it's constituents.  Hence, the stringized representation of this object will change if  that
 * of the constituents change.  A similar dependency exists for hashCode too.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class Marker {
	/**
	 * Any content to be stored in the marker.
	 */
	public final Object _content;

	/**
	 * Creates a new Marker object with <code>content</code> set to <code>null</code>.
	 */
	public Marker() {
		_content = null;
	}

	/**
	 * Creates a new Marker object.
	 *
	 * @param o is any content to be stored in the marker.
	 */
	public Marker(final Object o) {
		_content = o;
	}

	/**
	 * Checks if the given object is equal to this object.
	 *
	 * @param o is the object to be checked for equality.
	 *
	 * @return <code>true</code> if <code>o</code> equals this object; <code>false</code>, otherwise.
	 *
	 * @post result == true implies o.oclTypeOf(Marker) and (o._content.equals(_content) or o._content == _content)
	 * @post result == false implies (not o.oclTypeOf(Marker)) or not (o._content.equals(_content) or o._content == _content)
	 */
	public boolean equals(final Object o) {
		boolean result = false;

		if (o != null && o instanceof Marker) {
			Object temp = ((Marker) o)._content;

			if (temp == null && _content == null) {
				result = this == o;
			} else if (_content != null && temp != null) {
				result = _content.equals(temp);
			}
		}
		return result;
	}

	/**
	 * Returns the hash code of the object.  <code>content</code> field is used if it is non-null.
	 *
	 * @return the hash code of the object.
	 */
	public int hashCode() {
		int result;

		if (_content != null) {
			result = _content.hashCode();
		} else {
			result = super.hashCode();
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.

   Revision 1.7  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.6  2003/11/06 05:04:02  venku
   - renamed WorkBag to IWorkBag and the ripple effect.
   Revision 1.5  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.4  2003/08/11 08:12:26  venku
   Major changes in equals() method of Context, Pair, Marker, and Triple.
   Similar changes in hashCode()
   Spruced up Documentation and Specification.
   Formatted code.
   Revision 1.3  2003/08/11 07:13:58  venku
 *** empty log message ***
     Revision 1.2  2003/08/11 04:20:19  venku
     - Pair and Triple were changed to work in optimized and unoptimized mode.
     - Ripple effect of the previous change.
     - Documentation and specification of other classes.
     Revision 1.1  2003/08/07 06:42:16  venku
     Major:
      - Moved the package under indus umbrella.
      - Renamed isEmpty() to hasWork() in IWorkBag.
     Revision 1.4  2003/05/22 22:18:31  venku
     All the interfaces were renamed to start with an "I".
     Optimizing changes related Strings were made.
 */
