
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

package edu.ksu.cis.bandera.staticanalyses.interfaces;

import java.util.Collection;


/**
 * This interface provides the information pertaining to Java monitors in the analyzed system.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface MonitorInfo {
	/**
	 * The id of this interface.
	 */
	String ID = "Synchronization monitor Information";

	/**
	 * Returns a collection of <code>Triple</code>s of <code>EnterMonitorStmt</code>, <code>ExitMonitorStmt</code>, and
	 * <code>SootMethod</code>. The third element is the method in which the monitor occurs.  In case the first and the
	 * second element of the triple are <code>null</code> then this means the method is a synchronized.
	 *
	 * @return collection of monitors in the analyzed system.
	 *
	 * @post result->forall(o | o.oclIsKindOf(edu.ksu.cis.bandera.staticanalyses.support.Triple))
	 * @post result->forall(o | o.getFirst().oclIsOclKindOf(ca.mcgill.sable.soot.jimple.EnterMonitorStmt))
	 * @post result->forall(o | o.getSecond().oclIsOclKindOf(ca.mcgill.sable.soot.jimple.ExitMonitorStmt))
	 * @post result->forall(o | o.getThird().oclIsOclKindOf(ca.mcgill.sable.soot.SootMethod) && o.getThird() != null)
	 */
	Collection getMonitorTriples();
}

/*****
 ChangeLog:

$Log$

*****/
