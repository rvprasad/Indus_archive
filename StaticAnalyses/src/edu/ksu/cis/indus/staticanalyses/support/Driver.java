
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

package edu.ksu.cis.indus.staticanalyses.support;

import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import edu.ksu.cis.indus.commons.CompleteUnitGraphProvider;
import edu.ksu.cis.indus.interfaces.AbstractUnitGraphProvider;

import java.io.PrintStream;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * This class provides the basic infrastructure to drive and time various static analyses.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class Driver {
	/**
	 * This is the type of <code>String[]</code> in Soot type system.
	 */
	public static final ArrayType STR_ARRAY_TYPE = ArrayType.v(RefType.v("java.lang.String"), 1);

	/**
	 * This provides <code>UnitGraph</code>s required by the analyses.  By defaults this will be initialized to
	 * <code>CompleteUnitGraphProvider</code>.
	 */
	protected AbstractUnitGraphProvider cfgProvider;

	/**
	 * This manages basic block graphs of the methods being processed.  Subclasses should initialize this suitably.
	 */
	protected BasicBlockGraphMgr bbm;

	/**
	 * This is the set of methods which serve as the entry point into the system being analyzed.
	 *
	 * @invariant rootMethods.oclIsTypeOf(SootMethod)
	 */
	protected Collection rootMethods = new HashSet();

	/**
	 * This is used to maintain the execution time of each analysis/transformation.  This timing information is printed via
	 * <code>printTimingStats</code>.
	 *
	 * @invariant times.oclIsTypeOf(Map(String, Long))
	 */
	private final Map times = new LinkedHashMap();

	/**
	 * Creates a new Driver object.  This also initializes <code>cfgProvider</code> to <code>CompleteUnitGraphProvider</code> and
     * <code>bbm</code> to an instance of <code>BasicBlockGraphMgr</code> with <code>cfgProvider</code> as the unit graph provider.
	 */
	protected Driver() {
		cfgProvider = new CompleteUnitGraphProvider();
		bbm = new BasicBlockGraphMgr();
		bbm.setUnitGraphProvider(cfgProvider);
	}

	/**
	 * Adds an entry into the time log of this driver.  The subclasses should use this method to add time logs corresponding
	 * to each analysis they drive.
	 *
	 * @param name of the analysis for which the timing log is being created.
	 * @param milliseconds taken by the analysis.
	 *
	 * @pre name != null
	 */
	protected void addTimeLog(final String name, final long milliseconds) {
		times.put(getClass().getName() + ":" + name, new Long(milliseconds));
	}

	/**
	 * Loads up the given classes and also collects the possible entry points into the system being analyzed.  All
	 * <code>public static void main()</code> methods defined in <code>public</code> classes that are named via
	 * <code>args</code>are considered as entry points.
	 *
	 * @param args is the names of the classes to be loaded for analysis.
	 *
	 * @return a soot scene that provides the classes to be analyzed.
	 *
	 * @pre args != null
	 */
	protected final Scene loadupClassesAndCollectMains(final String[] args) {
		Scene result = Scene.v();
		boolean flag = false;
		Collection classNames = Arrays.asList(args);

		for (int i = 0; i < args.length; i++) {
			result.loadClassAndSupport(args[i]);
		}

		Collection mc = new HashSet();
		mc.addAll(result.getClasses());

		for (Iterator i = mc.iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();

			if (Util.implementsInterface(sc, "java.lang.Runnable")) {
				flag = true;
			}

			if (considerClassForEntryPoint(sc, classNames)) {
				Collection methods = sc.getMethods();

				for (Iterator j = methods.iterator(); j.hasNext();) {
					SootMethod sm = (SootMethod) j.next();
					trapRootMethods(sm);
				}
			}
		}

		if (flag) {
			SootClass sc = result.getSootClass("java.lang.Thread");
			SootMethod sm = sc.getMethodByName("start");
			Util.setThreadStartBody(sm);
		}
		return result;
	}

	/**
	 * Checks if the methods of the given class should be explored for entry points.
	 *
	 * @param sc is the class that may provide entry points.
	 * @param classNames is the names of the class that form the system to be analyzed.
	 *
	 * @return <code>true</code> if <code>sc</code> should be explored; <code>false</code>, otherwise.
	 *
	 * @pre sc != null and classNames != null and classNames.oclIsKindOf(Collection(String))
	 */
	protected boolean considerClassForEntryPoint(final SootClass sc, final Collection classNames) {
		return classNames.contains(sc.getName()) && sc.isPublic();
	}

	/**
	 * The analyses need to be driven in this method.  Concrete drivers should implement this method.
	 */
	protected abstract void execute();

	/**
	 * Prints the timing statistics into the given stream.
	 *
	 * @param stream into which the timing statistics should be written to.
	 *
	 * @pre stream != null
	 */
	protected void printTimingStats(final PrintStream stream) {
		stream.println("Timing statistics:");

		for (Iterator i = times.keySet().iterator(); i.hasNext();) {
			Object e = i.next();
			stream.println(e + " => " + times.get(e) + "ms");
		}
	}

	/**
	 * Records methods that should be considered as entry points into the system being analyzed.
	 *
	 * @param sm is the method that may be an entry point into the system.
	 *
	 * @post rootMethods->includes(sm) or not rootMethods->includes(sm)
	 * @pre sm != null
	 */
	protected void trapRootMethods(final SootMethod sm) {
		if (sm.getName().equals("main")
			  && sm.isStatic()
			  && sm.getParameterCount() == 1
			  && sm.getParameterType(0).equals(STR_ARRAY_TYPE)) {
			rootMethods.add(sm);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2003/09/28 03:16:20  venku
   - I don't know.  cvs indicates that there are no differences,
     but yet says it is out of sync.
   Revision 1.4  2003/09/08 02:22:22  venku
   - basic block graph manager can be cached via bbm.
   Revision 1.3  2003/08/11 07:13:58  venku
 *** empty log message ***
         Revision 1.2  2003/08/11 04:20:19  venku
         - Pair and Triple were changed to work in optimized and unoptimized mode.
         - Ripple effect of the previous change.
         - Documentation and specification of other classes.
         Revision 1.1  2003/08/10 03:43:26  venku
         Renamed Tester to Driver.
         Refactored logic to pick entry points.
         Provided for logging timing stats into any specified stream.
         Ripple effect in others.
         Revision 1.1  2003/08/07 06:42:16  venku
         Major:
          - Moved the package under indus umbrella.
          - Renamed isEmpty() to hasWork() in WorkBag.
 */
