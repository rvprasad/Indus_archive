
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

/**
 * The super interface to be implemented by classes which connect AST nodes to Non-AST nodes.  An implementation of this
 * interface separates the logic of connecting AST and Non-AST nodes depending on whether the AST node corresponds to  a
 * r-value or l-value expression when constructing the flow graph.  This helps realize something similar to the
 * <i>Strategy</i> pattern as given in "Gang of Four" book.
 * 
 * <p>
 * Created: Wed Jan 30 15:18:24 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public interface IFGNodeConnector {
	/**
	 * Connects the given AST node to the Non-AST node.
	 *
	 * @param ast the AST node to be connected to the Non-AST node.
	 * @param nonast the Non-AST node to be connected to the AST node.
	 *
	 * @pre ast != null and nonast != null
	 */
	void connect(IFGNode ast, IFGNode nonast);
}

/*
   ChangeLog:
   
   $Log$
   
   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.
    
   Revision 1.1  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
