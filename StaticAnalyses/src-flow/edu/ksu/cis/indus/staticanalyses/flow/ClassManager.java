
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

import soot.SootClass;

import edu.ksu.cis.indus.interfaces.IPrototype;
import edu.ksu.cis.indus.staticanalyses.Context;

import java.util.Collection;
import java.util.HashSet;


/**
 * This class manages class related primitive information and processing such as the processing of <code>&lt;
 * clinit&gt;</code> methods of classes being analyzed.
 * 
 * <p>
 * Created: Fri Mar  8 14:10:27 2002.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ClassManager
  implements IPrototype {
	/**
	 * The instance of the framework in which this object is used.
	 *
	 * @pre bfa != null
	 */
	protected final BFA bfa;

	/**
	 * The collection of classes for which the information has been processed.
	 */
	protected final Collection classes;

	/**
	 * Describe variable <code>context</code> here.
	 *
	 * @invariant context != null
	 */
	protected final Context context;

	/**
	 * Creates a new <code>ClassManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.  This cannot be <code>null</code>.
	 *
	 * @pre theAnalysis != null
	 */
	public ClassManager(final BFA theAnalysis) {
		classes = new HashSet();
		this.bfa = theAnalysis;
		context = new Context();
	}

	/**
	 * Creates a concrete object of the same class as this object but parameterized by <code>o</code>.
	 *
	 * @param o the instance of the analysis for which this object shall process information.  The actual type of
	 * 		  <code>o</code> needs to be <code>BFA</code>.
	 *
	 * @return an instance of <code>ClassManager</code> object parameterized by <code>o</code>.
	 *
	 * @pre o != null
	 * @post result != null
	 */
	public Object getClone(final Object o) {
		return new ClassManager((BFA) o);
	}

	/**
	 * This method is not supported by this class.
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException this method is not supported by this class.
	 */
	public Object getClone() {
		throw new UnsupportedOperationException("Parameterless prototype() method not supported.");
	}

	/**
	 * Processes the given class for assimilating class related primitive information into the analysis.  This implementation
	 * hooks in the class initialization method into the analysis.
	 *
	 * @param sc the class to be processed.  This cannot be <code>null</code>.
	 *
	 * @pre sc != null
	 */
	protected void process(final SootClass sc) {
		if (!classes.contains(sc)) {
			classes.add(sc);

			if (sc.declaresMethod("<clinit>")) {
				context.setRootMethod(sc.getMethod("<clinit>"));
				bfa.getMethodVariant(sc.getMethod("<clinit>"), context);
			}

			while (sc.hasSuperclass()) {
				SootClass temp = sc.getSuperclass();

				if (temp.declaresMethod("<clinit>")) {
					context.setRootMethod(temp.getMethod("<clinit>"));
					bfa.getMethodVariant(temp.getMethod("<clinit>"), context);
				}
			}
		}
	}

	/**
	 * Resets the manager.  Removes all information maintained about any classes.
	 */
	protected void reset() {
		classes.clear();
	}
}

/*
   ChangeLog:
   
   $Log$
   
   Revision 1.2  2003/08/12 18:39:56  venku
   Ripple effect of moving IPrototype to Indus.
   
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
    
   Revision 1.8  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
