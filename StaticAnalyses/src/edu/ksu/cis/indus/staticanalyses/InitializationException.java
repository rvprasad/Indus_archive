
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

package edu.ksu.cis.indus.staticanalyses;

/**
 * This exception can be thrown when initialization fails.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class InitializationException
  extends Exception {
	/**
	 * Creates a new InitializationException object.
	 *
	 * @param message the detailed message.
	 *
	 * @pre message != null
	 */
	public InitializationException(final String message) {
		super(message);
	}

	/**
	 * Creates a new InitializationException object. >
	 *
	 * @param message the detailed message.
	 * @param e the cause for the exception.
	 *
	 * @pre message != null and e != null
	 */
	public InitializationException(final String message, final Throwable e) {
		super(message, e);
	}

	/**
	 * Creates a new InitializationException object.
	 *
	 * @param e the cause for the exception.
	 *
	 * @pre e != null
	 */
	public InitializationException(final Throwable e) {
		super(e);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.

   Revision 1.2  2003/08/11 08:12:26  venku
   Major changes in equals() method of Context, Pair, Marker, and Triple.
   Similar changes in hashCode()
   Spruced up Documentation and Specification.
   Formatted code.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in IWorkBag.
   Revision 1.3  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
