
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

import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.common.structures.Pair;
import edu.ksu.cis.indus.common.structures.Pair.PairManager;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.InitializationException;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.ArrayType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.FieldRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;


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
 * @invariant dependeeMap.oclIsKindOf(Map(Object, Map(Pair(Stmt, SootMethod), Collection(Pair(Stmt, SootMethodMethod)))))
 * @invariant dependentMap.oclIsKindOf(Map(Object, Map(Pair(Stmt, SootMethod), Collection(Pair(Stmt, SootMethodMethod)))))
 */
public class InterferenceDAv1
  extends DependencyAnalysis {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(InterferenceDAv1.class);

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
	  extends AbstractValueAnalyzerBasedProcessor {
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
		 * @see edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzerBasedProcessor#callback(Stmt,Context)
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
		 * @see edu.ksu.cis.indus.interfaces.IProcessor#hookup(ProcessingController)
		 */
		public void hookup(final ProcessingController ppc) {
			ppc.register(AssignStmt.class, this);
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IProcessor#unhook(ProcessingController)
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
	 * Returns the statements on which the field/array reference at the given statement and method depends on.
	 *
	 * @param stmt is the statement in which the array/field reference occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre stmt.oclIsTypeOf(Stmt) or method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
	 */
	public Collection getDependees(final Object stmt, final Object method) {
		Collection result = Collections.EMPTY_LIST;
		Stmt temp = (Stmt) stmt;
		Map pair2set = null;
		Object dependent = null;

		if (temp.containsArrayRef()) {
			dependent = temp.getArrayRef().getBase().getType();
		} else if (temp.containsFieldRef()) {
			dependent = temp.getFieldRef().getField();
		}

		if (dependent != null) {
			pair2set = getDependeeMapFor(dependent);

			if (pair2set != null) {
				Collection set = (Collection) pair2set.get(pairMgr.getUnOptimizedPair(stmt, method));

				if (set != null) {
					result = Collections.unmodifiableCollection(set);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the statements which depend on the field/array reference at the given statement and method.
	 *
	 * @param stmt is the statement in which the array/field reference occurs.
	 * @param method in which <code>stmt</code> occurs.
	 *
	 * @return a colleciton of pairs comprising of a statement and a method.
	 *
	 * @pre stmt.oclIsTypeOf(Stmt) or method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.oclIsKindOf(Pair(Stmt, SootMethod))
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getDependees( java.lang.Object, java.lang.Object)
	 */
	public Collection getDependents(final Object stmt, final Object method) {
		Collection result = Collections.EMPTY_LIST;
		Stmt temp = (Stmt) stmt;
		Map pair2set = null;
		Object dependee = null;

		if (temp.containsArrayRef()) {
			dependee = temp.getArrayRef().getBase().getType();
		} else if (temp.containsFieldRef()) {
			dependee = temp.getFieldRef().getField();
		}

		if (dependee != null) {
			pair2set = getDependentMapFor(dependee);

			if (pair2set != null) {
				Collection set = (Collection) pair2set.get(pairMgr.getUnOptimizedPair(stmt, method));

				if (set != null) {
					result = Collections.unmodifiableCollection(set);
				}
			}
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#getId()
	 */
	public Object getId() {
		return DependencyAnalysis.INTERFERENCE_DA;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysis#analyze()
	 */
	public void analyze() {
		stable = false;

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Interference Dependence processing");
		}

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

					if (considerClassInitializers(dt, de) && isDependentOn(dt, de)) {
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

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END: Interference Dependence processing");
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
	 * @return <code>true</code> if the dependence exists; <code>false</code>, otherwise.
	 *
	 * @pre dependent != null and dependee != null
	 */
	protected boolean isDependentOn(final Pair dependent, final Pair dependee) {
		boolean result = true;
		Value de = ((AssignStmt) dependee.getFirst()).getLeftOp();
		Value dt = ((AssignStmt) dependent.getFirst()).getRightOp();

		if (de instanceof ArrayRef && dt instanceof ArrayRef) {
			Type t1 = ((ArrayRef) de).getBase().getType();
			Type t2 = ((ArrayRef) dt).getBase().getType();
			result = t1.equals(t2);
		} else if (dt instanceof InstanceFieldRef && de instanceof InstanceFieldRef) {
			SootField f1 = ((InstanceFieldRef) de).getField();
			SootField f2 = ((InstanceFieldRef) dt).getField();
			result = f1.equals(f2);
		}

		return result;
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

	/**
	 * Checks if the given dependence has any of the ends rooted in a class initializer and prunes the  dependence based on
	 * this information.
	 *
	 * @param dependent is the array/field read access site.
	 * @param dependee is the array/field write access site.
	 *
	 * @return <code>true</code> if the dependence should be considered; <code>false</code>, otherwise.
	 *
	 * @pre dependent != null and dependee != null
	 */
	private boolean considerClassInitializers(final Pair dependent, final Pair dependee) {
		SootMethod deMethod = (SootMethod) dependee.getSecond();
		SootMethod dtMethod = (SootMethod) dependent.getSecond();
		boolean result = true;

		// If any one of the method is a class initialization method then we can optimize.
		boolean deci = deMethod.getName().equals("<clinit>");
		boolean dtci = dtMethod.getName().equals("<clinit>");

		if (deci || dtci) {
			SootClass deClass = deMethod.getDeclaringClass();
			SootClass dtClass = dtMethod.getDeclaringClass();

			// if the classes of both the methods are relatec 
			if (Util.isHierarchicallyRelated(deClass, dtClass)) {
				result = false;
			} else {
				Value de = ((AssignStmt) dependee.getFirst()).getLeftOp();
				Value dt = ((AssignStmt) dependent.getFirst()).getRightOp();

				if (dt instanceof StaticFieldRef && de instanceof StaticFieldRef) {
					SootField f1 = ((StaticFieldRef) de).getField();
					SootField f2 = ((StaticFieldRef) dt).getField();

					if (f1.equals(f2)
						  && ((deci && f1.getDeclaringClass().equals(deClass))
						  || (dtci && f1.getDeclaringClass().equals(dtClass)))) {
						result = false;
					}
				}
			}
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.27  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.

   Revision 1.26  2003/12/08 12:15:57  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.

   Revision 1.25  2003/12/08 09:37:23  venku
   - use class initialization optimization by default.

   Revision 1.24  2003/12/02 09:42:36  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.23  2003/11/26 09:16:26  venku
   - structure via which information is stored is different
     from the structure assumed while querying.  FIXED.
   Revision 1.22  2003/11/26 06:14:02  venku
   - added logic to consider class initializers.
   Revision 1.21  2003/11/12 01:04:54  venku
   - each analysis implementation has to identify itself as
     belonging to a analysis category via an id.
   Revision 1.20  2003/11/10 20:03:55  venku
   - In Jimple, only one ArrayRef or FieldRef can occur in
     a statement.  We now use this information to make
     getDependeXXX() methods of signature type Stmt
     and Method.
   Revision 1.19  2003/11/10 08:06:01  venku
   - documentation.
   Revision 1.18  2003/11/10 03:17:18  venku
   - renamed AbstractProcessor to AbstractValueAnalyzerBasedProcessor.
   - ripple effect.
   Revision 1.17  2003/11/06 05:31:08  venku
   - moved IProcessor to processing package from interfaces.
   - ripple effect.
   - fixed documentation errors.
   Revision 1.16  2003/11/06 05:15:07  venku
   - Refactoring, Refactoring, Refactoring.
   - Generalized the processing controller to be available
     in Indus as it may be useful outside static anlaysis. This
     meant moving IProcessor, Context, and ProcessingController.
   - ripple effect of the above changes was large.
   Revision 1.15  2003/11/05 00:44:51  venku
   - added logging statements to track the execution.
   Revision 1.14  2003/10/05 16:23:34  venku
   - formatting.
   Revision 1.13  2003/10/05 16:20:58  venku
   - made dependence type-based.
   Revision 1.12  2003/09/29 06:40:35  venku
   - reset() was being called on an argument.  FIXED.
   Revision 1.11  2003/09/28 03:16:48  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.10  2003/09/12 22:33:08  venku
   - AbstractAnalysis extends IStatus.  Hence, analysis() does not return a value.
   - Ripple effect of the above changes.
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
