
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

package edu.ksu.cis.bandera.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.bandera.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.bandera.staticanalyses.flow.WorkList;

import org.apache.log4j.Logger;

import java.util.Collection;


// FGAccessNode.java

/**
 * <p>
 * This class extends the flow graph node by associating a work peice with it.  This optimizes the worklist by adding new
 * values to the work peice already on the work list and not generating a new work peice.
 * </p>
 * Created: Tue Jan 22 04:30:32 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FGAccessNode
  extends OFAFGNode {
	/**
	 * <p>
	 * An instance of <code>Logger</code> used for logging purpose.
	 * </p>
	 */
	private static final Logger logger = Logger.getLogger(FGAccessNode.class.getName());

	/**
	 * <p>
	 * The work associated with this node.
	 * </p>
	 */
	private final AbstractWork work;

	/**
	 * <p>
	 * Creates a new <code>FGAccessNode</code> instance.
	 * </p>
	 *
	 * @param work the work peice associated with this node.
	 * @param worklist the worklist in which <code>work</code> will be placed.
	 */
	public FGAccessNode(AbstractWork work, WorkList worklist) {
		super(worklist);
		this.work = work;
	}

	/**
	 * <p>
	 * Adds the given value to the work peice for processing.
	 * </p>
	 *
	 * @param value the value that needs to be processed at the given node.
	 */
	public void onNewValue(Object value) {
		super.onNewValue(value);
		logger.debug("Value: " + value + "\nSuccessors: " + succs);
		work.addValue(value);
		worklist.addWork(work);
	}

	/**
	 * <p>
	 * Adds the given values to the work peice for processing.
	 * </p>
	 *
	 * @param values the collection of values that need to be processed at the given node.
	 */
	public void onNewValues(Collection values) {
		super.onNewValues(values);
		logger.debug("Values: " + values + "\nSuccessors: " + succs);
		work.addValues(values);
		worklist.addWork(work);
	}
}

/*****
 ChangeLog:

$Log$

*****/
