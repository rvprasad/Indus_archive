
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

/**
 * This is a marker interface via which slice criterion is exposed to the external world.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ISliceCriterion {
}

/*
   ChangeLog:
   $Log$
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
