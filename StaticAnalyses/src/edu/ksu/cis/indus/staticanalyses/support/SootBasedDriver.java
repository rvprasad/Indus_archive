
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

import edu.ksu.cis.indus.common.TrapUnitGraphFactory;
import edu.ksu.cis.indus.interfaces.AbstractUnitGraphFactory;

import org.apache.commons.logging.Log;

import java.io.File;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class SootBasedDriver {
	/**
	 * This is the type of <code>String[]</code> in Soot type system.
	 */
	public static final ArrayType STR_ARRAY_TYPE = ArrayType.v(RefType.v("java.lang.String"), 1);

	/**
	 * This provides <code>UnitGraph</code>s required by the analyses.  By defaults this will be initialized to
	 * <code>TrapUnitGraphFactory</code>.
	 */
	protected AbstractUnitGraphFactory cfgProvider;

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
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected List classNames;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	protected Scene scene;

	/**
	 * The logger used by instances of this class and it's subclasses to log messages.
	 */
	private Log logger;

	/**
	 * This is used to maintain the execution time of each analysis/transformation.  This timing information is printed via
	 * <code>printTimingStats</code>.
	 *
	 * @invariant times.oclIsTypeOf(Map(String, Long))
	 */
	private final Map times = new LinkedHashMap();

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private String classpathToAdd;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private int count;

	/**
	 * Creates a new Test object.  This also initializes <code>cfgProvider</code> to <code>CompleteUnitGraphFactory</code>
	 * and <code>bbm</code> to an instance of <code>BasicBlockGraphMgr</code> with <code>cfgProvider</code> as the unit
	 * graph provider.
	 */
	protected SootBasedDriver() {
		cfgProvider = getUnitGraphFactory();
		bbm = new BasicBlockGraphMgr();
		bbm.setUnitGraphProvider(cfgProvider);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param s DOCUMENT ME!
	 */
	public void setClassNames(final String[] s) {
		classNames = Arrays.asList(s);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	protected AbstractUnitGraphFactory getUnitGraphFactory() {
		return new TrapUnitGraphFactory();
	}

	/**
	 * Adds an entry into the time log of this test.  The subclasses should use this method to add time logs corresponding to
	 * each analysis they test/drive.
	 *
	 * @param name of the analysis for which the timing log is being created.
	 * @param milliseconds taken by the analysis.
	 *
	 * @pre name != null
	 */
	protected void addTimeLog(final String name, final long milliseconds) {
		times.put("[" + count++ + "]" + name, new Long(milliseconds));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param classpath DOCUMENT ME!
	 */
	protected void addToSootClassPath(final String classpath) {
		classpathToAdd =
			classpath + File.pathSeparator + System.getProperty("java.home") + File.separator + "lib" + File.separator
			+ "rt.jar";
	}

	/**
	 * Initialize the driver.  Loads up the classes and sets up the scene.
	 *
	 * @throws RuntimeException DOCUMENT ME!
	 */
	public void initialize() {
		if (classNames == null) {
			throw new RuntimeException("Please call setClassNames() before using this TestCase object.");
		}
		writeInfo("Loading classes....");
		scene = loadupClassesAndCollectMains();
	}

	/**
	 * Loads up the classes specified via <code>setClassNames()</code> and also collects the possible entry points into the
	 * system being analyzed.  All <code>public static void main()</code> methods defined in <code>public</code> classes
	 * that are named via <code>args</code>are considered as entry points.
	 *
	 * @return a soot scene that provides the classes to be analyzed.
	 *
	 * @pre args != null and classNames != null
	 */
	protected final Scene loadupClassesAndCollectMains() {
		Scene result = Scene.v();
		String temp = result.getSootClassPath();

		if (temp != null) {
			temp += File.pathSeparator + classpathToAdd;
		} else {
			temp = classpathToAdd;
		}
		result.setSootClassPath(temp);

		for (Iterator i = classNames.iterator(); i.hasNext();) {
			result.loadClassAndSupport((String) i.next());
		}

		Collection mc = new HashSet();
		mc.addAll(result.getClasses());

		for (Iterator i = mc.iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();

			if (considerClassForEntryPoint(sc)) {
				Collection methods = sc.getMethods();

				for (Iterator j = methods.iterator(); j.hasNext();) {
					SootMethod sm = (SootMethod) j.next();
					trapRootMethods(sm);
				}
			}
		}
		Util.fixupThreadStartBody(result);
		return result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param myLogger DOCUMENT ME!
	 */
	protected void setLogger(final Log myLogger) {
		logger = myLogger;
	}

	/**
	 * Checks if the methods of the given class should be explored for entry points.
	 *
	 * @param sc is the class that may provide entry points.
	 *
	 * @return <code>true</code> if <code>sc</code> should be explored; <code>false</code>, otherwise.
	 *
	 * @pre sc != null and classNames != null
	 */
	protected boolean considerClassForEntryPoint(final SootClass sc) {
		return classNames.contains(sc.getName()) && sc.isPublic();
	}

	/**
	 * Prints the timing statistics into the given stream.
	 *
	 * @pre stream != null
	 */
	protected void printTimingStats() {
		writeInfo("Timing statistics:");

		for (Iterator i = times.keySet().iterator(); i.hasNext();) {
			Object e = i.next();
			writeInfo(e + " => " + times.get(e) + "ms");
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

	/**
	 * Logs the given object via the logging api.  Configure the logging via the logging implementation's configuration
	 * support.
	 *
	 * @param info to be logged.
	 */
	protected void writeInfo(final Object info) {
		if (logger != null && logger.isInfoEnabled()) {
			logger.info(info.toString());
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2003/11/12 10:45:36  venku
   - soot class path can be set in SootBasedDriver.
   - dependency tests are xmlunit based.

   Revision 1.4  2003/11/12 09:24:15  venku
   - soot class path was being injected at a wrong location. FIXED.
   Revision 1.3  2003/11/12 09:19:41  venku
   - added support to change soot-related class path.
   Revision 1.2  2003/11/12 09:12:25  venku
   - logged more info.
   Revision 1.1  2003/11/12 05:22:26  venku
   - moved this from src-test source directory to src.
   Revision 1.1  2003/11/12 05:05:45  venku
   - Renamed SootDependentTest to SootBasedDriver.
   - Switched the contents of DependencyXMLizer and DependencyTest.
   - Corrected errors which emitting xml tags.
   - added a scrapbook.
   Revision 1.1  2003/11/11 10:11:27  venku
   - in the process of making XMLization a user
     application and at the same time a tester application.
 */
