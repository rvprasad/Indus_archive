
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
import soot.Value;

import soot.jimple.AssignStmt;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.escape.EquivalenceClassBasedAnalysis;
import edu.ksu.cis.indus.staticanalyses.support.Pair;


/**
 * This class uses escape-analysis information as calculated by {@link
 * edu.ksu.cis.indus.staticanalyses.escape.EquivalenceClassBasedAnalysis EquivalenceClassBasedAnalysis} to prune the
 * interference dependence edges as calculated by it's parent class.  This can be further spruced by symbolic-analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @see InterferenceDAv1
 */
public class InterferenceDAv2
  extends InterferenceDAv1 {
	/**
	 * This provide information shared access in the analyzed system.  This is required by the analysis.
	 */
	private EquivalenceClassBasedAnalysis ecba;

	/**
	 * Checks if the given array/field access expression is dependent on the given array/field definition expression.
	 *
	 * @param dependent is the array/field read access site.
	 * @param dependee is the array/field write access site.
	 *
	 * @return <code>true</code> if <code>dependent</code> is dependent on <code>dependee</code>; <code>false</code>,
	 * 		   otherwise.
	 *
	 * @pre dependee.getFirst() != null and dependee.getSecond() != null
	 * @pre dependee.getFirst().oclIsTypeOf(AssignStmt) and dependee.getSecond().oclIsTypeOf(SootMethod)
	 * @pre dependent.getFirst() != null and dependent.getSecond() != null
	 * @pre dependent.getFirst().oclIsTypeOf(AssignStmt) and dependent.getSecond().oclIsTypeOf(SootMethod)
	 */
	protected boolean ifDependentOn(final Pair dependent, final Pair dependee) {
		SootMethod deMethod = (SootMethod) dependee.getSecond();
		SootMethod dtMethod = (SootMethod) dependent.getSecond();
		Value de = ((AssignStmt) dependee.getFirst()).getLeftOp();
		Value dt = ((AssignStmt) dependent.getFirst()).getRightOp();
		return ecba.isShared(de, deMethod) && ecba.isShared(dt, dtMethod);
	}

	/**
	 * Extracts information provided by the environment via <code>info</code> parameter to {@link #initialize initialize}.
	 *
	 * @throws InitializationException when and instance of pair managing service or interference analysis is not provided.
	 *
	 * @pre info.get(EquivalenceClassBasedAnalysis.ID) != null
	 *
	 * @see InterferenceDAv1#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		ecba = (EquivalenceClassBasedAnalysis) info.get(EquivalenceClassBasedAnalysis.ID);

		if (pairMgr == null) {
			throw new InitializationException(EquivalenceClassBasedAnalysis.ID + " was not provided in info.");
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.6  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.5  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.4  2003/08/09 23:52:54  venku
   - import reorganization
   Revision 1.3  2003/08/09 23:46:11  venku
   Well if the read and write access points are marked as shared, then pessimistically
   they occur in different threads.  In such situation, sequential path between
   these points does not bear any effect unless the escape analysis is thread and
   call-tree sensitive.
 */
