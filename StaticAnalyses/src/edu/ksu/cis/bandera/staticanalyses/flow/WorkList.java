
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

import edu.ksu.cis.bandera.staticanalyses.support.WorkBag;

import org.apache.log4j.Logger;


// WorkList.java

/**
 * <p>
 * A worklist implementation.
 * </p>
 * 
 * <p>
 * Created: Tue Jan 22 02:43:16 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class WorkList
  extends WorkBag {
	/**
	 * <p>
	 * An instance of <code>Logger</code> used for logging purposes.
	 * </p>
	 */
	private static final Logger logger = Logger.getLogger(WorkList.class);

	/**
	 * <p>
	 * Creates a new <code>WorkList</code> instance.
	 * </p>
	 */
	WorkList() {
		super(LIFO);
	}

	/**
	 * <p>
	 * Adds a work piece into the worklist.  If <code>w</code> exists in the worklist, it will not be added.  The existence
	 * check is done using <code>java.lang.Object.equals()</code> method.
	 * </p>
	 *
	 * @param w the work to be added into the worklist.
	 */
	public final void addWork(AbstractWork w) {
		logger.debug("Added new work:" + w);
		addWorkNoDuplicates(w);
	}

	/**
	 * <p>
	 * Executes the work pieces in the worklist.  This method returns when the worklist is empty, i.e., all the work peices
	 * have been executed.
	 * </p>
	 */
	void process() {
		while(!isEmpty()) {
			AbstractWork w = (AbstractWork) getWork();
			logger.debug("Processing work:" + w);
			w.execute();
		}

		// end of while (!list.isEmpty())
	}
}

/*****
 ChangeLog:

$Log$

*****/
