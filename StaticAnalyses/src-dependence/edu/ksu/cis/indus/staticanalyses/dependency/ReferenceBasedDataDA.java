
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import soot.SootMethod;

import soot.jimple.AssignStmt;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.IUseDefInfo;

import java.util.Collection;


/**
 * This class provides data dependence information which considers the effects of aliasing. It is an adapter for an
 * interprocedural use-def analysis which considers the effects of aliasing.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class AliasedDataDA
  extends DependencyAnalysis {
	/**
	 * This provides inter-procedural use-def information which considers the effects of aliasing.
	 */
	protected IUseDefInfo aliasedUD;

	/**
	 * A cache context object to be used to retrieve information from <code>interProceduralUD</code>.
	 */
	private final Context contextCache = new Context();

	/**
	 * Return the statements on which field/array access in <code>stmt</code> in <code>method</code> depends on.
	 *
	 * @param stmt in which aliased data is read.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statements which affect the data being read in <code>stmt</code>.
	 *
	 * @post result.oclIsKindOf(Pair(AssignStmt, SootMethod))
	 * @post result->forall(o | o.getFirst().getLeftOf().oclIsKindOf(FieldRef) or
	 * 		 o.getFirst().getLeftOf().oclIsKindOf(ArrayRef))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependees(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(final Object stmt, final Object method) {
		contextCache.setRootMethod((SootMethod) method);
		return aliasedUD.getDefs((AssignStmt) stmt, contextCache);
	}

	/**
	 * Return the statements which depend on the field/array access in <code>stmt</code> in <code>method</code>.
	 *
	 * @param stmt in which aliased data is written.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statements which are affectted by the data write in <code>stmt</code>.
	 *
	 * @post result.oclIsKindOf(Pair(AssignStmt, SootMethod))
	 * @post result->forall(o | o.getFirst().getRightOf().oclIsKindOf(FieldRef) or
	 * 		 o.getFirst().getRightOf().oclIsKindOf(ArrayRef))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependents(java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(final Object stmt, final Object method) {
		contextCache.setRootMethod((SootMethod) method);
		return aliasedUD.getUses((AssignStmt) stmt, contextCache);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#analyze()
	 */
	public boolean analyze() {
		return true;
	}

	/**
	 * Extracts information provided by environment at initialization time.
	 *
	 * @throws InitializationException if an implementation that provides aliased interprocedural use-def information is not
	 * 		   provided.
	 *
	 * @pre info.get(IUseDefInfo.ID) != null
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		aliasedUD = (IUseDefInfo) info.get(IUseDefInfo.ID);

		if (aliasedUD == null) {
			throw new InitializationException(IUseDefInfo.ID + " was not provided.");
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file

   Revision 1.2  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.1  2003/08/09 23:29:09  venku
   Renamed InterProceduralDataDAv1 to AliasedDataDA
   Renamed IntraProceduralDataDA to NonAliasedDataDA
 */
