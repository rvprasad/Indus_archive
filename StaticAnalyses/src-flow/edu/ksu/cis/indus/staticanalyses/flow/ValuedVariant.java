
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

import java.util.Collection;


/**
 * <p>
 * IVariant of entities associated with data such as AST nodes and fields.  All such data related variants should extend this
 * class.
 * </p>
 *
 * <p>
 * Created: Tue Jan 22 15:44:48 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractValuedVariant
  implements IVariant {
	/**
	 * <p>
	 * The flow graph node associated with this variant.
	 * </p>
	 */
	protected IFGNode node;

	/**
	 * <p>
	 * Creates a new <code>AbstractValuedVariant</code> instance.
	 * </p>
	 *
	 * @param nodeParam the flow graph node associated with this variant.
	 */
	AbstractValuedVariant(IFGNode nodeParam) {
		this.node = nodeParam;
	}

	/**
	 * <p>
	 * Sets the given node as the flow graph node of this variant.
	 * </p>
	 *
	 * @param node the node to be set as the flow graph node of this variant.
	 */
	public void setFGNode(IFGNode node) {
		this.node = node;
	}

	/**
	 * <p>
	 * Returns the flow graph node associated with this node.
	 * </p>
	 *
	 * @return the flow graph node associated with this node.
	 */
	public IFGNode getFGNode() {
		return node;
	}

	/**
	 * <p>
	 * Returns the set of values associated with this variant.
	 * </p>
	 *
	 * @return the set of values associated with this variant.
	 */
	public final Collection getValues() {
		return node.getValues();
	}

	/**
	 * <p>
	 * Performs nothing.  This will be called after a variant is created.
	 * </p>
	 */
	public void process() {
	}
}

/*****
 ChangeLog:

$Log$
Revision 0.9  2003/05/22 22:18:31  venku
All the interfaces were renamed to start with an "I".
Optimizing changes related Strings were made.


*****/
