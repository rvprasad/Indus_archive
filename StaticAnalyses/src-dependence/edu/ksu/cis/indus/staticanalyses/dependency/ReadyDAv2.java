
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

import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.InvokeStmt;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.escape.EquivalenceClassBasedAnalysis;
import edu.ksu.cis.indus.staticanalyses.support.Pair;
import edu.ksu.cis.indus.staticanalyses.support.Pair.PairManager;


/**
 * This class uses symbolic-analysis as calculated by <code>EquivalenceClassBasedAnalysis</code> to prune the ready
 * dependency information calculated by it's parent class.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @see edu.ksu.cis.indus.staticanalyses.dependency.ReadyDAv1
 */
public class ReadyDAv2
  extends ReadyDAv1 {
	/**
	 * This provides information to prune ready dependence edges.
	 */
	private EquivalenceClassBasedAnalysis ecba;

	/**
	 * @see ReadyDAv1
	 */
	public ReadyDAv2(final boolean acrossMethodCalls) {
		super(acrossMethodCalls);
	}

	/**
	 * Checks if the given enter-monitor statement is dependent on the exit-monitor statement according to rule 2. The
	 * results of a <code>EquivalenceClassbasedAnalysis</code>analysis is used to determine the dependence.
	 *
	 * @param enterPair is the enter monitor statement.
	 * @param exitPair is the exit monitor statement.
	 *
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 *
	 * @pre enterPair.getSecond() != null and exitPair.getSecond() != null
	 *
	 * @see ReadyDAv1#ifDependentOnByRule2(Pair, Pair)
	 */
	protected boolean ifDependentOnByRule2(final Pair enterPair, final Pair exitPair) {
		EnterMonitorStmt enter = (EnterMonitorStmt) enterPair.getFirst();
		ExitMonitorStmt exit = (ExitMonitorStmt) exitPair.getFirst();
		SootMethod enterMethod = (SootMethod) enterPair.getSecond();
		SootMethod exitMethod = (SootMethod) exitPair.getSecond();
		return ecba.isReadyDependent(exit, exitMethod, enter, enterMethod);
	}

	/**
	 * Checks if the given <code>wait()</code> call-site is dependent on the <code>notifyXX()</code> call-site according to
	 * rule 2.  The results of a <code>EquivalenceClassbasedAnalysis</code>analysis is used to determine the dependence.
	 *
	 * @param wPair is the statement in which <code>java.lang.Object.wait()</code> is invoked.
	 * @param nPair is the statement in which <code>java.lang.Object.notifyXX()</code> is invoked.
	 *
	 * @return <code>true</code> if there is a dependence; <code>false</code>, otherwise.
	 *
	 * @pre wPair.getSecond() != null and nPair.getSecond() != null
	 *
	 * @see ReadyDAv1#ifDependentOnByRule4(Pair, Pair)
	 */
	protected boolean ifDependentOnByRule4(final Pair wPair, final Pair nPair) {
		InvokeStmt notify = (InvokeStmt) nPair.getFirst();
		InvokeStmt wait = (InvokeStmt) wPair.getFirst();
		SootMethod wMethod = (SootMethod) wPair.getSecond();
		SootMethod nMethod = (SootMethod) nPair.getSecond();
		return ecba.isReadyDependent(wait, wMethod, notify, nMethod);
	}

	/**
	 * Extracts information as provided by environment at initialization time.  It collects <code>wait</code> and
	 * <code>notifyXX</code> methods as represented in the AST system. It also extract call graph info, pair manaing
	 * service, and environment from the <code>info</code> member.
	 *
	 * @throws InitializationException when call graph info, pair managing service, or environment is not available in
	 * 		   <code>info</code> member.
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();

		ecba = (EquivalenceClassBasedAnalysis) info.get(EquivalenceClassBasedAnalysis.ID);

		if (ecba == null) {
			throw new InitializationException(PairManager.ID + " was not provided in info.");
		}
	}

	/**
	 * Processes the system as per to rule 2 in the report.  This uses escape analysis results from
	 * <code>EquivalenceClassBasedAnalysis</code> to prune ready dependency edges.
	 */

	/*
	   private void processRule2() {
	       Collection temp = new HashSet();
	       for (Iterator i = exitMonitors.entrySet().iterator(); i.hasNext();) {
	           Map.Entry entry = (Map.Entry) i.next();
	           Object method = entry.getKey();
	           for (Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
	               Object o = j.next();
	               dependeeMap.put(o, Collections.EMPTY_LIST);
	               temp.add(pairMgr.getPair(method, o));
	           }
	       }
	       Collection nSet = new ArrayList();
	       Collection xSet = new ArrayList();
	       for (Iterator i = enterMonitors.entrySet().iterator(); i.hasNext();) {
	           Map.Entry entry = (Map.Entry) i.next();
	           SootMethod enterMethod = (SootMethod) entry.getKey();
	           for (Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
	               EnterMonitorStmt enter = (EnterMonitorStmt) j.next();
	               Pair enterPair = pairMgr.getPair(enter, enterMethod);
	               // This iteration adds dependency between enter-exit pair of a monitor block which may be false.
	               for (Iterator k = temp.iterator(); k.hasNext();) {
	                   Pair exitPair = (Pair) k.next();
	                   SootMethod exitMethod = (SootMethod) exitPair.getSecond();
	                   ExitMonitorStmt exit = (ExitMonitorStmt) exitPair.getFirst();
	                   xSet.clear();
	                   if () {
	                       xSet.add(enterPair);
	                       nSet.add(exitPair);
	                   }
	                   if (!xSet.isEmpty()) {
	                       Collection exitSet = (Collection) dependeeMap.get(exit);
	                       if (exitSet == null) {
	                           exitSet = new ArrayList();
	                           dependeeMap.put(exit, exitSet);
	                       }
	                       exitSet.addAll(xSet);
	                   }
	               }
	               if (nSet.isEmpty()) {
	                   dependentMap.put(enter, Collections.EMPTY_LIST);
	               } else {
	                   dependentMap.put(enter, nSet);
	                   nSet = new ArrayList();
	               }
	           }
	       }
	   }
	 */

	/**
	 * Processes the system as per to rule 4 in the report.  This uses results from
	 * <code>EquivalenceClassBasedAnalysis</code> to calculate ready dependency.
	 */

	/*
	   private void processRule4() {
	       Collection dependents = new HashSet();
	       for (Iterator i = waits.entrySet().iterator(); i.hasNext();) {
	           Map.Entry entry = (Map.Entry) i.next();
	           SootMethod wMethod = (SootMethod) entry.getKey();
	           for (Iterator j = ((Collection) entry.getValue()).iterator(); j.hasNext();) {
	               InvokeStmt wait = (InvokeStmt) j.next();
	               for (Iterator k = notifies.keySet().iterator(); k.hasNext();) {
	                   entry = (Map.Entry) k.next();
	                   SootMethod nMethod = (SootMethod) entry.getKey();
	                   for (Iterator l = ((Collection) entry.getValue()).iterator(); l.hasNext();) {
	                       InvokeStmt notify = (InvokeStmt) l.next();
	                       if (ecba.isReadyDependent(wait, wMethod, notify, nMethod)) {
	                           Collection temp = (Collection) dependeeMap.get(notify);
	                           if (temp == null) {
	                               temp = new HashSet();
	                               dependeeMap.put(notify, temp);
	                           }
	                           temp.add(pairMgr.getPair(wait, wMethod));
	                           dependents.add(pairMgr.getPair(notify, nMethod));
	                       }
	                   }
	               }
	               if (dependents.size() == 0) {
	                   dependentMap.put(wait, Collections.EMPTY_LIST);
	               } else {
	                   dependentMap.put(wait, dependents);
	                   dependents = new HashSet();
	               }
	           }
	       }
	   }
	 */
}

/*
   ChangeLog:
   $Log$
   Revision 1.4  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file

   Revision 1.3  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.2  2003/08/09 23:33:30  venku
    - Enabled ready dependency to be interprocedural.
    - Utilized containsXXX() method in Stmt.
 */
