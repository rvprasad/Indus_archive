
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

package edu.ksu.cis.bandera.staticanalyses.dependency;

import ca.mcgill.sable.soot.SootMethod;

import ca.mcgill.sable.soot.jimple.AssignStmt;
import ca.mcgill.sable.soot.jimple.Value;

import edu.ksu.cis.bandera.staticanalyses.InitializationException;
import edu.ksu.cis.bandera.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.bandera.staticanalyses.interference.EquivalenceClassBasedAnalysis;
import edu.ksu.cis.bandera.staticanalyses.support.Pair;
import edu.ksu.cis.bandera.staticanalyses.support.Pair.PairManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


/**
 * This class does a very naive interference dependency calculation.  For fields, it assumes any assignment to a field can
 * affect  any reference to the same field.  This is imprecise in the light of thread local objects and unrelated primaries.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class InterferenceDAv2
  extends InterferenceDAv1 {
	/**
	 * This provide information shared access in the analyzed system.  This is required by the analysis.
	 */
	private EquivalenceClassBasedAnalysis ecba;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IThreadGraphInfo tgi;

	/**
	 * @see edu.ksu.cis.bandera.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public boolean analyze() {
		// we return immediately if there are no start sites in the system.
		if (tgi.getStartSites().size() == 0) {
			return true;
		}

		for (Iterator i = dependeeMap.keySet().iterator(); i.hasNext();) {
			Object o = i.next();

			if (dependentMap.get(o) == null) {
				continue;
			}

			Map deMap = (Map) dependeeMap.get(o);
			Map dtMap = (Map) dependentMap.get(o);

			for (Iterator j = deMap.keySet().iterator(); j.hasNext();) {
				Pair p1 = (Pair) j.next();
				Value de = ((AssignStmt) p1.getFirst()).getRightOp();

				for (Iterator k = dtMap.keySet().iterator(); k.hasNext();) {
					Pair p2 = (Pair) k.next();
					Value dt = ((AssignStmt) p2.getFirst()).getLeftOp();

					if (ecba.isShared(de, (SootMethod) p1.getSecond()) && ecba.isShared(dt, (SootMethod) p2.getSecond())) {
						Collection t = (Collection) deMap.get(p1);

						if (t.equals(Collections.EMPTY_LIST)) {
							t = new HashSet();
							deMap.put(p1, t);
						}
						t.add(p2);
						t = (Collection) dtMap.get(p2);

						if (t.equals(Collections.EMPTY_LIST)) {
							t = new HashSet();
							dtMap.put(p2, t);
						}
						t.add(p1);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Resets in the internal data structures.
	 */
	public void reset() {
		super.reset();
	}

	/**
	 * Extracts information provided by the environment via <code>info</code> parameter to {@link #initialize initialize}.
	 *
	 * @throws InitializationException when and instance of pair managing service or interference analysis is not provided.
	 *
	 * @see InterferenceDAv1#setup()
	 */
	protected void setup() throws InitializationException {
		pairMgr = (PairManager) info.get(PairManager.ID);

		if (pairMgr == null) {
			throw new InitializationException(PairManager.ID + " was not provided in info.");
		}

		ecba = (EquivalenceClassBasedAnalysis) info.get(EquivalenceClassBasedAnalysis.ID);

		if (pairMgr == null) {
			throw new InitializationException(EquivalenceClassBasedAnalysis.ID + " was not provided in info.");
		}

		tgi = (IThreadGraphInfo) info.get(IThreadGraphInfo.ID);

		if (tgi == null) {
			throw new InitializationException(IThreadGraphInfo.ID + " was not provided in info.");
		}
	}
}

/*****
 ChangeLog:

$Log$

*****/
