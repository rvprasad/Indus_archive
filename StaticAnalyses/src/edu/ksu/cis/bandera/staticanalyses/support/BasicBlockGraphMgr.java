
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

package edu.ksu.cis.bandera.staticanalyses.support;

import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.JimpleBody;
import ca.mcgill.sable.soot.jimple.StmtGraph;

import java.lang.ref.WeakReference;

import java.util.HashMap;
import java.util.Map;


/**
 * This class manages a set of <code>BasicBlockGraph</code> instances.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class BasicBlockGraphMgr {
	/**
	 * This maps methods(<code>SootMethod</code>) to <code>BasicBlockGraph</code>s.
	 */
	private final Map method2graph = new HashMap();

	/**
	 * Retrieves the basic block graph corresonding to the given control flow graph.
	 *
	 * @param stmtGraph is the control flow graph of interest.
	 *
	 * @return the basic block graph corresonding to <code>stmtGraph</code>.
	 *
	 * @post result &lt;> null
	 */
	public BasicBlockGraph getBasicBlockGraph(StmtGraph stmtGraph) {
		SootMethod method = ((JimpleBody) stmtGraph.getBody()).getMethod();
		WeakReference ref = (WeakReference) method2graph.get(method);

		if(ref == null || ref.get() == null) {
			ref = new WeakReference(new BasicBlockGraph(stmtGraph));
			method2graph.put(method, ref);
		}
		return (BasicBlockGraph)ref.get();
	}
}

/*****
 ChangeLog:

$Log$

*****/
