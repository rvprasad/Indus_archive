
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

package edu.ksu.cis.bandera.staticanalyses.dependency.controller;

import edu.ksu.cis.bandera.staticanalyses.ProcessingController;
import edu.ksu.cis.bandera.staticanalyses.interfaces.CallGraphInfo;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;


/**
 * Call-Graph-based pre- or post-processing controller.  This only processes reachable methods.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class CGBasedProcessingController
  extends ProcessingController {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private CallGraphInfo cgi;

	/**
	 * Creates a new CGBasedProcessingController object.
	 *
	 * @param cgi DOCUMENT ME!
	 */
	public CGBasedProcessingController(CallGraphInfo cgi) {
		this.cgi = cgi;
	}

	/**
	 * Processes only those methods which are reachable in the given system.
	 *
	 * @see edu.ksu.cis.bandera.staticanalyses.flow.ProcessingController#processMethods(java.util.Collection)
	 */
	protected void processMethods(Collection methods) {
		super.processMethods(CollectionUtils.intersection(methods, cgi.getReachableMethods()));
	}
}

/*****
 ChangeLog:

$Log$

*****/
