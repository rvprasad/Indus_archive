
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

import soot.SootField;


/**
 * This class manages field variants.  This class only provides the implementation to create new field variants.  The super
 * class is responsible of managing the variants.
 * 
 * <p>
 * Created: Fri Jan 25 14:33:09 2002
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class FieldVariantManager
  extends AbstractVariantManager {
	/**
	 * Creates a new <code>FieldVariantManager</code> instance.
	 *
	 * @param theAnalysis the instance of the framework in which this object is used.
	 * @param indexManager the manager of indices which are used to map fields to their variants.
	 *
	 * @pre theAnalysis != null and indexManager != null
	 */
	public FieldVariantManager(final BFA theAnalysis, final AbstractIndexManager indexManager) {
		super(theAnalysis, indexManager);
	}

	/**
	 * Returns a new variant of the field represented by <code>o</code>.
	 *
	 * @param o the field whose variant is to be returned.  
	 *
	 * @return the variant associated with the field represetned by <code>o</code>.
	 *
	 * @pre o != null and o.oclIsKindOf(SootField)
	 */
	protected IVariant getNewVariant(final Object o) {
		return new FieldVariant((SootField) o, bfa.getNewFGNode());
	}
}

/*
   ChangeLog:

   $Log$

   Revision 1.1  2003/08/07 06:40:24  venku
   Major:
    - Moved the package under indus umbrella.

   Revision 0.7  2003/05/22 22:18:32  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
