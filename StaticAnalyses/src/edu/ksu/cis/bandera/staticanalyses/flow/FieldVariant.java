
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


//FieldVariant.java

/**
 * <p>
 * The variant that represents a field.
 * </p>
 * 
 * <p>
 * Created: Fri Jan 25 14:29:09 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FieldVariant
  extends AbstractValuedVariant {
	/**
	 * <p>
	 * An instance of <code>Logger</code> used for logging purpose.
	 * </p>
	 */
	private static final Logger logger = LogManager.getLogger(FieldVariant.class);

	/**
	 * <p>
	 * The field represented by this variant.
	 * </p>
	 */
	public final SootField field;

	/**
	 * <p>
	 * Creates a new <code>FieldVariant</code> instance.
	 * </p>
	 *
	 * @param field the field to be represented by this variant.  This cannot be <code>null</code>.
	 * @param node the node associated with this variant.  This cannot be <code>null</code>.
	 */
	public FieldVariant(SootField field, FGNode node) {
		super(node);
		this.field = field;
	}
}

/*****
 ChangeLog:

$Log$

*****/
