
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

package edu.ksu.cis.indus.common.soot;

import edu.ksu.cis.indus.common.graph.BasicBlockGraphMgr;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import soot.options.Options;


/**
 * This is generic driver that provides basic support to process a system represented in Jimple.
 * 
 * <p>
 * The classes in which to search for root methods can be configured via setting 1 system property named
 * "edu.ksu.cis.indus.common.soot.SootBasedDriver.rootClasses".  The syntax of this property a regular expression which will
 * be matched against the name of the class in which the method is declared.  Note if this is unspecified, then root methods
 * are searched only in the classes specified and not the classes which are required.  So, "edu.ksu.cis.indus.processing"
 * will find root methods occurring in classes defined in edu.ksu.cis.indus and having names starting with "processing".
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SootBasedDriver {
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
	 * This provides <code>UnitGraph</code>s required by the analyses.  By defaults this will be initialized to
	 * <code>TrapUnitGraphFactory</code>.
	 */
	protected IUnitGraphFactory cfgProvider;

	/**
	 * The list of classes that should be considered as the core of the system.
	 */
	protected List classNames;

	/**
	 * This traps the root methods.
	 */
	protected RootMethodTrapper rootMethodTrapper;

	/**
	 * The scene that contains the classes of the system.
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
	 * The class path that should be added.
	 */
	private String classpathToAdd;

	/**
	 * Creates a new Test object.  This also initializes <code>cfgProvider</code> to <code>CompleteUnitGraphFactory</code>
	 * and <code>bbm</code> to an instance of <code>BasicBlockGraphMgr</code> with <code>cfgProvider</code> as the unit
	 * graph provider.
	 */
	public SootBasedDriver() {
		cfgProvider = getUnitGraphFactory();
		bbm = new BasicBlockGraphMgr();
		bbm.setUnitGraphFactory(cfgProvider);
	}

	/**
	 * This class provides the service of trapping the root methods.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static class RootMethodTrapper {
		/**
		 * This is the type of <code>String[]</code> in Soot type system.
		 */
		public static final ArrayType STR_ARRAY_TYPE = ArrayType.v(RefType.v("java.lang.String"), 1);

		/**
		 * The names of the classes which can contribute entry points.
		 */
		protected Collection theClassNames;

		/**
		 * The regular expression that is used to match classes which may contain root methods.
		 */
		Pattern rootClasses;

		/**
		 * Creates a new RootMethodTrapper object.
		 */
		protected RootMethodTrapper() {
			final String _theRootClasses = System.getProperty("edu.ksu.cis.indus.common.soot.SootBasedDriver.rootClasses");

			if (_theRootClasses != null) {
				rootClasses = Pattern.compile(_theRootClasses);
			} else {
				rootClasses = null;
			}
		}

		/**
		 * Checks if the given class can contribute root methods.
		 *
		 * @param sc is the class to check.
		 *
		 * @return <code>true</code> if <code>sc</code> should be examined for possible root method contribution;
		 * 		   <code>false</code>, otherwise.
		 *
		 * @pre sc != null
		 */
		protected final boolean considerClassForEntryPoint(final SootClass sc) {
			boolean _result = false;

			if (rootClasses != null) {
				_result = rootClasses.matcher(sc.getName()).matches();
			}

			if (!_result && theClassNames != null) {
				_result = theClassNames.contains(sc.getName());
			}
			return _result;
		}

		/**
		 * Checks if the given method qualifies as a root/entry method in the given system.
		 *
		 * @param sm is the method that may be an entry point into the system.
		 *
		 * @return <code>true</code> if <code>_sm</code> should be considered as a root method; <code>false</code>,
		 * 		   otherwise.
		 *
		 * @pre sm != null
		 */
		protected final boolean trapRootMethods(final SootMethod sm) {
			boolean _result = false;

			if (sm.getName().equals("main")
				  && sm.isStatic()
				  && sm.getParameterCount() == 1
				  && sm.getParameterType(0).equals(STR_ARRAY_TYPE)) {
				_result = true;
			}
			return _result;
		}
	}

	/**
	 * Retrieves the basic block graph manager used by the application.
	 *
	 * @return the basic block graph manager.
	 *
	 * @post result != null
	 */
	public final BasicBlockGraphMgr getBbm() {
		return this.bbm;
	}

	/**
	 * Set the names of the classes to be loaded.
	 *
	 * @param s contains the class names.
	 *
	 * @pre s != null
	 */
	public final void setClassNames(final String[] s) {
		setClassNames(Arrays.asList(s));
	}

	/**
	 * Set the names of the classes to be loaded.
	 *
	 * @param s contains the class names.
	 *
	 * @pre s != null and s.oclIsKindOf(Collection(String))
	 */
	public final void setClassNames(final Collection s) {
		classNames = new ArrayList(s);
	}

	/**
	 * Sets the logger to be used.
	 *
	 * @param myLogger is the logger to be used.
	 */
	public final void setLogger(final Log myLogger) {
		logger = myLogger;
	}

	/**
	 * Retrieves the root methods in the system.
	 *
	 * @return the collection of root methods.
	 *
	 * @post result != null
	 */
	public final Collection getRootMethods() {
		return Collections.unmodifiableCollection(rootMethods);
	}

	/**
	 * Retrieves the scene used by the application.
	 *
	 * @return the scene.
	 */
	public final Scene getScene() {
		return this.scene;
	}

	/**
	 * Retrieves the unit graph factory to be used by other processes that are driven by this implementation. By default, it
	 * provides an instance of <code>TrapUnitGraphFactory</code>
	 *
	 * @return an unit graph factory
	 *
	 * @post return != null
	 */
	public final IUnitGraphFactory getUnitGraphFactory() {
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
	public final void addTimeLog(final String name, final long milliseconds) {
		times.put("[" + times.size() + "]" + name, new Long(milliseconds));
	}

	/**
	 * Records the given classpath in intention of using it while loading classes into the scene.
	 *
	 * @param classpath to be considered.
	 *
	 * @pre classpath != null
	 */
	public final void addToSootClassPath(final String classpath) {
		classpathToAdd = classpath;
	}

	/**
	 * Initialize the driver.  Loads up the classes and sets up the scene.  The given classes are loaded up as application
	 * classes.
	 *
	 * @throws RuntimeException when <code>setClassNames()</code> was not called before using this object.
	 */
	public final void initialize() {
		if (classNames == null) {
			throw new RuntimeException("Please call setClassNames() before using this object.");
		}
		writeInfo("Loading classes....");
		scene = loadupClassesAndCollectMains();
	}

	/**
	 * Prints the timing statistics into the given stream.
	 *
	 * @pre stream != null
	 */
	public final void printTimingStats() {
		writeInfo("Timing statistics:");

		for (final Iterator _i = times.keySet().iterator(); _i.hasNext();) {
			final Object _e = _i.next();
			writeInfo(_e + " => " + times.get(_e) + "ms");
		}
	}

	/**
	 * Logs the given object via the logging api.  Configure the logging via the logging implementation's configuration
	 * support.
	 *
	 * @param info to be logged.
	 */
	public void writeInfo(final Object info) {
		if (logger != null && logger.isInfoEnabled()) {
			if (info != null) {
				logger.info(info.toString());
			} else {
				logger.info("null");
			}
		}
	}

	/**
	 * Loads up the classes specified via <code>setClassNames()</code> and also collects the possible entry points into the
	 * system being analyzed.  All <code>public static void main()</code> methods defined in <code>public</code> classes
	 * that are named via <code>args</code>are considered as entry points.  It uses the classpath set via
	 * <code>addToSootClassPath</code>. It uses <code>use-original-names:false</code>, <code>jb.ls enabled:false</code>, and
	 * <code>jb.ulp enabled:false unsplit-original-locals:false</code> options for Soot class loading.
	 *
	 * @return a soot scene that provides the classes to be analyzed.
	 *
	 * @pre args != null and classNames != null
	 */
	private Scene loadupClassesAndCollectMains() {
		final Scene _result = Scene.v();
		String _temp = _result.getSootClassPath();
		Options.v().parse(Util.getSootOptions());

		if (_temp != null) {
			_temp += File.pathSeparator + classpathToAdd + File.pathSeparator + System.getProperty("java.class.path");
		} else {
			_temp = classpathToAdd;
		}
		_result.setSootClassPath(_temp);

		for (final Iterator _i = classNames.iterator(); _i.hasNext();) {
			final SootClass _sc = _result.loadClassAndSupport((String) _i.next());
			_sc.setApplicationClass();
		}

		final Collection _mc = new HashSet();
		_mc.addAll(_result.getClasses());

		RootMethodTrapper _rmt = rootMethodTrapper;

		if (_rmt == null) {
			_rmt = new RootMethodTrapper();
			_rmt.theClassNames = Collections.unmodifiableCollection(classNames);
		}

		for (final Iterator _i = _mc.iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			if (_rmt.considerClassForEntryPoint(_sc)) {
				final Collection _methods = _sc.getMethods();

				for (final Iterator _j = _methods.iterator(); _j.hasNext();) {
					final SootMethod _sm = (SootMethod) _j.next();

					if (_rmt.trapRootMethods(_sm)) {
						rootMethods.add(_sm);
					}
				}
			}
		}
		Util.fixupThreadStartBody(_result);
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.13  2004/03/05 11:59:40  venku
   - documentation.

   Revision 1.12  2004/02/09 04:39:40  venku
   - refactoring test classes still..
   - need to make xmlizer classes independent of their purpose.
     Hence, they need to be highly configurable.
   - For each concept, test setup should be in TestSetup
     rather than in the XMLizer.
   Revision 1.11  2004/02/09 01:18:04  venku
   - root methods can be retrieved via getRootMethods().
   Revision 1.10  2004/01/28 22:42:05  venku
   - check the entry class names if the class does not
     match the root soot classes.
   Revision 1.9  2004/01/16 21:18:57  venku
   - renamed setUnitGraphProvider() to setUnitGraphFactory()
     in BasicBlockGraphMgr.
   - ripple effect.
   Revision 1.8  2003/12/28 03:17:54  venku
   - checking if the class exists in a collection of class names.  FIXED.
   Revision 1.7  2003/12/28 02:08:29  venku
   - the specified rootclasses overrides the selection of
     root methods from application classes.
   Revision 1.6  2003/12/28 01:32:21  venku
   - coding convention.
   Revision 1.5  2003/12/28 01:08:04  venku
   - exposed rootMethodTrapper to children classes.
   Revision 1.4  2003/12/28 01:07:33  venku
   - refactoring: added a new class that traps the root methods.
   - class path now uses java.class.path instead of JVM dependent hacks.
   Revision 1.3  2003/12/14 16:49:15  venku
   - marks given classes as application classes after they
     are loaded.
   Revision 1.2  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.1  2003/12/08 12:15:48  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.13  2003/12/02 09:42:37  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.12  2003/11/26 01:49:34  venku
   - minor logical error. FIXED.
   Revision 1.11  2003/11/20 07:28:40  venku
   - formatting.
   Revision 1.10  2003/11/20 07:27:41  venku
   - Explicitly sets options on Soot to use local name splitting.
   Revision 1.9  2003/11/17 03:22:59  venku
   - added junit test support for Slicing.
   - refactored code in test for dependency to make it more
     simple.
   Revision 1.8  2003/11/17 02:23:56  venku
   - documentation.
   - xmlizers require streams/writers to be provided to them
     rather than they constructing them.
   Revision 1.7  2003/11/15 21:30:21  venku
   - added a new method to added class names stored in a collection.
   Revision 1.6  2003/11/14 21:12:00  venku
   - exposed initialize() as a public method.
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
