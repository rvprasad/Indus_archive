
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

import java.util.Collection;


/**
 * This class represents the variants of entities associated with AST nodes and fields.  This class should be extended as
 * required for such entities.
 * 
 * <p>
 * Created: Tue Jan 22 15:44:48 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class ValuedVariant
  implements IVariant {
	/**
	 * <p>
	 * The flow graph node associated with this variant.
	 * </p>
	 */
	protected IFGNode node;

	/**
	 * Creates a new <code>ValuedVariant</code> instance.
	 *
	 * @param flowNode the flow graph node associated with this variant.
	 *
	 * @pre flowNode != null
	 */
	ValuedVariant(final IFGNode flowNode) {
		this.node = flowNode;
	}

	/**
	 * Sets the given node as the flow graph node of this variant.
	 *
	 * @param flowNode the node to be set as the flow graph node of this variant.
	 *
	 * @pre flowNode != null
	 */
	public void setFGNode(final IFGNode flowNode) {
		this.node = flowNode;
	}

	/**
	 * Returns the flow graph node associated with this node.
	 *
	 * @return the flow graph node associated with this node.
	 *
	 * @post result != null
	 */
	public IFGNode getFGNode() {
		return node;
	}

	/**
	 * Returns the set of values associated with this variant.
	 *
	 * @return the set of values associated with this variant.
	 *
	 * @post result != null
	 */
	public final Collection getValues() {
		return node.getValues();
	}

	/**
	 * Performs nothing.  This will be called after a variant is created and should be implemented by subclasses.
	 */
	public void process() {
	}
}

/*
   ChangeLog:
   
   $Log$
   
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
    
   Revision 0.9  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
