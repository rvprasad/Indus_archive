
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import soot.SootMethod;
import soot.Value;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.interfaces.IUseDefInfo;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractProcessor;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.Pair.PairManager;

import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class AliasedUseDefInfo
  extends AbstractProcessor
  implements IUseDefInfo {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final IValueAnalyzer analyzer;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Map defsMap;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final Map usesMap;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private final PairManager PairMgr = new PairManager();

	/**
	 * Creates a new AliasedUseDefInfo object.
	 *
	 * @param iva DOCUMENT ME!
	 */
	AliasedUseDefInfo(IValueAnalyzer iva) {
		defsMap = new HashMap();
		usesMap = new HashMap();
		analyzer = iva;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IUseDefInfo#getDefs(soot.jimple.AssignStmt,
	 * 		edu.ksu.cis.indus.staticanalyses.Context)
	 */
	public Collection getDefs(AssignStmt useStmt, Context context) {
		Map stmt2defs = (Map) usesMap.get(context.getCurrentMethod());
		Collection result = null;

		if (stmt2defs != null) {
			result = (Collection) stmt2defs.get(useStmt);
		}

		return result == null ? Collections.EMPTY_SET
							  : result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IUseDefInfo#getUses(soot.jimple.AssignStmt,
	 * 		edu.ksu.cis.indus.staticanalyses.Context)
	 */
	public Collection getUses(AssignStmt defStmt, Context context) {
		Map stmt2uses = (Map) usesMap.get(context.getCurrentMethod());
		Collection result = null;

		if (stmt2uses != null) {
			result = (Collection) stmt2uses.get(defStmt);
		}

		return result == null ? Collections.EMPTY_SET
							  : result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(soot.jimple.Stmt,
	 * 		edu.ksu.cis.indus.staticanalyses.Context)
	 */
	public void callback(final Stmt stmt, final Context context) {
		AssignStmt as = (AssignStmt) stmt;
		SootMethod method = context.getCurrentMethod();

		if (as.containsArrayRef() || as.containsFieldRef()) {
			Value ref = as.getRightOp();

			if (ref instanceof ArrayRef || ref instanceof FieldRef) {
				Map stmt2ddees = (Map) defsMap.get(method);

				if (stmt2ddees == null) {
					stmt2ddees = new HashMap();
					defsMap.put(method, stmt2ddees);
				}
				stmt2ddees.put(stmt, Collections.EMPTY_SET);
			} else {
				ref = as.getLeftOp();

				Map stmt2ddents = (Map) usesMap.get(method);

				if (stmt2ddents == null) {
					stmt2ddents = new HashMap();
					defsMap.put(method, stmt2ddents);
				}
				stmt2ddents.put(stmt, Collections.EMPTY_SET);
			}
		}
	}

	/**
	 * Records naive interprocedural data dependence.  All it does is that
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#consolidate()
	 */
	public void consolidate() {
		Collection uses = new HashSet();
		Context context1 = new Context();
		Context context2 = new Context();

		for (Iterator i = usesMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry method2info1 = (Map.Entry) i.next();
			SootMethod useMethod = (SootMethod) method2info1.getKey();
			context1.setRootMethod(useMethod);

			for (Iterator j = ((Map) method2info1.getValue()).entrySet().iterator(); j.hasNext();) {
				Map.Entry stmt2uses = (Map.Entry) j.next();
				Stmt defStmt = (Stmt) stmt2uses.getKey();

				for (Iterator k = defsMap.entrySet().iterator(); k.hasNext();) {
					Map.Entry method2info2 = (Map.Entry) k.next();
					SootMethod defMethod = (SootMethod) method2info2.getKey();
					context2.setRootMethod(defMethod);

					for (Iterator l = ((Map) method2info2.getValue()).entrySet().iterator(); l.hasNext();) {
						Map.Entry stmt2defs = (Map.Entry) l.next();
						Stmt useStmt = (Stmt) stmt2defs.getKey();

						// initially host no dependence
						boolean useDef = false;

						if (defStmt.containsArrayRef()
							  && useStmt.containsArrayRef()
							  && defStmt.getArrayRef().equals(useStmt.getArrayRef())) {
							context1.setStmt(useStmt);

							Collection c1 = analyzer.getValues(useStmt.getArrayRef().getBase(), context1);

							context2.setStmt(defStmt);

							Collection c2 = analyzer.getValues(defStmt.getArrayRef().getBase(), context2);

							// if the primaries of the access expression alias atleast one object
							if (!CollectionUtils.intersection(c1, c2).isEmpty()) {
								useDef = true;
							}
						} else if (defStmt.containsFieldRef()
							  && useStmt.containsFieldRef()
							  && defStmt.getFieldRef().equals(useStmt.getFieldRef())) {
							FieldRef fr = useStmt.getFieldRef();

							// set the initial value to true assuming dependency in case of static field ref
							useDef = true;

							if (fr instanceof InstanceFieldRef) {
								context1.setStmt(useStmt);

								Collection c1 =
									analyzer.getValues(((InstanceFieldRef) useStmt.getFieldRef()).getBase(), context1);

								context2.setStmt(defStmt);

								Collection c2 = analyzer.getValues(((InstanceFieldRef) fr).getBase(), context2);

								// if the primaries of the access expression do not alias even one object.
								if (CollectionUtils.intersection(c1, c2).isEmpty()) {
									useDef = false;
								}
							}
						}

						if (useDef) {
							Collection defs = (Collection) stmt2defs.getValue();

							if (defs.equals(Collections.EMPTY_SET)) {
								defs = new HashSet();
								stmt2defs.setValue(defs);
							}
							defs.add(PairMgr.getPair(defStmt, defMethod));
							uses.add(PairMgr.getPair(useStmt, useMethod));
						}
					}
				}

				if (uses.size() != 0) {
					stmt2uses.setValue(new HashSet(uses));
					uses.clear();
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#hookup(
	 * 		edu.ksu.cis.indus.staticanalyses.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(AssignStmt.class, this);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#unhook(
	 * 		edu.ksu.cis.indus.staticanalyses.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(AssignStmt.class, this);
	}
}

/*****
 ChangeLog:

$Log$

*****/
