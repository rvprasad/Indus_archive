
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

package edu.ksu.cis.indus.slicer;

import edu.ksu.cis.indus.interfaces.IPoolable;


/**
 * This is a marker interface via which slice criterion is exposed to the external world.
 * 
 * <p>
 * The purpose of this interface is to identify an object as a poolable slicing criterion.  To be more precise,
 * </p>
 * 
 * <ul>
 * <li>
 * as it is poolable, it means  that  the application obtaining/creating a criterion with this interface is responsible for
 * returning the criterion to the pool and
 * </li>
 * <li>
 * to identify an object as a criterion
 * </li>
 * </ul>
 * 
 * <p>
 * Does this mean that any object that implements this interface is a valid slice criterion?  NO!  In other words, all slice
 * criteria accepted by the slicing engine will implement this interface but not all objects implementing this interface can
 * be slicing criteria.  Hence, the user may use this interface to identify if an object is  a slicing criterion.  However,
 * he/she may not use this interface to provide a new implementation of the slicing criterion as the slicing engine places
 * certain requirements on the implementation of the criterion and this is not exposed to the user.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ISliceCriterion
  extends IPoolable {
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2004/06/24 06:53:53  venku
   - refactored SliceConfiguration
     - added processBooleanProperty()
     - renamed getNamesOfDAToUse() to getIDOfDAToUse()
   - ripple effect
   - made AbstractSliceCriterion package private
   - made ISliceCriterion public
   Revision 1.2  2004/05/10 08:12:03  venku
   - streamlined the names of tags that are used.
   - deleted SlicingTag class.  NamedTag is used instead.
   - ripple effect.
   - SliceCriteriaFactory's interface is enhanced to generate individual
     slice criterion as well as criteria set for all nodes in the given AST chunk.
   Revision 1.1  2003/12/13 02:29:16  venku
   - Refactoring, documentation, coding convention, and
     formatting.
 */
