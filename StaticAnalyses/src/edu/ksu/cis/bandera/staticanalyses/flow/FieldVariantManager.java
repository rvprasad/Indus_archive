
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

import ca.mcgill.sable.soot.SootField;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


//FieldVariantManager.java

/**
 * <p>
 * This class manages field variants.  This class only provides the implementation to create new field variants.  The super
 * class is responsible of managing the variants.
 * </p>
 * 
 * <p>
 * Created: Fri Jan 25 14:33:09 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FieldVariantManager
  extends AbstractVariantManager {
	/**
	 * <p>
	 * An instance of <code>Logger</code> used for logging purposes.
	 * </p>
	 */
	private static final Logger logger = LogManager.getLogger(FieldVariantManager.class);

	/**
	 * <p>
	 * Creates a new <code>FieldVariantManager</code> instance.
	 * </p>
	 *
	 * @param bfa the instance of the framework in which this object is used. This parameter cannot be <code>null</code>.
	 * @param indexManager the manager of indices which are used to map fields to their variants.  This parameter cannot be
	 * 		  <code>null</code>.
	 */
	public FieldVariantManager(BFA bfa, AbstractIndexManager indexManager) {
		super(bfa, indexManager);
	}

	/**
	 * <p>
	 * Returns a new variant of the field represented by <code>o</code>.
	 * </p>
	 *
	 * @param o the field whose variant is to be returned.  The actual type of <code>o</code> needs to be
	 * 		  <code>SootField</code>.
	 *
	 * @return the variant associated with the field represetned by <code>o</code>.
	 */
	protected Variant getNewVariant(Object o) {
		return new FieldVariant((SootField) o, bfa.getNewFGNode());
	}
}

/*****
 ChangeLog:

$Log$

*****/
