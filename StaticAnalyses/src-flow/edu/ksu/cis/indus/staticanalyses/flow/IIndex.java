
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

/**
 * A marker interface to be implemented by all classes whose instances will be used as indices in FA framework.
 * 
 * <p>
 * Created: Tue Jan 22 04:55:30 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public interface IIndex {
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/09/28 03:16:33  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.3  2003/08/17 10:48:33  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.2  2003/08/16 02:50:22  venku
   Spruced up documentation and specification.
   Moved onNewXXX() methods from IFGNode to AbstractFGNode.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.1  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
