
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
	 * <p>
	 * An instance of <code>Logger</code> used for logging purpose.
	 * </p>
	 */
	protected Collection values;

	/**
	 * <p>
	 * The flow graph node associated with this work.
	 * </p>
	 */
	protected IFGNode node;

	/**
	 * <p>
	 * Creates a new <code>AbstractWork</code> instance.
	 * </p>
	 *
	 * @param node the flow graph node associated with this work.
	 * @param values the walues associated with this work.
	 */
	protected AbstractWork(IFGNode node, Collection values) {
		this.node = node;
		this.values = values;
	}

	/**
	 * <p>
	 * The actual work that needs to be done when this work is executed should be in this method.
	 * </p>
	 */
	public abstract void execute();

	/**
	 * <p>
	 * Associates a flow graph node with this work.
	 * </p>
	 *
	 * @param node the flow graph node to be associated.
	 */
	public final void setFGNode(IFGNode node) {
		this.node = node;
	}

	/**
	 * <p>
	 * Adds a value to the collection of values associated with this work.
	 * </p>
	 *
	 * @param o the value to be added.
	 */
	public final synchronized void addValue(Object o) {
		values.add(o);
	}

	/**
	 * <p>
	 * Adds a collection of values to the collection of values associated with this work.
	 * </p>
	 *
	 * @param values the collection of values to be added.
	 */
	public final synchronized void addValues(Collection values) {
		this.values.addAll(values);
	}
}

/*****
 ChangeLog:

$Log$
Revision 0.9  2003/05/22 22:18:50  venku
All the interfaces were renamed to start with an "I".
Optimizing changes related Strings were made.


*****/
