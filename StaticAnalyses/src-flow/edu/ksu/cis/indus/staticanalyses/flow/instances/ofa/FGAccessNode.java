
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractWork;
import edu.ksu.cis.indus.staticanalyses.flow.WorkList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collection;


/**
 * This class extends the flow graph node by associating a work peice with it.  This optimizes the worklist by adding new
 * values to the work peice already on the work list and not generating a new work peice.
 * 
 * <p>
 * Created: Tue Jan 22 04:30:32 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FGAccessNode
  extends OFAFGNode {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(FGAccessNode.class);

	/**
	 * The work associated with this node.
	 *
	 * @invariant work != null
	 */
	private final AbstractWork work;

	/**
	 * Creates a new <code>FGAccessNode</code> instance.
	 *
	 * @param workPeice the work peice associated with this node.
	 * @param worklistToUse the worklist in which <code>work</code> will be placed.
	 *
	 * @pre workPeice != null and worklistToUse != null
	 */
	public FGAccessNode(final AbstractWork workPeice, final WorkList worklistToUse) {
		super(worklistToUse);
		this.work = workPeice;
	}

	/**
	 * Adds the given value to the work peice for processing.
	 *
	 * @param newValue the value that needs to be processed at the given node.
	 *
	 * @pre newValue != null
	 */
	public void onNewValue(final Object newValue) {
		super.onNewValue(newValue);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Value: " + newValue + "\nSuccessors: " + succs);
		}
		work.addValue(newValue);
		worklist.addWork(work);
	}

	/**
	 * Adds the given values to the work peice for processing.
	 *
	 * @param newValues the collection of values that need to be processed at the given node.
	 *
	 * @pre newValues != null and newValues
	 */
	public void onNewValues(final Collection newValues) {
		super.onNewValues(newValues);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Values: " + newValues + "\nSuccessors: " + succs);
		}
		work.addValues(newValues);
		worklist.addWork(work);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/08/15 03:39:53  venku
   Spruced up documentation and specification.
   Tightened preconditions in the interface such that they can be loosened later on in implementaions.
   Renamed a few fields/parameter variables to avoid name confusion.
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
   Revision 1.6  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
