
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
 * This interface helps realize the <i>IPrototype</i> design pattern as defined in the Gang of Four book. It provides the
 * methods via which concrete object can be created from a prototype object.  The default implementation for these methods
 * should raise <code>UnsupportedOperationException</code>.
 * 
 * <p>
 * Created: Sun Jan 27 18:04:58 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public interface IPrototype {
	/**
	 * Creates a concrete object from this prototype object.
	 *
	 * @return concrete object based on this prototype object.
	 *
	 * @throws UnsupportedOperationException when this operation is not supported.
	 */
	Object getClone();

	/**
	 * Creates a concrete object from this prototype object.  The concrete object can be parameterized by the information in
	 * <code>o</code>.
	 *
	 * @param o object containing the information to parameterize the concrete object.
	 *
	 * @return concrete object based on this prototype object.
	 *
	 * @throws UnsupportedOperationException when this operation is not supported.
	 *
	 * @pre o != null
	 */
	Object getClone(Object o);
}

/*
   ChangeLog:
   
   $Log$
   
   Revision 1.2  2003/08/15 02:54:06  venku
   Spruced up specification and documentation for flow-insensitive classes.
   Changed names in AbstractExprSwitch.
   Ripple effect of above change.
   Formatting changes to IPrototype.
   
   Revision 1.1  2003/08/12 18:33:41  venku
   Created an umbrella project to host generic interfaces related to design patterns.
   Moving prototype pattern interface under this umbrella.
   
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
    
   Revision 1.1  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
