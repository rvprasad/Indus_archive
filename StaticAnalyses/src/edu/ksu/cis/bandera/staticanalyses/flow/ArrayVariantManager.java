
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

import ca.mcgill.sable.soot.ArrayType;


/**
 * <p>
 * This class manages variants corresponding to arrays.
 * </p>
 *
 * <p>
 * Created: Fri Jan 25 13:50:16 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ArrayVariantManager
  extends AbstractVariantManager {
	/**
	 * <p>
	 * Creates a new <code>ArrayVariantManager</code> instance.
	 * </p>
	 *
	 * @param bfa the instance of the framework in which this object is used.  This cannot be <code>null</code>.
	 * @param indexManager the manager of indices which map array variants to arrays.  This cannot be <code>null</code>.
	 */
	ArrayVariantManager(BFA bfa, AbstractIndexManager indexManager) {
		super(bfa, indexManager);
	}

	/**
	 * <p>
	 * Returns a new array variant corresponding to the given array type.
	 * </p>
	 *
	 * @param o the <code>ArrayType</code> whose variant is to be returned.
	 *
	 * @return a new <code>ArrayVariant</code> corresponding to <code>o</code>.
	 */
	protected IVariant getNewVariant(Object o) {
		return new ArrayVariant((ArrayType) o, bfa.getNewFGNode());
	}
}

/*****
 ChangeLog:

$Log$

*****/
