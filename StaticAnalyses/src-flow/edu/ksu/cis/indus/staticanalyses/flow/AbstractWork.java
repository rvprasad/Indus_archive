
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

import java.util.ArrayList;
import java.util.Collection;


/**
 * A piece of work that can be processed by <code>WorkList</code>.
 * 
 * <p>
 * Created: Tue Jan 22 02:54:57 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public abstract class AbstractWork {
	/**
	 * The collection of values to be processed.
	 */
	protected final Collection values = new ArrayList();

	/**
	 * The flow graph node associated with this work.
	 */
	protected IFGNode node;

	/**
	 * Creates a new <code>AbstractWork</code> instance.
	 */
	protected AbstractWork() {
	}

	/**
	 * The actual work that needs to be done when this work is executed should be in this method.
	 */
	public abstract void execute();

	/**
	 * Associates a flow graph node with this work.
	 *
	 * @param flowNode the flow graph node to be associated.
	 *
	 * @pre flowNode != null
	 */
	public final void setFGNode(final IFGNode flowNode) {
		this.node = flowNode;
	}

	/**
	 * Adds a value to the collection of values associated with this work.
	 *
	 * @param o the value to be added.
	 *
	 * @pre o != null
	 */
	public final void addValue(final Object o) {
		values.add(o);
	}

	/**
	 * Adds a collection of values to the collection of values associated with this work.
	 *
	 * @param valuesToBeProcessed the collection of values to be added for processing.
	 *
	 * @pre valuesToBeProcessed != null
	 */
	public final void addValues(final Collection valuesToBeProcessed) {
		this.values.addAll(valuesToBeProcessed);
	}

	/**
	 * This will be called by the worklit after this work has been done.
	 */
	protected void doneExecuting() {
	}
}

/*
   ChangeLog:

   $Log$

   Revision 1.2  2003/08/17 09:59:03  venku
   Spruced up documentation and specification.
   Documentation changes to FieldVariant.

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.

   Revision 0.9  2003/05/22 22:18:50  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
