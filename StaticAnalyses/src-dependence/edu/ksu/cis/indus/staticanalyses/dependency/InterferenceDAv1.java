
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */

package edu.ksu.cis.indus.staticanalyses.dependency;

import soot.ArrayType;
import soot.SootField;
import soot.SootMethod;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractProcessor;
import edu.ksu.cis.indus.staticanalyses.processing.ProcessingController;
import edu.ksu.cis.indus.staticanalyses.support.Pair;
import edu.ksu.cis.indus.staticanalyses.support.Pair.PairManager;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * This class provides interference dependency information.  This implementation refers to the technical report <a
 * href="http://www.cis.ksu.edu/santos/papers/technicalReports.html">A Formal  Study of Slicing for Multi-threaded Program
 * with JVM Concurrency Primitives"</a>.  The calculated information is very pessimistic.  For fields, it assumes any
 * assignment to a field can affect any reference to the same field.  This is imprecise in the light of thread local objects
 * and unrelated primaries.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependeeMap.oclIsKindOf(Map(Object, Map(SootMethod, Map(Stmt, Collection(Pair(Stmt, SootMethodMethod))))))
 * @invariant dependentMap.oclIsKindOf(Map(Object, Map(SootMethod, Map(Stmt, Collection(Pair(Stmt, SootMethodMethod))))))
 */
public class InterferenceDAv1
  extends DependencyAnalysis {
	/**
	 * This provides threading information pertaining to the system being analyzed.
	 */
	protected IThreadGraphInfo tgi;

	/**
	 * This manages pairs.
	 */
	protected PairManager pairMgr;

	/**
	 * Creates a new InterferenceDAv1 object.
	 */
	public InterferenceDAv1() {
		preprocessor = new PreProcessor();
	}

	/**
	 * A preprocessor which captures all the array and field access locations in the analyzed system.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	private class PreProcessor
	  extends AbstractProcessor {
		/**
		 * Called by the controller when it encounters an assignment statement.  This records array access and field access
		 * expressions.
		 *
		 * @param stmt in which the access expression occurs.
		 * @param context in which <code>stmt</code> occurs.
		 *
		 * @pre stmt.isOclKindOf(AssignStmt)
		 * @pre context.getCurrentMethod() != null
		 *
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#callback(Stmt,Context)
		 */
		public void callback(final Stmt stmt, final Context context) {
			SootMethod method = context.getCurrentMethod();
			AssignStmt as = (AssignStmt) stmt;
			Map temp = null;

			if (as.containsFieldRef()) {
				if (as.getLeftOp() instanceof FieldRef) {
					SootField sf = ((FieldRef) as.getLeftOp()).getField();
					temp = getDependeXXMapHelper(dependentMap, sf);
				} else {
					SootField sf = ((FieldRef) as.getRightOp()).getField();
					temp = getDependeXXMapHelper(dependeeMap, sf);
				}
			} else if (as.containsArrayRef()) {
				if (as.getLeftOp() instanceof ArrayRef) {
					ArrayType at = (ArrayType) ((ArrayRef) as.getLeftOp()).getBase().getType();
					temp = getDependeXXMapHelper(dependentMap, at);
				} else {
					ArrayType at = (ArrayType) ((ArrayRef) as.getRightOp()).getBase().getType();
					temp = getDependeXXMapHelper(dependeeMap, at);
				}
			}

			if (temp != null) {
				Pair p = pairMgr.getOptimizedPair(as, method);

				if (temp.get(p) == null) {
					temp.put(p, Collections.EMPTY_LIST);
				}
			}
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(AssignStmt.class, this);
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IProcessor#unhook(ProcessingController)
		 */
		public void unhook(final ProcessingController ppc) {
			ppc.unregister(AssignStmt.class, this);
		}

		/**
		 * Helper method for getDependeXXMap() methods.
		 *
		 * @param map from which to extract the result map.
		 * @param o is the field/array reference of interest.
		 *
		 * @return the map corresponding to the <code>o</code>.
		 */
		private Map getDependeXXMapHelper(final Map map, final Object o) {
			Map result = (Map) map.get(o);

			if (result == null) {
				result = new HashMap();
				map.put(o, result);
			}
			return result;
		}
	}

	/**
	 * Returns the statements on which the given field/array reference at the given statement and method depends on.
	 *
	 * @param dependent of interest.
	 * @param stmtMethodPair is the pair of statement and method in which <code>field</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre dependent.oclIsTypeOf(SootField) or dependent.oclIsTypeOf(ArrayRef)
	 * @pre stmtMethodPair.oclIsKindOf(Pair(Stmt, SootMethod))
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(final Object dependent, final Object stmtMethodPair) {
		Collection result = Collections.EMPTY_LIST;
		Map pair2set = getDependeeMapFor(dependent);

		if (pair2set != null) {
			Collection set = (Set) pair2set.get(stmtMethodPair);

			if (set != null) {
				result = Collections.unmodifiableCollection(set);
			}
		}
		return result;
	}

	/**
	 * Returns the statements which depend on the given field/array reference at the given statement and method.
	 *
	 * @param dependee of interest.
	 * @param stmtMethodPair is the pair of statement and method in which <code>field</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre dependee.oclIsTypeOf(SootField) or dependee.oclIsTypeOf(ArrayRef)
	 * @pre stmtMethodPair.oclIsKindOf(Pair(Stmt, SootMethod))
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(final Object dependee, final Object stmtMethodPair) {
		Collection result = Collections.EMPTY_LIST;
		Map pair2set = getDependentMapFor(dependee);

		if (pair2set != null) {
			Collection set = (Set) pair2set.get(stmtMethodPair);

			if (set != null) {
				result = Collections.unmodifiableCollection(set);
			}
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public void analyze() {
		stable = false;

		// we return immediately if there are no start sites in the system.
		if (tgi.getStartSites().size() == 0) {
			stable = true;
		}

		for (Iterator i = dependeeMap.keySet().iterator(); i.hasNext();) {
			Object o = i.next();

			if (dependentMap.get(o) == null) {
				continue;
			}

			Map deMap = (Map) dependeeMap.get(o);
			Map dtMap = (Map) dependentMap.get(o);

			for (Iterator j = deMap.keySet().iterator(); j.hasNext();) {
				Pair dt = (Pair) j.next();

				for (Iterator k = dtMap.keySet().iterator(); k.hasNext();) {
					Pair de = (Pair) k.next();

					if (isDependentOn(dt, de)) {
						Collection t = (Collection) deMap.get(dt);

						if (t.equals(Collections.EMPTY_LIST)) {
							t = new HashSet();
							deMap.put(dt, t);
						}
						t.add(de);
						t = (Collection) dtMap.get(de);

						if (t.equals(Collections.EMPTY_LIST)) {
							t = new HashSet();
							dtMap.put(de, t);
						}
						t.add(dt);
					}
				}
			}
		}
		stable = true;
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#reset()
	 */
	public void reset() {
		super.reset();
		pairMgr.reset();
	}

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		StringBuffer result =
			new StringBuffer("Statistics for Interference dependence as calculated by " + getClass().getName() + "\n");
		int localEdgeCount = 0;
		int edgeCount = 0;

		StringBuffer temp = new StringBuffer();

		for (Iterator i = dependeeMap.entrySet().iterator(); i.hasNext();) {
			Map.Entry entry = (Map.Entry) i.next();
			localEdgeCount = 0;

			for (Iterator j = ((Map) entry.getValue()).entrySet().iterator(); j.hasNext();) {
				Map.Entry entry2 = (Map.Entry) j.next();

				for (Iterator k = ((Collection) entry2.getValue()).iterator(); k.hasNext();) {
					temp.append("\t\t" + entry2.getKey() + " --> " + k.next() + "\n");
				}
				localEdgeCount += ((Collection) entry2.getValue()).size();
			}
			result.append("\tFor " + entry.getKey() + " there are " + localEdgeCount + " Interference dependence edges.\n");
			result.append(temp);
			temp.delete(0, temp.length());
			edgeCount += localEdgeCount;
		}
		result.append("A total of " + edgeCount + " Interference dependence edges exist.");
		return result.toString();
	}

	/**
	 * Returns the map containing dependee information pertaining to the given field or array reference.
	 *
	 * @param dependent of interest.
	 *
	 * @return the map for containing dependee information pertaining to <code>o</code>.
	 */
	protected Map getDependeeMapFor(final Object dependent) {
		Map result = (Map) dependeeMap.get(dependent);

		if (result == null) {
			result = Collections.EMPTY_MAP;
		}
		return result;
	}

	/**
	 * Returns the map containing dependent information pertaining to the given field or array reference.
	 *
	 * @param dependee of interest.
	 *
	 * @return the map for containing dependent information pertaining to <code>o</code>.
	 */
	protected Map getDependentMapFor(final Object dependee) {
		Map result = (Map) dependentMap.get(dependee);

		if (result == null) {
			result = Collections.EMPTY_MAP;
		}
		return result;
	}

	/**
	 * Checks if the given array/field access expression is dependent on the given array/field definition expression.
	 *
	 * @param dependent is the array/field read access site.
	 * @param dependee is the array/field write access site.
	 *
	 * @return <code>true</code>.
	 */
	protected boolean isDependentOn(final Pair dependent, final Pair dependee) {
		return true;
	}

	/**
	 * Extracts information as provided by environment at initialization time.
	 *
	 * @throws InitializationException when call graph info, pair managing service, or environment is not available in
	 * 		   <code>info</code> member.
	 *
	 * @pre info.get(PairManager.ID) != null and info.get(IThreadGraphInfo.ID) != null
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();
		pairMgr = (PairManager) info.get(PairManager.ID);

		if (pairMgr == null) {
			throw new InitializationException(PairManager.ID + " was not provided in info.");
		}

		tgi = (IThreadGraphInfo) info.get(IThreadGraphInfo.ID);

		if (tgi == null) {
			throw new InitializationException(IThreadGraphInfo.ID + " was not provided in info.");
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/09/11 12:35:48  venku
   - formatting.
   Revision 1.8  2003/09/10 11:49:31  venku
   - documentation change.
   Revision 1.7  2003/09/08 02:19:38  venku
   - it now only requires call graph info and basic block graph manager
   - checkForLoopEnclosedNewExpr() is now applicable to any allocation sites
   - added a new method to extract basic block graph
   Revision 1.6  2003/08/11 08:49:34  venku
   Javadoc documentation errors were fixed.
   Some classes were documented.
   Revision 1.5  2003/08/11 06:34:52  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.4  2003/08/11 06:31:55  venku
   Changed format of change log accumulation at the end of the file
   Revision 1.3  2003/08/11 04:20:19  venku
   - Pair and Triple were changed to work in optimized and unoptimized mode.
   - Ripple effect of the previous change.
   - Documentation and specification of other classes.
   Revision 1.2  2003/08/09 23:32:40  venku
   - Utilized containsXXX() method in Stmt
   - Even with escape information there is an issue with sequential paths.
     This issue has been injected as TODO item, but not addressed.
 */
