
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

package edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.symbolic;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.IIndex;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.OneContextInfoIndex;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class manages indices associated with fields and array components  in allocation-site sensitive mode.  In reality, it
 * provides the implementation to create new indices.  Created: Tue Mar  5 14:08:18 2002.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class SymbolSensitiveIndexManager
  extends AbstractIndexManager {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SymbolSensitiveIndexManager.class);

	/**
	 * Returns a new instance of this class.
	 *
	 * @return a new instance of this class.
	 */
	public Object getClone() {
		return new SymbolSensitiveIndexManager();
	}

	/**
	 * Returns an index corresponding to the given entity and context.
	 *
	 * @param o the entity for which the index in required.  Although it is not enforced, this should be of type
	 *           <code>FielRef</code> or <code>ArrayRef</code>.
	 * @param c the context in which information pertaining to <code>o</code> needs to be captured.
	 *
	 * @return the index that uniquely identifies <code>o</code> in context, <code>c</code>.
	 *
	 * @pre c.oclIsKindof(SymbolicContext)
	 */
	protected IIndex getIndex(Object o, Context c) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Getting index for " + o + " in " + c);
		}

		SymbolicContext ctxt = (SymbolicContext) c;

		return new OneContextInfoIndex(o, ctxt.getAllocationSite());
	}
}

/*****
 ChangeLog:

$Log$
Revision 1.4  2003/05/22 22:18:32  venku
All the interfaces were renamed to start with an "I".
Optimizing changes related Strings were made.


*****/
