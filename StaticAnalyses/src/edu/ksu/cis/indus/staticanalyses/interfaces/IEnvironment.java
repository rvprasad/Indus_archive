
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

package edu.ksu.cis.indus.staticanalyses.interfaces;

import soot.SootClass;

import java.util.Collection;


/**
 * This interface exposes the information pertaining to the environment in which the analyses function. It provides the
 * non-functional information about the system.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IEnvironment {
	/**
	 * The id of this interface.
	 */
	String ID = "The Environment";

	/**
	 * Returns the Jimple representation of the given class.
	 *
	 * @param className is the name of the class whose Jimple representation is to be returned.
	 *
	 * @return the requested class.
	 *
	 * @post result.oclType = SootClass
	 */
	SootClass getClass(String className);

	/**
	 * Returns the classes that form the system.
	 *
	 * @return the classes the form the system.
	 *
	 * @post result->forall(o | o.oclType = SoptClass)
	 */
	Collection getClasses();

	/**
	 * Retrieves the methods that serve as the entry points or as "roots" of the system being analyzed.
	 *
	 * @return a collection of methods that are the "roots".
	 *
	 * @post result != null and result.oclIsKindOf(Collection(soot.SootMethod))
	 */
	Collection getRoots();
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/08/21 08:22:51  venku
   Changed ID value.
   Revision 1.3  2003/08/17 10:48:34  venku
   Renamed BFA to FA.  Also renamed bfa variables to fa.
   Ripple effect was huge.
   Revision 1.2  2003/08/11 07:11:47  venku
   Changed format of change log accumulation at the end of the file.
   Spruced up Documentation and Specification.
   Formatted source.
   Moved getRoots() into the environment.
   Added support to inject new roots in FA.
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 1.1  2003/05/22 22:16:45  venku
   All the interfaces were renamed to start with an "I".
 */
