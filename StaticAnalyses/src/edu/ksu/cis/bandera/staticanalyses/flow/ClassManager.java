
/*
 * Bandera, a Java(TM) analysis and transformation toolkit
 * Copyright (C) 2002, 2003, 2004.
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://www.cis.ksu.edu/santos/bandera).
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
 *                http://www.cis.ksu.edu/santos/bandera
 */

package edu.ksu.cis.bandera.staticanalyses.flow;

import ca.mcgill.sable.soot.SootClass;

import edu.ksu.cis.bandera.staticanalyses.flow.Prototype;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;


//ClassManager.java

/**
 * <p>
 * This class manages class related primitive information and processing such as the processing of <code>&lt;
 * clinit&gt;</code> methods of classes being analyzed.
 * </p>
 * 
 * <p>
 * Created: Fri Mar  8 14:10:27 2002.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ClassManager
  implements Prototype {
	/**
	 * <p>
	 * An instance of <code>Logger</code> used for logging purpose.
	 * </p>
	 */
	private static final Logger logger = LogManager.getLogger(ClassManager.class);

	/**
	 * <p>
	 * The instance of the framework in which this object is used.
	 * </p>
	 */
	protected final BFA bfa;

	/**
	 * <p>
	 * The collection of classes for which the information has been processed.
	 * </p>
	 */
	protected final Collection classes;

	/**
	 * <p>
	 * Describe variable <code>context</code> here.
	 * </p>
	 */
	protected final Context context;

	/**
	 * <p>
	 * Creates a new <code>ClassManager</code> instance.
	 * </p>
	 *
	 * @param bfa the instance of the framework in which this object is used.  This cannot be <code>null</code>.
	 */
	public ClassManager(BFA bfa) {
		classes = new HashSet();
		this.bfa = bfa;
		context = new Context();
	}

	/**
	 * <p>
	 * Creates a concrete object of the same class as this object but parameterized by <code>o</code>.
	 * </p>
	 *
	 * @param o the instance of the analysis for which this object shall process information.  The actual type of
	 * 		  <code>o</code> needs to be <code>BFA</code>.  This cannot be <code>null</code>.
	 *
	 * @return an instance of <code>ClassManager</code> object parameterized by <code>o</code>.
	 */
	public Object prototype(Object o) {
		return new ClassManager((BFA) o);
	}

	/**
	 * <p>
	 * This method is not supported by this class.
	 * </p>
	 *
	 * @return (This method raises an exception.)
	 *
	 * @throws UnsupportedOperationException this method is not supported by this class.
	 */
	public Object prototype() {
		throw new UnsupportedOperationException("Parameterless prototype() method not supported.");
	}

	/**
	 * <p>
	 * Processes the given class for assimilating class related primitive information into the analysis.  This implementation
	 * hooks in the class initialization method into the analysis.
	 * </p>
	 *
	 * @param sc the class to be processed.  This cannot be <code>null</code>.
	 */
	protected void process(SootClass sc) {
		if(!classes.contains(sc)) {
			classes.add(sc);

			if(sc.declaresMethod("<clinit>")) {
				context.setRootMethod(sc.getMethod("<clinit>"));
				bfa.getMethodVariant(sc.getMethod("<clinit>"), context);
			}

			// end of if (sc.declaresMethod("<clinit>"))
			while(sc.hasSuperClass()) {
				sc = sc.getSuperClass();

				if(sc.declaresMethod("<clinit>")) {
					context.setRootMethod(sc.getMethod("<clinit>"));
					bfa.getMethodVariant(sc.getMethod("<clinit>"), context);
				}

				// end of if (sc.declaresMethod("<clinit>"))
			}

			// end of while (sc.hasSuperClass())
		}

		// end of if (!classes.contains(sc))
	}

	/**
	 * <p>
	 * Resets the manager.  Removes all information maintained about any classes.
	 * </p>
	 */
	protected void reset() {
		classes.clear();
	}
}

/*****
 ChangeLog:

$Log$

*****/
