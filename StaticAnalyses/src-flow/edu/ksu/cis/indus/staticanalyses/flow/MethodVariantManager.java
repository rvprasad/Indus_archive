
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

package edu.ksu.cis.indus.staticanalyses.flow;

import soot.SootClass;
import soot.SootMethod;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


/**
 * <p>
 * This class manages of  method variants.  This only provides the implementation to create new method variants.  The super
 * class is responsible of managing the variants.
 * </p>
 *
 * <p>
 * Created: Tue Jan 22 05:21:42 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class MethodVariantManager
  extends AbstractVariantManager {
	/**
	 * <p>
	 * An instance of <code>Logger</code> used for logging purposes.
	 * </p>
	 */
	private static final Logger LOGGER = LogManager.getLogger(MethodVariantManager.class);

	/**
	 * <p>
	 * A prototype object used to create index managers related to AST nodes.  Objects created via this prototype object are
	 * used by <code>MethodVariant</code>s to manage the variants corresponding to the AST nodes that exists in them.
	 * </p>
	 */
	protected final AbstractIndexManager astIndexManager;

	/**
	 * <p>
	 * Creates a new <code>MethodVariantManager</code> instance.
	 * </p>
	 *
	 * @param bfa the instance of the framework in which this object is used.  This parameter cannot be <code>null</code>.
	 * @param indexManager the manager to indices which are used to map methods to their variants.  This parameter cannot be
	 *           <code>null</code>.
	 * @param astIndexManager the prototype object used to create index managers related to AST nodes.  This parameter cannot
	 *           be <code>null</code>.
	 */
	MethodVariantManager(BFA bfa, AbstractIndexManager indexManager, AbstractIndexManager astIndexManager) {
		super(bfa, indexManager);
		this.astIndexManager = astIndexManager;
	}

	/**
	 * <p>
	 * Returns the class, starting from the given class and above it in the class hierarchy, that declares the given method.
	 * </p>
	 *
	 * @param sc the class from which to start the search in the class hierarchy.  This parameter cannot be
	 *           <code>null</code>.
	 * @param sm the method to search for in the class hierarchy.  This parameter cannot be <code>null</code>.
	 *
	 * @return the <code>SootMethod</code> corresponding to the implementation of <code>sm</code>.
	 *
	 * @throws IllegalStateException if <code>sm</code> is not available in the given branch of the class hierarchy.
	 */
	public static SootMethod findDeclaringMethod(SootClass sc, SootMethod sm) throws IllegalStateException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(sc + "." + sm.getName());
		}

		if (sc.declaresMethod(sm.getName(), sm.getParameterTypes(), sm.getReturnType())) {
			return sc.getMethod(sm.getName(), sm.getParameterTypes(), sm.getReturnType());
		} else if (sc.hasSuperclass()) {
			sc = sc.getSuperclass();

			return findDeclaringMethod(sc, sm);
		} else {
			throw new IllegalStateException("Method " + sm + " not available in class " + sc + ".");
		}
	}

	/**
	 * <p>
	 * Returns a new variant of the method represented by <code>o</code>.
	 * </p>
	 *
	 * @param o the method whose variant is to be returned.  The actual type of <code>o</code> needs to be
	 *           <code>SootMethod</code>.
	 *
	 * @return the new <code>MethodVariant</code> corresponding to method <code>o</code>.
	 */
	protected IVariant getNewVariant(Object o) {
		return new MethodVariant((SootMethod) o,
			new ASTVariantManager(bfa, (AbstractIndexManager) astIndexManager.getClone()), bfa);
	}
}

/*****
 ChangeLog:

$Log$
Revision 0.10  2003/05/22 22:18:31  venku
All the interfaces were renamed to start with an "I".
Optimizing changes related Strings were made.


*****/
