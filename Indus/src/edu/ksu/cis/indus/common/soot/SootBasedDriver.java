
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
import org.apache.commons.logging.LogFactory;

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
 * <code>indus.common.soot.SootBasedDriver.rootClasses</code>.  The syntax of this property a regular expression which will
 * be matched against the name of the class in which the method is declared.  Note if this is unspecified, then root methods
 * are searched only in the classes specified and not the classes which are required.  So, "edu.ksu.cis.indus.Processing"
 * will find root methods occurring in classes defined in class <code>edu.ksu.cis.indus.Processing</code>.  Likewise, the
 * user can control which methods are picked as root methods based on their signature by specifying the pattern via the
 * system property,  <code>indus.common.soot.SootBasedDriver.rootMethods</code>.
 * </p>
 * 
 * <p>
 * The user can provide a root method trapper object to be used to identify root methods.  However, if the user does not
 * povide one,  then an instance of the class named via
 * <code>indus.common.soot.SootBasedDriver.RootMethodTrapper.class</code> property will be used.  This named class should be
 * a subclass of <code>edu.ksu.cis.indus.common.soot.SootBasedDriver$RootMethodTrapper</code>. If this property is
 * unspecified, the an instance of <code>SootBasedDriver.RootMethodMapper</code> is created via reflection and used.  Hence,
 * the class specified by the property should have a no-argument constructor.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SootBasedDriver {
	/**
	 * The name of the property via which the name of the root method trapper can be specified.
	 */
	public static final String TRAPPER_CLASS_PROPERTY;

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER;

	static {
		LOGGER = LogFactory.getLog(SootBasedDriver.class);
		TRAPPER_CLASS_PROPERTY = "indus.common.soot.SootBasedDriver.RootMethodTrapper.class";

		final String _className = System.getProperty(TRAPPER_CLASS_PROPERTY);
		RootMethodTrapper _rmt = new RootMethodTrapper();

		if (_className != null) {
			try {
				final Object _o = ClassLoader.getSystemClassLoader().loadClass(_className).newInstance();

				if (_o instanceof RootMethodTrapper) {
					_rmt = (RootMethodTrapper) _o;
				} else {
					throw new IllegalArgumentException(_className
						+ " is not a subclass of SootBasedDriver.RootMethodTrapper.");
				}
			} catch (final ClassNotFoundException _e) {
				LOGGER.fatal("class " + _className + " could not be loaded/resolved. Bailing.", _e);
				throw new RuntimeException(_e);
			} catch (final InstantiationException _e) {
				LOGGER.fatal("An instance of class " + _className + " could not be created. Bailing.", _e);
				throw new RuntimeException(_e);
			} catch (final IllegalAccessException _e) {
				LOGGER.fatal("No-arg constructor of " + _className + " cannot be accessed.  Bailing.", _e);
				throw new RuntimeException(_e);
			}
		}
		DEFAULT_INSTANCE_OF_ROOT_METHOD_TRAPPER = _rmt;
	}

	/**
	 * The default object that can be used for trapping root methods.
	 */
	private static final RootMethodTrapper DEFAULT_INSTANCE_OF_ROOT_METHOD_TRAPPER;

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
	 * <code>TrapStmtGraphFactory</code>.
	 */
	protected IStmtGraphFactory cfgProvider;

	/**
	 * The list of classes that should be considered as the core of the system.
	 */
	protected List classNames;

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
	 * This traps the root methods.
	 */
	private RootMethodTrapper rootMethodTrapper;

	/**
	 * The class path that should be added.
	 */
	private String classpathToAdd;

	/**
	 * Creates a new Test object.  This also initializes <code>cfgProvider</code> to <code>CompleteStmtGraphFactory</code>
	 * and <code>bbm</code> to an instance of <code>BasicBlockGraphMgr</code> with <code>cfgProvider</code> as the unit
	 * graph provider.
	 */
	public SootBasedDriver() {
		cfgProvider = getStmtGraphFactory();
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
		 * The names of the classes which can contribute entry points.
		 */
		protected Collection theClassNames;

		/**
		 * The regular expression that is used to match classes which may contain root methods.
		 */
		private Pattern rootClasses;

		/**
		 * The regular expression that is used to match methods which should be root methods.
		 */
		private Pattern rootMethods;

		/**
		 * Creates a new RootMethodTrapper object.
		 */
		protected RootMethodTrapper() {
			final String _theRootClasses = System.getProperty("indus.common.soot.SootBasedDriver.rootClasses");

			if (_theRootClasses != null) {
				rootClasses = Pattern.compile(_theRootClasses);
			} else {
				rootClasses = null;
			}

			final String _theRootMethods = System.getProperty("indus.common.soot.SootBasedDriver.rootMethods");

			if (_theRootMethods != null) {
				rootMethods = Pattern.compile(_theRootMethods);
			} else {
				rootMethods = null;
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
		protected boolean considerClassForEntryPoint(final SootClass sc) {
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
		protected boolean trapRootMethods(final SootMethod sm) {
			boolean _result = false;

			if (rootMethods != null) {
				_result = rootMethods.matcher(sm.getSignature()).matches();
			} else if (sm.getName().equals("main")
				  && sm.isStatic()
				  && sm.getParameterCount() == 1
				  && sm.getParameterType(0).equals(ArrayType.v(RefType.v("java.lang.String"), 1))) {
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
	 * Sets the root method trapper.
	 *
	 * @param trapper to be used to trap root methods.
	 */
	public final void setRootMethodTrapper(final RootMethodTrapper trapper) {
		rootMethodTrapper = trapper;
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
	 * provides an instance of <code>ExceptionFlowSensitiveStmtGraphFactory</code> initialized to prune synchronization
	 * related exceptions.
	 *
	 * @return an unit graph factory
	 *
	 * @post return != null
	 */
	public IStmtGraphFactory getStmtGraphFactory() {
		final IStmtGraphFactory _result =
			new ExceptionFlowSensitiveStmtGraphFactory(ExceptionFlowSensitiveStmtGraphFactory.SYNC_RELATED_EXCEPTIONS, true);
		return _result;
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
	 * @param options to be used while setting up Soot infrastructure.
	 *
	 * @throws RuntimeException when <code>setClassNames()</code> was not called before using this object.
	 *
	 * @pre options != null
	 */
	public final void initialize(final String[] options) {
		if (classNames == null) {
			throw new RuntimeException("Please call setClassNames() before using this object.");
		}
		writeInfo("Loading classes....");
		scene = loadupClassesAndCollectMains(options);
	}

	/**
	 * Initialize the driver with the soot options available from <code>Util.getSootOptions</code> method.
	 */
	public final void initialize() {
		initialize(Util.getSootOptions());
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
	 * <code>addToSootClassPath</code>.
	 *
	 * @param options to be used while setting up Soot infrastructure.
	 *
	 * @return a soot scene that provides the classes to be analyzed.
	 *
	 * @pre args != null and classNames != null and options != null
	 */
	private Scene loadupClassesAndCollectMains(final String[] options) {
		final Scene _result = Scene.v();
		String _temp = _result.getSootClassPath();
		Options.v().parse(options);

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
			_rmt = DEFAULT_INSTANCE_OF_ROOT_METHOD_TRAPPER;
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
   Revision 1.23  2004/05/13 03:30:03  venku
   - coding convention.
   - documentation.
   - refactoring: added a new method getFileName() to IXMLizer instead of AbstractXMLizer.
   Revision 1.22  2004/05/12 13:38:43  venku
   - Comparing Soot type objects acquired across reset()s will result in
     inequality.  This causes RootMethodTrapper to fail.  FIXED.
   Revision 1.21  2004/05/09 09:35:00  venku
   - changes to enable configuration of root method trapper via a system property.
   Revision 1.20  2004/05/06 17:12:48  venku
   - by default, returns exceptionflow sensitive stmt graph factories.
   Revision 1.19  2004/04/16 17:45:31  venku
   - added default implementation for initialize for sake of convenience.
   Revision 1.18  2004/04/16 17:42:04  venku
   - coding convention
   - enabled the user to pass soot options while initializing the driver.
   Revision 1.17  2004/04/04 11:12:29  venku
   - STR_ARRAY_TYPE was non-static-ized.
   Revision 1.16  2004/03/29 01:55:16  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.15  2004/03/26 00:07:26  venku
   - renamed XXXXUnitGraphFactory to XXXXStmtGraphFactory.
   - ripple effect in classes and method names.
   Revision 1.14  2004/03/07 00:42:49  venku
   - added a new method to extract the options to be used by
     soot to use Indus.
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
