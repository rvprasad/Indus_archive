
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

import edu.ksu.cis.indus.staticanalyses.support.WorkBag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A worklist implementation.
 * 
 * <p>
 * Created: Tue Jan 22 02:43:16 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class WorkList {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(WorkList.class);

	/**
	 * The backend workbag object which holds the work piece.
	 */
	private final WorkBag workbag;

	/**
	 * Creates a new <code>WorkList</code> instance.
	 */
	WorkList() {
		workbag = new WorkBag(WorkBag.LIFO);
	}

	/**
	 * Adds a work piece into the worklist.  If <code>w</code> exists in the worklist, it will not be added.  The existence
	 * check is done using <code>java.lang.Object.equals()</code> method.
	 *
	 * @param w the work to be added into the worklist.
	 *
	 * @pre w != null
	 */
	public final void addWork(final AbstractWork w) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Added new work:" + w);
		}
		workbag.addWorkNoDuplicates(w);
	}

	/**
	 * Removes any work in the work list without processing them.
	 */
	final void clear() {
		workbag.clear();
	}

	/**
	 * Executes the work pieces in the worklist.  This method returns when the worklist is empty, i.e., all the work peices
	 * have been executed.
	 */
	void process() {
		while (workbag.hasWork()) {
			AbstractWork w = (AbstractWork) workbag.getWork();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing work:" + w);
			}
			w.execute();
			w.finished();
		}
	}
}

/*
   ChangeLog:

   $Log$
   Revision 1.5  2003/08/18 11:08:10  venku
   Name change for pooling support.
   Revision 1.4  2003/08/17 11:19:13  venku
   Placed the simple SendValuesWork class into a separate file.
   Extended it with work pool support.
   Amended AbstractWork and WorkList to enable work pool support.
   Revision 1.3  2003/08/17 10:33:03  venku
   WorkList does not inherit from WorkBag rather contains an instance of WorkBag.
   Ripple effect of the above change.

   Revision 1.2  2003/08/15 04:07:56  venku
   Spruced up documentation and specification.
   - Important change is that previously all types of retype and nullconstant were let through.
     This is incorrect as there is not type filtering happening.  This has been fixed.  We now
     only let those that are not of the monitored type.

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.

   Revision 0.9  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
