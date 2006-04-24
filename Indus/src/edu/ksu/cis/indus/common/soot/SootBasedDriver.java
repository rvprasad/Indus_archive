/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;
import edu.ksu.cis.indus.annotations.NonNullContainer;
import edu.ksu.cis.indus.common.scoping.SpecificationBasedScopeDefinition;
import edu.ksu.cis.indus.interfaces.IEnvironment;
import edu.ksu.cis.indus.processing.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.Printer;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.options.Options;
import soot.util.Chain;

/**
 * This is generic driver that provides basic support to process a system represented in Jimple.
 * <p>
 * The classes in which to search for root methods can be configured via setting 1 system property named
 * <code>indus.common.soot.SootBasedDriver.rootClasses</code>. The syntax of this property a regular expression which will
 * be matched against the name of the class in which the method is declared. Note if this is unspecified, then root methods
 * are searched only in the classes specified and not the classes which are required. So, "edu.ksu.cis.indus.Processing" will
 * find root methods occurring in classes defined in class <code>edu.ksu.cis.indus.Processing</code>. Likewise, the user
 * can control which methods are picked as root methods based on their signature by specifying the pattern via the system
 * property, <code>indus.common.soot.SootBasedDriver.rootMethods</code>.
 * </p>
 * <p>
 * The user can provide a root method trapper object to be used to identify root methods. However, if the user does not
 * provide one, then an instance of the class named via <code>indus.common.soot.RootMethodTrapper.class</code> property will
 * be used. This named class should be a subclass of <code>edu.ksu.cis.indus.common.soot.RootMethodTrapper</code>. As
 * reflection is used on to instantiate an object, the specified class by the property should have a no-argument constructor.
 * </p>
 * <p>
 * The user can provide a statement graph factory object to be used to identify root methods. However, if the user does not
 * provide one, then an instance of the class named via <code>indus.common.soot.SootBasedDriver.StmtGraphFactory.class</code>
 * property will be used. This named class should be a subclass of
 * <code>edu.ksu.cis.indus.common.soot.IStmtGraphFactory</code>. As reflection is used on to instantiate an object, the
 * specified class by the property should have a no-argument constructor.
 * </p>
 * <p>
 * Please refer to <code>edu.ksu.cis.indus.Constants</code> for a file-based approach to specifying these properties.
 * </p>
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SootBasedDriver {

	/**
	 * The default object that can be used for trapping root methods.
	 */
	private static final RootMethodTrapper DEFAULT_INSTANCE_OF_ROOT_METHOD_TRAPPER;

	/**
	 * The default object that can be used for created statement graphs.
	 */
	private static final IStmtGraphFactory<?> DEFAULT_INSTANCE_OF_STMT_GRAPH_FACTORY;

	/**
	 * The logger used by instances of this class and it's subclasses to log messages.
	 */
	private static final Logger LOGGER;

	static {
		LOGGER = LoggerFactory.getLogger(SootBasedDriver.class);

		final String _rmtClassName = Constants.getRootMethodTrapperClassName();

		try {
			final Object _o = SootBasedDriver.class.getClassLoader().loadClass(_rmtClassName).newInstance();

			if (_o instanceof RootMethodTrapper) {
				DEFAULT_INSTANCE_OF_ROOT_METHOD_TRAPPER = (RootMethodTrapper) _o;
			} else {
				final String _msg = _rmtClassName + " is not a subclass of SootBasedDriver.RootMethodTrapper.";
				throw new IllegalArgumentException(_msg);
			}
		} catch (final ClassNotFoundException _e) {
			LOGGER.error("class " + _rmtClassName + " could not be loaded/resolved. Bailing.", _e);
			throw new RuntimeException(_e);
		} catch (final InstantiationException _e) {
			LOGGER.error("An instance of class " + _rmtClassName + " could not be created. Bailing.", _e);
			throw new RuntimeException(_e);
		} catch (final IllegalAccessException _e) {
			LOGGER.error("No-arg constructor of " + _rmtClassName + " cannot be accessed.  Bailing.", _e);
			throw new RuntimeException(_e);
		}

		final String _nameOfStmtGraphFactoryClass = Constants.getStmtGraphFactoryClassName();

		try {
			final Object _o = SootBasedDriver.class.getClassLoader().loadClass(_nameOfStmtGraphFactoryClass).newInstance();

			if (_o instanceof IStmtGraphFactory) {
				DEFAULT_INSTANCE_OF_STMT_GRAPH_FACTORY = (IStmtGraphFactory) _o;
			} else {
				throw new IllegalArgumentException(_nameOfStmtGraphFactoryClass + " is not a subclass of IStmtGraphFactory.");
			}
		} catch (final ClassNotFoundException _e) {
			LOGGER.error("class " + _nameOfStmtGraphFactoryClass + " could not be loaded/resolved. Bailing.", _e);
			throw new RuntimeException(_e);
		} catch (final InstantiationException _e) {
			LOGGER.error("An instance of class " + _nameOfStmtGraphFactoryClass + " could not be created. Bailing.", _e);
			throw new RuntimeException(_e);
		} catch (final IllegalAccessException _e) {
			LOGGER.error("No-arg constructor of " + _nameOfStmtGraphFactoryClass + " cannot be accessed.  Bailing.", _e);
			throw new RuntimeException(_e);
		}
	}

	/**
	 * This manages basic block graphs of the methods being processed. Subclasses should initialize this suitably.
	 */
	protected BasicBlockGraphMgr bbm;

	/**
	 * This provides <code>UnitGraph</code>s required by the analyses. By defaults this will be initialized to
	 * <code>ExceptionFlowSensitiveStmtGraphFactory</code>.
	 */
	@NonNull protected IStmtGraphFactory<?> cfgProvider;

	/**
	 * The list of classes that should be considered as the core of the system.
	 */
	@NonNullContainer @NonNull protected List<String> classNames;

	/**
	 * Logger to be used to log information written via <code>writeInfo</code>.
	 */
	protected Logger infoLogger;

	/**
	 * This is the set of methods which serve as the entry point into the system being analyzed.
	 */
	@NonNullContainer @NonNull protected Collection<SootMethod> rootMethods = new HashSet<SootMethod>();

	/**
	 * The scene that contains the classes of the system.
	 */
	@NonNull protected Scene scene;

	/**
	 * The class path that should be added.
	 */
	private String classpathToAdd;

	/**
	 * The environment.
	 */
	@NonNull private Environment env;

	/**
	 * This traps the root methods.
	 */
	@NonNull private RootMethodTrapper rootMethodTrapper;

	/**
	 * This is used to maintain the execution time of each analysis/transformation. This timing information is printed via
	 * <code>printTimingStats</code>.
	 */
	@NonNullContainer @NonNull private final Map<String, Long> times = new LinkedHashMap<String, Long>();

	/**
	 * Creates a new Test object. This also initializes <code>cfgProvider</code> to <code>CompleteStmtGraphFactory</code>
	 * and <code>bbm</code> to an instance of <code>BasicBlockGraphMgr</code> with <code>cfgProvider</code> as the unit
	 * graph provider.
	 */
	public SootBasedDriver() {
		cfgProvider = getDefaultStmtGraphFactory();
		bbm = new BasicBlockGraphMgr();
		bbm.setStmtGraphFactory(cfgProvider);
	}

	/**
	 * Provides the default statement graph factory as configured by properties at the time of class initialization. By
	 * default, it provides an instance of <code>CompleteStmtGraphFactory</code>.
	 * 
	 * @return a unit graph factory.
	 */
	@NonNull public static IStmtGraphFactory<?> getDefaultStmtGraphFactory() {
		return DEFAULT_INSTANCE_OF_STMT_GRAPH_FACTORY;
	}

	/**
	 * Adds an entry into the time log of this test. The subclasses should use this method to add time logs corresponding to
	 * each analysis they test/drive.
	 * 
	 * @param name of the analysis for which the timing log is being created.
	 * @param milliseconds taken by the analysis.
	 */
	public final void addTimeLog(@NonNull @Immutable final String name, final long milliseconds) {
		times.put("[" + times.size() + "]" + name, new Long(milliseconds));
	}

	/**
	 * Records the given classpath in intention of using it while loading classes into the scene.
	 * 
	 * @param classpath to be considered.
	 */
	public final void addToSootClassPath(@NonNull @Immutable final String classpath) {
		classpathToAdd = classpath;
	}

	/**
	 * Retrieves the basic block graph manager used by the application.
	 * 
	 * @return the basic block graph manager.
	 */
	@Functional @NonNull public final BasicBlockGraphMgr getBbm() {
		return this.bbm;
	}

	/**
	 * Retrieves the environment used by the application.
	 * 
	 * @return the environment.
	 */
	@Functional @NonNull public final IEnvironment getEnvironment() {
		return this.env;
	}

	/**
	 * Retrieves the root methods in the system.
	 * 
	 * @return the collection of root methods.
	 */
	@Functional @NonNullContainer @NonNull public final Collection<SootMethod> getRootMethods() {
		return Collections.unmodifiableCollection(rootMethods);
	}

	/**
	 * Retrieves the unit graph factory to be used by other processes that are driven by this implementation.
	 * 
	 * @return an unit graph factory
	 */
	@Functional @NonNull public IStmtGraphFactory<?> getStmtGraphFactory() {
		return cfgProvider;
	}

	/**
	 * Initialize the driver with the soot options available from <code>Util.getSootOptions</code> method.
	 */
	public final void initialize() {
		initialize(Util.getSootOptions());
	}

	/**
	 * Initialize the driver. Loads up the classes and sets up the scene. The given classes are loaded up as application
	 * classes.
	 * 
	 * @param options to be used while setting up Soot infrastructure.
	 * @throws RuntimeException when <code>setClassNames()</code> was not called before using this object.
	 */
	public final void initialize(@NonNull final String[] options) {
		if (classNames == null) {
			throw new RuntimeException("Please call setClassNames() before using this object.");
		}
		writeInfo("Loading classes....");
		scene = loadupClassesAndCollectMains(options);
		env = new Environment(scene);
	}

	/**
	 * Prints the timing statistics into the given stream.
	 */
	@Functional public final void printTimingStats() {
		writeInfo("Timing statistics:");

		for (final Iterator<String> _i = times.keySet().iterator(); _i.hasNext();) {
			final Object _e = _i.next();
			writeInfo(_e + " => " + times.get(_e) + "ms");
		}
	}

	/**
	 * Resets internal data structure.
	 */
	public void reset() {
		rootMethods.clear();
		scene = null;
		times.clear();
	}

	/**
	 * Set the names of the classes to be loaded.
	 * 
	 * @param s contains the class names.
	 */
	public final void setClassNames(@NonNullContainer @NonNull final Collection<String> s) {
		classNames = new ArrayList<String>(s);
	}

	/**
	 * Sets the logger to be used to log information written via <code>writeInfo</code>.
	 * 
	 * @param myLogger is the logger to be used.
	 */
	public final void setInfoLogger(@NonNull final Logger myLogger) {
		infoLogger = myLogger;
	}

	/**
	 * Sets the root method trapper.
	 * 
	 * @param trapper to be used to trap root methods.
	 */
	public final void setRootMethodTrapper(@NonNull final RootMethodTrapper trapper) {
		rootMethodTrapper = trapper;
	}

	/**
	 * Logs the given object via the logging api. Configure the logging via the logging implementation's configuration
	 * support.
	 * 
	 * @param info to be logged.
	 */
	public void writeInfo(final Object info) {
		if (infoLogger != null && infoLogger.isInfoEnabled()) {
			infoLogger.info(String.valueOf(info));
		}
	}

	/**
	 * Dumps jimple for the classes in the scene.
	 * 
	 * @param outputDirectory is the directory in which jimple files will be dumped.
	 * @param jimpleFile <code>true</code> indicates if .jimple file should be dumped; <code>false</code>, indicates
	 *            otherwise.
	 * @param classFile <code>true</code> indicates if .class file should be dumped; <code>false</code>, indicates
	 *            otherwise.
	 */
	protected void dumpJimpleAndClassFiles(@Immutable @NonNull final String outputDirectory, final boolean jimpleFile,
			final boolean classFile) {
		if (!jimpleFile && !classFile) {
			return;
		}

		final Printer _printer = Printer.v();

		for (@SuppressWarnings("unchecked") final Iterator<SootClass> _i = scene.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = _i.next();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Dumping jimple for " + _sc);
			}

			for (@SuppressWarnings("unchecked") final Iterator<SootMethod> _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = _j.next();

				if (_sm.isConcrete()) {
					try {
						_sm.retrieveActiveBody();
					} catch (final RuntimeException _e) {
						LOGGER.error("Failed to retrieve body for method " + _sm, _e);
					}
				}
			}

			PrintWriter _writer = null;

			try {
				if (jimpleFile) {
					// write .jimple file
					final File _file = new File(outputDirectory + File.separator + _sc.getName() + ".jimple");
					_writer = new PrintWriter(new FileWriter(_file));
					_printer.printTo(_sc, _writer);
				}

				if (classFile) {
					// write .class file
					_printer.write(_sc, outputDirectory);
				}
			} catch (final IOException _e) {
				LOGGER.error("Error while writing " + _sc, _e);
			} catch (final RuntimeException _e) {
				LOGGER.error("Error while writing class file of " + _sc, _e);
			} finally {
				if (_writer != null) {
					_writer.flush();
					_writer.close();
				}
			}
		}
	}

	/**
	 * Sets the name of the file containing the scope specification.
	 * 
	 * @param scopeSpecFileName of the scope spec.
	 * @return the scope definition stored in the given file.
	 */
	@Functional protected final SpecificationBasedScopeDefinition setScopeSpecFile(
			@Immutable @NonNull final String scopeSpecFileName) {
		SpecificationBasedScopeDefinition _result = null;

		if (scopeSpecFileName != null) {
			try {
				final InputStream _in = new FileInputStream(scopeSpecFileName);
				final String _contents = IOUtils.toString(_in);
				IOUtils.closeQuietly(_in);
				_result = SpecificationBasedScopeDefinition.deserialize(_contents);
			} catch (final IOException _e) {
				final String _msg = "Error retrieved specification from " + scopeSpecFileName;
				LOGGER.error(_msg, _e);

				final IllegalArgumentException _i = new IllegalArgumentException(_msg);
				_i.initCause(_e);
				throw _i;
			} catch (final JiBXException _e) {
				final String _msg = "JiBX failed during deserialization.";
				LOGGER.error(_msg, _e);

				final IllegalStateException _i = new IllegalStateException(_msg);
				_i.initCause(_e);
				throw _i;
			}
		}
		cfgProvider.setScope(_result, getEnvironment());
		return _result;
	}

	/**
	 * Loads up the classes specified via <code>setClassNames()</code> and also collects the possible entry points into the
	 * system being analyzed. All <code>public static void main()</code> methods defined in <code>public</code> classes
	 * that are named via <code>args</code>are considered as entry points. It uses the classpath set via
	 * <code>addToSootClassPath</code>.
	 * 
	 * @param options to be used while setting up Soot infrastructure.
	 * @return a soot scene that provides the classes to be analyzed.
	 */
	@NonNull private Scene loadupClassesAndCollectMains(@NonNull final String[] options) {
		final Scene _result = Scene.v();
		String _temp = _result.getSootClassPath();
		Options.v().parse(options);

		if (_temp != null) {
			_temp += File.pathSeparator + classpathToAdd + File.pathSeparator + System.getProperty("java.class.path");
		} else {
			_temp = classpathToAdd;
		}
		_result.setSootClassPath(_temp);

		for (final Iterator<String> _i = classNames.iterator(); _i.hasNext();) {
			final SootClass _sc = _result.loadClassAndSupport(_i.next());
			_sc.setApplicationClass();
		}

		final Collection<SootClass> _mc = new HashSet<SootClass>();
		_mc.addAll(_result.getClasses());

		RootMethodTrapper _rmt = rootMethodTrapper;

		if (_rmt == null) {
			_rmt = DEFAULT_INSTANCE_OF_ROOT_METHOD_TRAPPER;
		}

		_rmt.setClassNames(Collections.unmodifiableCollection(classNames));

		for (final Iterator<SootClass> _i = _mc.iterator(); _i.hasNext();) {
			final SootClass _sc = _i.next();

			if (_rmt.considerClassForEntryPoint(_sc)) {
				final Collection<SootMethod> _methods = _sc.getMethods();

				for (final Iterator<SootMethod> _j = _methods.iterator(); _j.hasNext();) {
					final SootMethod _sm = _j.next();

					if (_rmt.isThisARootMethod(_sm)) {
						rootMethods.add(_sm);
					}
				}
			}
		}
		Util.fixupThreadStartBody(_result);

		if (Constants.shouldLoadMethodBodiesDuringInitialization()) {
			loadupMethodBodies();
		}

		return _result;
	}

	/**
	 * Loads the bodies of all methods in the system.
	 */
	private void loadupMethodBodies() {
		final Chain _classes = Scene.v().getClasses();
		@SuppressWarnings("unchecked") final Iterator<SootClass> _i = _classes.iterator();
		final int _iEnd = _classes.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final SootClass _sc = _i.next();
			@SuppressWarnings("unchecked") final List<SootMethod> _methods = _sc.getMethods();
			final Iterator<SootMethod> _j = _methods.iterator();
			final int _jEnd = _methods.size();

			for (int _jIndex = 0; _jIndex < _jEnd; _jIndex++) {
				final SootMethod _sm = _j.next();

				if (_sm.isConcrete()) {
					_sm.retrieveActiveBody();
				}
			}
		}
	}
}

// End of File
