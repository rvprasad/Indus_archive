
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

package edu.ksu.cis.indus.interfaces;

/**
 * This class is used to indicate the status of an object before dispatching any methods on it.  The intent of this interface
 * is that the users of information-providing interfaces should be able to check if the object providing that interface is
 * in a stable state to do so.  Hence, it is adviced that information-providing interfaces inherit this interface.  On a
 * general note,  this interface can be used in situations where a support to query status is required.
 * 
 * <p>
 * A note on stability.  In most cases, active entities like analysis may provide different answers for identical queries at
 * different times when they are active, i.e, when analysis is happening. However, while not active, it will usually provide
 * the same answer for identical queries at different times.  Nevertheless the answer may be incorrect, but the analysis is
 * consistent in it's answer. It is this state in which the analysis provide consistent answers that we refer to as stable
 * state,  a state in which the external behavior of an object will be consistent.  Hence, implementation can use
 * <code>isStable</code> to indicate/detect activeness.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface IStatus {
	/**
	 * Checks if the object is in a stable state so that it can be queried for information.
	 *
	 * @return <code>true</code> if the implementation is in a stable state; <code>false</code>, otherwise.
	 */
	boolean isStable();
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/08/21 03:30:34  venku
   Added a new interface to query stableness of objects.
   Analyses/Engine/Transformation objects in particular.
 */
